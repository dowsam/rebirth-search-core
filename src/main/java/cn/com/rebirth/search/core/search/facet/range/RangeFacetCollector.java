/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RangeFacetCollector.java 2012-7-6 14:29:25 l.xue.nong$$
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
 * The Class RangeFacetCollector.
 *
 * @author l.xue.nong
 */
public class RangeFacetCollector extends AbstractFacetCollector {

	/** The index field name. */
	private final String indexFieldName;

	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	/** The field data type. */
	private final FieldDataType fieldDataType;

	/** The field data. */
	private NumericFieldData fieldData;

	/** The entries. */
	private final RangeFacet.Entry[] entries;

	/** The range proc. */
	private final RangeProc rangeProc;

	/**
	 * Instantiates a new range facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param entries the entries
	 * @param context the context
	 */
	public RangeFacetCollector(String facetName, String fieldName, RangeFacet.Entry[] entries, SearchContext context) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();
		this.entries = entries;

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}

		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		indexFieldName = smartMappers.mapper().names().indexName();
		fieldDataType = smartMappers.mapper().fieldDataType();

		rangeProc = new RangeProc(entries);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (NumericFieldData) fieldDataCache.cache(fieldDataType, reader, indexFieldName);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		for (RangeFacet.Entry entry : entries) {
			entry.foundInDoc = false;
		}
		fieldData.forEachValueInDoc(doc, rangeProc);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.FacetCollector#facet()
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

		/**
		 * Instantiates a new range proc.
		 *
		 * @param entries the entries
		 */
		public RangeProc(RangeFacet.Entry[] entries) {
			this.entries = entries;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
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
					entry.totalCount++;
					entry.total += value;
					if (value < entry.min) {
						entry.min = value;
					}
					if (value > entry.max) {
						entry.max = value;
					}
				}
			}
		}
	}
}
