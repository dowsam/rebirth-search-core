/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ValueGeoDistanceFacetCollector.java 2012-7-6 14:30:28 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.geodistance;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.field.data.NumericFieldData;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;

/**
 * The Class ValueGeoDistanceFacetCollector.
 *
 * @author l.xue.nong
 */
public class ValueGeoDistanceFacetCollector extends GeoDistanceFacetCollector {

	/** The index value field name. */
	private final String indexValueFieldName;

	/** The value field data type. */
	private final FieldDataType valueFieldDataType;

	/**
	 * Instantiates a new value geo distance facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 * @param unit the unit
	 * @param geoDistance the geo distance
	 * @param entries the entries
	 * @param context the context
	 * @param valueFieldName the value field name
	 */
	public ValueGeoDistanceFacetCollector(String facetName, String fieldName, double lat, double lon,
			DistanceUnit unit, GeoDistance geoDistance, GeoDistanceFacet.Entry[] entries, SearchContext context,
			String valueFieldName) {
		super(facetName, fieldName, lat, lon, unit, geoDistance, entries, context);

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(valueFieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + valueFieldName + "]");
		}
		this.indexValueFieldName = smartMappers.mapper().names().indexName();
		this.valueFieldDataType = smartMappers.mapper().fieldDataType();
		this.aggregator = new Aggregator(fixedSourceDistance, entries);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.geodistance.GeoDistanceFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		super.doSetNextReader(reader, docBase);
		((Aggregator) this.aggregator).valueFieldData = (NumericFieldData) fieldDataCache.cache(valueFieldDataType,
				reader, indexValueFieldName);
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

		/** The value field data. */
		NumericFieldData valueFieldData;

		/** The value aggregator. */
		final ValueAggregator valueAggregator = new ValueAggregator();

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
					valueAggregator.entry = entry;
					valueFieldData.forEachValueInDoc(docId, valueAggregator);
				}
			}
		}
	}

	/**
	 * The Class ValueAggregator.
	 *
	 * @author l.xue.nong
	 */
	public static class ValueAggregator implements NumericFieldData.DoubleValueInDocProc {

		/** The entry. */
		GeoDistanceFacet.Entry entry;

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.field.data.NumericFieldData.DoubleValueInDocProc#onValue(int, double)
		 */
		@Override
		public void onValue(int docId, double value) {
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
