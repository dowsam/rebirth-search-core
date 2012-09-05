/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core FetchPhase.java 2012-7-6 14:29:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.fetch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.FieldMappers;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.SourceFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.mapper.selector.AllButSourceFieldSelector;
import cn.com.rebirth.search.core.index.mapper.selector.FieldMappersFieldSelector;
import cn.com.rebirth.search.core.index.mapper.selector.UidAndSourceFieldSelector;
import cn.com.rebirth.search.core.index.mapper.selector.UidFieldSelector;
import cn.com.rebirth.search.core.indices.TypeMissingException;
import cn.com.rebirth.search.core.search.SearchHitField;
import cn.com.rebirth.search.core.search.SearchParseElement;
import cn.com.rebirth.search.core.search.SearchPhase;
import cn.com.rebirth.search.core.search.fetch.explain.ExplainFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.matchedfilters.MatchedFiltersFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.partial.PartialFieldsFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.script.ScriptFieldsFetchSubPhase;
import cn.com.rebirth.search.core.search.fetch.version.VersionFetchSubPhase;
import cn.com.rebirth.search.core.search.highlight.HighlightPhase;
import cn.com.rebirth.search.core.search.internal.InternalSearchHit;
import cn.com.rebirth.search.core.search.internal.InternalSearchHitField;
import cn.com.rebirth.search.core.search.internal.InternalSearchHits;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The Class FetchPhase.
 *
 * @author l.xue.nong
 */
public class FetchPhase implements SearchPhase {

	/** The fetch sub phases. */
	private final FetchSubPhase[] fetchSubPhases;

	/**
	 * Instantiates a new fetch phase.
	 *
	 * @param highlightPhase the highlight phase
	 * @param scriptFieldsPhase the script fields phase
	 * @param partialFieldsPhase the partial fields phase
	 * @param matchFiltersPhase the match filters phase
	 * @param explainPhase the explain phase
	 * @param versionPhase the version phase
	 */
	@Inject
	public FetchPhase(HighlightPhase highlightPhase, ScriptFieldsFetchSubPhase scriptFieldsPhase,
			PartialFieldsFetchSubPhase partialFieldsPhase, MatchedFiltersFetchSubPhase matchFiltersPhase,
			ExplainFetchSubPhase explainPhase, VersionFetchSubPhase versionPhase) {
		this.fetchSubPhases = new FetchSubPhase[] { scriptFieldsPhase, partialFieldsPhase, matchFiltersPhase,
				explainPhase, highlightPhase, versionPhase };
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#parseElements()
	 */
	@Override
	public Map<String, ? extends SearchParseElement> parseElements() {
		ImmutableMap.Builder<String, SearchParseElement> parseElements = ImmutableMap.builder();
		parseElements.put("fields", new FieldsParseElement());
		for (FetchSubPhase fetchSubPhase : fetchSubPhases) {
			parseElements.putAll(fetchSubPhase.parseElements());
		}
		return parseElements.build();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#preProcess(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	@Override
	public void preProcess(SearchContext context) {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhase#execute(cn.com.rebirth.search.core.search.internal.SearchContext)
	 */
	public void execute(SearchContext context) {
		ResetFieldSelector fieldSelector;
		List<String> extractFieldNames = null;
		boolean sourceRequested = false;
		if (!context.hasFieldNames()) {
			if (context.hasPartialFields()) {

				fieldSelector = new UidAndSourceFieldSelector();
				sourceRequested = false;
			} else if (context.hasScriptFields()) {

				fieldSelector = UidFieldSelector.INSTANCE;
				sourceRequested = false;
			} else {
				fieldSelector = new UidAndSourceFieldSelector();
				sourceRequested = true;
			}
		} else if (context.fieldNames().isEmpty()) {
			fieldSelector = UidFieldSelector.INSTANCE;
			sourceRequested = false;
		} else {
			boolean loadAllStored = false;
			FieldMappersFieldSelector fieldSelectorMapper = null;
			for (String fieldName : context.fieldNames()) {
				if (fieldName.equals("*")) {
					loadAllStored = true;
					continue;
				}
				if (fieldName.equals(SourceFieldMapper.NAME)) {
					sourceRequested = true;
					continue;
				}
				FieldMappers x = context.smartNameFieldMappers(fieldName);
				if (x != null && x.mapper().stored()) {
					if (fieldSelectorMapper == null) {
						fieldSelectorMapper = new FieldMappersFieldSelector();
					}
					fieldSelectorMapper.add(x);
				} else {
					if (extractFieldNames == null) {
						extractFieldNames = Lists.newArrayList();
					}
					extractFieldNames.add(fieldName);
				}
			}

			if (loadAllStored) {
				if (sourceRequested || extractFieldNames != null) {
					fieldSelector = null;
				} else {
					fieldSelector = AllButSourceFieldSelector.INSTANCE;
				}
			} else if (fieldSelectorMapper != null) {

				fieldSelectorMapper.add(UidFieldMapper.NAME);
				if (extractFieldNames != null) {
					fieldSelectorMapper.add(SourceFieldMapper.NAME);
				}
				fieldSelector = fieldSelectorMapper;
			} else if (extractFieldNames != null || sourceRequested) {
				fieldSelector = new UidAndSourceFieldSelector();
			} else {
				fieldSelector = UidFieldSelector.INSTANCE;
			}
		}

		InternalSearchHit[] hits = new InternalSearchHit[context.docIdsToLoadSize()];
		for (int index = 0; index < context.docIdsToLoadSize(); index++) {
			int docId = context.docIdsToLoad()[context.docIdsToLoadFrom() + index];
			Document doc = loadDocument(context, fieldSelector, docId);
			Uid uid = extractUid(context, doc);

			DocumentMapper documentMapper = context.mapperService().documentMapper(uid.type());

			if (documentMapper == null) {
				throw new TypeMissingException(new Index(context.shardTarget().index()), uid.type(),
						"failed to find type loaded for doc [" + uid.id() + "]");
			}

			byte[] source = extractSource(doc, documentMapper);

			InternalSearchHit searchHit = new InternalSearchHit(docId, uid.id(), uid.type(), sourceRequested ? source
					: null, null);
			hits[index] = searchHit;

			for (Object oField : doc.getFields()) {
				Fieldable field = (Fieldable) oField;
				String name = field.name();

				if (name.equals(UidFieldMapper.NAME)) {
					continue;
				}

				if (name.equals(SourceFieldMapper.NAME)) {
					continue;
				}

				Object value = null;
				FieldMappers fieldMappers = documentMapper.mappers().indexName(field.name());
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

				if (searchHit.fieldsOrNull() == null) {
					searchHit.fields(new HashMap<String, SearchHitField>(2));
				}

				SearchHitField hitField = searchHit.fields().get(name);
				if (hitField == null) {
					hitField = new InternalSearchHitField(name, new ArrayList<Object>(2));
					searchHit.fields().put(name, hitField);
				}
				hitField.values().add(value);
			}

			int readerIndex = context.searcher().readerIndex(docId);
			IndexReader subReader = context.searcher().subReaders()[readerIndex];
			int subDoc = docId - context.searcher().docStarts()[readerIndex];

			context.lookup().setNextReader(subReader);
			context.lookup().setNextDocId(subDoc);
			if (source != null) {
				context.lookup().source().setNextSource(source, 0, source.length);
			}
			if (extractFieldNames != null) {
				for (String extractFieldName : extractFieldNames) {
					Object value = context.lookup().source().extractValue(extractFieldName);
					if (value != null) {
						if (searchHit.fieldsOrNull() == null) {
							searchHit.fields(new HashMap<String, SearchHitField>(2));
						}

						SearchHitField hitField = searchHit.fields().get(extractFieldName);
						if (hitField == null) {
							hitField = new InternalSearchHitField(extractFieldName, new ArrayList<Object>(2));
							searchHit.fields().put(extractFieldName, hitField);
						}
						hitField.values().add(value);
					}
				}
			}

			for (FetchSubPhase fetchSubPhase : fetchSubPhases) {
				FetchSubPhase.HitContext hitContext = new FetchSubPhase.HitContext();
				if (fetchSubPhase.hitExecutionNeeded(context)) {
					hitContext.reset(searchHit, subReader, subDoc, context.searcher().getIndexReader(), docId, doc);
					fetchSubPhase.hitExecute(context, hitContext);
				}
			}
		}

		for (FetchSubPhase fetchSubPhase : fetchSubPhases) {
			if (fetchSubPhase.hitsExecutionNeeded(context)) {
				fetchSubPhase.hitsExecute(context, hits);
			}
		}

		context.fetchResult().hits(
				new InternalSearchHits(hits, context.queryResult().topDocs().totalHits, context.queryResult().topDocs()
						.getMaxScore()));
	}

	/**
	 * Extract source.
	 *
	 * @param doc the doc
	 * @param documentMapper the document mapper
	 * @return the byte[]
	 */
	private byte[] extractSource(Document doc, DocumentMapper documentMapper) {
		Fieldable sourceField = doc.getFieldable(SourceFieldMapper.NAME);
		if (sourceField != null) {
			return documentMapper.sourceMapper().nativeValue(sourceField);
		}
		return null;
	}

	/**
	 * Extract uid.
	 *
	 * @param context the context
	 * @param doc the doc
	 * @return the uid
	 */
	private Uid extractUid(SearchContext context, Document doc) {

		String sUid = doc.get(UidFieldMapper.NAME);
		if (sUid != null) {
			return Uid.createUid(sUid);
		}

		List<String> fieldNames = new ArrayList<String>();
		for (Fieldable field : doc.getFields()) {
			fieldNames.add(field.name());
		}
		throw new FetchPhaseExecutionException(context,
				"Failed to load uid from the index, missing internal _uid field, current fields in the doc ["
						+ fieldNames + "]");
	}

	/**
	 * Load document.
	 *
	 * @param context the context
	 * @param fieldSelector the field selector
	 * @param docId the doc id
	 * @return the document
	 */
	private Document loadDocument(SearchContext context, @Nullable ResetFieldSelector fieldSelector, int docId) {
		try {
			if (fieldSelector != null)
				fieldSelector.reset();
			return context.searcher().doc(docId, fieldSelector);
		} catch (IOException e) {
			throw new FetchPhaseExecutionException(context, "Failed to fetch doc id [" + docId + "]", e);
		}
	}
}
