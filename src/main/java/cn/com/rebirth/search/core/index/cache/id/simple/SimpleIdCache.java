/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimpleIdCache.java 2012-7-6 14:30:04 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id.simple;

import gnu.trove.impl.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.util.StringHelper;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.concurrent.ConcurrentCollections;
import cn.com.rebirth.commons.exception.RebirthException;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.trove.ExtTObjectIntHasMap;
import cn.com.rebirth.core.inject.Inject;
import cn.com.rebirth.search.commons.BytesWrap;
import cn.com.rebirth.search.core.index.AbstractIndexComponent;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.index.cache.id.IdCache;
import cn.com.rebirth.search.core.index.cache.id.IdReaderCache;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.ParentFieldMapper;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.settings.IndexSettings;

/**
 * The Class SimpleIdCache.
 *
 * @author l.xue.nong
 */
public class SimpleIdCache extends AbstractIndexComponent implements IdCache, IndexReader.ReaderFinishedListener {

	/** The id readers. */
	private final ConcurrentMap<Object, SimpleIdReaderCache> idReaders;

	/**
	 * Instantiates a new simple id cache.
	 *
	 * @param index the index
	 * @param indexSettings the index settings
	 */
	@Inject
	public SimpleIdCache(Index index, @IndexSettings Settings indexSettings) {
		super(index, indexSettings);
		idReaders = ConcurrentCollections.newConcurrentMap();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.component.CloseableComponent#close()
	 */
	@Override
	public void close() throws RebirthException {
		clear();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdCache#clear()
	 */
	@Override
	public void clear() {
		idReaders.clear();
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.index.IndexReader.ReaderFinishedListener#finished(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void finished(IndexReader reader) {
		clear(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdCache#clear(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public void clear(IndexReader reader) {
		idReaders.remove(reader.getCoreCacheKey());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdCache#reader(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public IdReaderCache reader(IndexReader reader) {
		return idReaders.get(reader.getCoreCacheKey());
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public Iterator<IdReaderCache> iterator() {
		return (Iterator<IdReaderCache>) idReaders.values();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdCache#refresh(org.apache.lucene.index.IndexReader[])
	 */
	@SuppressWarnings({ "StringEquality" })
	@Override
	public void refresh(IndexReader[] readers) throws Exception {

		if (refreshNeeded(readers)) {
			synchronized (idReaders) {
				if (!refreshNeeded(readers)) {
					return;
				}

				Map<Object, Map<String, TypeBuilder>> builders = new HashMap<Object, Map<String, TypeBuilder>>();

				for (IndexReader reader : readers) {
					if (idReaders.containsKey(reader.getCoreCacheKey())) {

						continue;
					}

					reader.addReaderFinishedListener(this);
					HashMap<String, TypeBuilder> readerBuilder = new HashMap<String, TypeBuilder>();
					builders.put(reader.getCoreCacheKey(), readerBuilder);

					String field = StringHelper.intern(UidFieldMapper.NAME);
					TermDocs termDocs = reader.termDocs();
					TermEnum termEnum = reader.terms(new Term(field));
					try {
						do {
							Term term = termEnum.term();
							if (term == null || term.field() != field)
								break;

							Uid uid = Uid.createUid(term.text());

							TypeBuilder typeBuilder = readerBuilder.get(uid.type());
							if (typeBuilder == null) {
								typeBuilder = new TypeBuilder(reader);
								readerBuilder.put(StringHelper.intern(uid.type()), typeBuilder);
							}

							BytesWrap idAsBytes = checkIfCanReuse(builders, new BytesWrap(uid.id()));
							termDocs.seek(termEnum);
							while (termDocs.next()) {

								if (!reader.isDeleted(termDocs.doc())) {
									typeBuilder.idToDoc.put(idAsBytes, termDocs.doc());
								}
							}
						} while (termEnum.next());
					} finally {
						termDocs.close();
						termEnum.close();
					}
				}

				for (IndexReader reader : readers) {
					if (idReaders.containsKey(reader.getCoreCacheKey())) {

						continue;
					}

					Map<String, TypeBuilder> readerBuilder = builders.get(reader.getCoreCacheKey());

					String field = StringHelper.intern(ParentFieldMapper.NAME);
					TermDocs termDocs = reader.termDocs();
					TermEnum termEnum = reader.terms(new Term(field));
					try {
						do {
							Term term = termEnum.term();
							if (term == null || term.field() != field)
								break;

							Uid uid = Uid.createUid(term.text());

							TypeBuilder typeBuilder = readerBuilder.get(uid.type());
							if (typeBuilder == null) {
								typeBuilder = new TypeBuilder(reader);
								readerBuilder.put(StringHelper.intern(uid.type()), typeBuilder);
							}

							BytesWrap idAsBytes = checkIfCanReuse(builders, new BytesWrap(uid.id()));
							boolean added = false;

							termDocs.seek(termEnum);
							while (termDocs.next()) {

								if (!reader.isDeleted(termDocs.doc())) {
									if (!added) {
										typeBuilder.parentIdsValues.add(idAsBytes);
										added = true;
									}
									typeBuilder.parentIdsOrdinals[termDocs.doc()] = typeBuilder.t;
								}
							}
							if (added) {
								typeBuilder.t++;
							}
						} while (termEnum.next());
					} finally {
						termDocs.close();
						termEnum.close();
					}
				}

				for (Map.Entry<Object, Map<String, TypeBuilder>> entry : builders.entrySet()) {
					MapBuilder<String, SimpleIdReaderTypeCache> types = MapBuilder.newMapBuilder();
					for (Map.Entry<String, TypeBuilder> typeBuilderEntry : entry.getValue().entrySet()) {
						types.put(
								typeBuilderEntry.getKey(),
								new SimpleIdReaderTypeCache(typeBuilderEntry.getKey(),
										typeBuilderEntry.getValue().idToDoc,
										typeBuilderEntry.getValue().parentIdsValues
												.toArray(new BytesWrap[typeBuilderEntry.getValue().parentIdsValues
														.size()]), typeBuilderEntry.getValue().parentIdsOrdinals));
					}
					SimpleIdReaderCache readerCache = new SimpleIdReaderCache(entry.getKey(), types.immutableMap());
					idReaders.put(readerCache.readerCacheKey(), readerCache);
				}
			}
		}
	}

	/**
	 * Check if can reuse.
	 *
	 * @param builders the builders
	 * @param idAsBytes the id as bytes
	 * @return the bytes wrap
	 */
	private BytesWrap checkIfCanReuse(Map<Object, Map<String, TypeBuilder>> builders, BytesWrap idAsBytes) {
		BytesWrap finalIdAsBytes;

		for (SimpleIdReaderCache idReaderCache : idReaders.values()) {
			finalIdAsBytes = idReaderCache.canReuse(idAsBytes);
			if (finalIdAsBytes != null) {
				return finalIdAsBytes;
			}
		}
		for (Map<String, TypeBuilder> map : builders.values()) {
			for (TypeBuilder typeBuilder : map.values()) {
				finalIdAsBytes = typeBuilder.canReuse(idAsBytes);
				if (finalIdAsBytes != null) {
					return finalIdAsBytes;
				}
			}
		}
		return idAsBytes;
	}

	/**
	 * Refresh needed.
	 *
	 * @param readers the readers
	 * @return true, if successful
	 */
	private boolean refreshNeeded(IndexReader[] readers) {
		for (IndexReader reader : readers) {
			if (!idReaders.containsKey(reader.getCoreCacheKey())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The Class TypeBuilder.
	 *
	 * @author l.xue.nong
	 */
	static class TypeBuilder {

		/** The id to doc. */
		final ExtTObjectIntHasMap<BytesWrap> idToDoc = new ExtTObjectIntHasMap<BytesWrap>(Constants.DEFAULT_CAPACITY,
				Constants.DEFAULT_LOAD_FACTOR, -1);

		/** The parent ids values. */
		final ArrayList<BytesWrap> parentIdsValues = new ArrayList<BytesWrap>();

		/** The parent ids ordinals. */
		final int[] parentIdsOrdinals;

		/** The t. */
		int t = 1;

		/**
		 * Instantiates a new type builder.
		 *
		 * @param reader the reader
		 */
		TypeBuilder(IndexReader reader) {
			parentIdsOrdinals = new int[reader.maxDoc()];

			parentIdsValues.add(null);
		}

		/**
		 * Can reuse.
		 *
		 * @param id the id
		 * @return the bytes wrap
		 */
		public BytesWrap canReuse(BytesWrap id) {
			return idToDoc.key(id);
		}
	}
}
