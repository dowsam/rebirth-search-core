/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchScrollRequest.java 2012-3-29 15:01:02 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.core.action.ActionRequest;
import cn.com.rebirth.search.core.action.ActionRequestValidationException;
import cn.com.rebirth.search.core.action.ValidateActions;
import cn.com.rebirth.search.core.search.Scroll;


/**
 * The Class SearchScrollRequest.
 *
 * @author l.xue.nong
 */
public class SearchScrollRequest implements ActionRequest {

	
	/** The scroll id. */
	private String scrollId;

	
	/** The scroll. */
	private Scroll scroll;

	
	/** The listener threaded. */
	private boolean listenerThreaded = false;

	
	/** The operation threading. */
	private SearchOperationThreading operationThreading = SearchOperationThreading.THREAD_PER_SHARD;

	
	/**
	 * Instantiates a new search scroll request.
	 */
	public SearchScrollRequest() {
	}

	
	/**
	 * Instantiates a new search scroll request.
	 *
	 * @param scrollId the scroll id
	 */
	public SearchScrollRequest(String scrollId) {
		this.scrollId = scrollId;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#validate()
	 */
	@Override
	public ActionRequestValidationException validate() {
		ActionRequestValidationException validationException = null;
		if (scrollId == null) {
			validationException = ValidateActions.addValidationError("scrollId is missing", validationException);
		}
		return validationException;
	}

	
	/**
	 * Operation threading.
	 *
	 * @return the search operation threading
	 */
	public SearchOperationThreading operationThreading() {
		return this.operationThreading;
	}

	
	/**
	 * Operation threading.
	 *
	 * @param operationThreading the operation threading
	 * @return the search scroll request
	 */
	public SearchScrollRequest operationThreading(SearchOperationThreading operationThreading) {
		this.operationThreading = operationThreading;
		return this;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded()
	 */
	@Override
	public boolean listenerThreaded() {
		return listenerThreaded;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.ActionRequest#listenerThreaded(boolean)
	 */
	@Override
	public SearchScrollRequest listenerThreaded(boolean threadedListener) {
		this.listenerThreaded = threadedListener;
		return this;
	}

	
	/**
	 * Scroll id.
	 *
	 * @return the string
	 */
	public String scrollId() {
		return scrollId;
	}

	
	/**
	 * Scroll id.
	 *
	 * @param scrollId the scroll id
	 * @return the search scroll request
	 */
	public SearchScrollRequest scrollId(String scrollId) {
		this.scrollId = scrollId;
		return this;
	}

	
	/**
	 * Scroll.
	 *
	 * @return the scroll
	 */
	public Scroll scroll() {
		return scroll;
	}

	
	/**
	 * Scroll.
	 *
	 * @param scroll the scroll
	 * @return the search scroll request
	 */
	public SearchScrollRequest scroll(Scroll scroll) {
		this.scroll = scroll;
		return this;
	}

	
	/**
	 * Scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search scroll request
	 */
	public SearchScrollRequest scroll(TimeValue keepAlive) {
		return scroll(new Scroll(keepAlive));
	}

	
	/**
	 * Scroll.
	 *
	 * @param keepAlive the keep alive
	 * @return the search scroll request
	 */
	public SearchScrollRequest scroll(String keepAlive) {
		return scroll(new Scroll(TimeValue.parseTimeValue(keepAlive, null)));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		operationThreading = SearchOperationThreading.fromId(in.readByte());
		scrollId = in.readUTF();
		if (in.readBoolean()) {
			scroll = Scroll.readScroll(in);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeByte(operationThreading.id());
		out.writeUTF(scrollId);
		if (scroll == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			scroll.writeTo(out);
		}
	}
}
