/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core GeoUtils.java 2012-3-29 15:02:34 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.search.geo;


/**
 * The Class GeoUtils.
 *
 * @author l.xue.nong
 */
public class GeoUtils {

    /**
     * Normalize lon.
     *
     * @param lon the lon
     * @return the double
     */
    public static double normalizeLon(double lon) {
        return centeredModulus(lon, 360);
    }

    /**
     * Normalize lat.
     *
     * @param lat the lat
     * @return the double
     */
    public static double normalizeLat(double lat) {
        return centeredModulus(lat, 180);
    }

    /**
     * Centered modulus.
     *
     * @param dividend the dividend
     * @param divisor the divisor
     * @return the double
     */
    private static double centeredModulus(double dividend, double divisor) {
        double rtn = dividend % divisor;
        if (rtn <= 0)
            rtn += divisor;
        if (rtn > divisor/2)
            rtn -= divisor;
        return rtn;
    }

}
