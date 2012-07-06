/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardSegments.java 2012-3-29 15:02:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.segments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationResponse;
import cn.com.rebirth.search.core.cluster.routing.ImmutableShardRouting;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.index.engine.Segment;

import com.google.common.collect.ImmutableList;


/**
 * The Class ShardSegments.
 *
 * @author l.xue.nong
 */
public class ShardSegments extends BroadcastShardOperationResponse implements Iterable<Segment> {

	
	/** The shard routing. */
	private ShardRouting shardRouting;

	
	/** The segments. */
	private List<Segment> segments;

	
	/**
	 * Instantiates a new shard segments.
	 */
	ShardSegments() {
	}

	
	/**
	 * Instantiates a new shard segments.
	 *
	 * @param shardRouting the shard routing
	 * @param segments the segments
	 */
	public ShardSegments(ShardRouting shardRouting, List<Segment> segments) {
		super(shardRouting.index(), shardRouting.id());
		this.shardRouting = shardRouting;
		this.segments = segments;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Segment> iterator() {
		return segments.iterator();
	}

	
	/**
	 * Shard routing.
	 *
	 * @return the shard routing
	 */
	public ShardRouting shardRouting() {
		return this.shardRouting;
	}

	
	/**
	 * Gets the shard routing.
	 *
	 * @return the shard routing
	 */
	public ShardRouting getShardRouting() {
		return this.shardRouting;
	}

	
	/**
	 * Segments.
	 *
	 * @return the list
	 */
	public List<Segment> segments() {
		return this.segments;
	}

	
	/**
	 * Gets the segments.
	 *
	 * @return the segments
	 */
	public List<Segment> getSegments() {
		return segments;
	}

	
	/**
	 * Number of committed.
	 *
	 * @return the int
	 */
	public int numberOfCommitted() {
		int count = 0;
		for (Segment segment : segments) {
			if (segment.committed()) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * Number of search.
	 *
	 * @return the int
	 */
	public int numberOfSearch() {
		int count = 0;
		for (Segment segment : segments) {
			if (segment.search()) {
				count++;
			}
		}
		return count;
	}

	
	/**
	 * Read shard segments.
	 *
	 * @param in the in
	 * @return the shard segments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ShardSegments readShardSegments(StreamInput in) throws IOException {
		ShardSegments shard = new ShardSegments();
		shard.readFrom(in);
		return shard;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		super.readFrom(in);
		shardRouting =ImmutableShardRouting. readShardRoutingEntry(in);
		int size = in.readVInt();
		if (size == 0) {
			segments = ImmutableList.of();
		} else {
			segments = new ArrayList<Segment>(size);
			for (int i = 0; i < size; i++) {
				segments.add(Segment.readSegment(in));
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationResponse#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		super.writeTo(out);
		shardRouting.writeTo(out);
		out.writeVInt(segments.size());
		for (Segment segment : segments) {
			segment.writeTo(out);
		}
	}
}