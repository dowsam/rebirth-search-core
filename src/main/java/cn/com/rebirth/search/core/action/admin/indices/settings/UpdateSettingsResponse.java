/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UpdateSettingsResponse.java 2012-3-29 15:02:21 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.settings;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;


/**
 * The Class UpdateSettingsResponse.
 *
 * @author l.xue.nong
 */
public class UpdateSettingsResponse implements ActionResponse, Streamable {

	
	/**
	 * Instantiates a new update settings response.
	 */
	UpdateSettingsResponse() {
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
