/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BinaryFieldMapper.java 2012-7-6 14:29:45 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.mapper.core;

import static cn.com.rebirth.commons.xcontent.support.XContentMapValues.nodeBooleanValue;
import static cn.com.rebirth.search.core.index.mapper.MapperBuilders.binaryField;
import static cn.com.rebirth.search.core.index.mapper.core.TypeParsers.parseField;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.compress.lzf.LZFDecoder;
import cn.com.rebirth.commons.exception.RebirthParseException;
import cn.com.rebirth.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.commons.io.stream.LZFStreamOutput;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.Base64;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;

/**
 * The Class BinaryFieldMapper.
 *
 * @author l.xue.nong
 */
public class BinaryFieldMapper extends AbstractFieldMapper<byte[]> {

	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "binary";

	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		/** The Constant COMPRESS_THRESHOLD. */
		public static final long COMPRESS_THRESHOLD = -1;
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends AbstractFieldMapper.Builder<Builder, BinaryFieldMapper> {

		/** The compress. */
		private Boolean compress = null;

		/** The compress threshold. */
		private long compressThreshold = Defaults.COMPRESS_THRESHOLD;

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
		 * Compress.
		 *
		 * @param compress the compress
		 * @return the builder
		 */
		public Builder compress(boolean compress) {
			this.compress = compress;
			return this;
		}

		/**
		 * Compress threshold.
		 *
		 * @param compressThreshold the compress threshold
		 * @return the builder
		 */
		public Builder compressThreshold(long compressThreshold) {
			this.compressThreshold = compressThreshold;
			return this;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper.Builder#indexName(java.lang.String)
		 */
		@Override
		public Builder indexName(String indexName) {
			return super.indexName(indexName);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.mapper.Mapper.Builder#build(cn.com.rebirth.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public BinaryFieldMapper build(BuilderContext context) {
			return new BinaryFieldMapper(buildNames(context), compress, compressThreshold);
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
			BinaryFieldMapper.Builder builder = binaryField(name);
			parseField(builder, name, node, parserContext);
			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("compress") && fieldNode != null) {
					builder.compress(nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("compress_threshold") && fieldNode != null) {
					if (fieldNode instanceof Number) {
						builder.compressThreshold(((Number) fieldNode).longValue());
						builder.compress(true);
					} else {
						builder.compressThreshold(ByteSizeValue.parseBytesSizeValue(fieldNode.toString()).bytes());
						builder.compress(true);
					}
				}
			}
			return builder;
		}
	}

	/** The compress. */
	private Boolean compress;

	/** The compress threshold. */
	private long compressThreshold;

	/**
	 * Instantiates a new binary field mapper.
	 *
	 * @param names the names
	 * @param compress the compress
	 * @param compressThreshold the compress threshold
	 */
	protected BinaryFieldMapper(Names names, Boolean compress, long compressThreshold) {
		super(names, Field.Index.NO, Field.Store.YES, Field.TermVector.NO, 1.0f, true, true, null, null);
		this.compress = compress;
		this.compressThreshold = compressThreshold;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#valueForSearch(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public Object valueForSearch(Fieldable field) {
		return value(field);
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public byte[] value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value != null && LZF.isCompressed(value)) {
			try {
				return LZFDecoder.decode(value);
			} catch (IOException e) {
				throw new RebirthParseException("failed to decompress source", e);
			}
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public byte[] valueFromString(String value) {

		try {
			return Base64.decode(value);
		} catch (Exception e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		return null;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.rebirth.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		byte[] value;
		if (context.parser().currentToken() == XContentParser.Token.VALUE_NULL) {
			return null;
		} else {
			value = context.parser().binaryValue();
			if (compress != null && compress && !LZF.isCompressed(value, 0, value.length)) {
				if (compressThreshold == -1 || value.length > compressThreshold) {
					CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
					LZFStreamOutput streamOutput = cachedEntry.cachedLZFBytes();
					streamOutput.writeBytes(value, 0, value.length);
					streamOutput.flush();

					value = cachedEntry.bytes().copiedByteArray();
					CachedStreamOutput.pushEntry(cachedEntry);
				}
			}
		}
		if (value == null) {
			return null;
		}
		return new Field(names.indexName(), value);
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
		builder.startObject(names.name());
		builder.field("type", contentType());
		if (!names.name().equals(names.indexNameClean())) {
			builder.field("index_name", names.indexNameClean());
		}
		if (compress != null) {
			builder.field("compress", compress);
		}
		if (compressThreshold != -1) {
			builder.field("compress_threshold", new ByteSizeValue(compressThreshold).toString());
		}
		builder.endObject();
		return builder;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.rebirth.search.core.index.mapper.Mapper, cn.com.rebirth.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		BinaryFieldMapper sourceMergeWith = (BinaryFieldMapper) mergeWith;
		if (!mergeContext.mergeFlags().simulate()) {
			if (sourceMergeWith.compress != null) {
				this.compress = sourceMergeWith.compress;
			}
			if (sourceMergeWith.compressThreshold != -1) {
				this.compressThreshold = sourceMergeWith.compressThreshold;
			}
		}
	}

}