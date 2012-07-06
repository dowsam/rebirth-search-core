/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core StatisticalFacetCollector.java 2012-7-6 14:29:00 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class StatisticalFacetCollector.
 *
 * @author l.xue.nong
 */
public class StatisticalFacetCollector extends AbstractFacetCollector {

	/** The index field name. */
	private final String indexFieldName;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private NumericFieldData fieldData;

	/** The stats proc. */
	private final StatsProc statsProc = new StatsProc();

	/**
	 * Instantiates a new statistical facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param context the context
	 */
	public StatisticalFacetCollector(String facetName, String fieldName, SearchContext context) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}

		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		indexFieldName = smartMappers.mapper().names().indexName();
		fieldDataType = smartMappers.mapper().fieldDataType();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, statsProc);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
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
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc#onValue(int, double)
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
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.MissingDoubleValueInDocProc#onMissing(int)
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
