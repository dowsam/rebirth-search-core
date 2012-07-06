/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StringFieldsFunctionDataComparator.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.field.function.sort;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SortField;

import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource;


/**
 * The Class StringFieldsFunctionDataComparator.
 *
 * @author l.xue.nong
 */
public class StringFieldsFunctionDataComparator extends FieldComparator {

	
	/**
	 * Comparator source.
	 *
	 * @param script the script
	 * @return the extended field comparator source
	 */
	public static ExtendedFieldComparatorSource comparatorSource(SearchScript script) {
		return new InnerSource(script);
	}

	
	/**
	 * The Class InnerSource.
	 *
	 * @author l.xue.nong
	 */
	private static class InnerSource extends ExtendedFieldComparatorSource {

		
		/** The script. */
		private final SearchScript script;

		
		/**
		 * Instantiates a new inner source.
		 *
		 * @param script the script
		 */
		private InnerSource(SearchScript script) {
			this.script = script;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.search.FieldComparatorSource#newComparator(java.lang.String, int, int, boolean)
		 */
		@Override
		public FieldComparator newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
				throws IOException {
			return new StringFieldsFunctionDataComparator(numHits, script);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.index.field.data.ExtendedFieldComparatorSource#reducedType()
		 */
		@Override
		public int reducedType() {
			return SortField.STRING;
		}
	}

	
	/** The script. */
	private final SearchScript script;

	
	/** The values. */
	private String[] values;

	
	/** The bottom. */
	private String bottom;

	
	/**
	 * Instantiates a new string fields function data comparator.
	 *
	 * @param numHits the num hits
	 * @param script the script
	 */
	public StringFieldsFunctionDataComparator(int numHits, SearchScript script) {
		this.script = script;
		values = new String[numHits];
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException {
		script.setNextReader(reader);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) {
		script.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compare(int, int)
	 */
	@Override
	public int compare(int slot1, int slot2) {
		final String val1 = values[slot1];
		final String val2 = values[slot2];
		if (val1 == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		}

		return val1.compareTo(val2);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {
		script.setNextDocId(doc);
		final String val2 = script.run().toString();
		if (bottom == null) {
			if (val2 == null) {
				return 0;
			}
			return -1;
		} else if (val2 == null) {
			return 1;
		}
		return bottom.compareTo(val2);
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		script.setNextDocId(doc);
		values[slot] = script.run().toString();
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#setBottom(int)
	 */
	@Override
	public void setBottom(final int bottom) {
		this.bottom = values[bottom];
	}

	
	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#value(int)
	 */
	@Override
	public Comparable value(int slot) {
		return values[slot];
	}
}
