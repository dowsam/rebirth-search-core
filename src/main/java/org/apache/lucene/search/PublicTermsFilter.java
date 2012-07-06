/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core PublicTermsFilter.java 2012-7-6 14:30:27 l.xue.nong$$
 */

package org.apache.lucene.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Class PublicTermsFilter.
 *
 * @author l.xue.nong
 */
public class PublicTermsFilter extends Filter {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2151598365150915730L;

	/** The terms. */
	Set<Term> terms = new TreeSet<Term>();

	/**
	 * Adds the term.
	 *
	 * @param term the term
	 */
	public void addTerm(Term term) {
		terms.add(term);
	}

	/**
	 * Gets the terms.
	 *
	 * @return the terms
	 */
	public Set<Term> getTerms() {
		return terms;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		PublicTermsFilter test = (PublicTermsFilter) obj;
		return (terms == test.terms || (terms != null && terms.equals(test.terms)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = 9;
		for (Iterator<Term> iter = terms.iterator(); iter.hasNext();) {
			Term term = iter.next();
			hash = 31 * hash + term.hashCode();
		}
		return hash;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
	 */
	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		FixedBitSet result = null;
		TermDocs td = reader.termDocs();
		try {
			for (Term term : terms) {
				td.seek(term);
				if (td.next()) {
					if (result == null) {
						result = new FixedBitSet(reader.maxDoc());
					}
					result.set(td.doc());
					while (td.next()) {
						result.set(td.doc());
					}
				}
			}
		} finally {
			td.close();
		}
		return result;
	}
}
