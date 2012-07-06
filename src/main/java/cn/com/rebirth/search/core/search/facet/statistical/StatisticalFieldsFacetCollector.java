/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StatisticalFieldsFacetCollector.java 2012-3-29 15:01:51 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class StatisticalFieldsFacetCollector.
 *
 * @author l.xue.nong
 */
public class StatisticalFieldsFacetCollector extends AbstractFacetCollector {

	
	/** The index fields names. */
	private final String[] indexFieldsNames;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/** The fields data type. */
	private final FieldDataType[] fieldsDataType;

	
	/** The fields data. */
	private NumericFieldData[] fieldsData;

	
	/** The stats proc. */
	private final StatsProc statsProc = new StatsProc();

	
	/**
	 * Instantiates a new statistical fields facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldsNames the fields names
	 * @param context the context
	 */
	public StatisticalFieldsFacetCollector(String facetName, String[] fieldsNames, SearchContext context) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();

		fieldsDataType = new FieldDataType[fieldsNames.length];
		fieldsData = new NumericFieldData[fieldsNames.length];
		indexFieldsNames = new String[fieldsNames.length];

		for (int i = 0; i < fieldsNames.length; i++) {
			FieldMapper mapper = context.smartNameFieldMapper(fieldsNames[i]);
			if (mapper == null) {
				throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldsNames[i] + "]");
			}
			indexFieldsNames[i] = mapper.names().indexName();
			fieldsDataType[i] = mapper.fieldDataType();
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		for (NumericFieldData fieldData : fieldsData) {
			fieldData.forEachValueInDoc(doc, statsProc);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		for (int i = 0; i < indexFieldsNames.length; i++) {
			fieldsData[i] = (NumericFieldData) fieldDataCache.cache(fieldsDataType[i], reader, indexFieldsNames[i]);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalStatisticalFacet(facetName, statsProc.min(), statsProc.max(), statsProc.total(),
				statsProc.sumOfSquares(), statsProc.count());
	}

	
	/**
	 * The Class StatsProc.
	 *
	 * @author l.xue.nong
	 */
	public static class StatsProc implements NumericFieldData.MissingDoubleValueInDocProc {

		
		/** The min. */
		double min = Double.POSITIVE_INFINITY;

		
		/** The max. */
		double max = Double.NEGATIVE_INFINITY;

		
		/** The total. */
		double total = 0;

		
		/** The sum of squares. */
		double sumOfSquares = 0.0;

		
		/** The count. */
		long count;

		
		/** The missing. */
		int missing;

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc#onValue(int, double)
		 */
		@Override
		public void onValue(int docId, double value) {
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
			sumOfSquares += value * value;
			total += value;
			count++;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc#onMissing(int)
		 */
		@Override
		public void onMissing(int docId) {
			missing++;
		}

		
		/**
		 * Min.
		 *
		 * @return the double
		 */
		public final double min() {
			return min;
		}

		
		/**
		 * Max.
		 *
		 * @return the double
		 */
		public final double max() {
			return max;
		}

		
		/**
		 * Total.
		 *
		 * @return the double
		 */
		public final double total() {
			return total;
		}

		
		/**
		 * Count.
		 *
		 * @return the long
		 */
		public final long count() {
			return count;
		}

		
		/**
		 * Sum of squares.
		 *
		 * @return the double
		 */
		public final double sumOfSquares() {
			return sumOfSquares;
		}
	}
}
