/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BoostFieldMapper.java 2012-7-6 14:30:30 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.commons.Numbers;
import cn.com.rebirth.search.core.index.analysis.NumericFloatAnalyzer;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.FloatFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.NumericRangeFieldDataFilter;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class BoostFieldMapper.
 *
 * @author l.xue.nong
 */
public class BoostFieldMapper extends NumberFieldMapper<Float> implements InternalMapper, RootMapper {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_boost";

	/** The Constant NAME. */
	public static final String NAME = "_boost";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends NumberFieldMapper.Defaults {

		/** The Constant NAME. */
		public static final String NAME = "_boost";

		/** The Constant NULL_VALUE. */
		public static final Float NULL_VALUE = null;

		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NO;

		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.NO;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, BoostFieldMapper> {

		/** The null value. */
		protected Float nullValue = Defaults.NULL_VALUE;

		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			super(name);
			builder = this;
			index = Defaults.INDEX;
			store = Defaults.STORE;
		}

		/**
		 * Null value.
		 *
		 * @param nullValue the null value
		 * @return the builder
		 */
		public Builder nullValue(float nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public BoostFieldMapper build(BuilderContext context) {
			return new BoostFieldMapper(name, buildIndexName(context), precisionStep, index, store, boost, omitNorms,
					omitTermFreqAndPositions, nullValue);
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
		public Mapper.Builder parse(String fieldName, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			String name = node.get("name") == null ? BoostFieldMapper.Defaults.NAME : node.get("name").toString();
			BoostFieldMapper.Builder builder = MapperBuilders.boost(name);
			TypeParsers.parseNumberField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(XContentMapValues.nodeFloatValue(propNode));
				}
			}
			return builder;
		}
	}

	/** The null value. */
	private final Float nullValue;

	/**
	 * Instantiates a new boost field mapper.
	 */
	public BoostFieldMapper() {
		this(Defaults.NAME, Defaults.NAME);
	}

	/**
	 * Instantiates a new boost field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 */
	protected BoostFieldMapper(String name, String indexName) {
		this(name, indexName, Defaults.PRECISION_STEP, Defaults.INDEX, Defaults.STORE, Defaults.BOOST,
				Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.NULL_VALUE);
	}

	/**
	 * Instantiates a new boost field mapper.
	 *
	 * @param name the name
	 * @param indexName the index name
	 * @param precisionStep the precision step
	 * @param index the index
	 * @param store the store
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 */
	protected BoostFieldMapper(String name, String indexName, int precisionStep, Field.Index index, Field.Store store,
			float boost, boolean omitNorms, boolean omitTermFreqAndPositions, Float nullValue) {
		super(new Names(name, indexName, indexName, name), precisionStep, null, index, store, boost, omitNorms,
				omitTermFreqAndPositions, new NamedAnalyzer("_float/" + precisionStep, new NumericFloatAnalyzer(
						precisionStep)), new NamedAnalyzer("_float/max", new NumericFloatAnalyzer(Integer.MAX_VALUE)));
		this.nullValue = nullValue;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#maxPrecisionStep()
	 */
	@Override
	protected int maxPrecisionStep() {
		return 32;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Float value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return null;
		}
		return Numbers.bytesToFloat(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Float valueFromString(String value) {
		return Float.parseFloat(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return NumericUtils.floatToPrefixCoded(Float.parseFloat(value));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		float iValue = Float.parseFloat(value);
		float iSim = Float.parseFloat(minSim);
		return NumericRangeQuery.newFloatRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		float iValue = Float.parseFloat(value);
		float iSim = (float) (minSim * dFuzzyFactor);
		return NumericRangeQuery.newFloatRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeQuery.newFloatRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Float.parseFloat(lowerTerm),
				upperTerm == null ? null : Float.parseFloat(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeFilter.newFloatRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Float.parseFloat(lowerTerm),
				upperTerm == null ? null : Float.parseFloat(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm, boolean includeLower,
			boolean includeUpper, @Nullable QueryParseContext context) {
		return NumericRangeFieldDataFilter.newFloatRange(fieldDataCache, names.indexName(), lowerTerm == null ? null
				: Float.parseFloat(lowerTerm), upperTerm == null ? null : Float.parseFloat(upperTerm), includeLower,
				includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#preParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#postParse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#validate(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {

		float value = parseFloatValue(context);
		if (!Float.isNaN(value)) {
			context.doc().setBoost(value);
		}
		super.parse(context);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		final float value = parseFloatValue(context);
		if (Float.isNaN(value)) {
			return null;
		}
		context.doc().setBoost(value);
		return new FloatFieldMapper.CustomFloatNumericField(this, value);
	}

	/**
	 * Parses the float value.
	 *
	 * @param context the context
	 * @return the float
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private float parseFloatValue(ParseContext context) throws IOException {
		float value;
		if (context.parser().currentToken() == XContentParser.Token.VALUE_NULL) {
			if (nullValue == null) {
				return Float.NaN;
			}
			value = nullValue;
		} else {
			value = context.parser().floatValue();
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.FLOAT;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {

		if (name().equals(Defaults.NAME) && nullValue == null) {
			return builder;
		}
		builder.startObject(contentType());
		if (!name().equals(Defaults.NAME)) {
			builder.field("name", name());
		}
		if (nullValue != null) {
			builder.field("null_value", nullValue);
		}
		builder.endObject();
		return builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {

	}
}
