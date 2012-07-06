/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoundedCountHistogramFacetCollector.java 2012-7-6 14:28:44 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.histogram.bounded;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class BoundedCountHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class BoundedCountHistogramFacetCollector extends AbstractFacetCollector {

	/** The index field name. */
	private final String indexFieldName;

	/** The comparator type. */
	private final HistogramFacet.ComparatorType comparatorType;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private NumericFieldData fieldData;

	/** The histo proc. */
	private final HistogramProc histoProc;

	/**
	 * Instantiates a new bounded count histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param interval the interval
	 * @param from the from
	 * @param to the to
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public BoundedCountHistogramFacetCollector(String facetName, String fieldName, long interval, long from, long to,
			HistogramFacet.ComparatorType comparatorType, SearchContext context) {
		super(facetName);
		this.comparatorType = comparatorType;
		this.fieldDataCache = context.fieldDataCache();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}

		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		FieldMapper mapper = smartMappers.mapper();

		indexFieldName = mapper.names().indexName();
		fieldDataType = mapper.fieldDataType();

		long normalizedFrom = (((long) ((double) from / interval)) * interval);
		long normalizedTo = (((long) ((double) to / interval)) * interval);
		if ((to % interval) != 0) {
			normalizedTo += interval;
		}
		long offset = -normalizedFrom;
		int size = (int) ((normalizedTo - normalizedFrom) / interval);

		histoProc = new HistogramProc(from, to, interval, offset, size);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, histoProc);
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
		return new InternalBoundedCountHistogramFacet(facetName, comparatorType, histoProc.interval, -histoProc.offset,
				histoProc.size, histoProc.counts, true);
	}

	/**
	 * The Class HistogramProc.
	 *
	 * @author l.xue.nong
	 */
	public static class HistogramProc implements NumericFieldData.LongValueInDocProc {

		/** The from. */
		final long from;

		/** The to. */
		final long to;

		/** The interval. */
		final long interval;

		/** The offset. */
		final long offset;

		/** The size. */
		final int size;

		/** The counts. */
		final int[] counts;

		/**
		 * Instantiates a new histogram proc.
		 *
		 * @param from the from
		 * @param to the to
		 * @param interval the interval
		 * @param offset the offset
		 * @param size the size
		 */
		public HistogramProc(long from, long to, long interval, long offset, int size) {
			this.from = from;
			this.to = to;
			this.interval = interval;
			this.offset = offset;
			this.size = size;
			this.counts = CacheRecycler.popIntArray(size);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.LongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			if (value <= from || value > to) {
				return;
			}
			counts[((int) ((value + offset) / interval))]++;
		}
	}
}