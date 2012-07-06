/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core GeoDistance.java 2012-7-6 14:30:07 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.search.geo;

import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.unit.DistanceUnit;

/**
 * The Enum GeoDistance.
 *
 * @author l.xue.nong
 */
public enum GeoDistance {

	/** The plane. */
	PLANE() {
		@Override
		public double calculate(double sourceLatitude, double sourceLongitude, double targetLatitude,
				double targetLongitude, DistanceUnit unit) {
			double px = targetLongitude - sourceLongitude;
			double py = targetLatitude - sourceLatitude;
			return Math.sqrt(px * px + py * py) * unit.getDistancePerDegree();
		}

		@Override
		public double normalize(double distance, DistanceUnit unit) {
			return distance;
		}

		@Override
		public FixedSourceDistance fixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			return new PlaneFixedSourceDistance(sourceLatitude, sourceLongitude, unit);
		}
	},

	/** The factor. */
	FACTOR() {
		@Override
		public double calculate(double sourceLatitude, double sourceLongitude, double targetLatitude,
				double targetLongitude, DistanceUnit unit) {
			double longitudeDifference = targetLongitude - sourceLongitude;
			double a = Math.toRadians(90D - sourceLatitude);
			double c = Math.toRadians(90D - targetLatitude);
			return (Math.cos(a) * Math.cos(c))
					+ (Math.sin(a) * Math.sin(c) * Math.cos(Math.toRadians(longitudeDifference)));
		}

		@Override
		public double normalize(double distance, DistanceUnit unit) {
			return Math.cos(distance / unit.getEarthRadius());
		}

		@Override
		public FixedSourceDistance fixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			return new FactorFixedSourceDistance(sourceLatitude, sourceLongitude, unit);
		}
	},

	/** The arc. */
	ARC() {
		@Override
		public double calculate(double sourceLatitude, double sourceLongitude, double targetLatitude,
				double targetLongitude, DistanceUnit unit) {
			double longitudeDifference = targetLongitude - sourceLongitude;
			double a = Math.toRadians(90D - sourceLatitude);
			double c = Math.toRadians(90D - targetLatitude);
			double factor = (Math.cos(a) * Math.cos(c))
					+ (Math.sin(a) * Math.sin(c) * Math.cos(Math.toRadians(longitudeDifference)));

			if (factor < -1D) {
				return Math.PI * unit.getEarthRadius();
			} else if (factor >= 1D) {
				return 0;
			} else {
				return Math.acos(factor) * unit.getEarthRadius();
			}
		}

		@Override
		public double normalize(double distance, DistanceUnit unit) {
			return distance;
		}

		@Override
		public FixedSourceDistance fixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			return new ArcFixedSourceDistance(sourceLatitude, sourceLongitude, unit);
		}
	};

	/**
	 * Normalize.
	 *
	 * @param distance the distance
	 * @param unit the unit
	 * @return the double
	 */
	public abstract double normalize(double distance, DistanceUnit unit);

	/**
	 * Calculate.
	 *
	 * @param sourceLatitude the source latitude
	 * @param sourceLongitude the source longitude
	 * @param targetLatitude the target latitude
	 * @param targetLongitude the target longitude
	 * @param unit the unit
	 * @return the double
	 */
	public abstract double calculate(double sourceLatitude, double sourceLongitude, double targetLatitude,
			double targetLongitude, DistanceUnit unit);

	/**
	 * Fixed source distance.
	 *
	 * @param sourceLatitude the source latitude
	 * @param sourceLongitude the source longitude
	 * @param unit the unit
	 * @return the fixed source distance
	 */
	public abstract FixedSourceDistance fixedSourceDistance(double sourceLatitude, double sourceLongitude,
			DistanceUnit unit);

	/** The Constant MIN_LAT. */
	private static final double MIN_LAT = Math.toRadians(-90d);

	/** The Constant MAX_LAT. */
	private static final double MAX_LAT = Math.toRadians(90d);

	/** The Constant MIN_LON. */
	private static final double MIN_LON = Math.toRadians(-180d);

	/** The Constant MAX_LON. */
	private static final double MAX_LON = Math.toRadians(180d);

	/**
	 * Distance bounding check.
	 *
	 * @param sourceLatitude the source latitude
	 * @param sourceLongitude the source longitude
	 * @param distance the distance
	 * @param unit the unit
	 * @return the distance bounding check
	 */
	public static DistanceBoundingCheck distanceBoundingCheck(double sourceLatitude, double sourceLongitude,
			double distance, DistanceUnit unit) {

		double radDist = distance / unit.getEarthRadius();

		double radLat = Math.toRadians(sourceLatitude);
		double radLon = Math.toRadians(sourceLongitude);

		double minLat = radLat - radDist;
		double maxLat = radLat + radDist;

		double minLon, maxLon;
		if (minLat > MIN_LAT && maxLat < MAX_LAT) {
			double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(radLat));
			minLon = radLon - deltaLon;
			if (minLon < MIN_LON)
				minLon += 2d * Math.PI;
			maxLon = radLon + deltaLon;
			if (maxLon > MAX_LON)
				maxLon -= 2d * Math.PI;
		} else {

			minLat = Math.max(minLat, MIN_LAT);
			maxLat = Math.min(maxLat, MAX_LAT);
			minLon = MIN_LON;
			maxLon = MAX_LON;
		}

		Point topLeft = new Point(Math.toDegrees(maxLat), Math.toDegrees(minLon));
		Point bottomRight = new Point(Math.toDegrees(minLat), Math.toDegrees(maxLon));
		if (minLon > maxLon) {
			return new Meridian180DistanceBoundingCheck(topLeft, bottomRight);
		}
		return new SimpleDistanceBoundingCheck(topLeft, bottomRight);
	}

	/**
	 * From string.
	 *
	 * @param s the s
	 * @return the geo distance
	 */
	public static GeoDistance fromString(String s) {
		if ("plane".equals(s)) {
			return PLANE;
		} else if ("arc".equals(s)) {
			return ARC;
		} else if ("factor".equals(s)) {
			return FACTOR;
		}
		throw new RebirthIllegalArgumentException("No geo distance for [" + s + "]");
	}

	/**
	 * The Interface FixedSourceDistance.
	 *
	 * @author l.xue.nong
	 */
	public static interface FixedSourceDistance {

		/**
		 * Calculate.
		 *
		 * @param targetLatitude the target latitude
		 * @param targetLongitude the target longitude
		 * @return the double
		 */
		double calculate(double targetLatitude, double targetLongitude);
	}

	/**
	 * The Interface DistanceBoundingCheck.
	 *
	 * @author l.xue.nong
	 */
	public static interface DistanceBoundingCheck {

		/**
		 * Checks if is within.
		 *
		 * @param targetLatitude the target latitude
		 * @param targetLongitude the target longitude
		 * @return true, if is within
		 */
		boolean isWithin(double targetLatitude, double targetLongitude);

		/**
		 * Top left.
		 *
		 * @return the point
		 */
		Point topLeft();

		/**
		 * Bottom right.
		 *
		 * @return the point
		 */
		Point bottomRight();
	}

	/** The always instance. */
	public static AlwaysDistanceBoundingCheck ALWAYS_INSTANCE = new AlwaysDistanceBoundingCheck();

	/**
	 * The Class AlwaysDistanceBoundingCheck.
	 *
	 * @author l.xue.nong
	 */
	private static class AlwaysDistanceBoundingCheck implements DistanceBoundingCheck {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#isWithin(double, double)
		 */
		@Override
		public boolean isWithin(double targetLatitude, double targetLongitude) {
			return true;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#topLeft()
		 */
		@Override
		public Point topLeft() {
			return null;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#bottomRight()
		 */
		@Override
		public Point bottomRight() {
			return null;
		}
	}

	/**
	 * The Class Meridian180DistanceBoundingCheck.
	 *
	 * @author l.xue.nong
	 */
	public static class Meridian180DistanceBoundingCheck implements DistanceBoundingCheck {

		/** The top left. */
		private final Point topLeft;

		/** The bottom right. */
		private final Point bottomRight;

		/**
		 * Instantiates a new meridian180 distance bounding check.
		 *
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 */
		public Meridian180DistanceBoundingCheck(Point topLeft, Point bottomRight) {
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#isWithin(double, double)
		 */
		@Override
		public boolean isWithin(double targetLatitude, double targetLongitude) {
			return (targetLatitude >= bottomRight.lat && targetLatitude <= topLeft.lat)
					&& (targetLongitude >= topLeft.lon || targetLongitude <= bottomRight.lon);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#topLeft()
		 */
		@Override
		public Point topLeft() {
			return topLeft;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#bottomRight()
		 */
		@Override
		public Point bottomRight() {
			return bottomRight;
		}
	}

	/**
	 * The Class SimpleDistanceBoundingCheck.
	 *
	 * @author l.xue.nong
	 */
	public static class SimpleDistanceBoundingCheck implements DistanceBoundingCheck {

		/** The top left. */
		private final Point topLeft;

		/** The bottom right. */
		private final Point bottomRight;

		/**
		 * Instantiates a new simple distance bounding check.
		 *
		 * @param topLeft the top left
		 * @param bottomRight the bottom right
		 */
		public SimpleDistanceBoundingCheck(Point topLeft, Point bottomRight) {
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#isWithin(double, double)
		 */
		@Override
		public boolean isWithin(double targetLatitude, double targetLongitude) {
			return (targetLatitude >= bottomRight.lat && targetLatitude <= topLeft.lat)
					&& (targetLongitude >= topLeft.lon && targetLongitude <= bottomRight.lon);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#topLeft()
		 */
		@Override
		public Point topLeft() {
			return topLeft;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.DistanceBoundingCheck#bottomRight()
		 */
		@Override
		public Point bottomRight() {
			return bottomRight;
		}
	}

	/**
	 * The Class PlaneFixedSourceDistance.
	 *
	 * @author l.xue.nong
	 */
	public static class PlaneFixedSourceDistance implements FixedSourceDistance {

		/** The source latitude. */
		private final double sourceLatitude;

		/** The source longitude. */
		private final double sourceLongitude;

		/** The distance per degree. */
		private final double distancePerDegree;

		/**
		 * Instantiates a new plane fixed source distance.
		 *
		 * @param sourceLatitude the source latitude
		 * @param sourceLongitude the source longitude
		 * @param unit the unit
		 */
		public PlaneFixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			this.sourceLatitude = sourceLatitude;
			this.sourceLongitude = sourceLongitude;
			this.distancePerDegree = unit.getDistancePerDegree();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.FixedSourceDistance#calculate(double, double)
		 */
		@Override
		public double calculate(double targetLatitude, double targetLongitude) {
			double px = targetLongitude - sourceLongitude;
			double py = targetLatitude - sourceLatitude;
			return Math.sqrt(px * px + py * py) * distancePerDegree;
		}
	}

	/**
	 * The Class FactorFixedSourceDistance.
	 *
	 * @author l.xue.nong
	 */
	public static class FactorFixedSourceDistance implements FixedSourceDistance {

		/** The source latitude. */
		private final double sourceLatitude;

		/** The source longitude. */
		private final double sourceLongitude;

		/** The earth radius. */
		private final double earthRadius;

		/** The a. */
		private final double a;

		/** The sin a. */
		private final double sinA;

		/** The cos a. */
		private final double cosA;

		/**
		 * Instantiates a new factor fixed source distance.
		 *
		 * @param sourceLatitude the source latitude
		 * @param sourceLongitude the source longitude
		 * @param unit the unit
		 */
		public FactorFixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			this.sourceLatitude = sourceLatitude;
			this.sourceLongitude = sourceLongitude;
			this.earthRadius = unit.getEarthRadius();
			this.a = Math.toRadians(90D - sourceLatitude);
			this.sinA = Math.sin(a);
			this.cosA = Math.cos(a);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.FixedSourceDistance#calculate(double, double)
		 */
		@Override
		public double calculate(double targetLatitude, double targetLongitude) {
			double longitudeDifference = targetLongitude - sourceLongitude;
			double c = Math.toRadians(90D - targetLatitude);
			return (cosA * Math.cos(c)) + (sinA * Math.sin(c) * Math.cos(Math.toRadians(longitudeDifference)));
		}
	}

	/**
	 * The Class ArcFixedSourceDistance.
	 *
	 * @author l.xue.nong
	 */
	public static class ArcFixedSourceDistance implements FixedSourceDistance {

		/** The source latitude. */
		private final double sourceLatitude;

		/** The source longitude. */
		private final double sourceLongitude;

		/** The earth radius. */
		private final double earthRadius;

		/** The a. */
		private final double a;

		/** The sin a. */
		private final double sinA;

		/** The cos a. */
		private final double cosA;

		/**
		 * Instantiates a new arc fixed source distance.
		 *
		 * @param sourceLatitude the source latitude
		 * @param sourceLongitude the source longitude
		 * @param unit the unit
		 */
		public ArcFixedSourceDistance(double sourceLatitude, double sourceLongitude, DistanceUnit unit) {
			this.sourceLatitude = sourceLatitude;
			this.sourceLongitude = sourceLongitude;
			this.earthRadius = unit.getEarthRadius();
			this.a = Math.toRadians(90D - sourceLatitude);
			this.sinA = Math.sin(a);
			this.cosA = Math.cos(a);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.search.geo.GeoDistance.FixedSourceDistance#calculate(double, double)
		 */
		@Override
		public double calculate(double targetLatitude, double targetLongitude) {
			double longitudeDifference = targetLongitude - sourceLongitude;
			double c = Math.toRadians(90D - targetLatitude);
			double factor = (cosA * Math.cos(c)) + (sinA * Math.sin(c) * Math.cos(Math.toRadians(longitudeDifference)));

			if (factor < -1D) {
				return Math.PI * earthRadius;
			} else if (factor >= 1D) {
				return 0;
			} else {
				return Math.acos(factor) * earthRadius;
			}
		}
	}
}
