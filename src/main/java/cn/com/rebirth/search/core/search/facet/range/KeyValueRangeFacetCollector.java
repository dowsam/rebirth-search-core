/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core KeyValueRangeFacetCollector.java 2012-3-29 15:02:35 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.range;

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
 * The Class KeyValueRangeFacetCollector.
 *
 * @author l.xue.nong
 */
public class KeyValueRangeFacetCollector extends AbstractFacetCollector {

	
	/** The key index field name. */
	private final String keyIndexFieldName;

	
	/** The value index field name. */
	private final String valueIndexFieldName;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/** The key field data type. */
	private final FieldDataType keyFieldDataType;

	
	/** The key field data. */
	private NumericFieldData keyFieldData;

	
	/** The value field data type. */
	private final FieldDataType valueFieldDataType;

	
	/** The entries. */
	private final RangeFacet.Entry[] entries;

	
	/** The range proc. */
	private final RangeProc rangeProc;

	
	/**
	 * Instantiates a new key value range facet collector.
	 *
	 * @param facetName the facet name
	 * @param keyFieldName the key field name
	 * @param valueFieldName the value field name
	 * @param entries the entries
	 * @param context the context
	 */
	public KeyValueRangeFacetCollector(String facetName, String keyFieldName, String valueFieldName,
			RangeFacet.Entry[] entries, SearchContext context) {
		super(facetName);
		this.entries = entries;
		this.fieldDataCache = context.fieldDataCache();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(keyFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + keyFieldName + "]");
		}

		
		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		keyIndexFieldName = smartMappers.mapper().names().indexName();
		keyFieldDataType = smartMappers.mapper().fieldDataType();

		smartMappers = context.smartFieldMappers(valueFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for value_field [" + valueFieldName
					+ "]");
		}
		valueIndexFieldName = smartMappers.mapper().names().indexName();
		valueFieldDataType = smartMappers.mapper().fieldDataType();

		this.rangeProc = new RangeProc(entries);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		keyFieldData = (NumericFieldData) fieldDataCache.cache(keyFieldDataType, reader, keyIndexFieldName);
		rangeProc.valueFieldData = (NumericFieldData) fieldDataCache.cache(valueFieldDataType, reader,
				valueIndexFieldName);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		for (RangeFacet.Entry entry : entries) {
			entry.foundInDoc = false;
		}
		keyFieldData.forEachValueInDoc(doc, rangeProc);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalRangeFacet(facetName, entries);
	}

	
	/**
	 * The Class RangeProc.
	 *
	 * @author l.xue.nong
	 */
	public static class RangeProc implements NumericFieldData.DoubleValueInDocProc {

		
		/** The entries. */
		private final RangeFacet.Entry[] entries;

		
		/** The value field data. */
		NumericFieldData valueFieldData;

		
		/**
		 * Instantiates a new range proc.
		 *
		 * @param entries the entries
		 */
		public RangeProc(RangeFacet.Entry[] entries) {
			this.entries = entries;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
		 */
		@Override
		public void onValue(int docId, double value) {
			for (RangeFacet.Entry entry : entries) {
				if (entry.foundInDoc) {
					continue;
				}
				if (value >= entry.getFrom() && value < entry.getTo()) {
					entry.foundInDoc = true;
					entry.count++;
					if (valueFieldData.multiValued()) {
						double[] valuesValues = valueFieldData.doubleValues(docId);
						entry.totalCount += valuesValues.length;
						for (double valueValue : valuesValues) {
							entry.total += valueValue;
							if (valueValue < entry.min) {
								entry.min = valueValue;
							}
							if (valueValue > entry.max) {
								entry.max = valueValue;
							}
						}
					} else {
						double valueValue = valueFieldData.doubleValue(docId);
						entry.totalCount++;
						entry.total += valueValue;
						if (valueValue < entry.min) {
							entry.min = valueValue;
						}
						if (valueValue > entry.max) {
							entry.max = valueValue;
						}
					}
				}
			}
		}
	}
}
