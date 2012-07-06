/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchResponse.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.rest.RestStatus;
import cn.com.rebirth.search.core.search.SearchHits;
import cn.com.rebirth.search.core.search.facet.Facets;
import cn.com.rebirth.search.core.search.internal.InternalSearchResponse;


/**
 * The Class SearchResponse.
 *
 * @author l.xue.nong
 */
public class SearchResponse implements ActionResponse, ToXContent {

	
	/** The internal response. */
	private InternalSearchResponse internalResponse;

	
	/** The scroll id. */
	private String scrollId;

	
	/** The total shards. */
	private int totalShards;

	
	/** The successful shards. */
	private int successfulShards;

	
	/** The shard failures. */
	private ShardSearchFailure[] shardFailures;

	
	/** The took in millis. */
	private long tookInMillis;

	
	/**
	 * Instantiates a new search response.
	 */
	public SearchResponse() {
	}

	
	/**
	 * Instantiates a new search response.
	 *
	 * @param internalResponse the internal response
	 * @param scrollId the scroll id
	 * @param totalShards the total shards
	 * @param successfulShards the successful shards
	 * @param tookInMillis the took in millis
	 * @param shardFailures the shard failures
	 */
	public SearchResponse(InternalSearchResponse internalResponse, String scrollId, int totalShards,
			int successfulShards, long tookInMillis, ShardSearchFailure[] shardFailures) {
		this.internalResponse = internalResponse;
		this.scrollId = scrollId;
		this.totalShards = totalShards;
		this.successfulShards = successfulShards;
		this.tookInMillis = tookInMillis;
		this.shardFailures = shardFailures;
	}

	
	/**
	 * Status.
	 *
	 * @return the rest status
	 */
	public RestStatus status() {
		if (shardFailures.length == 0) {
			return RestStatus.OK;
		}
		if (successfulShards == 0 && totalShards > 0) {
			RestStatus status = shardFailures[0].status();
			if (shardFailures.length > 1) {
				for (int i = 1; i < shardFailures.length; i++) {
					if (shardFailures[i].status().getStatus() >= 500) {
						status = shardFailures[i].status();
					}
				}
			}
			return status;
		}
		return RestStatus.OK;
	}

	
	/**
	 * Hits.
	 *
	 * @return the search hits
	 */
	public SearchHits hits() {
		return internalResponse.hits();
	}

	
	/**
	 * Gets the hits.
	 *
	 * @return the hits
	 */
	public SearchHits getHits() {
		return hits();
	}

	
	/**
	 * Facets.
	 *
	 * @return the facets
	 */
	public Facets facets() {
		return internalResponse.facets();
	}

	
	/**
	 * Gets the facets.
	 *
	 * @return the facets
	 */
	public Facets getFacets() {
		return facets();
	}

	
	/**
	 * Timed out.
	 *
	 * @return true, if successful
	 */
	public boolean timedOut() {
		return internalResponse.timedOut();
	}

	
	/**
	 * Checks if is timed out.
	 *
	 * @return true, if is timed out
	 */
	public boolean isTimedOut() {
		return timedOut();
	}

	
	/**
	 * Took.
	 *
	 * @return the time value
	 */
	public TimeValue took() {
		return new TimeValue(tookInMillis);
	}

	
	/**
	 * Gets the took.
	 *
	 * @return the took
	 */
	public TimeValue getTook() {
		return took();
	}

	
	/**
	 * Took in millis.
	 *
	 * @return the long
	 */
	public long tookInMillis() {
		return tookInMillis;
	}

	
	/**
	 * Gets the took in millis.
	 *
	 * @return the took in millis
	 */
	public long getTookInMillis() {
		return tookInMillis();
	}

	
	/**
	 * Total shards.
	 *
	 * @return the int
	 */
	public int totalShards() {
		return totalShards;
	}

	
	/**
	 * Gets the total shards.
	 *
	 * @return the total shards
	 */
	public int getTotalShards() {
		return totalShards;
	}

	
	/**
	 * Successful shards.
	 *
	 * @return the int
	 */
	public int successfulShards() {
		return successfulShards;
	}

	
	/**
	 * Gets the successful shards.
	 *
	 * @return the successful shards
	 */
	public int getSuccessfulShards() {
		return successfulShards;
	}

	
	/**
	 * Failed shards.
	 *
	 * @return the int
	 */
	public int failedShards() {
		return totalShards - successfulShards;
	}

	
	/**
	 * Gets the failed shards.
	 *
	 * @return the failed shards
	 */
	public int getFailedShards() {
		return failedShards();
	}

	
	/**
	 * Shard failures.
	 *
	 * @return the shard search failure[]
	 */
	public ShardSearchFailure[] shardFailures() {
		return this.shardFailures;
	}

	
	/**
	 * Gets the shard failures.
	 *
	 * @return the shard failures
	 */
	public ShardSearchFailure[] getShardFailures() {
		return shardFailures;
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
	 * Gets the scroll id.
	 *
	 * @return the scroll id
	 */
	public String getScrollId() {
		return scrollId;
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant _SCROLL_ID. */
		static final XContentBuilderString _SCROLL_ID = new XContentBuilderString("_scroll_id");

		
		/** The Constant _SHARDS. */
		static final XContentBuilderString _SHARDS = new XContentBuilderString("_shards");

		
		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		
		/** The Constant SUCCESSFUL. */
		static final XContentBuilderString SUCCESSFUL = new XContentBuilderString("successful");

		
		/** The Constant FAILED. */
		static final XContentBuilderString FAILED = new XContentBuilderString("failed");

		
		/** The Constant FAILURES. */
		static final XContentBuilderString FAILURES = new XContentBuilderString("failures");

		
		/** The Constant STATUS. */
		static final XContentBuilderString STATUS = new XContentBuilderString("status");

		
		/** The Constant INDEX. */
		static final XContentBuilderString INDEX = new XContentBuilderString("index");

		
		/** The Constant SHARD. */
		static final XContentBuilderString SHARD = new XContentBuilderString("shard");

		
		/** The Constant REASON. */
		static final XContentBuilderString REASON = new XContentBuilderString("reason");

		
		/** The Constant TOOK. */
		static final XContentBuilderString TOOK = new XContentBuilderString("took");

		
		/** The Constant TIMED_OUT. */
		static final XContentBuilderString TIMED_OUT = new XContentBuilderString("timed_out");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		if (scrollId != null) {
			builder.field(Fields._SCROLL_ID, scrollId);
		}
		builder.field(Fields.TOOK, tookInMillis);
		builder.field(Fields.TIMED_OUT, timedOut());
		builder.startObject(Fields._SHARDS);
		builder.field(Fields.TOTAL, totalShards());
		builder.field(Fields.SUCCESSFUL, successfulShards());
		builder.field(Fields.FAILED, failedShards());

		if (shardFailures.length > 0) {
			builder.startArray(Fields.FAILURES);
			for (ShardSearchFailure shardFailure : shardFailures) {
				builder.startObject();
				if (shardFailure.shard() != null) {
					builder.field(Fields.INDEX, shardFailure.shard().index());
					builder.field(Fields.SHARD, shardFailure.shard().shardId());
				}
				builder.field(Fields.STATUS, shardFailure.status().getStatus());
				builder.field(Fields.REASON, shardFailure.reason());
				builder.endObject();
			}
			builder.endArray();
		}

		builder.endObject();
		internalResponse.toXContent(builder, params);
		return builder;
	}

	
	/**
	 * Read search response.
	 *
	 * @param in the in
	 * @return the search response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static SearchResponse readSearchResponse(StreamInput in) throws IOException {
		SearchResponse response = new SearchResponse();
		response.readFrom(in);
		return response;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		internalResponse = InternalSearchResponse.readInternalSearchResponse(in);
		totalShards = in.readVInt();
		successfulShards = in.readVInt();
		int size = in.readVInt();
		if (size == 0) {
			shardFailures = ShardSearchFailure.EMPTY_ARRAY;
		} else {
			shardFailures = new ShardSearchFailure[size];
			for (int i = 0; i < shardFailures.length; i++) {
				shardFailures[i] = ShardSearchFailure.readShardSearchFailure(in);
			}
		}
		if (in.readBoolean()) {
			scrollId = in.readUTF();
		}
		tookInMillis = in.readVLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		internalResponse.writeTo(out);
		out.writeVInt(totalShards);
		out.writeVInt(successfulShards);

		out.writeVInt(shardFailures.length);
		for (ShardSearchFailure shardSearchFailure : shardFailures) {
			shardSearchFailure.writeTo(out);
		}

		if (scrollId == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(scrollId);
		}
		out.writeVLong(tookInMillis);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder().prettyPrint();
			builder.startObject();
			toXContent(builder, EMPTY_PARAMS);
			builder.endObject();
			return builder.string();
		} catch (IOException e) {
			return "{ \"error\" : \"" + e.getMessage() + "\"}";
		}
	}
}
