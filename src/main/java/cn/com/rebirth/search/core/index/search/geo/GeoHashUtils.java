/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoHashUtils.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.geo;

import gnu.trove.map.hash.TIntIntHashMap;

/**
 * The Class GeoHashUtils.
 *
 * @author l.xue.nong
 */
public class GeoHashUtils {

	/** The Constant BASE_32. */
	private static final char[] BASE_32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	/** The Constant DECODE_MAP. */
	private final static TIntIntHashMap DECODE_MAP = new TIntIntHashMap();

	/** The Constant PRECISION. */
	public static final int PRECISION = 12;

	/** The Constant BITS. */
	private static final int[] BITS = { 16, 8, 4, 2, 1 };

	static {
		for (int i = 0; i < BASE_32.length; i++) {
			DECODE_MAP.put(BASE_32[i], i);
		}
	}

	/**
	 * Instantiates a new geo hash utils.
	 */
	private GeoHashUtils() {
	}

	/**
	 * Encode.
	 *
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @return the string
	 */
	public static String encode(double latitude, double longitude) {
		return encode(latitude, longitude, PRECISION);
	}

	/**
	 * Encode.
	 *
	 * @param latitude the latitude
	 * @param longitude the longitude
	 * @param precision the precision
	 * @return the string
	 */
	public static String encode(double latitude, double longitude, int precision) {

		double latInterval0 = -90.0;
		double latInterval1 = 90.0;
		double lngInterval0 = -180.0;
		double lngInterval1 = 180.0;

		final StringBuilder geohash = new StringBuilder();
		boolean isEven = true;

		int bit = 0;
		int ch = 0;

		while (geohash.length() < precision) {
			double mid = 0.0;
			if (isEven) {

				mid = (lngInterval0 + lngInterval1) / 2D;
				if (longitude > mid) {
					ch |= BITS[bit];

					lngInterval0 = mid;
				} else {

					lngInterval1 = mid;
				}
			} else {

				mid = (latInterval0 + latInterval1) / 2D;
				if (latitude > mid) {
					ch |= BITS[bit];

					latInterval0 = mid;
				} else {

					latInterval1 = mid;
				}
			}

			isEven = !isEven;

			if (bit < 4) {
				bit++;
			} else {
				geohash.append(BASE_32[ch]);
				bit = 0;
				ch = 0;
			}
		}

		return geohash.toString();
	}

	/**
	 * Decode.
	 *
	 * @param geohash the geohash
	 * @return the double[]
	 */
	public static double[] decode(String geohash) {
		double[] ret = new double[2];
		decode(geohash, ret);
		return ret;
	}

	/**
	 * Decode.
	 *
	 * @param geohash the geohash
	 * @param ret the ret
	 */
	public static void decode(String geohash, double[] ret) {

		double latInterval0 = -90.0;
		double latInterval1 = 90.0;
		double lngInterval0 = -180.0;
		double lngInterval1 = 180.0;

		boolean isEven = true;

		for (int i = 0; i < geohash.length(); i++) {
			final int cd = DECODE_MAP.get(geohash.charAt(i));

			for (int mask : BITS) {
				if (isEven) {
					if ((cd & mask) != 0) {

						lngInterval0 = (lngInterval0 + lngInterval1) / 2D;
					} else {

						lngInterval1 = (lngInterval0 + lngInterval1) / 2D;
					}
				} else {
					if ((cd & mask) != 0) {

						latInterval0 = (latInterval0 + latInterval1) / 2D;
					} else {

						latInterval1 = (latInterval0 + latInterval1) / 2D;
					}
				}
				isEven = !isEven;
			}

		}

		ret[0] = (latInterval0 + latInterval1) / 2D;

		ret[1] = (lngInterval0 + lngInterval1) / 2D;

	}
}