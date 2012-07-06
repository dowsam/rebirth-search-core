/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core IndexMetaData.java 2012-7-6 14:28:48 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.Preconditions;
import cn.com.rebirth.search.commons.settings.ImmutableSettings;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.cluster.node.DiscoveryNodeFilters;
import cn.com.rebirth.search.core.rest.RestStatus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The Class IndexMetaData.
 *
 * @author l.xue.nong
 */
public class IndexMetaData {

	/** The dynamic settings. */
	private static ImmutableSet<String> dynamicSettings = ImmutableSet.<String> builder()
			.add(IndexMetaData.SETTING_NUMBER_OF_REPLICAS).add(IndexMetaData.SETTING_AUTO_EXPAND_REPLICAS)
			.add(IndexMetaData.SETTING_READ_ONLY).add(IndexMetaData.SETTING_BLOCKS_READ)
			.add(IndexMetaData.SETTING_BLOCKS_WRITE).add(IndexMetaData.SETTING_BLOCKS_METADATA).build();

	/** The Constant INDEX_READ_ONLY_BLOCK. */
	public static final ClusterBlock INDEX_READ_ONLY_BLOCK = new ClusterBlock(5, "index read-only (api)", false, false,
			RestStatus.FORBIDDEN, ClusterBlockLevel.WRITE, ClusterBlockLevel.METADATA);

	/** The Constant INDEX_READ_BLOCK. */
	public static final ClusterBlock INDEX_READ_BLOCK = new ClusterBlock(7, "index read (api)", false, false,
			RestStatus.FORBIDDEN, ClusterBlockLevel.READ);

	/** The Constant INDEX_WRITE_BLOCK. */
	public static final ClusterBlock INDEX_WRITE_BLOCK = new ClusterBlock(8, "index write (api)", false, false,
			RestStatus.FORBIDDEN, ClusterBlockLevel.WRITE);

	/** The Constant INDEX_METADATA_BLOCK. */
	public static final ClusterBlock INDEX_METADATA_BLOCK = new ClusterBlock(9, "index metadata (api)", false, false,
			RestStatus.FORBIDDEN, ClusterBlockLevel.METADATA);

	/**
	 * Dynamic settings.
	 *
	 * @return the immutable set
	 */
	public static ImmutableSet<String> dynamicSettings() {
		return dynamicSettings;
	}

	/**
	 * Checks for dynamic setting.
	 *
	 * @param key the key
	 * @return true, if successful
	 */
	public static boolean hasDynamicSetting(String key) {
		for (String dynamicSetting : dynamicSettings) {
			if (Regex.simpleMatch(dynamicSetting, key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the dynamic settings.
	 *
	 * @param settings the settings
	 */
	public static synchronized void addDynamicSettings(String... settings) {
		HashSet<String> updatedSettings = new HashSet<String>(dynamicSettings);
		updatedSettings.addAll(Arrays.asList(settings));
		dynamicSettings = ImmutableSet.copyOf(updatedSettings);
	}

	/**
	 * The Enum State.
	 *
	 * @author l.xue.nong
	 */
	public static enum State {

		/** The open. */
		OPEN((byte) 0),

		/** The close. */
		CLOSE((byte) 1);

		/** The id. */
		private final byte id;

		/**
		 * Instantiates a new state.
		 *
		 * @param id the id
		 */
		State(byte id) {
			this.id = id;
		}

		/**
		 * Id.
		 *
		 * @return the byte
		 */
		public byte id() {
			return this.id;
		}

		/**
		 * From id.
		 *
		 * @param id the id
		 * @return the state
		 */
		public static State fromId(byte id) {
			if (id == 0) {
				return OPEN;
			} else if (id == 1) {
				return CLOSE;
			}
			throw new RebirthIllegalStateException("No state match for id [" + id + "]");
		}

		/**
		 * From string.
		 *
		 * @param state the state
		 * @return the state
		 */
		public static State fromString(String state) {
			if ("open".equals(state)) {
				return OPEN;
			} else if ("close".equals(state)) {
				return CLOSE;
			}
			throw new RebirthIllegalStateException("No state match for [" + state + "]");
		}
	}

	/** The Constant SETTING_NUMBER_OF_SHARDS. */
	public static final String SETTING_NUMBER_OF_SHARDS = "index.number_of_shards";

	/** The Constant SETTING_NUMBER_OF_REPLICAS. */
	public static final String SETTING_NUMBER_OF_REPLICAS = "index.number_of_replicas";

	/** The Constant SETTING_AUTO_EXPAND_REPLICAS. */
	public static final String SETTING_AUTO_EXPAND_REPLICAS = "index.auto_expand_replicas";

	/** The Constant SETTING_READ_ONLY. */
	public static final String SETTING_READ_ONLY = "index.blocks.read_only";

	/** The Constant SETTING_BLOCKS_READ. */
	public static final String SETTING_BLOCKS_READ = "index.blocks.read";

	/** The Constant SETTING_BLOCKS_WRITE. */
	public static final String SETTING_BLOCKS_WRITE = "index.blocks.write";

	/** The Constant SETTING_BLOCKS_METADATA. */
	public static final String SETTING_BLOCKS_METADATA = "index.blocks.metadata";

	/** The Constant SETTING_VERSION_CREATED. */
	public static final String SETTING_VERSION_CREATED = "index.version.created";

	/** The index. */
	private final String index;

	/** The version. */
	private final long version;

	/** The state. */
	private final State state;

	/** The aliases. */
	private final ImmutableMap<String, AliasMetaData> aliases;

	/** The settings. */
	private final Settings settings;

	/** The mappings. */
	private final ImmutableMap<String, MappingMetaData> mappings;

	/** The total number of shards. */
	private transient final int totalNumberOfShards;

	/** The include filters. */
	private final DiscoveryNodeFilters includeFilters;

	/** The exclude filters. */
	private final DiscoveryNodeFilters excludeFilters;

	/**
	 * Instantiates a new index meta data.
	 *
	 * @param index the index
	 * @param version the version
	 * @param state the state
	 * @param settings the settings
	 * @param mappings the mappings
	 * @param aliases the aliases
	 */
	private IndexMetaData(String index, long version, State state, Settings settings,
			ImmutableMap<String, MappingMetaData> mappings, ImmutableMap<String, AliasMetaData> aliases) {
		Preconditions.checkArgument(settings.getAsInt(SETTING_NUMBER_OF_SHARDS, -1) != -1,
				"must specify numberOfShards for index [" + index + "]");
		Preconditions.checkArgument(settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, -1) != -1,
				"must specify numberOfReplicas for index [" + index + "]");
		this.index = index;
		this.version = version;
		this.state = state;
		this.settings = settings;
		this.mappings = mappings;
		this.totalNumberOfShards = numberOfShards() * (numberOfReplicas() + 1);

		this.aliases = aliases;

		ImmutableMap<String, String> includeMap = settings.getByPrefix("index.routing.allocation.include.").getAsMap();
		if (includeMap.isEmpty()) {
			includeFilters = null;
		} else {
			includeFilters = DiscoveryNodeFilters.buildFromKeyValue(includeMap);
		}
		ImmutableMap<String, String> excludeMap = settings.getByPrefix("index.routing.allocation.exclude.").getAsMap();
		if (excludeMap.isEmpty()) {
			excludeFilters = null;
		} else {
			excludeFilters = DiscoveryNodeFilters.buildFromKeyValue(excludeMap);
		}
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		return index;
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return this.version;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public long getVersion() {
		return this.version;
	}

	/**
	 * State.
	 *
	 * @return the state
	 */
	public State state() {
		return this.state;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public State getState() {
		return state();
	}

	/**
	 * Number of shards.
	 *
	 * @return the int
	 */
	public int numberOfShards() {
		return settings.getAsInt(SETTING_NUMBER_OF_SHARDS, -1);
	}

	/**
	 * Gets the number of shards.
	 *
	 * @return the number of shards
	 */
	public int getNumberOfShards() {
		return numberOfShards();
	}

	/**
	 * Number of replicas.
	 *
	 * @return the int
	 */
	public int numberOfReplicas() {
		return settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, -1);
	}

	/**
	 * Gets the number of replicas.
	 *
	 * @return the number of replicas
	 */
	public int getNumberOfReplicas() {
		return numberOfReplicas();
	}

	/**
	 * Total number of shards.
	 *
	 * @return the int
	 */
	public int totalNumberOfShards() {
		return totalNumberOfShards;
	}

	/**
	 * Gets the total number of shards.
	 *
	 * @return the total number of shards
	 */
	public int getTotalNumberOfShards() {
		return totalNumberOfShards();
	}

	/**
	 * Settings.
	 *
	 * @return the settings
	 */
	public Settings settings() {
		return settings;
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
	 * Aliases.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, AliasMetaData> aliases() {
		return this.aliases;
	}

	/**
	 * Gets the aliases.
	 *
	 * @return the aliases
	 */
	public ImmutableMap<String, AliasMetaData> getAliases() {
		return aliases();
	}

	/**
	 * Mappings.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, MappingMetaData> mappings() {
		return mappings;
	}

	/**
	 * Gets the mappings.
	 *
	 * @return the mappings
	 */
	public ImmutableMap<String, MappingMetaData> getMappings() {
		return mappings();
	}

	/**
	 * Mapping.
	 *
	 * @param mappingType the mapping type
	 * @return the mapping meta data
	 */
	public MappingMetaData mapping(String mappingType) {
		return mappings.get(mappingType);
	}

	/**
	 * Include filters.
	 *
	 * @return the discovery node filters
	 */
	@Nullable
	public DiscoveryNodeFilters includeFilters() {
		return includeFilters;
	}

	/**
	 * Exclude filters.
	 *
	 * @return the discovery node filters
	 */
	@Nullable
	public DiscoveryNodeFilters excludeFilters() {
		return excludeFilters;
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

		IndexMetaData that = (IndexMetaData) o;

		if (!aliases.equals(that.aliases))
			return false;
		if (!index.equals(that.index))
			return false;
		if (!mappings.equals(that.mappings))
			return false;
		if (!settings.equals(that.settings))
			return false;
		if (state != that.state)
			return false;

		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = index.hashCode();
		result = 31 * result + state.hashCode();
		result = 31 * result + aliases.hashCode();
		result = 31 * result + settings.hashCode();
		result = 31 * result + mappings.hashCode();
		return result;
	}

	/**
	 * New index meta data builder.
	 *
	 * @param index the index
	 * @return the builder
	 */
	public static Builder newIndexMetaDataBuilder(String index) {
		return new Builder(index);
	}

	/**
	 * New index meta data builder.
	 *
	 * @param indexMetaData the index meta data
	 * @return the builder
	 */
	public static Builder newIndexMetaDataBuilder(IndexMetaData indexMetaData) {
		return new Builder(indexMetaData);
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The index. */
		private String index;

		/** The state. */
		private State state = State.OPEN;

		/** The version. */
		private long version = 1;

		/** The settings. */
		private Settings settings = ImmutableSettings.Builder.EMPTY_SETTINGS;

		/** The mappings. */
		private MapBuilder<String, MappingMetaData> mappings = MapBuilder.newMapBuilder();

		/** The aliases. */
		private MapBuilder<String, AliasMetaData> aliases = MapBuilder.newMapBuilder();

		/**
		 * Instantiates a new builder.
		 *
		 * @param index the index
		 */
		public Builder(String index) {
			this.index = index;
		}

		/**
		 * Instantiates a new builder.
		 *
		 * @param indexMetaData the index meta data
		 */
		public Builder(IndexMetaData indexMetaData) {
			this(indexMetaData.index());
			settings(indexMetaData.settings());
			mappings.putAll(indexMetaData.mappings);
			aliases.putAll(indexMetaData.aliases);
			this.state = indexMetaData.state;
			this.version = indexMetaData.version;
		}

		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return index;
		}

		/**
		 * Number of shards.
		 *
		 * @param numberOfShards the number of shards
		 * @return the builder
		 */
		public Builder numberOfShards(int numberOfShards) {
			settings = ImmutableSettings.settingsBuilder().put(settings).put(SETTING_NUMBER_OF_SHARDS, numberOfShards)
					.build();
			return this;
		}

		/**
		 * Number of shards.
		 *
		 * @return the int
		 */
		public int numberOfShards() {
			return settings.getAsInt(SETTING_NUMBER_OF_SHARDS, -1);
		}

		/**
		 * Number of replicas.
		 *
		 * @param numberOfReplicas the number of replicas
		 * @return the builder
		 */
		public Builder numberOfReplicas(int numberOfReplicas) {
			settings = ImmutableSettings.settingsBuilder().put(settings)
					.put(SETTING_NUMBER_OF_REPLICAS, numberOfReplicas).build();
			return this;
		}

		/**
		 * Number of replicas.
		 *
		 * @return the int
		 */
		public int numberOfReplicas() {
			return settings.getAsInt(SETTING_NUMBER_OF_REPLICAS, -1);
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
		 * @param type the type
		 * @param source the source
		 * @return the builder
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public Builder putMapping(String type, String source) throws IOException {
			XContentParser parser = XContentFactory.xContent(source).createParser(source);
			try {
				putMapping(new MappingMetaData(type, parser.mapOrdered()));
			} finally {
				parser.close();
			}
			return this;
		}

		/**
		 * Put mapping.
		 *
		 * @param mappingMd the mapping md
		 * @return the builder
		 */
		public Builder putMapping(MappingMetaData mappingMd) {
			mappings.put(mappingMd.type(), mappingMd);
			return this;
		}

		/**
		 * State.
		 *
		 * @param state the state
		 * @return the builder
		 */
		public Builder state(State state) {
			this.state = state;
			return this;
		}

		/**
		 * Put alias.
		 *
		 * @param aliasMetaData the alias meta data
		 * @return the builder
		 */
		public Builder putAlias(AliasMetaData aliasMetaData) {
			aliases.put(aliasMetaData.alias(), aliasMetaData);
			return this;
		}

		/**
		 * Put alias.
		 *
		 * @param aliasMetaData the alias meta data
		 * @return the builder
		 */
		public Builder putAlias(AliasMetaData.Builder aliasMetaData) {
			aliases.put(aliasMetaData.alias(), aliasMetaData.build());
			return this;
		}

		/**
		 * Remover alias.
		 *
		 * @param alias the alias
		 * @return the builder
		 */
		public Builder removerAlias(String alias) {
			aliases.remove(alias);
			return this;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		/**
		 * Version.
		 *
		 * @param version the version
		 * @return the builder
		 */
		public Builder version(long version) {
			this.version = version;
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the index meta data
		 */
		public IndexMetaData build() {
			MapBuilder<String, AliasMetaData> tmpAliases = aliases;
			Settings tmpSettings = settings;

			String[] legacyAliases = settings.getAsArray("index.aliases");
			if (legacyAliases.length > 0) {
				tmpAliases = MapBuilder.newMapBuilder();
				for (String alias : legacyAliases) {
					AliasMetaData aliasMd = AliasMetaData.newAliasMetaDataBuilder(alias).build();
					tmpAliases.put(alias, aliasMd);
				}
				tmpAliases.putAll(aliases.immutableMap());

				tmpSettings = ImmutableSettings.settingsBuilder().put(settings).putArray("index.aliases").build();
			}

			return new IndexMetaData(index, version, state, tmpSettings, mappings.immutableMap(),
					tmpAliases.immutableMap());
		}

		/**
		 * To x content.
		 *
		 * @param indexMetaData the index meta data
		 * @param builder the builder
		 * @param params the params
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void toXContent(IndexMetaData indexMetaData, XContentBuilder builder, ToXContent.Params params)
				throws IOException {
			builder.startObject(indexMetaData.index(), XContentBuilder.FieldCaseConversion.NONE);

			builder.field("version", indexMetaData.version());
			builder.field("state", indexMetaData.state().toString().toLowerCase());

			boolean binary = params.paramAsBoolean("binary", false);

			builder.startObject("settings");
			for (Map.Entry<String, String> entry : indexMetaData.settings().getAsMap().entrySet()) {
				builder.field(entry.getKey(), entry.getValue());
			}
			builder.endObject();

			builder.startArray("mappings");
			for (Map.Entry<String, MappingMetaData> entry : indexMetaData.mappings().entrySet()) {
				if (binary) {
					builder.value(entry.getValue().source().compressed());
				} else {
					byte[] data = entry.getValue().source().uncompressed();
					XContentParser parser = XContentFactory.xContent(data).createParser(data);
					Map<String, Object> mapping = parser.mapOrdered();
					parser.close();
					builder.map(mapping);
				}
			}
			builder.endArray();

			builder.startObject("aliases");
			for (AliasMetaData alias : indexMetaData.aliases().values()) {
				AliasMetaData.Builder.toXContent(alias, builder, params);
			}
			builder.endObject();

			builder.endObject();
		}

		/**
		 * From x content.
		 *
		 * @param parser the parser
		 * @return the index meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexMetaData fromXContent(XContentParser parser) throws IOException {
			if (parser.currentToken() == XContentParser.Token.START_OBJECT) {
				parser.nextToken();
			}
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
								builder.putMapping(new MappingMetaData(mappingType, mappingSource));
							}
						}
					} else if ("aliases".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							builder.putAlias(AliasMetaData.Builder.fromXContent(parser));
						}
					}
				} else if (token == XContentParser.Token.START_ARRAY) {
					if ("mappings".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
							if (token == XContentParser.Token.VALUE_EMBEDDED_OBJECT) {
								builder.putMapping(new MappingMetaData(new CompressedString(parser.binaryValue())));
							} else {
								Map<String, Object> mapping = parser.mapOrdered();
								if (mapping.size() == 1) {
									String mappingType = mapping.keySet().iterator().next();
									builder.putMapping(new MappingMetaData(mappingType, mapping));
								}
							}
						}
					}
				} else if (token.isValue()) {
					if ("state".equals(currentFieldName)) {
						builder.state(State.fromString(parser.text()));
					} else if ("version".equals(currentFieldName)) {
						builder.version(parser.longValue());
					}
				}
			}
			return builder.build();
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the index meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static IndexMetaData readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder(in.readUTF());
			builder.version(in.readLong());
			builder.state(State.fromId(in.readByte()));
			builder.settings(ImmutableSettings.readSettingsFromStream(in));
			int mappingsSize = in.readVInt();
			for (int i = 0; i < mappingsSize; i++) {
				MappingMetaData mappingMd = MappingMetaData.readFrom(in);
				builder.putMapping(mappingMd);
			}
			int aliasesSize = in.readVInt();
			for (int i = 0; i < aliasesSize; i++) {
				AliasMetaData aliasMd = AliasMetaData.Builder.readFrom(in);
				builder.putAlias(aliasMd);
			}
			return builder.build();
		}

		/**
		 * Write to.
		 *
		 * @param indexMetaData the index meta data
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(IndexMetaData indexMetaData, StreamOutput out) throws IOException {
			out.writeUTF(indexMetaData.index());
			out.writeLong(indexMetaData.version());
			out.writeByte(indexMetaData.state().id());
			ImmutableSettings.writeSettingsToStream(indexMetaData.settings(), out);
			out.writeVInt(indexMetaData.mappings().size());
			for (MappingMetaData mappingMd : indexMetaData.mappings().values()) {
				MappingMetaData.writeTo(mappingMd, out);
			}
			out.writeVInt(indexMetaData.aliases().size());
			for (AliasMetaData aliasMd : indexMetaData.aliases().values()) {
				AliasMetaData.Builder.writeTo(aliasMd, out);
			}
		}
	}
}
