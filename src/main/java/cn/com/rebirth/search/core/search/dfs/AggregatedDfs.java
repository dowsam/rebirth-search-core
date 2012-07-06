/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AggregatedDfs.java 2012-3-29 15:01:00 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.dfs;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;

import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.trove.ExtTObjectIntHasMap;


/**
 * The Class AggregatedDfs.
 *
 * @author l.xue.nong
 */
public class AggregatedDfs implements Streamable {

	
	/** The df map. */
	private TObjectIntHashMap<Term> dfMap;

	
	/** The max doc. */
	private long maxDoc;

	
	/**
	 * Instantiates a new aggregated dfs.
	 */
	private AggregatedDfs() {

	}

	
	/**
	 * Instantiates a new aggregated dfs.
	 *
	 * @param dfMap the df map
	 * @param maxDoc the max doc
	 */
	public AggregatedDfs(TObjectIntHashMap<Term> dfMap, long maxDoc) {
		this.dfMap = dfMap;
		this.maxDoc = maxDoc;
	}

	
	/**
	 * Df map.
	 *
	 * @return the t object int hash map
	 */
	public TObjectIntHashMap<Term> dfMap() {
		return dfMap;
	}

	
	/**
	 * Max doc.
	 *
	 * @return the long
	 */
	public long maxDoc() {
		return maxDoc;
	}

	
	/**
	 * Read aggregated dfs.
	 *
	 * @param in the in
	 * @return the aggregated dfs
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static AggregatedDfs readAggregatedDfs(StreamInput in) throws IOException {
		AggregatedDfs result = new AggregatedDfs();
		result.readFrom(in);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		dfMap = new ExtTObjectIntHasMap<Term>(size, Constants.DEFAULT_LOAD_FACTOR, -1);
		for (int i = 0; i < size; i++) {
			dfMap.put(new Term(in.readUTF(), in.readUTF()), in.readVInt());
		}
		maxDoc = in.readVLong();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(final StreamOutput out) throws IOException {
		out.writeVInt(dfMap.size());

		for (TObjectIntIterator<Term> it = dfMap.iterator(); it.hasNext();) {
			it.advance();
			out.writeUTF(it.key().field());
			out.writeUTF(it.key().text());
			out.writeVInt(it.value());
		}
		out.writeVLong(maxDoc);
	}
}
