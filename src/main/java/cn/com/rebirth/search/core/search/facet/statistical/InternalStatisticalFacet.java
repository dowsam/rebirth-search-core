/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalStatisticalFacet.java 2012-7-6 14:29:33 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet.statistical;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.core.search.facet.Facet;
import cn.com.rebirth.search.core.search.facet.InternalFacet;

/**
 * The Class InternalStatisticalFacet.
 *
 * @author l.xue.nong
 */
public class InternalStatisticalFacet implements StatisticalFacet, InternalFacet {

	/** The Constant STREAM_TYPE. */
	private static final String STREAM_TYPE = "statistical";

	/**
	 * Register streams.
	 */
	public static void registerStreams() {
		Streams.registerStream(STREAM, STREAM_TYPE);
	}

	/** The stream. */
	static Stream STREAM = new Stream() {
		@Override
		public Facet readFacet(String type, StreamInput in) throws IOException {
			return readStatisticalFacet(in);
		}
	};

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.InternalFacet#streamType()
	 */
	@Override
	public String streamType() {
		return STREAM_TYPE;
	}

	/** The name. */
	private String name;

	/** The min. */
	private double min;

	/** The max. */
	private double max;

	/** The total. */
	private double total;

	/** The sum of squares. */
	private double sumOfSquares;

	/** The count. */
	private long count;

	/**
	 * Instantiates a new internal statistical facet.
	 */
	private InternalStatisticalFacet() {
	}

	/**
	 * Instantiates a new internal statistical facet.
	 *
	 * @param name the name
	 * @param min the min
	 * @param max the max
	 * @param total the total
	 * @param sumOfSquares the sum of squares
	 * @param count the count
	 */
	public InternalStatisticalFacet(String name, double min, double max, double total, double sumOfSquares, long count) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.total = total;
		this.sumOfSquares = sumOfSquares;
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#name()
	 */
	@Override
	public String name() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getName()
	 */
	@Override
	public String getName() {
		return name();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#type()
	 */
	@Override
	public String type() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.Facet#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#count()
	 */
	@Override
	public long count() {
		return this.count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getCount()
	 */
	@Override
	public long getCount() {
		return count();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#total()
	 */
	@Override
	public double total() {
		return this.total;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getTotal()
	 */
	@Override
	public double getTotal() {
		return total();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#sumOfSquares()
	 */
	@Override
	public double sumOfSquares() {
		return this.sumOfSquares;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getSumOfSquares()
	 */
	@Override
	public double getSumOfSquares() {
		return sumOfSquares();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#mean()
	 */
	@Override
	public double mean() {
		if (count == 0) {
			return 0;
		}
		return total / count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getMean()
	 */
	@Override
	public double getMean() {
		return mean();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#min()
	 */
	@Override
	public double min() {
		return this.min;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getMin()
	 */
	@Override
	public double getMin() {
		return min();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#max()
	 */
	@Override
	public double max() {
		return this.max;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getMax()
	 */
	@Override
	public double getMax() {
		return max();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#variance()
	 */
	public double variance() {
		return (sumOfSquares - ((total * total) / count)) / count;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getVariance()
	 */
	public double getVariance() {
		return variance();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#stdDeviation()
	 */
	public double stdDeviation() {
		return Math.sqrt(variance());
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.search.facet.statistical.StatisticalFacet#getStdDeviation()
	 */
	public double getStdDeviation() {
		return stdDeviation();
	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant _TYPE. */
		static final XContentBuilderString _TYPE = new XContentBuilderString("_type");

		/** The Constant COUNT. */
		static final XContentBuilderString COUNT = new XContentBuilderString("count");

		/** The Constant TOTAL. */
		static final XContentBuilderString TOTAL = new XContentBuilderString("total");

		/** The Constant MIN. */
		static final XContentBuilderString MIN = new XContentBuilderString("min");

		/** The Constant MAX. */
		static final XContentBuilderString MAX = new XContentBuilderString("max");

		/** The Constant MEAN. */
		static final XContentBuilderString MEAN = new XContentBuilderString("mean");

		/** The Constant SUM_OF_SQUARES. */
		static final XContentBuilderString SUM_OF_SQUARES = new XContentBuilderString("sum_of_squares");

		/** The Constant VARIANCE. */
		static final XContentBuilderString VARIANCE = new XContentBuilderString("variance");

		/** The Constant STD_DEVIATION. */
		static final XContentBuilderString STD_DEVIATION = new XContentBuilderString("std_deviation");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(name);
		builder.field(Fields._TYPE, StatisticalFacet.TYPE);
		builder.field(Fields.COUNT, count());
		builder.field(Fields.TOTAL, total());
		builder.field(Fields.MIN, min());
		builder.field(Fields.MAX, max());
		builder.field(Fields.MEAN, mean());
		builder.field(Fields.SUM_OF_SQUARES, sumOfSquares());
		builder.field(Fields.VARIANCE, variance());
		builder.field(Fields.STD_DEVIATION, stdDeviation());
		builder.endObject();
		return builder;
	}

	/**
	 * Read statistical facet.
	 *
	 * @param in the in
	 * @return the statistical facet
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static StatisticalFacet readStatisticalFacet(StreamInput in) throws IOException {
		InternalStatisticalFacet facet = new InternalStatisticalFacet();
		facet.readFrom(in);
		return facet;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		name = in.readUTF();
		count = in.readVLong();
		total = in.readDouble();
		min = in.readDouble();
		max = in.readDouble();
		sumOfSquares = in.readDouble();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(name);
		out.writeVLong(count);
		out.writeDouble(total);
		out.writeDouble(min);
		out.writeDouble(max);
		out.writeDouble(sumOfSquares);
	}
}
