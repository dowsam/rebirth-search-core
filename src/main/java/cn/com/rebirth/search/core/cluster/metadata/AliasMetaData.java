/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core AliasMetaData.java 2012-3-29 15:02:29 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import java.io.IOException;
import java.util.Map;

import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.RestartGenerationException;


/**
 * The Class AliasMetaData.
 *
 * @author l.xue.nong
 */
public class AliasMetaData {

	
	/** The alias. */
	private final String alias;

	
	/** The filter. */
	private final CompressedString filter;

	
	/** The index routing. */
	private String indexRouting;

	
	/** The search routing. */
	private String searchRouting;

	
	/**
	 * Instantiates a new alias meta data.
	 *
	 * @param alias the alias
	 * @param filter the filter
	 * @param indexRouting the index routing
	 * @param searchRouting the search routing
	 */
	private AliasMetaData(String alias, CompressedString filter, String indexRouting, String searchRouting) {
		this.alias = alias;
		this.filter = filter;
		this.indexRouting = indexRouting;
		this.searchRouting = searchRouting;
	}

	
	/**
	 * Alias.
	 *
	 * @return the string
	 */
	public String alias() {
		return alias;
	}

	
	/**
	 * Gets the alias.
	 *
	 * @return the alias
	 */
	public String getAlias() {
		return alias();
	}

	
	/**
	 * Filter.
	 *
	 * @return the compressed string
	 */
	public CompressedString filter() {
		return filter;
	}

	
	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public CompressedString getFilter() {
		return filter();
	}

	
	/**
	 * Gets the search routing.
	 *
	 * @return the search routing
	 */
	public String getSearchRouting() {
		return searchRouting();
	}

	
	/**
	 * Search routing.
	 *
	 * @return the string
	 */
	public String searchRouting() {
		return searchRouting;
	}

	
	/**
	 * Gets the index routing.
	 *
	 * @return the index routing
	 */
	public String getIndexRouting() {
		return indexRouting();
	}

	
	/**
	 * Index routing.
	 *
	 * @return the string
	 */
	public String indexRouting() {
		return indexRouting;
	}

	
	/**
	 * New alias meta data builder.
	 *
	 * @param alias the alias
	 * @return the builder
	 */
	public static Builder newAliasMetaDataBuilder(String alias) {
		return new Builder(alias);
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

		AliasMetaData that = (AliasMetaData) o;

		if (alias != null ? !alias.equals(that.alias) : that.alias != null)
			return false;
		if (filter != null ? !filter.equals(that.filter) : that.filter != null)
			return false;
		if (indexRouting != null ? !indexRouting.equals(that.indexRouting) : that.indexRouting != null)
			return false;
		if (searchRouting != null ? !searchRouting.equals(that.searchRouting) : that.searchRouting != null)
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = alias != null ? alias.hashCode() : 0;
		result = 31 * result + (filter != null ? filter.hashCode() : 0);
		result = 31 * result + (indexRouting != null ? indexRouting.hashCode() : 0);
		result = 31 * result + (searchRouting != null ? searchRouting.hashCode() : 0);
		return result;
	}

	
	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		
		/** The alias. */
		private String alias;

		
		/** The filter. */
		private CompressedString filter;

		
		/** The index routing. */
		private String indexRouting;

		
		/** The search routing. */
		private String searchRouting;

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param alias the alias
		 */
		public Builder(String alias) {
			this.alias = alias;
		}

		
		/**
		 * Instantiates a new builder.
		 *
		 * @param aliasMetaData the alias meta data
		 */
		public Builder(AliasMetaData aliasMetaData) {
			this(aliasMetaData.alias());
			filter = aliasMetaData.filter();
			indexRouting = aliasMetaData.indexRouting();
			searchRouting = aliasMetaData.searchRouting();
		}

		
		/**
		 * Alias.
		 *
		 * @return the string
		 */
		public String alias() {
			return alias;
		}

		
		/**
		 * Filter.
		 *
		 * @param filter the filter
		 * @return the builder
		 */
		public Builder filter(CompressedString filter) {
			this.filter = filter;
			return this;
		}

		
		/**
		 * Filter.
		 *
		 * @param filter the filter
		 * @return the builder
		 */
		public Builder filter(String filter) {
			if (!Strings.hasLength(filter)) {
				this.filter = null;
				return this;
			}
			try {
				XContentParser parser = XContentFactory.xContent(filter).createParser(filter);
				try {
					filter(parser.mapOrdered());
				} finally {
					parser.close();
				}
				return this;
			} catch (IOException e) {
				throw new RestartGenerationException("Failed to generate [" + filter + "]", e);
			}
		}

		
		/**
		 * Filter.
		 *
		 * @param filter the filter
		 * @return the builder
		 */
		public Builder filter(Map<String, Object> filter) {
			if (filter == null || filter.isEmpty()) {
				this.filter = null;
				return this;
			}
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder().map(filter);
				this.filter = new CompressedString(builder.underlyingBytes(), 0, builder.underlyingBytesLength());
				return this;
			} catch (IOException e) {
				throw new RestartGenerationException("Failed to build json for alias request", e);
			}
		}

		
		/**
		 * Filter.
		 *
		 * @param filterBuilder the filter builder
		 * @return the builder
		 */
		public Builder filter(XContentBuilder filterBuilder) {
			try {
				return filter(filterBuilder.string());
			} catch (IOException e) {
				throw new RestartGenerationException("Failed to build json for alias request", e);
			}
		}

		
		/**
		 * Routing.
		 *
		 * @param routing the routing
		 * @return the builder
		 */
		public Builder routing(String routing) {
			this.indexRouting = routing;
			this.searchRouting = routing;
			return this;
		}

		
		/**
		 * Index routing.
		 *
		 * @param indexRouting the index routing
		 * @return the builder
		 */
		public Builder indexRouting(String indexRouting) {
			this.indexRouting = indexRouting;
			return this;
		}

		
		/**
		 * Search routing.
		 *
		 * @param searchRouting the search routing
		 * @return the builder
		 */
		public Builder searchRouting(String searchRouting) {
			this.searchRouting = searchRouting;
			return this;
		}

		
		/**
		 * Builds the.
		 *
		 * @return the alias meta data
		 */
		public AliasMetaData build() {
			return new AliasMetaData(alias, filter, indexRouting, searchRouting);
		}

		
		/**
		 * To x content.
		 *
		 * @param aliasMetaData the alias meta data
		 * @param builder the builder
		 * @param params the params
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void toXContent(AliasMetaData aliasMetaData, XContentBuilder builder, ToXContent.Params params)
				throws IOException {
			builder.startObject(aliasMetaData.alias(), XContentBuilder.FieldCaseConversion.NONE);

			boolean binary = params.paramAsBoolean("binary", false);

			if (aliasMetaData.filter() != null) {
				if (binary) {
					builder.field("filter", aliasMetaData.filter.compressed());
				} else {
					byte[] data = aliasMetaData.filter().uncompressed();
					XContentParser parser = XContentFactory.xContent(data).createParser(data);
					Map<String, Object> filter = parser.mapOrdered();
					parser.close();
					builder.field("filter", filter);
				}
			}
			if (aliasMetaData.indexRouting() != null) {
				builder.field("index_routing", aliasMetaData.indexRouting());
			}
			if (aliasMetaData.searchRouting() != null) {
				builder.field("search_routing", aliasMetaData.searchRouting());
			}

			builder.endObject();
		}

		
		/**
		 * From x content.
		 *
		 * @param parser the parser
		 * @return the alias meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static AliasMetaData fromXContent(XContentParser parser) throws IOException {
			Builder builder = new Builder(parser.currentName());

			String currentFieldName = null;
			XContentParser.Token token = parser.nextToken();
			if (token == null) {
				
				return builder.build();
			}
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("filter".equals(currentFieldName)) {
						Map<String, Object> filter = parser.mapOrdered();
						builder.filter(filter);
					}
				} else if (token == XContentParser.Token.VALUE_EMBEDDED_OBJECT) {
					if ("filter".equals(currentFieldName)) {
						builder.filter(new CompressedString(parser.binaryValue()));
					}
				} else if (token == XContentParser.Token.VALUE_STRING) {
					if ("routing".equals(currentFieldName)) {
						builder.routing(parser.text());
					} else if ("index_routing".equals(currentFieldName) || "indexRouting".equals(currentFieldName)) {
						builder.indexRouting(parser.text());
					} else if ("search_routing".equals(currentFieldName) || "searchRouting".equals(currentFieldName)) {
						builder.searchRouting(parser.text());
					}
				}
			}
			return builder.build();
		}

		
		/**
		 * Write to.
		 *
		 * @param aliasMetaData the alias meta data
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(AliasMetaData aliasMetaData, StreamOutput out) throws IOException {
			out.writeUTF(aliasMetaData.alias());
			if (aliasMetaData.filter() != null) {
				out.writeBoolean(true);
				aliasMetaData.filter.writeTo(out);
			} else {
				out.writeBoolean(false);
			}
			if (aliasMetaData.indexRouting() != null) {
				out.writeBoolean(true);
				out.writeUTF(aliasMetaData.indexRouting());
			} else {
				out.writeBoolean(false);
			}
			if (aliasMetaData.searchRouting() != null) {
				out.writeBoolean(true);
				out.writeUTF(aliasMetaData.searchRouting());
			} else {
				out.writeBoolean(false);
			}

		}

		
		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the alias meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static AliasMetaData readFrom(StreamInput in) throws IOException {
			String alias = in.readUTF();
			CompressedString filter = null;
			if (in.readBoolean()) {
				filter = CompressedString.readCompressedString(in);
			}
			String indexRouting = null;
			if (in.readBoolean()) {
				indexRouting = in.readUTF();
			}
			String searchRouting = null;
			if (in.readBoolean()) {
				searchRouting = in.readUTF();
			}
			return new AliasMetaData(alias, filter, indexRouting, searchRouting);
		}
	}

}
