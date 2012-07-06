/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterRerouteResponse.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.cluster.reroute;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class ClusterRerouteResponse.
 *
 * @author l.xue.nong
 */
public class ClusterRerouteResponse implements ActionResponse {

	
	/**
	 * Instantiates a new cluster reroute response.
	 */
	ClusterRerouteResponse() {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
	}
}
