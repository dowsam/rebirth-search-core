/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoDistanceFacetCollector.java 2012-3-29 15:01:40 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet.geodistance;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import cn.com.rebirth.commons.unit.DistanceUnit;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.MapperService;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;
import cn.com.rebirth.search.core.index.search.geo.GeoDistance;
import cn.com.rebirth.search.core.search.facet.AbstractFacetCollector;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.FacetPhaseExecutionException;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class GeoDistanceFacetCollector.
 *
 * @author l.xue.nong
 */
public class GeoDistanceFacetCollector extends AbstractFacetCollector {

	
	/** The index field name. */
	protected final String indexFieldName;

	
	/** The lat. */
	protected final double lat;

	
	/** The lon. */
	protected final double lon;

	
	/** The unit. */
	protected final DistanceUnit unit;

	
	/** The geo distance. */
	protected final GeoDistance geoDistance;

	
	/** The fixed source distance. */
	protected final GeoDistance.FixedSourceDistance fixedSourceDistance;

	
	/** The field data cache. */
	protected final FieldDataCache fieldDataCache;

	
	/** The field data. */
	protected GeoPointFieldData fieldData;

	
	/** The entries. */
	protected final GeoDistanceFacet.Entry[] entries;

	
	/** The aggregator. */
	protected GeoPointFieldData.ValueInDocProc aggregator;

	
	/**
	 * Instantiates a new geo distance facet collector.
	 *
	 * @param facetName the facet name
	 * @param fieldName the field name
	 * @param lat the lat
	 * @param lon the lon
	 * @param unit the unit
	 * @param geoDistance the geo distance
	 * @param entries the entries
	 * @param context the context
	 */
	public GeoDistanceFacetCollector(String facetName, String fieldName, double lat, double lon, DistanceUnit unit,
			GeoDistance geoDistance, GeoDistanceFacet.Entry[] entries, SearchContext context) {
		super(facetName);
		this.lat = lat;
		this.lon = lon;
		this.unit = unit;
		this.entries = entries;
		this.geoDistance = geoDistance;
		this.fieldDataCache = context.fieldDataCache();

		this.fixedSourceDistance = geoDistance.fixedSourceDistance(lat, lon, unit);

		MapperService.SmartNameFieldMappers smartMappers = context.smartFieldMappers(fieldName);
		if (smartMappers == null || !smartMappers.hasMapper()) {
			throw new FacetPhaseExecutionException(facetName, "No mapping found for field [" + fieldName + "]");
		}
		if (smartMappers.mapper().fieldDataType() != GeoPointFieldDataType.TYPE) {
			throw new FacetPhaseExecutionException(facetName, "field [" + fieldName + "] is not a geo_point field");
		}

		
		if (smartMappers.explicitTypeInNameWithDocMapper()) {
			setFilter(context.filterCache().cache(smartMappers.docMapper().typeFilter()));
		}

		this.indexFieldName = smartMappers.mapper().names().indexName();
		this.aggregator = new Aggregator(fixedSourceDistance, entries);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doSetNextReader(org.apache.lucene.index.IndexReader, int)
	 */
	@Override
	protected void doSetNextReader(IndexReader reader, int docBase) throws IOException {
		fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE, reader, indexFieldName);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.AbstractFacetCollector#doCollect(int)
	 */
	@Override
	protected void doCollect(int doc) throws IOException {
		for (GeoDistanceFacet.Entry entry : entries) {
			entry.foundInDoc = false;
		}
		fieldData.forEachValueInDoc(doc, aggregator);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.search.facet.FacetCollector#facet()
	 */
	@Override
	public Facet facet() {
		return new InternalGeoDistanceFacet(facetName, entries);
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
		 * @see cn.com.summall.search.core.index.mapper.geo.GeoPointFieldData.ValueInDocProc#onValue(int, double, double)
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
					entry.total += distance;
					if (distance < entry.min) {
						entry.min = distance;
					}
					if (distance > entry.max) {
						entry.max = distance;
					}
				}
			}
		}
	}
}
