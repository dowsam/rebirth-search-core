/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IpFieldMapper.java 2012-7-6 14:29:29 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.ip;

import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.ipField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseNumberField;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.search.commons.Numbers;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.analysis.NumericAnalyzer;
import cn.com.rebirth.search.core.index.analysis.NumericTokenizer;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.core.LongFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.NumericRangeFieldDataFilter;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class IpFieldMapper.
 *
 * @author l.xue.nong
 */
public class IpFieldMapper extends NumberFieldMapper<Long> {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "ip";

	/**
	 * Long to ip.
	 *
	 * @param longIp the long ip
	 * @return the string
	 */
	public static String longToIp(long longIp) {
		int octet3 = (int) ((longIp >> 24) % 256);
		int octet2 = (int) ((longIp >> 16) % 256);
		int octet1 = (int) ((longIp >> 8) % 256);
		int octet0 = (int) ((longIp) % 256);
		return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
	}

	/** The Constant pattern. */
	private static final Pattern pattern = Pattern.compile("\\.");

	/**
	 * Ip to long.
	 *
	 * @param ip the ip
	 * @return the long
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public static long ipToLong(String ip) throws RebirthIllegalArgumentException {
		try {
			String[] octets = pattern.split(ip);
			if (octets.length != 4) {
				throw new RebirthIllegalArgumentException("failed to parse ip [" + ip
						+ "], not full ip address (4 dots)");
			}
			return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16)
					+ (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
		} catch (Exception e) {
			if (e instanceof RebirthIllegalArgumentException) {
				throw (RebirthIllegalArgumentException) e;
			}
			throw new RebirthIllegalArgumentException("failed to parse ip [" + ip + "]", e);
		}
	}

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends NumberFieldMapper.Defaults {

		/** The Constant NULL_VALUE. */
		public static final String NULL_VALUE = null;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, IpFieldMapper> {

		/** The null value. */
		protected String nullValue = Defaults.NULL_VALUE;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			builder = this;
		}

		/**
		 * Null value.
		 *
		 * @param nullValue the null value
		 * @return the builder
		 */
		public Builder nullValue(String nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public IpFieldMapper build(BuilderContext context) {
			IpFieldMapper fieldMapper = new IpFieldMapper(buildNames(context), precisionStep, index, store, boost,
					omitNorms, omitTermFreqAndPositions, nullValue);
			fieldMapper.includeInAll(includeInAll);
			return fieldMapper;
		}
	}

	/**
	 * The Class TypeParser.
	 *
	 * @author l.xue.nong
	 */
	public static class TypeParser implements Mapper.TypeParser {

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.rebirth.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			IpFieldMapper.Builder builder = ipField(name);
			parseNumberField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(propNode.toString());
				}
			}
			return builder;
		}
	}

	/** The null value. */
	private String nullValue;

	/**
	 * Instantiates a new ip field mapper.
	 *
	 * @param names the names
	 * @param precisionStep the precision step
	 * @param index the index
	 * @param store the store
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 */
	protected IpFieldMapper(Names names, int precisionStep, Field.Index index, Field.Store store, float boost,
			boolean omitNorms, boolean omitTermFreqAndPositions, String nullValue) {
		super(names, precisionStep, null, index, store, boost, omitNorms, omitTermFreqAndPositions, new NamedAnalyzer(
				"_ip/" + precisionStep, new NumericIpAnalyzer(precisionStep)), new NamedAnalyzer("_ip/max",
				new NumericIpAnalyzer(Integer.MAX_VALUE)));
		this.nullValue = nullValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#maxPrecisionStep()
	 */
	@Override
	protected int maxPrecisionStep() {
		return 64;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Long value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return null;
		}
		return Numbers.bytesToLong(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Long valueFromString(String value) {
		return ipToLong(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return valueAsString(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		Long value = value(field);
		if (value == null) {
			return null;
		}
		return longToIp(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return NumericUtils.longToPrefixCoded(ipToLong(value));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		long iValue = ipToLong(value);
		long iSim;
		try {
			iSim = ipToLong(minSim);
		} catch (RebirthIllegalArgumentException e) {
			try {
				iSim = Long.parseLong(minSim);
			} catch (NumberFormatException e1) {
				iSim = (long) Double.parseDouble(minSim);
			}
		}
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		return new FuzzyQuery(names().createIndexNameTerm(value), (float) minSim, prefixLength, maxExpansions);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeQuery.newLongRange(names.indexName(), precisionStep, lowerTerm == null ? null
				: ipToLong(lowerTerm), upperTerm == null ? null : ipToLong(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeFilter.newLongRange(names.indexName(), precisionStep, lowerTerm == null ? null
				: ipToLong(lowerTerm), upperTerm == null ? null : ipToLong(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm, boolean includeLower,
			boolean includeUpper, @Nullable QueryParseContext context) {
		return NumericRangeFieldDataFilter.newLongRange(fieldDataCache, names.indexName(), lowerTerm == null ? null
				: ipToLong(lowerTerm), upperTerm == null ? null : ipToLong(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		String ipAsString;
		if (context.externalValueSet()) {
			ipAsString = (String) context.externalValue();
			if (ipAsString == null) {
				ipAsString = nullValue;
			}
		} else {
			if (context.parser().currentToken() == XContentParser.Token.VALUE_NULL) {
				ipAsString = nullValue;
			} else {
				ipAsString = context.parser().text();
			}
		}

		if (ipAsString == null) {
			return null;
		}
		if (context.includeInAll(includeInAll, this)) {
			context.allEntries().addText(names.fullName(), ipAsString, boost);
		}

		final long value = ipToLong(ipAsString);
		return new LongFieldMapper.CustomLongNumericField(this, value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.LONG;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		super.merge(mergeWith, mergeContext);
		if (!this.getClass().equals(mergeWith.getClass())) {
			return;
		}
		if (!mergeContext.mergeFlags().simulate()) {
			this.nullValue = ((IpFieldMapper) mergeWith).nullValue;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#doXContentBody(cn.com.rebirth.search.commons.xcontent.XContentBuilder)
	 */
	@Override
	protected void doXContentBody(XContentBuilder builder) throws IOException {
		super.doXContentBody(builder);
		if (index != Defaults.INDEX) {
			builder.field("index", index.name().toLowerCase());
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		if (termVector != Defaults.TERM_VECTOR) {
			builder.field("term_vector", termVector.name().toLowerCase());
		}
		if (omitNorms != Defaults.OMIT_NORMS) {
			builder.field("omit_norms", omitNorms);
		}
		if (omitTermFreqAndPositions != Defaults.OMIT_TERM_FREQ_AND_POSITIONS) {
			builder.field("omit_term_freq_and_positions", omitTermFreqAndPositions);
		}
		if (precisionStep != Defaults.PRECISION_STEP) {
			builder.field("precision_step", precisionStep);
		}
		if (nullValue != null) {
			builder.field("null_value", nullValue);
		}
		if (includeInAll != null) {
			builder.field("include_in_all", includeInAll);
		}
	}

	/**
	 * The Class NumericIpAnalyzer.
	 *
	 * @author l.xue.nong
	 */
	public static class NumericIpAnalyzer extends NumericAnalyzer<NumericIpTokenizer> {

		/** The precision step. */
		private final int precisionStep;

		/**
		 * Instantiates a new numeric ip analyzer.
		 */
		public NumericIpAnalyzer() {
			this(NumericUtils.PRECISION_STEP_DEFAULT);
		}

		/**
		 * Instantiates a new numeric ip analyzer.
		 *
		 * @param precisionStep the precision step
		 */
		public NumericIpAnalyzer(int precisionStep) {
			this.precisionStep = precisionStep;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.NumericAnalyzer#createNumericTokenizer(java.io.Reader, char[])
		 */
		@Override
		protected NumericIpTokenizer createNumericTokenizer(Reader reader, char[] buffer) throws IOException {
			return new NumericIpTokenizer(reader, precisionStep, buffer);
		}
	}

	/**
	 * The Class NumericIpTokenizer.
	 *
	 * @author l.xue.nong
	 */
	public static class NumericIpTokenizer extends NumericTokenizer {

		/**
		 * Instantiates a new numeric ip tokenizer.
		 *
		 * @param reader the reader
		 * @param precisionStep the precision step
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public NumericIpTokenizer(Reader reader, int precisionStep) throws IOException {
			super(reader, new NumericTokenStream(precisionStep), null);
		}

		/**
		 * Instantiates a new numeric ip tokenizer.
		 *
		 * @param reader the reader
		 * @param precisionStep the precision step
		 * @param buffer the buffer
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public NumericIpTokenizer(Reader reader, int precisionStep, char[] buffer) throws IOException {
			super(reader, new NumericTokenStream(precisionStep), buffer, null);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.analysis.NumericTokenizer#setValue(org.apache.lucene.analysis.NumericTokenStream, java.lang.String)
		 */
		@Override
		protected void setValue(NumericTokenStream tokenStream, String value) {
			tokenStream.setLongValue(ipToLong(value));
		}
	}
}
