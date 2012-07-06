/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SourceFieldMapper.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.Tuple;
import cn.com.rebirth.commons.compress.lzf.LZF;
import cn.com.rebirth.commons.compress.lzf.LZFDecoder;
import cn.com.rebirth.commons.exception.RestartParseException;
import cn.com.rebirth.commons.io.stream.CachedStreamInput;
import cn.com.rebirth.commons.io.stream.LZFStreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;
import cn.com.rebirth.search.commons.io.stream.CachedStreamOutput;
import cn.com.rebirth.search.commons.io.stream.LZFStreamOutput;
import cn.com.rebirth.search.commons.lucene.Lucene;
import cn.com.rebirth.search.commons.lucene.document.ResetFieldSelector;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentType;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.index.mapper.InternalMapper;
import cn.com.rebirth.search.core.index.mapper.Mapper;
import cn.com.rebirth.search.core.index.mapper.MapperBuilders;
import cn.com.rebirth.search.core.index.mapper.MapperParsingException;
import cn.com.rebirth.search.core.index.mapper.MergeContext;
import cn.com.rebirth.search.core.index.mapper.MergeMappingException;
import cn.com.rebirth.search.core.index.mapper.ParseContext;
import cn.com.rebirth.search.core.index.mapper.RootMapper;
import cn.com.rebirth.search.core.index.mapper.core.AbstractFieldMapper;

import com.google.common.base.Objects;


/**
 * The Class SourceFieldMapper.
 *
 * @author l.xue.nong
 */
public class SourceFieldMapper extends AbstractFieldMapper<byte[]> implements InternalMapper, RootMapper {

	
	/** The Constant NAME. */
	public static final String NAME = "_source";

	
	/** The Constant CONTENT_TYPE. */
	public static final String CONTENT_TYPE = "_source";

	
	/**
	 * The Class Defaults.
	 *
	 * @author l.xue.nong
	 */
	public static class Defaults extends AbstractFieldMapper.Defaults {

		
		/** The Constant NAME. */
		public static final String NAME = SourceFieldMapper.NAME;

		
		/** The Constant ENABLED. */
		public static final boolean ENABLED = true;

		
		/** The Constant COMPRESS_THRESHOLD. */
		public static final long COMPRESS_THRESHOLD = -1;

		
		/** The Constant FORMAT. */
		public static final String FORMAT = null; 

		
		/** The Constant INDEX. */
		public static final Field.Index INDEX = Field.Index.NO;

		
		/** The Constant STORE. */
		public static final Field.Store STORE = Field.Store.YES;

		
		/** The Constant OMIT_NORMS. */
		public static final boolean OMIT_NORMS = true;

		
		/** The Constant OMIT_TERM_FREQ_AND_POSITIONS. */
		public static final boolean OMIT_TERM_FREQ_AND_POSITIONS = true;

		
		/** The Constant INCLUDES. */
		public static final String[] INCLUDES = Strings.EMPTY_ARRAY;

		
		/** The Constant EXCLUDES. */
		public static final String[] EXCLUDES = Strings.EMPTY_ARRAY;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder extends Mapper.Builder<Builder, SourceFieldMapper> {

		
		/** The enabled. */
		private boolean enabled = Defaults.ENABLED;

		
		/** The compress threshold. */
		private long compressThreshold = Defaults.COMPRESS_THRESHOLD;

		
		/** The compress. */
		private Boolean compress = null;

		
		/** The format. */
		private String format = Defaults.FORMAT;

		
		/** The includes. */
		private String[] includes = Defaults.INCLUDES;

		
		/** The excludes. */
		private String[] excludes = Defaults.EXCLUDES;

		
		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			super(Defaults.NAME);
		}

		
		/**
		 * Enabled.
		 *
		 * @param enabled the enabled
		 * @return the builder
		 */
		public Builder enabled(boolean enabled) {
			this.enabled = enabled;
			return this;
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

		
		/**
		 * Format.
		 *
		 * @param format the format
		 * @return the builder
		 */
		public Builder format(String format) {
			this.format = format;
			return this;
		}

		
		/**
		 * Includes.
		 *
		 * @param includes the includes
		 * @return the builder
		 */
		public Builder includes(String[] includes) {
			this.includes = includes;
			return this;
		}

		
		/**
		 * Excludes.
		 *
		 * @param excludes the excludes
		 * @return the builder
		 */
		public Builder excludes(String[] excludes) {
			this.excludes = excludes;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.mapper.Mapper.Builder#build(cn.com.summall.search.core.index.mapper.Mapper.BuilderContext)
		 */
		@Override
		public SourceFieldMapper build(BuilderContext context) {
			return new SourceFieldMapper(name, enabled, format, compress, compressThreshold, includes, excludes);
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
			SourceFieldMapper.Builder builder = MapperBuilders.source();

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String fieldName = Strings.toUnderscoreCase(entry.getKey());
				Object fieldNode = entry.getValue();
				if (fieldName.equals("enabled")) {
					builder.enabled(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("compress") && fieldNode != null) {
					builder.compress(XContentMapValues.nodeBooleanValue(fieldNode));
				} else if (fieldName.equals("compress_threshold") && fieldNode != null) {
					if (fieldNode instanceof Number) {
						builder.compressThreshold(((Number) fieldNode).longValue());
						builder.compress(true);
					} else {
						builder.compressThreshold(ByteSizeValue.parseBytesSizeValue(fieldNode.toString()).bytes());
						builder.compress(true);
					}
				} else if ("format".equals(fieldName)) {
					builder.format(XContentMapValues.nodeStringValue(fieldNode, null));
				} else if (fieldName.equals("includes")) {
					List<Object> values = (List<Object>) fieldNode;
					String[] includes = new String[values.size()];
					for (int i = 0; i < includes.length; i++) {
						includes[i] = values.get(i).toString();
					}
					builder.includes(includes);
				} else if (fieldName.equals("excludes")) {
					List<Object> values = (List<Object>) fieldNode;
					String[] excludes = new String[values.size()];
					for (int i = 0; i < excludes.length; i++) {
						excludes[i] = values.get(i).toString();
					}
					builder.excludes(excludes);
				}
			}
			return builder;
		}
	}

	
	/** The enabled. */
	private final boolean enabled;

	
	/** The compress. */
	private Boolean compress;

	
	/** The compress threshold. */
	private long compressThreshold;

	
	/** The includes. */
	private String[] includes;

	
	/** The excludes. */
	private String[] excludes;

	
	/** The format. */
	private String format;

	
	/** The format content type. */
	private XContentType formatContentType;

	
	/**
	 * Instantiates a new source field mapper.
	 */
	public SourceFieldMapper() {
		this(Defaults.NAME, Defaults.ENABLED, Defaults.FORMAT, null, -1, Defaults.INCLUDES, Defaults.EXCLUDES);
	}

	
	/**
	 * Instantiates a new source field mapper.
	 *
	 * @param name the name
	 * @param enabled the enabled
	 * @param format the format
	 * @param compress the compress
	 * @param compressThreshold the compress threshold
	 * @param includes the includes
	 * @param excludes the excludes
	 */
	protected SourceFieldMapper(String name, boolean enabled, String format, Boolean compress, long compressThreshold,
			String[] includes, String[] excludes) {
		super(new Names(name, name, name, name), Defaults.INDEX, Defaults.STORE, Defaults.TERM_VECTOR, Defaults.BOOST,
				Defaults.OMIT_NORMS, Defaults.OMIT_TERM_FREQ_AND_POSITIONS, Lucene.KEYWORD_ANALYZER,
				Lucene.KEYWORD_ANALYZER);
		this.enabled = enabled;
		this.compress = compress;
		this.compressThreshold = compressThreshold;
		this.includes = includes;
		this.excludes = excludes;
		this.format = format;
		this.formatContentType = format == null ? null : XContentType.fromRestContentType(format);
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
	 * Field selector.
	 *
	 * @return the reset field selector
	 */
	public ResetFieldSelector fieldSelector() {
		return SourceFieldSelector.INSTANCE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#preParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void preParse(ParseContext context) throws IOException {
		super.parse(context);
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#postParse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void postParse(ParseContext context) throws IOException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parse(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void parse(ParseContext context) throws IOException {
		
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#validate(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	public void validate(ParseContext context) throws MapperParsingException {
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.RootMapper#includeInObject()
	 */
	@Override
	public boolean includeInObject() {
		return false;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#parseCreateField(cn.com.summall.search.core.index.mapper.ParseContext)
	 */
	@Override
	protected Field parseCreateField(ParseContext context) throws IOException {
		if (!enabled) {
			return null;
		}
		if (store == Field.Store.NO) {
			return null;
		}
		if (context.flyweight()) {
			return null;
		}
		byte[] data = context.source();
		int dataOffset = context.sourceOffset();
		int dataLength = context.sourceLength();

		boolean filtered = includes.length > 0 || excludes.length > 0;
		if (filtered) {
			

			Tuple<XContentType, Map<String, Object>> mapTuple = XContentHelper.convertToMap(data, dataOffset,
					dataLength, true);
			Map<String, Object> filteredSource = XContentMapValues.filter(mapTuple.v2(), includes, excludes);
			CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
			StreamOutput streamOutput;
			if (compress != null && compress && (compressThreshold == -1 || dataLength > compressThreshold)) {
				streamOutput = cachedEntry.cachedLZFBytes();
			} else {
				streamOutput = cachedEntry.cachedBytes();
			}
			XContentType contentType = formatContentType;
			if (contentType == null) {
				contentType = mapTuple.v1();
			}
			XContentBuilder builder = XContentFactory.contentBuilder(contentType, streamOutput).map(filteredSource);
			builder.close();

			data = cachedEntry.bytes().copiedByteArray();
			dataOffset = 0;
			dataLength = data.length;

			CachedStreamOutput.pushEntry(cachedEntry);
		} else if (compress != null && compress && !LZF.isCompressed(data, dataOffset, dataLength)) {
			if (compressThreshold == -1 || dataLength > compressThreshold) {
				CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
				try {
					XContentType contentType = XContentFactory.xContentType(data, dataOffset, dataLength);
					if (formatContentType != null && formatContentType != contentType) {
						XContentBuilder builder = XContentFactory.contentBuilder(formatContentType,
								cachedEntry.cachedLZFBytes());
						builder.copyCurrentStructure(XContentFactory.xContent(contentType).createParser(data,
								dataOffset, dataLength));
						builder.close();
					} else {
						LZFStreamOutput streamOutput = cachedEntry.cachedLZFBytes();
						streamOutput.writeBytes(data, dataOffset, dataLength);
						streamOutput.flush();
					}
					
					
					data = cachedEntry.bytes().copiedByteArray();
					dataOffset = 0;
					dataLength = data.length;
					
					context.source(data, dataOffset, dataLength);
				} finally {
					CachedStreamOutput.pushEntry(cachedEntry);
				}
			}
		} else if (formatContentType != null) {
			
			if (LZF.isCompressed(data, dataOffset, dataLength)) {
				BytesStreamInput siBytes = new BytesStreamInput(data, dataOffset, dataLength, false);
				LZFStreamInput siLzf = CachedStreamInput.cachedLzf(siBytes);
				XContentType contentType = XContentFactory.xContentType(siLzf);
				siLzf.resetToBufferStart();
				if (contentType != formatContentType) {
					
					CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
					try {
						LZFStreamOutput streamOutput = cachedEntry.cachedLZFBytes();
						XContentBuilder builder = XContentFactory.contentBuilder(formatContentType, streamOutput);
						builder.copyCurrentStructure(XContentFactory.xContent(contentType).createParser(siLzf));
						builder.close();
						data = cachedEntry.bytes().copiedByteArray();
						dataOffset = 0;
						dataLength = data.length;
						
						context.source(data, dataOffset, dataLength);
					} finally {
						CachedStreamOutput.pushEntry(cachedEntry);
					}
				}
			} else {
				XContentType contentType = XContentFactory.xContentType(data, dataOffset, dataLength);
				if (contentType != formatContentType) {
					
					
					CachedStreamOutput.Entry cachedEntry = CachedStreamOutput.popEntry();
					try {
						XContentBuilder builder = XContentFactory.contentBuilder(formatContentType,
								cachedEntry.cachedBytes());
						builder.copyCurrentStructure(XContentFactory.xContent(contentType).createParser(data,
								dataOffset, dataLength));
						builder.close();
						data = cachedEntry.bytes().copiedByteArray();
						dataOffset = 0;
						dataLength = data.length;
						
						context.source(data, dataOffset, dataLength);
					} finally {
						CachedStreamOutput.pushEntry(cachedEntry);
					}
				}
			}
		}
		return new Field(names().indexName(), data, dataOffset, dataLength);
	}

	
	/**
	 * Value.
	 *
	 * @param document the document
	 * @return the byte[]
	 */
	public byte[] value(Document document) {
		Fieldable field = document.getFieldable(names.indexName());
		return field == null ? null : value(field);
	}

	
	/**
	 * Native value.
	 *
	 * @param field the field
	 * @return the byte[]
	 */
	public byte[] nativeValue(Fieldable field) {
		return field.getBinaryValue();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#value(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public byte[] value(Fieldable field) {
		byte[] value = field.getBinaryValue();
		if (value == null) {
			return value;
		}
		if (LZF.isCompressed(value)) {
			try {
				return LZFDecoder.decode(value);
			} catch (IOException e) {
				throw new RestartParseException("failed to decompress source", e);
			}
		}
		return value;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueFromString(java.lang.String)
	 */
	@Override
	public byte[] valueFromString(String value) {
		return null;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.FieldMapper#valueAsString(org.apache.lucene.document.Fieldable)
	 */
	@Override
	public String valueAsString(Fieldable field) {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#indexedValue(java.lang.String)
	 */
	@Override
	public String indexedValue(String value) {
		return value;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#contentType()
	 */
	@Override
	protected String contentType() {
		return CONTENT_TYPE;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		
		if (enabled == Defaults.ENABLED && compress == null && compressThreshold == -1 && includes.length == 0
				&& excludes.length == 0) {
			return builder;
		}
		builder.startObject(contentType());
		if (enabled != Defaults.ENABLED) {
			builder.field("enabled", enabled);
		}
		if (!Objects.equal(format, Defaults.FORMAT)) {
			builder.field("format", format);
		}
		if (compress != null) {
			builder.field("compress", compress);
		}
		if (compressThreshold != -1) {
			builder.field("compress_threshold", new ByteSizeValue(compressThreshold).toString());
		}
		if (includes.length > 0) {
			builder.field("includes", includes);
		}
		if (excludes.length > 0) {
			builder.field("excludes", excludes);
		}
		builder.endObject();
		return builder;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.core.index.mapper.core.AbstractFieldMapper#merge(cn.com.summall.search.core.index.mapper.Mapper, cn.com.summall.search.core.index.mapper.MergeContext)
	 */
	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext) throws MergeMappingException {
		SourceFieldMapper sourceMergeWith = (SourceFieldMapper) mergeWith;
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
