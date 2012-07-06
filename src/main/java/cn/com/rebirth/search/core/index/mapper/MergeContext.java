/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MergeContext.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * The Class MergeContext.
 *
 * @author l.xue.nong
 */
public class MergeContext {

	/** The document mapper. */
	private final DocumentMapper documentMapper;

	/** The merge flags. */
	private final DocumentMapper.MergeFlags mergeFlags;

	/** The merge conflicts. */
	private final List<String> mergeConflicts = Lists.newArrayList();

	/**
	 * Instantiates a new merge context.
	 *
	 * @param documentMapper the document mapper
	 * @param mergeFlags the merge flags
	 */
	public MergeContext(DocumentMapper documentMapper, DocumentMapper.MergeFlags mergeFlags) {
		this.documentMapper = documentMapper;
		this.mergeFlags = mergeFlags;
	}

	/**
	 * Doc mapper.
	 *
	 * @return the document mapper
	 */
	public DocumentMapper docMapper() {
		return documentMapper;
	}

	/**
	 * Merge flags.
	 *
	 * @return the document mapper. merge flags
	 */
	public DocumentMapper.MergeFlags mergeFlags() {
		return mergeFlags;
	}

	/**
	 * Adds the conflict.
	 *
	 * @param mergeFailure the merge failure
	 */
	public void addConflict(String mergeFailure) {
		mergeConflicts.add(mergeFailure);
	}

	/**
	 * Checks for conflicts.
	 *
	 * @return true, if successful
	 */
	public boolean hasConflicts() {
		return !mergeConflicts.isEmpty();
	}

	/**
	 * Builds the conflicts.
	 *
	 * @return the string[]
	 */
	public String[] buildConflicts() {
		return mergeConflicts.toArray(new String[mergeConflicts.size()]);
	}
}
