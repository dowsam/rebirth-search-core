/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core SimpleIdReaderTypeCache.java 2012-7-6 14:30:12 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.cache.id.simple;

import cn.com.rebirth.commons.trove.ExtTObjectIntHasMap;
import cn.com.rebirth.search.commons.BytesWrap;
import cn.com.rebirth.search.core.index.cache.id.IdReaderTypeCache;

/**
 * The Class SimpleIdReaderTypeCache.
 *
 * @author l.xue.nong
 */
public class SimpleIdReaderTypeCache implements IdReaderTypeCache {

	/** The type. */
	private final String type;

	/** The id to doc. */
	private final ExtTObjectIntHasMap<BytesWrap> idToDoc;

	/** The parent ids values. */
	private final BytesWrap[] parentIdsValues;

	/** The parent ids ordinals. */
	private final int[] parentIdsOrdinals;

	/**
	 * Instantiates a new simple id reader type cache.
	 *
	 * @param type the type
	 * @param idToDoc the id to doc
	 * @param parentIdsValues the parent ids values
	 * @param parentIdsOrdinals the parent ids ordinals
	 */
	public SimpleIdReaderTypeCache(String type, ExtTObjectIntHasMap<BytesWrap> idToDoc, BytesWrap[] parentIdsValues,
			int[] parentIdsOrdinals) {
		this.type = type;
		this.idToDoc = idToDoc;
		this.idToDoc.trimToSize();
		this.parentIdsValues = parentIdsValues;
		this.parentIdsOrdinals = parentIdsOrdinals;
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderTypeCache#parentIdByDoc(int)
	 */
	public BytesWrap parentIdByDoc(int docId) {
		return parentIdsValues[parentIdsOrdinals[docId]];
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.cache.id.IdReaderTypeCache#docById(cn.com.rebirth.search.commons.BytesWrap)
	 */
	public int docById(BytesWrap id) {
		return idToDoc.get(id);
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
