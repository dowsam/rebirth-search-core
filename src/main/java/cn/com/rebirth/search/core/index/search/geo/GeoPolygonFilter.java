/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoPolygonFilter.java 2012-3-29 15:02:33 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.geo;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import cn.com.rebirth.search.commons.lucene.docset.GetDocSet;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldData;
import cn.com.rebirth.search.core.index.mapper.geo.GeoPointFieldDataType;


/**
 * The Class GeoPolygonFilter.
 *
 * @author l.xue.nong
 */
public class GeoPolygonFilter extends Filter {

    
    /** The points. */
    private final Point[] points;

    
    /** The field name. */
    private final String fieldName;

    
    /** The field data cache. */
    private final FieldDataCache fieldDataCache;

    
    /**
     * Instantiates a new geo polygon filter.
     *
     * @param points the points
     * @param fieldName the field name
     * @param fieldDataCache the field data cache
     */
    public GeoPolygonFilter(Point[] points, String fieldName, FieldDataCache fieldDataCache) {
        this.points = points;
        this.fieldName = fieldName;
        this.fieldDataCache = fieldDataCache;
    }

    
    /**
     * Points.
     *
     * @return the point[]
     */
    public Point[] points() {
        return points;
    }

    
    /**
     * Field name.
     *
     * @return the string
     */
    public String fieldName() {
        return this.fieldName;
    }

    
    /* (non-Javadoc)
     * @see org.apache.lucene.search.Filter#getDocIdSet(org.apache.lucene.index.IndexReader)
     */
    @Override
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
        final GeoPointFieldData fieldData = (GeoPointFieldData) fieldDataCache.cache(GeoPointFieldDataType.TYPE, reader, fieldName);
        return new GeoPolygonDocSet(reader.maxDoc(), fieldData, points);
    }

    
    /**
     * The Class GeoPolygonDocSet.
     *
     * @author l.xue.nong
     */
    public static class GeoPolygonDocSet extends GetDocSet {
        
        
        /** The field data. */
        private final GeoPointFieldData fieldData;
        
        
        /** The points. */
        private final Point[] points;

        
        /**
         * Instantiates a new geo polygon doc set.
         *
         * @param maxDoc the max doc
         * @param fieldData the field data
         * @param points the points
         */
        public GeoPolygonDocSet(int maxDoc, GeoPointFieldData fieldData, Point[] points) {
            super(maxDoc);
            this.fieldData = fieldData;
            this.points = points;
        }

        
        /* (non-Javadoc)
         * @see org.apache.lucene.search.DocIdSet#isCacheable()
         */
        @Override
        public boolean isCacheable() {
            
            
            
            return false;
        }

        
        /* (non-Javadoc)
         * @see cn.com.summall.search.commons.lucene.docset.DocSet#get(int)
         */
        @Override
        public boolean get(int doc) {
            if (!fieldData.hasValue(doc)) {
                return false;
            }

            if (fieldData.multiValued()) {
                double[] lats = fieldData.latValues(doc);
                double[] lons = fieldData.lonValues(doc);
                for (int i = 0; i < lats.length; i++) {
                    if (pointInPolygon(points, lats[i], lons[i])) {
                        return true;
                    }
                }
            } else {
                double lat = fieldData.latValue(doc);
                double lon = fieldData.lonValue(doc);
                return pointInPolygon(points, lat, lon);
            }
            return false;
        }

        
        /**
         * Point in polygon.
         *
         * @param points the points
         * @param lat the lat
         * @param lon the lon
         * @return true, if successful
         */
        private static boolean pointInPolygon(Point[] points, double lat, double lon) {
            int i;
            int j = points.length - 1;
            boolean inPoly = false;

            for (i = 0; i < points.length; i++) {
                if (points[i].lon < lon && points[j].lon >= lon
                        || points[j].lon < lon && points[i].lon >= lon) {
                    if (points[i].lat + (lon - points[i].lon) /
                            (points[j].lon - points[i].lon) * (points[j].lat - points[i].lat) < lat) {
                        inPoly = !inPoly;
                    }
                }
                j = i;
            }
            return inPoly;
        }
    }
}
