/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core UidFilter.java 2012-3-29 15:02:36 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.UnicodeUtil;

import cn.com.rebirth.commons.Unicode;
import cn.com.rebirth.search.commons.bloom.BloomFilter;
import cn.com.rebirth.search.core.index.cache.bloom.BloomCache;
import cn.com.rebirth.search.core.index.mapper.Uid;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;


/**
 * The Class UidFilter.
 *
 * @author l.xue.nong
 */
public class UidFilter extends Filter {

	
	/** The uids. */
	private final Set<Term> uids;

	
	/** The bloom cache. */
	private final BloomCache bloomCache;

	
	/**
	 * Instantiates a new uid filter.
	 *
	 * @param types the types
	 * @param ids the ids
	 * @param bloomCache the bloom cache
	 */
	public UidFilter(Collection<String> types, List<String> ids, BloomCache bloomCache) {
		this.bloomCache = bloomCache;
		this.uids = new TreeSet<Term>();
		for (String type : types) {
			for (String id : ids) {
				uids.add(UidFieldMapper.TERM_FACTORY.createTerm(Uid.createUid(type, id)));
			}
		}
	}

	
	
	
	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		BloomFilter filter = bloomCache.filter(reader, UidFieldMapper.NAME, true);
		FixedBitSet set = null;
		TermDocs td = null;
		try {
			for (Term uid : uids) {
				UnicodeUtil.UTF8Result utf8 = Unicode.fromStringAsUtf8(uid.text());
				if (!filter.isPresent(utf8.result, 0, utf8.length)) {
					continue;
				}
				if (td == null) {
					td = reader.termDocs();
				}
				td.seek(uid);
				while (td.next()) {
					if (set == null) {
						set = new FixedBitSet(reader.maxDoc());
					}
					set.set(td.doc());
				}
			}
		} finally {
			if (td != null) {
				td.close();
			}
		}
		return set;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UidFilter uidFilter = (UidFilter) o;
		return !uids.equals(uidFilter.uids);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return uids.hashCode();
	}
}