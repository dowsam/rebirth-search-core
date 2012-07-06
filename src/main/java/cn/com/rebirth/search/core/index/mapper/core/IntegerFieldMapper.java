/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IntegerFieldMapper.java 2012-3-29 15:01:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.core;

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
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.Numbers;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.analysis.NumericIntegerAnalyzer;
import cn.com.rebirth.search.core.index.cache.field.data.FieldDataCache;
import cn.com.rebirth.search.core.index.field.data.FieldDataType;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.query.QueryParseContext;
import cn.com.rebirth.search.core.index.search.NumericRangeFieldDataFilter;
import cn.com.rebirth.search.index.analysis.NamedAnalyzer;


/**
 * The Class IntegerFieldMapper.
 *
 * @author l.xue.nong
 */
public class IntegerFieldMapper extends NumberFieldMapper<Integer> {

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "integer";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends NumberFieldMapper.Defaults {

		
		/** The Constant NULL_VALUE. */
		public static final Integer NULL_VALUE = null;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, IntegerFieldMapper> {

		
		/** The null value. */
		protected Integer nullValue = Defaults.NULL_VALUE;

		
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
		public Builder nullValue(int nullValue) {
			this.nullValue = nullValue;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public IntegerFieldMapper build(BuilderContext context) {
			IntegerFieldMapper fieldMapper = new IntegerFieldMapper(buildNames(context), precisionStep, fuzzyFactor,
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
		 * @see cn.com.summall.search.core.index.mapper.Mapper.TypeParser#parse(java.lang.String, java.util.Map, cn.com.summall.search.core.index.mapper.Mapper.TypeParser.ParserContext)
		 */
		@Override
		public Mapper.Builder parse(String name, Map<String, Object> node, ParserContext parserContext)
				throws MapperParsingException {
			IntegerFieldMapper.Builder builder = MapperBuilders.integerField(name);
			TypeParsers.parseNumberField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String propName = Strings.toUnderscoreCase(entry.getKey());
				Object propNode = entry.getValue();
				if (propName.equals("null_value")) {
					builder.nullValue(XContentMapValues.nodeIntegerValue(propNode));
				}
			}
			return builder;
		}
	}

	
	/** The null value. */
	private Integer nullValue;

	
	/** The null value as string. */
	private String nullValueAsString;

	
	/**
	 * Instantiates a new integer field mapper.
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
	protected IntegerFieldMapper(Names names, int precisionStep, String fuzzyFactor, Field.Index index,
			Field.Store store, float boost, boolean omitNorms, boolean omitTermFreqAndPositions, Integer nullValue) {
		super(names, precisionStep, fuzzyFactor, index, store, boost, omitNorms, omitTermFreqAndPositions,
				new NamedAnalyzer("_int/" + precisionStep, new NumericIntegerAnalyzer(precisionStep)),
				new NamedAnalyzer("_int/max", new NumericIntegerAnalyzer(Integer.MAX_VALUE)));
		this.nullValue = nullValue;
		this.nullValueAsString = nullValue == null ? null : nullValue.toString();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#maxPrecisionStep()
	 */
	@Override
	protected int maxPrecisionStep() {
		return 32;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Integer value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return null;
		}
		return Numbers.bytesToInt(value);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public Integer valueFromString(String value) {
		return Integer.parseInt(value);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return NumericUtils.intToPrefixCoded(Integer.parseInt(value));
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, java.lang.String, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, String minSim, int prefixLength, int maxExpansions) {
		int iValue = Integer.parseInt(value);
		int iSim;
		try {
			iSim = Integer.parseInt(minSim);
		} catch (NumberFormatException e) {
			iSim = (int) Float.parseFloat(minSim);
		}
		return NumericRangeQuery
				.newIntRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#fuzzyQuery(java.lang.String, double, int, int)
	 */
	@Override
	public Query fuzzyQuery(String value, double minSim, int prefixLength, int maxExpansions) {
		int iValue = Integer.parseInt(value);
		int iSim = (int) (minSim * dFuzzyFactor);
		return NumericRangeQuery
				.newIntRange(names.indexName(), precisionStep, iValue - iSim, iValue + iSim, true, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#fieldQuery(java.lang.String, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query fieldQuery(String value, @Nullable QueryParseContext context) {
		int iValue = Integer.parseInt(value);
		return NumericRangeQuery.newIntRange(names.indexName(), precisionStep, iValue, iValue, true, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#rangeQuery(java.lang.String, java.lang.String, boolean, boolean, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Query rangeQuery(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeQuery.newIntRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Integer.parseInt(lowerTerm),
				upperTerm == null ? null : Integer.parseInt(upperTerm), includeLower, includeUpper);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#fieldFilter(java.lang.String, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter fieldFilter(String value, @Nullable QueryParseContext context) {
		int iValue = Integer.parseInt(value);
		return NumericRangeFilter.newIntRange(names.indexName(), precisionStep, iValue, iValue, true, true);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(java.lang.String, java.lang.String, boolean, boolean, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper,
			@Nullable QueryParseContext context) {
		return NumericRangeFilter.newIntRange(names.indexName(), precisionStep,
				lowerTerm == null ? null : Integer.parseInt(lowerTerm),
				upperTerm == null ? null : Integer.parseInt(upperTerm), includeLower, includeUpper);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#rangeFilter(cn.com.summall.search.core.index.cache.field.data.FieldDataCache, java.lang.String, java.lang.String, boolean, boolean, cn.com.summall.search.core.index.query.QueryParseContext)
	 */
	@Override
	public Filter rangeFilter(FieldDataCache fieldDataCache, String lowerTerm, String upperTerm, boolean includeLower,
			boolean includeUpper, @Nullable QueryParseContext context) {
		return NumericRangeFieldDataFilter.newIntRange(fieldDataCache, names.indexName(), lowerTerm == null ? null
				: Integer.parseInt(lowerTerm), upperTerm == null ? null : Integer.parseInt(upperTerm), includeLower,
				includeUpper);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#customBoost()
	 */
	@Override
	protected boolean customBoost() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		int value;
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
					value = Integer.parseInt(sExternalValue);
				}
			} else {
				value = ((Number) externalValue).intValue();
			}
			if (context.includeInAll(includeInAll, this)) {
				context.allEntries().addText(names.fullName(), Integer.toString(value), boost);
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
				Integer objValue = nullValue;
				while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
					if (token == XContentParser.Token.FIELD_NAME) {
						currentFieldName = parser.currentName();
					} else {
						if ("value".equals(currentFieldName) || "_value".equals(currentFieldName)) {
							if (parser.currentToken() != XContentParser.Token.VALUE_NULL) {
								objValue = parser.intValue();
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
				value = parser.intValue();
				if (context.includeInAll(includeInAll, this)) {
					context.allEntries().addText(names.fullName(), parser.text(), boost);
				}
			}
		}

		CustomIntegerNumericField field = new CustomIntegerNumericField(this, value);
		field.setBoost(boost);
		return field;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#fieldDataType()
	 */
	@Override
	public FieldDataType fieldDataType() {
		return FieldDataType.DefaultTypes.INT;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		super.merge(mergeWith, mergeContext);
		if (!this.getClass().equals(mergeWith.getClass())) {
			return;
		}
		if (!mergeContext.mergeFlags().simulate()) {
			this.nullValue = ((IntegerFieldMapper) mergeWith).nullValue;
			this.nullValueAsString = ((IntegerFieldMapper) mergeWith).nullValueAsString;
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#doXContentBody(cn.com.summall.search.commons.xcontent.XContentBuilder)
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
	 * The Class CustomIntegerNumericField.
	 *
	 * @author l.xue.nong
	 */
	public static class CustomIntegerNumericField extends CustomNumericField {

		
		/** The number. */
		private final int number;

		
		/** The mapper. */
		private final NumberFieldMapper mapper;

		
		/**
		 * Instantiates a new custom integer numeric field.
		 *
		 * @param mapper the mapper
		 * @param number the number
		 */
		public CustomIntegerNumericField(NumberFieldMapper mapper, int number) {
			super(mapper, mapper.stored() ? Numbers.intToBytes(number) : null);
			this.mapper = mapper;
			this.number = number;
		}

		
		/* (non-Javadoc)
		 * @see org.apache.lucene.document.Fieldable#tokenStreamValue()
		 */
		@Override
		public TokenStream tokenStreamValue() {
			if (isIndexed) {
				return mapper.popCachedStream().setIntValue(number);
			}
			return null;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper.CustomNumericField#numericAsString()
		 */
		@Override
		public String numericAsString() {
			return Integer.toString(number);
		}
	}
}
