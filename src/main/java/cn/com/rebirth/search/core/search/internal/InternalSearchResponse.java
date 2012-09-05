/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalSearchResponse.java 2012-7-6 14:30:47 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.core.search.SearchHits;
import cn.com.rebirth.search.core.search.facet.Facets;
import cn.com.rebirth.search.core.search.facet.InternalFacets;

/**
 * The Class InternalSearchResponse.
 *
 * @author l.xue.nong
 */
public class InternalSearchResponse implements Streamable, ToXContent {

	/** The hits. */
	private InternalSearchHits hits;

	/** The facets. */
	private InternalFacets facets;

	/** The timed out. */
	private boolean timedOut;

	/** The Constant EMPTY. */
	public static final InternalSearchResponse EMPTY = new InternalSearchResponse(new InternalSearchHits(
			new InternalSearchHit[0], 0, 0), null, false);

	/**
	 * Instantiates a new internal search response.
	 */
	private InternalSearchResponse() {
	}

	/**
	 * Instantiates a new internal search response.
	 *
	 * @param hits the hits
	 * @param facets the facets
	 * @param timedOut the timed out
	 */
	public InternalSearchResponse(InternalSearchHits hits, InternalFacets facets, boolean timedOut) {
		this.hits = hits;
		this.facets = facets;
		this.timedOut = timedOut;
	}

	/**
	 * Timed out.
	 *
	 * @return true, if successful
	 */
	public boolean timedOut() {
		return this.timedOut;
	}

	/**
	 * Hits.
	 *
	 * @return the search hits
	 */
	public SearchHits hits() {
		return hits;
	}

	/**
	 * Facets.
	 *
	 * @return the facets
	 */
	public Facets facets() {
		return facets;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		hits.toXContent(builder, params);
		if (facets != null) {
			facets.toXContent(builder, params);
		}
		return builder;
	}

	/**
	 * Read internal search response.
	 *
	 * @param in the in
	 * @return the internal search response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static InternalSearchResponse readInternalSearchResponse(StreamInput in) throws IOException {
		InternalSearchResponse response = new InternalSearchResponse();
		response.readFrom(in);
		return response;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		hits = InternalSearchHits.readSearchHits(in);
		if (in.readBoolean()) {
			facets = InternalFacets.readFacets(in);
		}
		timedOut = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		hits.writeTo(out);
		if (facets == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			facets.writeTo(out);
		}
		out.writeBoolean(timedOut);
	}
}
