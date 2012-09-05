/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ShardGetService.java 2012-7-6 14:30:11 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.get;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.commons.metrics.CounterMetric;
import cn.com.rebirth.search.commons.metrics.MeanMetric;
import cn.com.rebirth.search.core.index.cache.IndexCache;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.ParentFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.RoutingFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.SizeFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TTLFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TimestampFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.selector.FieldMappersFieldSelector;
import cn.com.rebirth.search.core.index.settings.IndexSettings;
import cn.com.rebirth.search.core.index.shard.AbstractIndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.shard.service.IndexShard;
import cn.com.rebirth.search.core.index.translog.Translog;
import cn.com.rebirth.search.core.script.ScriptService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.lookup.SearchLookup;
import cn.com.rebirth.search.core.search.lookup.SourceLookup;

/**
 * The Class ShardGetService.
 *
 * @author l.xue.nong
 */
public class ShardGetService extends AbstractIndexShardComponent {

	/** The script service. */
	private final ScriptService scriptService;

	/** The mapper service. */
	private final MapperService mapperService;

	/** The index cache. */
	private final IndexCache indexCache;

	/** The index shard. */
	private IndexShard indexShard;

	/** The exists metric. */
	private final MeanMetric existsMetric = new MeanMetric();

	/** The missing metric. */
	private final MeanMetric missingMetric = new MeanMetric();

	/** The current metric. */
	private final CounterMetric currentMetric = new CounterMetric();

	/**
	 * Instantiates a new shard get service.
	 *
	 * @param shardId the shard id
	 * @param indexSettings the index settings
	 * @param scriptService the script service
	 * @param mapperService the mapper service
	 * @param indexCache the index cache
	 */
	@Inject
	public ShardGetService(ShardId shardId, @IndexSettings Settings indexSettings, ScriptService scriptService,
			MapperService mapperService, IndexCache indexCache) {
		super(shardId, indexSettings);
		this.scriptService = scriptService;
		this.mapperService = mapperService;
		this.indexCache = indexCache;
	}

	/**
	 * Stats.
	 *
	 * @return the gets the stats
	 */
	public GetStats stats() {
		return new GetStats(existsMetric.count(), TimeUnit.NANOSECONDS.toMillis(existsMetric.sum()),
				missingMetric.count(), TimeUnit.NANOSECONDS.toMillis(missingMetric.sum()), currentMetric.count());
	}

	/**
	 * Sets the index shard.
	 *
	 * @param indexShard the index shard
	 * @return the shard get service
	 */
	public ShardGetService setIndexShard(IndexShard indexShard) {
		this.indexShard = indexShard;
		return this;
	}

	/**
	 * Gets the.
	 *
	 * @param type the type
	 * @param id the id
	 * @param gFields the g fields
	 * @param realtime the realtime
	 * @return the gets the result
	 * @throws RebirthException the rebirth exception
	 */
	public GetResult get(String type, String id, String[] gFields, boolean realtime) throws RebirthException {
		currentMetric.inc();
		try {
			long now = System.nanoTime();
			GetResult getResult = innerGet(type, id, gFields, realtime);
			if (getResult.exists()) {
				existsMetric.inc(System.nanoTime() - now);
			} else {
				missingMetric.inc(System.nanoTime() - now);
			}
			return getResult;
		} finally {
			currentMetric.dec();
		}
	}

	/**
	 * Inner get.
	 *
	 * @param type the type
	 * @param id the id
	 * @param gFields the g fields
	 * @param realtime the realtime
	 * @return the gets the result
	 * @throws RebirthException the rebirth exception
	 */
	public GetResult innerGet(String type, String id, String[] gFields, boolean realtime) throws RebirthException {
		boolean loadSource = gFields == null || gFields.length > 0;
		Engine.GetResult get = null;
		if (type == null || type.equals("_all")) {
			for (String typeX : mapperService.types()) {
				get = indexShard.get(new Engine.Get(realtime, UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(
						typeX, id))).loadSource(loadSource));
				if (get.exists()) {
					type = typeX;
					break;
				} else {
					get.release();
				}
			}
			if (get == null) {
				return new GetResult(shardId.index().name(), type, id, -1, false, null, null);
			}
			if (!get.exists()) {

				return new GetResult(shardId.index().name(), type, id, -1, false, null, null);
			}
		} else {
			get = indexShard.get(new Engine.Get(realtime, UidFieldMapper.TERM_FACTORY.createTerm(Uid
					.createUid(type, id))).loadSource(loadSource));
			if (!get.exists()) {
				get.release();
				return new GetResult(shardId.index().name(), type, id, -1, false, null, null);
			}
		}

		DocumentMapper docMapper = mapperService.documentMapper(type);
		if (docMapper == null) {
			get.release();
			return new GetResult(shardId.index().name(), type, id, -1, false, null, null);
		}

		try {

			if (get.docIdAndVersion() != null) {
				Map<String, GetField> fields = null;
				byte[] source = null;
				UidField.DocIdAndVersion docIdAndVersion = get.docIdAndVersion();
				ResetFieldSelector fieldSelector = buildFieldSelectors(docMapper, gFields);
				if (fieldSelector != null) {
					fieldSelector.reset();
					Document doc;
					try {
						doc = docIdAndVersion.reader.document(docIdAndVersion.docId, fieldSelector);
					} catch (IOException e) {
						throw new RebirthException("Failed to get type [" + type + "] and id [" + id + "]", e);
					}
					source = extractSource(doc, docMapper);

					for (Object oField : doc.getFields()) {
						Fieldable field = (Fieldable) oField;
						String name = field.name();
						Object value = null;
						FieldMappers fieldMappers = docMapper.mappers().indexName(field.name());
						if (fieldMappers != null) {
							FieldMapper mapper = fieldMappers.mapper();
							if (mapper != null) {
								name = mapper.names().fullName();
								value = mapper.valueForSearch(field);
							}
						}
						if (value == null) {
							if (field.isBinary()) {
								value = field.getBinaryValue();
							} else {
								value = field.stringValue();
							}
						}

						if (fields == null) {
							fields = newHashMapWithExpectedSize(2);
						}

						GetField getField = fields.get(name);
						if (getField == null) {
							getField = new GetField(name, new ArrayList<Object>(2));
							fields.put(name, getField);
						}
						getField.values().add(value);
					}
				}

				if (gFields != null && gFields.length > 0) {
					SearchLookup searchLookup = null;
					for (String field : gFields) {
						Object value = null;
						if (field.contains("_source.") || field.contains("doc[")) {
							if (searchLookup == null) {
								searchLookup = new SearchLookup(mapperService, indexCache.fieldData());
							}
							SearchScript searchScript = scriptService.search(searchLookup, "mvel", field, null);
							searchScript.setNextReader(docIdAndVersion.reader);
							searchScript.setNextDocId(docIdAndVersion.docId);

							try {
								value = searchScript.run();
							} catch (RuntimeException e) {
								if (logger.isTraceEnabled()) {
									logger.trace("failed to execute get request script field [{}]", e, field);
								}

							}
						} else {
							FieldMappers x = docMapper.mappers().smartName(field);
							if (x == null || !x.mapper().stored()) {
								if (searchLookup == null) {
									searchLookup = new SearchLookup(mapperService, indexCache.fieldData());
									searchLookup.setNextReader(docIdAndVersion.reader);
									searchLookup.setNextDocId(docIdAndVersion.docId);
								}
								value = searchLookup.source().extractValue(field);
							}
						}

						if (value != null) {
							if (fields == null) {
								fields = newHashMapWithExpectedSize(2);
							}
							GetField getField = fields.get(field);
							if (getField == null) {
								getField = new GetField(field, new ArrayList<Object>(2));
								fields.put(field, getField);
							}
							getField.values().add(value);
						}
					}
				}

				return new GetResult(shardId.index().name(), type, id, get.version(), get.exists(),
						source == null ? null : new BytesHolder(source), fields);
			} else {
				Translog.Source source = get.source();

				Map<String, GetField> fields = null;
				boolean sourceRequested = false;

				if (gFields == null) {
					sourceRequested = true;
				} else if (gFields.length == 0) {

					sourceRequested = false;
				} else {
					Map<String, Object> sourceAsMap = null;
					SearchLookup searchLookup = null;
					for (String field : gFields) {
						if (field.equals("_source")) {
							sourceRequested = true;
							continue;
						}
						Object value = null;
						if (field.equals(RoutingFieldMapper.NAME) && docMapper.routingFieldMapper().stored()) {
							value = source.routing;
						} else if (field.equals(ParentFieldMapper.NAME) && docMapper.parentFieldMapper() != null
								&& docMapper.parentFieldMapper().stored()) {
							value = source.parent;
						} else if (field.equals(TimestampFieldMapper.NAME) && docMapper.timestampFieldMapper().stored()) {
							value = source.timestamp;
						} else if (field.equals(TTLFieldMapper.NAME) && docMapper.TTLFieldMapper().stored()) {

							if (source.ttl > 0) {
								value = docMapper.TTLFieldMapper().valueForSearch(source.timestamp + source.ttl);
							}
						} else if (field.equals(SizeFieldMapper.NAME)
								&& docMapper.rootMapper(SizeFieldMapper.class).stored()) {
							value = source.source.length();
						} else {
							if (field.contains("_source.")) {
								if (searchLookup == null) {
									searchLookup = new SearchLookup(mapperService, indexCache.fieldData());
								}
								if (sourceAsMap == null) {
									sourceAsMap = SourceLookup.sourceAsMap(source.source.bytes(),
											source.source.offset(), source.source.length());
								}
								SearchScript searchScript = scriptService.search(searchLookup, "mvel", field, null);

								searchScript.setNextSource(sourceAsMap);

								try {
									value = searchScript.run();
								} catch (RuntimeException e) {
									if (logger.isTraceEnabled()) {
										logger.trace("failed to execute get request script field [{}]", e, field);
									}

								}
							} else {
								if (searchLookup == null) {
									searchLookup = new SearchLookup(mapperService, indexCache.fieldData());
									searchLookup.source().setNextSource(source.source.bytes(), source.source.offset(),
											source.source.length());
								}

								FieldMapper<?> x = docMapper.mappers().smartNameFieldMapper(field);
								value = searchLookup.source().extractValue(field);
								if (x != null && value instanceof String) {
									value = x.valueFromString((String) value);
								}
							}
						}
						if (value != null) {
							if (fields == null) {
								fields = newHashMapWithExpectedSize(2);
							}
							GetField getField = fields.get(field);
							if (getField == null) {
								getField = new GetField(field, new ArrayList<Object>(2));
								fields.put(field, getField);
							}
							getField.values().add(value);
						}
					}
				}

				if (sourceRequested && !docMapper.sourceMapper().enabled()) {
					sourceRequested = false;
				}

				return new GetResult(shardId.index().name(), type, id, get.version(), get.exists(),
						sourceRequested ? source.source : null, fields);
			}
		} finally {
			get.release();
		}
	}

	/**
	 * Builds the field selectors.
	 *
	 * @param docMapper the doc mapper
	 * @param fields the fields
	 * @return the reset field selector
	 */
	private static ResetFieldSelector buildFieldSelectors(DocumentMapper docMapper, String... fields) {
		if (fields == null) {
			return docMapper.sourceMapper().fieldSelector();
		}

		if (fields.length == 0) {
			return null;
		}

		FieldMappersFieldSelector fieldSelector = null;
		for (String fieldName : fields) {
			FieldMappers x = docMapper.mappers().smartName(fieldName);
			if (x != null && x.mapper().stored()) {
				if (fieldSelector == null) {
					fieldSelector = new FieldMappersFieldSelector();
				}
				fieldSelector.add(x);
			}
		}

		return fieldSelector;
	}

	/**
	 * Extract source.
	 *
	 * @param doc the doc
	 * @param documentMapper the document mapper
	 * @return the byte[]
	 */
	private static byte[] extractSource(Document doc, DocumentMapper documentMapper) {
		byte[] source = null;
		Fieldable sourceField = doc.getFieldable(documentMapper.sourceMapper().names().indexName());
		if (sourceField != null) {
			source = documentMapper.sourceMapper().nativeValue(sourceField);
			doc.removeField(documentMapper.sourceMapper().names().indexName());
		}
		return source;
	}
}
