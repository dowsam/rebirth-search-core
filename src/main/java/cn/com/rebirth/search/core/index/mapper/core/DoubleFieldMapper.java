/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core DoubleFieldMapper.java 2012-7-6 14:29:15 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.doubleField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseNumberField;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.NumericRangeFilter;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.NumericUtils;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.commons.Numbers;
import cn.com.rebirth.search.core.index.analysis.NumericDoubleAnalyzer;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.NumericRangeFieldDataFilter;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;

/**
 * The Class DoubleFieldMapper.
 *
 * @author l.xue.nong
 */
public class DoubleFieldMapper extends NumberFieldMapper<Double> {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "double";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends NumberFieldMapper.Defaults {

		/** The Constant NULL_VALUE. */
		public static final Double NULL_VALUE = null;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, DoubleFieldMapper> {

		/** The null value. */
		protected Double nullValue = Defaults.NULL_VALUE;

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
		public Builder nullValue(double nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public DoubleFieldMapper build(BuilderContext context) {
			DoubleFieldMapper fieldMapper = new DoubleFieldMapper(buildNames(context), precisionStep, fuzzyFactor,
					index, store, boost, omitNorms, omitTermFreqAndPositions, nullValue);
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
			DoubleFieldMapper.Builder builder = doubleField(name);
			parseNumberField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = entry.getKey();
				Object propNode = entry.getValue();
				if (propName.equals("nullValue") || propName.equals("null_value")) {
					builder.nullValue(XContentMapValues.nodeDoubleValue(propNode));
				}
			}
			return builder;
		}
	}

	/** The null value. */
	private Double nullValue;

	/** The null value as string. */
	private String nullValueAsString;

	/**
	 * Instantiates a new double field mapper.
	 *
	 * @param names the names
	 * @param precisionStep the precision step
	 * @param fuzzyFactor the fuzzy factor
	 * @param index the index
	 * @param store the store
	 * @param boost the boost
	 * @param omitNorms the omit norms
	 * @param omitTermFreqAndPositions the omit term freq and positions
	 * @param nullValue the null value
	 */
	protected DoubleFieldMapper(Names names, int precisionStep, String fuzzyFactor, Field.Index index,
			Field.Store store, float boost, boolean omitNorms, boolean omitTermFreqAndPositions, Double nullValue) {
		super(names, precisionStep, fuzzyFactor, index, store, boost, omitNorms, omitTermFreqAndPositions,
				new NamedAnalyzer("_double/" + precisionStep, new NumericDoubleAnalyzer(precisionStep)),
				new NamedAnalyzer("_double/max", new NumericDoubleAnalyzer(Integer.MAX_VALUE)));
		this.nullValue = nullValue;
		this.nullValueAsString = nullValue == null ? null : nullValue.toString();
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
	public Double value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return null;
		}
		return Numbers.bytesToDouble(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Double valueFromString(String value) {
		return Double.valueOf(value);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return NumericUtils.doubleToPrefixCoded(Double.parseDouble(value));
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		double iValue = Double.parseDouble(value);
		double iSim = Double.parseDouble(minSim);
		return NumericRangeQuery.newDoubleRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		double iValue = Double.parseDouble(value);
		double iSim = minSim * dFuzzyFactor;
		return NumericRangeQuery.newDoubleRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true,
				true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldQuery(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		double dValue = Double.parseDouble(value);
		return NumericRangeQuery.newDoubleRange(names.indexName(), precisionStep, dValue, dValue, true, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeQuery.newDoubleRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Double.parseDouble(lowerTerm),
				upperTerm == null ? null : Double.parseDouble(upperTerm), includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldFilter(java.lang.String, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		double dValue = Double.parseDouble(value);
		return NumericRangeFilter.newDoubleRange(names.indexName(), precisionStep, dValue, dValue, true, true);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeFilter.newDoubleRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Double.parseDouble(lowerTerm),
				upperTerm == null ? null : Double.parseDouble(upperTerm), includeLower, includeUpper);
	}

	/**
	 * Range filter.
	 *
	 * @param lowerTerm the lower term
	 * @param upperTerm the upper term
	 * @param includeLower the include lower
	 * @param includeUpper the include upper
	 * @return the filter
	 */
	public Filter rangeFilter(Double lowerTerm, Double upperTerm, boolean includeLower, boolean includeUpper) {
		return NumericRangeFilter.newDoubleRange(names.indexName(), precisionStep, lowerTerm, upperTerm, includeLower,
				includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache, java.lang.String, java.lang.String, boolean, boolean, cn.com.rebirth.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm, boolean includeLower,
			boolean includeUpper, @Nullable QueryParseContext context) {
		return NumericRangeFieldDataFilter.newDoubleRange(fieldDataCache, names.indexName(), lowerTerm == null ? null
				: Double.parseDouble(lowerTerm), upperTerm == null ? null : Double.parseDouble(upperTerm),
				includeLower, includeUpper);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#customBoost()
	 */
	@Override
	protected boolean customBoost() {
		return true;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		double value;
		float boost = this.boost;
		if (context.externalValueSet()) {
			Object externalValue = context.externalValue();
			if (externalValue == null) {
				if (nullValue == null) {
					return null;
				}
				value = nullValue;
			} else if (externalValue instanceof String) {
				String sExternalValue = (String) externalValue;
				if (sExternalValue.length() == 0) {
					if (nullValue == null) {
						return null;
					}
					value = nullValue;
				} else {
					value = Double.parseDouble(sExternalValue);
				}
			} else {
				value = ((Number) externalValue).doubleValue();
			}
			if (context.includeInAll(includeInAll, this)) {
				context.allEntries().addText(names.fullName(), Double.toString(value), boost);
			}
		} else {
			XContentParser parser = context.parser();
			if (parser.currentToken() == XContentParser.Token.VALUE_NULL
					|| (parser.currentToken() == XContentParser.Token.VALUE_STRING && parser.textLength() == 0)) {
				if (nullValue == null) {
					return null;
				}
				value = nullValue;
				if (nullValueAsString != null && (context.includeInAll(includeInAll, this))) {
					context.allEntries().addText(names.fullName(), nullValueAsString, boost);
				}
			} else if (parser.currentToken() == XContentParser.Token.START_OBJECT) {
				XContentParser.Token token;
				String currentFieldName = null;
				Double objValue = nullValue;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else {
						if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
							if (parser.currentToken() != XContentParser.Token.VALUE_NULL) {
								objValue = parser.doubleValue();
							}
						} else if ("boost".equals(currentFieldName) || "_boost".equals(currentFieldName)) {
							boost = parser.floatValue();
						}
					}
				}
				if (objValue == null) {

					return null;
				}
				value = objValue;
			} else {
				value = parser.doubleValue();
				if (context.includeInAll(includeInAll, this)) {
					context.allEntries().addText(names.fullName(), parser.text(), boost);
				}
			}
		}

		CustomDoubleNumericField field = new CustomDoubleNumericField(this, value);
		field.setBoost(boost);
		return field;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.DOUBLE;
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
			this.nullValue = ((DoubleFieldMapper) mergeWith).nullValue;
			this.nullValueAsString = ((DoubleFieldMapper) mergeWith).nullValueAsString;
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
		if (fuzzyFactor != Defaults.FUZZY_FACTOR) {
			builder.field("fuzzy_factor", fuzzyFactor);
		}
		if (nullValue != null) {
			builder.field("null_value", nullValue);
		}
		if (includeInAll != null) {
			builder.field("include_in_all", includeInAll);
		}
	}

	/**
	 * The Class CustomDoubleNumericField.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomDoubleNumericField extends CustomNumericField {

		/** The number. */
		private final double number;

		/** The mapper. */
		private final NumberFieldMapper mapper;

		/**
		 * Instantiates a new custom double numeric field.
		 *
		 * @param mapper the mapper
		 * @param number the number
		 */
		public CustomDoubleNumericField(NumberFieldMapper mapper, double number) {
			super(mapper, mapper.stored() ? Numbers.doubleToBytes(number) : null);
			this.mapper = mapper;
			this.number = number;
		}

		/* (non-Javadoc)
		 * @see org.apache.lucene.document.Fieldable#tokenStreamValue()
		 */
		@Override
		public TokenStream tokenStreamValue() {
			if (isIndexed) {
				return mapper.popCachedStream().setDoubleValue(number);
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper.CustomNumericField#numericAsString()
		 */
		@Override
		public String numericAsString() {
			return Double.toString(number);
		}
	}
}