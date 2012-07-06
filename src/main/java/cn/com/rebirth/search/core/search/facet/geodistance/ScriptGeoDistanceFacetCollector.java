/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ScriptGeoDistanceFacetCollector.java 2012-7-6 14:30:41 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.geodistance;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.script.SearchScript;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ScriptGeoDistanceFacetCollector.
 *
 * @author l.xue.nong
 */
public class ScriptGeoDistanceFacetCollector extends GeoDistanceFacetCollector {

	/** The script. */
	private final SearchScript script;

	/** The script aggregator. */
	private Aggregator scriptAggregator;

	/**
	 * Instantiates a new script geo distance facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 * @param unit the unit
	 * @param geoDistance the geo distance
	 * @param entries the entries
	 * @param context the context
	 * @param scriptLang the script lang
	 * @param script the script
	 * @param params the params
	 */
	public ScriptGeoDistanceFacetCollector(String facetName, String fieldName, double lat, double lon,
			DistanceUnit unit, GeoDistance geoDistance, GeoDistanceFacet.Entry[] entries, SearchContext context,
			String scriptLang, String script, Map<String, Object> params) {
		super(facetName, fieldName, lat, lon, unit, geoDistance, entries, context);

		this.script = context.scriptService().search(context.lookup(), scriptLang, script, params);
		this.aggregator = new Aggregator(fixedSourceDistance, entries);
		this.scriptAggregator = (Aggregator) this.aggregator;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.AbstractFacetCollector#setScorer(org.apache.lucene.search.Scorer)
	 */
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		script.setScorer(scorer);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.geodistance.GeoDistanceFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		super.doSetNextReader(reader, docBase);
		script.setNextReader(reader);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.geodistance.GeoDistanceFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		script.setNextDocId(doc);
		this.scriptAggregator.scriptValue = script.runAsDouble();
		super.doCollect(doc);
	}

	/**
	 * The Class Aggregator.
	 *
	 * @author l.xue.nong
	 */
	public static class Aggregator implements GeoPointFieldData.ValueInDocProc {

		/** The fixed source distance. */
		private final GeoDistance.FixedSourceDistance fixedSourceDistance;

		/** The entries. */
		private final GeoDistanceFacet.Entry[] entries;

		/** The script value. */
		double scriptValue;

		/**
		 * Instantiates a new aggregator.
		 *
		 * @param fixedSourceDistance the fixed source distance
		 * @param entries the entries
		 */
		public Aggregator(GeoDistance.FixedSourceDistance fixedSourceDistance, GeoDistanceFacet.Entry[] entries) {
			this.fixedSourceDistance = fixedSourceDistance;
			this.entries = entries;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData.ValueInDocProc#onValue(int, double, double)
		 */
		@Override
		public void onValue(int docId, double lat, double lon) {
			double distance = fixedSourceDistance.calculate(lat, lon);
			for (GeoDistanceFacet.Entry entry : entries) {
				if (entry.foundInDoc) {
					continue;
				}
				if (distance >= entry.getFrom() && distance < entry.getTo()) {
					entry.foundInDoc = true;
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
}
