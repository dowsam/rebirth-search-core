/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndicesSegmentsRequest.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.admin.indices.segments;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastOperationRequest;

/**
 * The Class IndicesSegmentsRequest.
 *
 * @author l.xue.nong
 */
public class IndicesSegmentsRequest extends BroadcastOperationRequest {

	/**
	 * Instantiates a new indices segments request.
	 */
	public IndicesSegmentsRequest() {
		this(Strings.EMPTY_ARRAY);
	}

	/**
	 * Instantiates a new indices segments request.
	 *
	 * @param indices the indices
	 */
	public IndicesSegmentsRequest(String... indices) {
		super(indices);
	}
}