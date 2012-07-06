/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core TTLFieldMapper.java 2012-3-29 15:01:39 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.AlreadyExpiredException;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.LongFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.NumberFieldMapper;
import cn.com.rebirth.search.core.index.mapper.core.TypeParsers;
import cn.com.rebirth.search.core.search.internal.SearchContext;


/**
 * The Class TTLFieldMapper.
 *
 * @author l.xue.nong
 */
public class TTLFieldMapper extends LongFieldMapper implements InternalMapper, RootMapper {

	
	/** The Constant NAME. */
	public static final String NAME = "_ttl";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_ttl";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends LongFieldMapper.Defaults {

		
		/** The Constant NAME. */
		public static final String NAME = TTLFieldMapper.CONTENT_TYPE;

		
		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.YES;

		
		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NOT_ANALYZED;

		
		/** The Constant ENABLED. */
		public static final boolean ENABLED = false;

		
		/** The Constant DEFAULT. */
		public static final long DEFAULT = -1;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends NumberFieldMapper.Builder<Builder, TTLFieldMapper> {

		
		/** The enabled. */
		private boolean enabled = Defaults.ENABLED;

		
		/** The default ttl. */
		private long defaultTTL = Defaults.DEFAULT;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
			store = Defaults.STORE;
			index = Defaults.INDEX;
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
		 * Default ttl.
		 *
		 * @param defaultTTL the default ttl
		 * @return the builder
		 */
		public Builder defaultTTL(long defaultTTL) {
			this.defaultTTL = defaultTTL;
			return builder;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public TTLFieldMapper build(BuilderContext context) {
			return new TTLFieldMapper(store, index, enabled, defaultTTL);
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
			TTLFieldMapper.Builder builder = new TTLFieldMapper.Builder();
			TypeParsers.parseField(builder, builder.name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("default")) {
					TimeValue ttlTimeValue = XContentMapValues.nodeTimeValue(fieldNode, null);
					if (ttlTimeValue != null) {
						builder.defaultTTL(ttlTimeValue.millis());
					}
				}
			}
			return builder;
		}
	}

	
	/** The enabled. */
	private boolean enabled;

	
	/** The default ttl. */
	private long defaultTTL;

	
	/**
	 * Instantiates a new tTL field mapper.
	 */
	public TTLFieldMapper() {
		this(Defaults.STORE, Defaults.INDEX, Defaults.ENABLED, Defaults.DEFAULT);
	}

	
	/**
	 * Instantiates a new tTL field mapper.
	 *
	 * @param store the store
	 * @param index the index
	 * @param enabled the enabled
	 * @param defaultTTL the default ttl
	 */
	protected TTLFieldMapper(Field.Store store, Field.Index index, boolean enabled, long defaultTTL) {
		super(new Names(Defaults.NAME, Defaults.NAME, Defaults.NAME, Defaults.NAME), Defaults.PRECISION_STEP,
				Defaults.FUZZY_FACTOR, index, store, Defaults.BOOST, Defaults.OMIT_NORMS,
				Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Defaults.NULL_VALUE);
		this.enabled = enabled;
		this.defaultTTL = defaultTTL;
	}

	
	/**
	 * Enabled.
	 *
	 * @return true, if successful
	 */
	public boolean enabled() {
		return this.enabled;
	}

	
	/**
	 * Default ttl.
	 *
	 * @return the long
	 */
	public long defaultTTL() {
		return this.defaultTTL;
	}

	
	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.NumberFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		long now;
		SearchContext searchContext = SearchContext.current();
		if (searchContext != null) {
			now = searchContext.nowInMillis();
		} else {
			now = System.currentTimeMillis();
		}
		long value = value(field);
		return value - now;
	}

	
	
	/**
	 * Value for search.
	 *
	 * @param expirationTime the expiration time
	 * @return the object
	 */
	public Object valueForSearch(long expirationTime) {
		return expirationTime - System.currentTimeMillis();
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
	public void parse(ParseContext context) throws IOException, MapperParsingException {
		if (context.sourceToParse().ttl() < 0) { 
			long ttl;
			if (context.parser().currentToken() == XContentParser.Token.VALUE_STRING) {
				ttl = TimeValue.parseTimeValue(context.parser().text(), null).millis();
			} else {
				ttl = context.parser().longValue();
			}
			if (ttl <= 0) {
				throw new MapperParsingException("TTL value must be > 0. Illegal value provided [" + ttl + "]");
			}
			context.sourceToParse().ttl(ttl);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return true;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.LongFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Fieldable parseCreateField(ParseContext context) throws IOException, AlreadyExpiredException {
		if (enabled) {
			long ttl = context.sourceToParse().ttl();
			if (ttl <= 0 && defaultTTL > 0) { 
				ttl = defaultTTL;
				context.sourceToParse().ttl(ttl);
			}
			if (ttl > 0) { 
				long timestamp = context.sourceToParse().timestamp();
				long expire = new Date(timestamp + ttl).getTime();
				long now = System.currentTimeMillis();
				
				if (now >= expire) {
					throw new AlreadyExpiredException(context.index(), context.type(), context.id(), timestamp, ttl,
							now);
				}
				
				return new CustomLongNumericField(this, expire);
			}
		}
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		
		if (enabled == Defaults.ENABLED && defaultTTL == Defaults.DEFAULT) {
			return builder;
		}
		builder.startObject(CONTENT_TYPE);
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (defaultTTL != Defaults.DEFAULT) {
			builder.field("default", defaultTTL);
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.LongFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		TTLFieldMapper ttlMergeWith = (TTLFieldMapper) mergeWith;
		if (!mergeContext.mergeFlags().simulate()) {
			if (ttlMergeWith.defaultTTL != -1) {
				this.defaultTTL = ttlMergeWith.defaultTTL;
			}
		}
	}
}
