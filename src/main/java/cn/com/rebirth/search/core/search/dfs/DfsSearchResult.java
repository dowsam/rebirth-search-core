/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DfsSearchResult.java 2012-7-6 14:29:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.dfs;

import java.io.IOException;

import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.search.SearchPhaseResult;
import cn.com.rebirth.search.core.search.SearchShardTarget;

/**
 * The Class DfsSearchResult.
 *
 * @author l.xue.nong
 */
public class DfsSearchResult implements SearchPhaseResult {

	/** The empty terms. */
	private static Term[] EMPTY_TERMS = new Term[0];

	/** The empty freqs. */
	private static int[] EMPTY_FREQS = new int[0];

	/** The shard target. */
	private SearchShardTarget shardTarget;

	/** The id. */
	private long id;

	/** The terms. */
	private Term[] terms;

	/** The freqs. */
	private int[] freqs;

	/** The max doc. */
	private int maxDoc;

	/**
	 * Instantiates a new dfs search result.
	 */
	public DfsSearchResult() {

	}

	/**
	 * Instantiates a new dfs search result.
	 *
	 * @param id the id
	 * @param shardTarget the shard target
	 */
	public DfsSearchResult(long id, SearchShardTarget shardTarget) {
		this.id = id;
		this.shardTarget = shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#id()
	 */
	public long id() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget()
	 */
	public SearchShardTarget shardTarget() {
		return shardTarget;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.SearchPhaseResult#shardTarget(cn.com.rebirth.search.core.search.SearchShardTarget)
	 */
	@Override
	public void shardTarget(SearchShardTarget shardTarget) {
		this.shardTarget = shardTarget;
	}

	/**
	 * Max doc.
	 *
	 * @param maxDoc the max doc
	 * @return the dfs search result
	 */
	public DfsSearchResult maxDoc(int maxDoc) {
		this.maxDoc = maxDoc;
		return this;
	}

	/**
	 * Max doc.
	 *
	 * @return the int
	 */
	public int maxDoc() {
		return maxDoc;
	}

	/**
	 * Terms and freqs.
	 *
	 * @param terms the terms
	 * @param freqs the freqs
	 * @return the dfs search result
	 */
	public DfsSearchResult termsAndFreqs(Term[] terms, int[] freqs) {
		this.terms = terms;
		this.freqs = freqs;
		return this;
	}

	/**
	 * Terms.
	 *
	 * @return the term[]
	 */
	public Term[] terms() {
		return terms;
	}

	/**
	 * Freqs.
	 *
	 * @return the int[]
	 */
	public int[] freqs() {
		return freqs;
	}

	/**
	 * Read dfs search result.
	 *
	 * @param in the in
	 * @return the dfs search result
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static DfsSearchResult readDfsSearchResult(StreamInput in) throws IOException, ClassNotFoundException {
		DfsSearchResult result = new DfsSearchResult();
		result.readFrom(in);
		return result;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readLong();

		int termsSize = in.readVInt();
		if (termsSize == 0) {
			terms = EMPTY_TERMS;
		} else {
			terms = new Term[termsSize];
			for (int i = 0; i < terms.length; i++) {
				terms[i] = new Term(in.readUTF(), in.readUTF());
			}
		}
		int freqsSize = in.readVInt();
		if (freqsSize == 0) {
			freqs = EMPTY_FREQS;
		} else {
			freqs = new int[freqsSize];
			for (int i = 0; i < freqs.length; i++) {
				freqs[i] = in.readVInt();
			}
		}
		maxDoc = in.readVInt();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeLong(id);

		out.writeVInt(terms.length);
		for (Term term : terms) {
			out.writeUTF(term.field());
			out.writeUTF(term.text());
		}
		out.writeVInt(freqs.length);
		for (int freq : freqs) {
			out.writeVInt(freq);
		}
		out.writeVInt(maxDoc);
	}
}
