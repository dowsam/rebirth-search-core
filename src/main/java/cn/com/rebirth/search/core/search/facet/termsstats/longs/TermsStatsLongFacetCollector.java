/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TermsStatsLongFacetCollector.java 2012-3-29 15:02:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.termsstats.longs;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.exception.RestartIllegalArgumentException;
import cn.com.rebirth.commons.trove.ExtTLongObjectHashMap;
import cn.com.rebirth.search.commons.CacheRecycler;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.termsstats.TermsStatsFacet;
import cn.com.rebirth.search.core.search.internal.SearchContext;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


/**
 * The Class TermsStatsLongFacetCollector.
 *
 * @author l.xue.nong
 */
public class TermsStatsLongFacetCollector extends AbstractFacetCollector {

	
	/** The comparator type. */
	private final TermsStatsFacet.ComparatorType comparatorType;

	
	/** The field data cache. */
	private final FieldDataCache fieldDataCache;

	
	/** The key field name. */
	private final String keyFieldName;

	
	/** The value field name. */
	private final String valueFieldName;

	
	/** The size. */
	private final int size;

	
	/** The number of shards. */
	private final int numberOfShards;

	
	/** The key field data type. */
	private final FieldDataType keyFieldDataType;

	
	/** The key field data. */
	private NumericFieldData keyFieldData;

	
	/** The value field data type. */
	private final FieldDataType valueFieldDataType;

	
	/** The script. */
	private final SearchScript script;

	
	/** The aggregator. */
	private final Aggregator aggregator;

	
	/**
	 * Instantiates a new terms stats long facet collector.
	 *
	 * @param facetName the facet name
	 * @param keyFieldName the key field name
	 * @param valueFieldName the value field name
	 * @param size the size
	 * @param comparatorType the comparator type
	 * @param context the context
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 */
	public TermsStatsLongFacetCollector(String facetName, String keyFieldName, String valueFieldName, int size,
			TermsStatsFacet.ComparatorType comparatorType, SearchContext context, String scriptLang, String script,
			Map<String, Object> params) {
		super(facetName);
		this.fieldDataCache = context.fieldDataCache();
		this.size = size;
		this.comparatorType = comparatorType;
		this.numberOfShards = context.numberOfShards();

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(keyFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			this.keyFieldName = keyFieldName;
			this.keyFieldDataType = FieldDataType.DefaultTypes.STRING;
		} else {
			
			if (smartMappers.explicitTypeInNameWithDocMapper()) {
				setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
			}

			this.keyFieldName = smartMappers.mapper().names().indexName();
			this.keyFieldDataType = smartMappers.mapper().fieldDataType();
		}

		if (script == null) {
			smartMappers = context.smartFieldMappers(valueFieldName);
			if (smartMappers == null || !smartMappers.hasMapper()) {
				throw new RestartIllegalArgumentException("failed to find mappings for [" + valueFieldName + "]");
			}
			this.valueFieldName = smartMappers.mapper().names().indexName();
			this.valueFieldDataType = smartMappers.mapper().fieldDataType();
			this.script = null;
			this.aggregator = new Aggregator();
		} else {
			this.valueFieldName = null;
			this.valueFieldDataType = null;
			this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
			this.aggregator = new ScriptAggregator(this.script);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		if (script != null) {
			script.setScorer(scorer);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		keyFieldData = (NumericFieldData) fieldDataCache.cache(keyFieldDataType, reader, keyFieldName);
		if (script != null) {
			script.setNextReader(reader);
		} else {
			aggregator.valueFieldData = (NumericFieldData) fieldDataCache.cache(valueFieldDataType, reader,
					valueFieldName);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		keyFieldData.forEachValueInDoc(doc, aggregator);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		if (aggregator.entries.isEmpty()) {
			return new InternalTermsStatsLongFacet(facetName, comparatorType, size,
					ImmutableList.<InternalTermsStatsLongFacet.LongEntry> of(), aggregator.missing);
		}
		if (size == 0) { 
			
			return new InternalTermsStatsLongFacet(facetName, comparatorType, 0 ,
					aggregator.entries.valueCollection(), aggregator.missing);
		}

		
		Object[] values = aggregator.entries.internalValues();
		Arrays.sort(values, (Comparator) comparatorType.comparator());

		int limit = size;
		List<InternalTermsStatsLongFacet.LongEntry> ordered = Lists.newArrayList();
		for (int i = 0; i < limit; i++) {
			InternalTermsStatsLongFacet.LongEntry value = (InternalTermsStatsLongFacet.LongEntry) values[i];
			if (value == null) {
				break;
			}
			ordered.add(value);
		}
		CacheRecycler.pushLongObjectMap(aggregator.entries);
		return new InternalTermsStatsLongFacet(facetName, comparatorType, size, ordered, aggregator.missing);
	}

	
	/**
	 * The Class Aggregator.
	 *
	 * @author l.xue.nong
	 */
	public static class Aggregator implements NumericFieldData.MissingLongValueInDocProc {

		
		/** The entries. */
		final ExtTLongObjectHashMap<InternalTermsStatsLongFacet.LongEntry> entries = CacheRecycler.popLongObjectMap();

		
		/** The missing. */
		int missing;

		
		/** The value field data. */
		NumericFieldData valueFieldData;

		
		/** The value aggregator. */
		final ValueAggregator valueAggregator = new ValueAggregator();

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			InternalTermsStatsLongFacet.LongEntry longEntry = entries.get(value);
			if (longEntry == null) {
				longEntry = new InternalTermsStatsLongFacet.LongEntry(value, 0, 0, 0, Double.POSITIVE_INFINITY,
						Double.NEGATIVE_INFINITY);
				entries.put(value, longEntry);
			}
			longEntry.count++;
			valueAggregator.longEntry = longEntry;
			valueFieldData.forEachValueInDoc(docId, valueAggregator);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.MissingLongValueInDocProc#onMissing(int)
		 */
		@Override
		public void onMissing(int docId) {
			missing++;
		}

		
		/**
		 * The Class ValueAggregator.
		 *
		 * @author l.xue.nong
		 */
		public static class ValueAggregator implements NumericFieldData.DoubleValueInDocProc {

			
			/** The long entry. */
			InternalTermsStatsLongFacet.LongEntry longEntry;

			
			/* (non-Javadoc)
			 * @see cn.com.summall.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
			 */
			@Override
			public void onValue(int docId, double value) {
				if (value < longEntry.min) {
					longEntry.min = value;
				}
				if (value > longEntry.max) {
					longEntry.max = value;
				}
				longEntry.total += value;
				longEntry.totalCount++;
			}
		}
	}

	
	/**
	 * The Class ScriptAggregator.
	 *
	 * @author l.xue.nong
	 */
	public static class ScriptAggregator extends Aggregator {

		
		/** The script. */
		private final SearchScript script;

		
		/**
		 * Instantiates a new script aggregator.
		 *
		 * @param script the script
		 */
		public ScriptAggregator(SearchScript script) {
			this.script = script;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.search.facet.termsstats.longs.TermsStatsLongFacetCollector.Aggregator#onValue(int, long)
		 */
		@Override
		public void onValue(int docId, long value) {
			InternalTermsStatsLongFacet.LongEntry longEntry = entries.get(value);
			if (longEntry == null) {
				longEntry = new InternalTermsStatsLongFacet.LongEntry(value, 1, 0, 0, Double.POSITIVE_INFINITY,
						Double.NEGATIVE_INFINITY);
				entries.put(value, longEntry);
			} else {
				longEntry.count++;
			}
			script.setNextDocId(docId);
			double valueValue = script.runAsDouble();
			if (valueValue < longEntry.min) {
				longEntry.min = valueValue;
			}
			if (valueValue > longEntry.max) {
				longEntry.max = valueValue;
			}
			longEntry.totalCount++;
			longEntry.total += valueValue;
		}
	}
}