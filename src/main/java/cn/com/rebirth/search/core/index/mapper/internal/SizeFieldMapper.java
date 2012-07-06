/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SizeFieldMapper.java 2012-3-29 15:02:47 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.IntegerFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;


/**
 * The Class SizeFieldMapper.
 *
 * @author l.xue.nong
 */
public class SizeFieldMapper extends IntegerFieldMapper implements RootMapper {

	
	/** The Constant NAME. */
	public static final String NAME = "_size";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_size";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends IntegerFieldMapper.Defaults {

		
		/** The Constant NAME. */
		public static final String NAME = CONTENT_TYPE;

		
		/** The Constant ENABLED. */
		public static final boolean ENABLED = false;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, IntegerFieldMapper> {

		
		/** The enabled. */
		protected boolean enabled = Defaults.ENABLED;

		
		/** The store. */
		protected Field.Store store = Defaults.STORE;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			builder = this;
		}

		
		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return builder;
		}

		
		/**
		 * Store.
		 *
		 * @param store the store
		 * @return the builder
		 */
		public Builder store(Field.Store store) {
			this.store = store;
			return builder;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public SizeFieldMapper build(BuilderContext context) {
			return new SizeFieldMapper(enabled, store);
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
			SizeFieldMapper.Builder builder = new SizeFieldMapper.Builder();
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("store")) {
					builder.store(TypeParsers.parseStore(fieldName, fieldNode.toString()));
				}
			}
			return builder;
		}
	}

	
	/** The enabled. */
	private final boolean enabled;

	
	/**
	 * Instantiates a new size field mapper.
	 */
	public SizeFieldMapper() {
		this(Defaults.ENABLED, Defaults.STORE);
	}

	
	/**
	 * Instantiates a new size field mapper.
	 *
	 * @param enabled the enabled
	 * @param store the store
	 */
	public SizeFieldMapper(boolean enabled, Field.Store store) {
		super(new Names(Defaults.NAME), Defaults.PRECISION_STEP, Defaults.FUZZY_FACTOR, Defaults.INDEX, store,
				Defaults.BOOST, Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.NULL_VALUE);
		this.enabled = enabled;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.IntegerFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return Defaults.NAME;
	}

	
	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	public boolean enabled() {
		return this.enabled;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#validate(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#preParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#postParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
		
		super.parse(context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.IntegerFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException {
		if (!enabled) {
			return null;
		}
		return new CustomIntegerNumericField(this, context.sourceLength());
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		
		if (enabled == Defaults.ENABLED && store == Defaults.STORE) {
			return builder;
		}
		builder.startObject(contentType());
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (store != Defaults.STORE) {
			builder.field("store", store.name().toLowerCase());
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.IntegerFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		
	}
}