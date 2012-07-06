/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Point.java 2012-3-29 15:02:06 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.geo;


/**
 * The Class Point.
 *
 * @author l.xue.nong
 */
public class Point {
    
    
    /** The lat. */
    public double lat;
    
    
    /** The lon. */
    public double lon;

    
    /**
     * Instantiates a new point.
     */
    public Point() {
    }

    
    /**
     * Instantiates a new point.
     *
     * @param lat the lat
     * @param lon the lon
     */
    public Point(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (Double.compare(point.lat, lat) != 0) return false;
        if (Double.compare(point.lon, lon) != 0) return false;

        return true;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = lat != +0.0d ? Double.doubleToLongBits(lat) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = lon != +0.0d ? Double.doubleToLongBits(lon) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[" + lat + ", " + lon + "]";
    }
}
