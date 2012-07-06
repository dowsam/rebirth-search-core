/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DoubleFieldsFunctionDataComparator.java 2012-7-6 14:30:23 l.xue.nong$$
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
 * The Class DoubleFieldsFunctionDataComparator.
 *
 * @author l.xue.nong
 */
public class DoubleFieldsFunctionDataComparator extends FieldComparator {

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
			return new DoubleFieldsFunctionDataComparator(numHits, script);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.index.field.data.ExtendedFieldComparatorSource#reducedType()
		 */
		@Override
		public int reducedType() {
			return SortField.DOUBLE;
		}
	}

	/** The script. */
	private final SearchScript script;

	/** The values. */
	private final double[] values;

	/** The bottom. */
	private double bottom;

	/**
	 * Instantiates a new double fields function data comparator.
	 *
	 * @param numHits the num hits
	 * @param script the script
	 */
	public DoubleFieldsFunctionDataComparator(int numHits, SearchScript script) {
		this.script = script;
		values = new double[numHits];
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
		final double v1 = values[slot1];
		final double v2 = values[slot2];
		if (v1 > v2) {
			return 1;
		} else if (v1 < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#compareBottom(int)
	 */
	@Override
	public int compareBottom(int doc) {
		script.setNextDocId(doc);
		final double v2 = script.runAsDouble();
		if (bottom > v2) {
			return 1;
		} else if (bottom < v2) {
			return -1;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.search.FieldComparator#copy(int, int)
	 */
	@Override
	public void copy(int slot, int doc) {
		script.setNextDocId(doc);
		values[slot] = script.runAsDouble();
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
		return Double.valueOf(values[slot]);
	}
}
