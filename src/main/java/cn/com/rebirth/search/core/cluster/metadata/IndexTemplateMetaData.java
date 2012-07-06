/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core IndexTemplateMetaData.java 2012-3-29 15:02:32 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;

import com.google.common.collect.ImmutableMap;


/**
 * The Class IndexTemplateMetaData.
 *
 * @author l.xue.nong
 */
public class IndexTemplateMetaData {

	
	/** The name. */
	private final String name;

	
	/** The order. */
	private final int order;

	
	/** The template. */
	private final String template;

	
	/** The settings. */
	private final Settings settings;

	
	
	/** The mappings. */
	private final ImmutableMap<String, CompressedString> mappings;

	
	/**
	 * Instantiates a new index template meta data.
	 *
	 * @param name the name
	 * @param order the order
	 * @param template the template
	 * @param settings the settings
	 * @param mappings the mappings
	 */
	public IndexTemplateMetaData(String name, int order, String template, Settings settings,
			ImmutableMap<String, CompressedString> mappings) {
		this.name = name;
		this.order = order;
		this.template = template;
		this.settings = settings;
		this.mappings = mappings;
	}

	
	/**
	 * Name.
	 *
	 * @return the string
	 */
	public String name() {
		return this.name;
	}

	
	/**
	 * Order.
	 *
	 * @return the int
	 */
	public int order() {
		return this.order;
	}

	
	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return order();
	}

	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	
	/**
	 * Template.
	 *
	 * @return the string
	 */
	public String template() {
		return this.template;
	}

	
	/**
	 * Gets the template.
	 *
	 * @return the template
	 */
	public String getTemplate() {
		return this.template;
	}

	
	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	public Settings settings() {
		return this.settings;
	}

	
	/**
	 * Gets the settings.
	 *
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings();
	}

	
	/**
	 * Mappings.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, CompressedString> mappings() {
		return this.mappings;
	}

	
	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	public ImmutableMap<String, CompressedString> getMappings() {
		return this.mappings;
	}

	
	/**
	 * Builder.
	 *
	 * @param name the name
	 * @return the builder
	 */
	public static Builder builder(String name) {
		return new Builder(name);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		IndexTemplateMetaData that = (IndexTemplateMetaData) o;

		if (order != that.order)
			return false;
		if (!mappings.equals(that.mappings))
			return false;
		if (!name.equals(that.name))
			return false;
		if (!settings.equals(that.settings))
			return false;
		if (!template.equals(that.template))
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + order;
		result = 31 * result + template.hashCode();
		result = 31 * result + settings.hashCode();
		result = 31 * result + mappings.hashCode();
		return result;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		
		/** The name. */
		private String name;

		
		/** The order. */
		private int order;

		
		/** The template. */
		private String template;

		
		/** The settings. */
		private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

		
		/** The mappings. */
		private MapBuilder<String, CompressedString> mappings = MapBuilder.newMapBuilder();

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param name the name
		 */
		public Builder(String name) {
			this.name = name;
		}

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param indexTemplateMetaData the index template meta data
		 */
		public Builder(IndexTemplateMetaData indexTemplateMetaData) {
			this(indexTemplateMetaData.name());
			order(indexTemplateMetaData.order());
			template(indexTemplateMetaData.template());
			settings(indexTemplateMetaData.settings());
			mappings.putAll(indexTemplateMetaData.mappings());
		}

		
		/**
		 * Order.
		 *
		 * @param order the order
		 * @return the builder
		 */
		public Builder order(int order) {
			this.order = order;
			return this;
		}

		
		/**
		 * Template.
		 *
		 * @param template the template
		 * @return the builder
		 */
		public Builder template(String template) {
			this.template = template;
			return this;
		}

		
		/**
		 * Template.
		 *
		 * @return the string
		 */
		public String template() {
			return template;
		}

		
		/**
		 * Settings.
		 *
		 * @param settings the settings
		 * @return the builder
		 */
		public Builder settings(Settings.Builder settings) {
			this.settings = settings.build();
			return this;
		}

		
		/**
		 * Settings.
		 *
		 * @param settings the settings
		 * @return the builder
		 */
		public Builder settings(Settings settings) {
			this.settings = settings;
			return this;
		}

		
		/**
		 * Removes the mapping.
		 *
		 * @param mappingType the mapping type
		 * @return the builder
		 */
		public Builder removeMapping(String mappingType) {
			mappings.remove(mappingType);
			return this;
		}

		
		/**
		 * Put mapping.
		 *
		 * @param mappingType the mapping type
		 * @param mappingSource the mapping source
		 * @return the builder
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Builder putMapping(String mappingType, CompressedString mappingSource) throws IOException {
			mappings.put(mappingType, mappingSource);
			return this;
		}

		
		/**
		 * Put mapping.
		 *
		 * @param mappingType the mapping type
		 * @param mappingSource the mapping source
		 * @return the builder
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Builder putMapping(String mappingType, String mappingSource) throws IOException {
			mappings.put(mappingType, new CompressedString(mappingSource));
			return this;
		}

		
		/**
		 * Builds the.
		 *
		 * @return the index template meta data
		 */
		public IndexTemplateMetaData build() {
			return new IndexTemplateMetaData(name, order, template, settings, mappings.immutableMap());
		}

		
		/**
		 * To x content.
		 *
		 * @param indexTemplateMetaData the index template meta data
		 * @param builder the builder
		 * @param params the params
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void toXContent(IndexTemplateMetaData indexTemplateMetaData, XContentBuilder builder,
				ToXContent.Params params) throws IOException {
			builder.startObject(indexTemplateMetaData.name(), XContentBuilder.FieldCaseConversion.NONE);

			builder.field("order", indexTemplateMetaData.order());
			builder.field("template", indexTemplateMetaData.template());

			builder.startObject("settings");
			for (Map.Entry<String, String> entry : indexTemplateMetaData.settings().getAsMap().entrySet()) {
				builder.field(entry.getKey(), entry.getValue());
			}
			builder.endObject();

			builder.startArray("mappings");
			for (Map.Entry<String, CompressedString> entry : indexTemplateMetaData.mappings().entrySet()) {
				byte[] data = entry.getValue().uncompressed();
				XContentParser parser = XContentFactory.xContent(data).createParser(data);
				Map<String, Object> mapping = parser.mapOrderedAndClose();
				builder.map(mapping);
			}
			builder.endArray();

			builder.endObject();
		}

		
		/**
		 * From x content standalone.
		 *
		 * @param parser the parser
		 * @return the index template meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexTemplateMetaData fromXContentStandalone(XContentParser parser) throws IOException {
			XContentParser.Token token = parser.nextToken();
			if (token == null) {
				throw new IOException("no data");
			}
			if (token != XContentParser.Token.START_OBJECT) {
				throw new IOException("should start object");
			}
			token = parser.nextToken();
			if (token != XContentParser.Token.FIELD_NAME) {
				throw new IOException("the first field should be the template name");
			}
			return fromXContent(parser);
		}

		
		/**
		 * From x content.
		 *
		 * @param parser the parser
		 * @return the index template meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexTemplateMetaData fromXContent(XContentParser parser) throws IOException {
			Builder builder = new Builder(parser.currentName());

			String currentFieldName = null;
			XContentParser.Token token = parser.nextToken();
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("settings".equals(currentFieldName)) {
						ImmutableSettings.Builder settingsBuilder = ImmutableSettings.settingsBuilder();
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							String key = parser.currentName();
							token = parser.nextToken();
							String value = parser.text();
							settingsBuilder.put(key, value);
						}
						builder.settings(settingsBuilder.build());
					} else if ("mappings".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							if (token == XContentParser.Token.FIELD_NAME) {
								currentFieldName = parser.currentName();
							} else if (token == XContentParser.Token.START_OBJECT) {
								String mappingType = currentFieldName;
								Map<String, Object> mappingSource = MapBuilder.<String, Object> newMapBuilder()
										.put(mappingType, parser.mapOrdered()).map();
								builder.putMapping(mappingType, XContentFactory.jsonBuilder().map(mappingSource)
										.string());
							}
						}
					}
				} else if (token == XContentParser.Token.START_ARRAY) {
					if ("mappings".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							Map<String, Object> mapping = parser.mapOrdered();
							if (mapping.size() == 1) {
								String mappingType = mapping.keySet().iterator().next();
								String mappingSource = XContentFactory.jsonBuilder().map(mapping).string();

								if (mappingSource == null) {
									
								} else {
									builder.putMapping(mappingType, mappingSource);
								}
							}
						}
					}
				} else if (token.isValue()) {
					if ("template".equals(currentFieldName)) {
						builder.template(parser.text());
					} else if ("order".equals(currentFieldName)) {
						builder.order(parser.intValue());
					}
				}
			}
			return builder.build();
		}

		
		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the index template meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexTemplateMetaData readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder(in.readUTF());
			builder.order(in.readInt());
			builder.template(in.readUTF());
			builder.settings(ImmutableSettings.readSettingsFromStream(in));
			int mappingsSize = in.readVInt();
			for (int i = 0; i < mappingsSize; i++) {
				builder.putMapping(in.readUTF(), CompressedString.readCompressedString(in));
			}
			return builder.build();
		}

		
		/**
		 * Write to.
		 *
		 * @param indexTemplateMetaData the index template meta data
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(IndexTemplateMetaData indexTemplateMetaData, StreamOutput out) throws IOException {
			out.writeUTF(indexTemplateMetaData.name());
			out.writeInt(indexTemplateMetaData.order());
			out.writeUTF(indexTemplateMetaData.template());
			ImmutableSettings.writeSettingsToStream(indexTemplateMetaData.settings(), out);
			out.writeVInt(indexTemplateMetaData.mappings().size());
			for (Map.Entry<String, CompressedString> entry : indexTemplateMetaData.mappings().entrySet()) {
				out.writeUTF(entry.getKey());
				entry.getValue().writeTo(out);
			}
		}
	}

}
