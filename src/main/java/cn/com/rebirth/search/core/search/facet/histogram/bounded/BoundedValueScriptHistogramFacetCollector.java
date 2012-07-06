/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core BoundedValueScriptHistogramFacetCollector.java 2012-3-29 15:01:43 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.histogram.bounded;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.FieldMapper;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.facet.histogram.HistogramFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class BoundedValueScriptHistogramFacetCollector.
 *
 * @author l.xue.nong
 */
public class BoundedValueScriptHistogramFacetCollector extends AbstractFacetCollector {

	
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

	
	/** The value script. */
	private final SearchScript valueScript;

	
	/** The histo proc. */
	private final HistogramProc histoProc;

	
	/**
	 * Instantiates a new bounded value script histogram facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param scriptLang the script lang
	 * @param valueScript the value script
	 * @param params the params
	 * @param interval the interval
	 * @param from the from
	 * @param to the to
	 * @param comparatorType the comparator type
	 * @param context the context
	 */
	public BoundedValueScriptHistogramFacetCollector(String facetName, String fieldName, String scriptLang,
			String valueScript, Map<String, Object> params, long interval, long from, long to,
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

		this.valueScript = context.scriptService().search(context.lookup(), scriptLang, valueScript, params);

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

		histoProc = new HistogramProc(from, to, interval, offset, size, this.valueScript);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		fieldData.forEachValueInDoc(doc, histoProc);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		valueScript.setScorer(scorer);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
		valueScript.setNextReader(reader);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalBoundedFullHistogramFacet(facetName, comparatorType, histoProc.interval, -histoProc.offset,
				histoProc.size, histoProc.entries, true);
	}

	
	/**
	 * Bucket.
	 *
	 * @param value the value
	 * @param interval the interval
	 * @return the long
	 */
	public static long bucket(double value, long interval) {
		return (((long) (value / interval)) * interval);
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

		
		/** The entries. */
		final Object[] entries;

		
		/** The value script. */
		private final SearchScript valueScript;

		
		/**
		 * Instantiates a new histogram proc.
		 *
		 * @param from the from
		 * @param to the to
		 * @param interval the interval
		 * @param offset the offset
		 * @param size the size
		 * @param valueScript the value script
		 */
		public HistogramProc(long from, long to, long interval, long offset, int size, SearchScript valueScript) {
			this.from = from;
			this.to = to;
			this.interval = interval;
			this.offset = offset;
			this.size = size;
			this.entries = CacheRecycler.popObjectArray(size);
			this.valueScript = valueScript;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.LongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			if (value <= from || value > to) { 
				return;
			}
			int index = ((int) ((value + offset) / interval));

			valueScript.setNextDocId(docId);
			double scriptValue = valueScript.runAsDouble();

			InternalBoundedFullHistogramFacet.FullEntry entry = (InternalBoundedFullHistogramFacet.FullEntry) entries[index];
			if (entry == null) {
				entries[index] = new InternalBoundedFullHistogramFacet.FullEntry(index, 1, scriptValue, scriptValue, 1,
						scriptValue);
			} else {
				entry.count++;
				entry.totalCount++;
				entry.total += scriptValue;
				if (scriptValue < entry.min) {
					entry.min = scriptValue;
				}
				if (scriptValue > entry.max) {
					entry.max = scriptValue;
				}
			}
		}
	}
}