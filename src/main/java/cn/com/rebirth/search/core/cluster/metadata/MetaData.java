/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core MetaData.java 2012-7-6 14:28:56 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.metadata;

import static cn.com.rebirth.commons.collect.MapBuilder.newMapBuilder;
import static cn.com.rebirth.commons.settings.ImmutableSettings.readSettingsFromStream;
import static cn.com.rebirth.commons.settings.ImmutableSettings.settingsBuilder;
import static cn.com.rebirth.commons.settings.ImmutableSettings.writeSettingsToStream;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import gnu.trove.set.hash.THashSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.exception.RebirthIllegalArgumentException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.regex.Regex;
import cn.com.rebirth.commons.settings.ImmutableSettings;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.commons.xcontent.ToXContent;
import cn.com.rebirth.commons.xcontent.XContentBuilder;
import cn.com.rebirth.commons.xcontent.XContentFactory;
import cn.com.rebirth.commons.xcontent.XContentParser;
import cn.com.rebirth.commons.xcontent.XContentType;
import cn.com.rebirth.search.core.cluster.block.ClusterBlock;
import cn.com.rebirth.search.core.cluster.block.ClusterBlockLevel;
import cn.com.rebirth.search.core.index.Index;
import cn.com.rebirth.search.core.indices.IndexMissingException;
import cn.com.rebirth.search.core.rest.RestStatus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

/**
 * The Class MetaData.
 *
 * @author l.xue.nong
 */
public class MetaData implements Iterable<IndexMetaData> {

	/** The Constant SETTING_READ_ONLY. */
	public static final String SETTING_READ_ONLY = "cluster.blocks.read_only";

	/** The Constant CLUSTER_READ_ONLY_BLOCK. */
	public static final ClusterBlock CLUSTER_READ_ONLY_BLOCK = new ClusterBlock(6, "cluster read-only (api)", false,
			false, RestStatus.FORBIDDEN, ClusterBlockLevel.WRITE, ClusterBlockLevel.METADATA);

	/** The dynamic settings. */
	private static ImmutableSet<String> dynamicSettings = ImmutableSet.<String> builder().add(SETTING_READ_ONLY)
			.build();

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

	/** The Constant EMPTY_META_DATA. */
	public static final MetaData EMPTY_META_DATA = newMetaDataBuilder().build();

	/** The version. */
	private final long version;

	/** The transient settings. */
	private final Settings transientSettings;

	/** The persistent settings. */
	private final Settings persistentSettings;

	/** The settings. */
	private final Settings settings;

	/** The indices. */
	private final ImmutableMap<String, IndexMetaData> indices;

	/** The templates. */
	private final ImmutableMap<String, IndexTemplateMetaData> templates;

	/** The total number of shards. */
	private final transient int totalNumberOfShards;

	/** The all indices. */
	private final String[] allIndices;

	/** The all open indices. */
	private final String[] allOpenIndices;

	/** The aliases. */
	private final ImmutableMap<String, ImmutableMap<String, AliasMetaData>> aliases;

	/** The alias to index to search routing map. */
	private final ImmutableMap<String, ImmutableMap<String, ImmutableSet<String>>> aliasToIndexToSearchRoutingMap;

	/** The index to alias filtering required map. */
	private final ImmutableMap<String, ImmutableMap<String, Boolean>> indexToAliasFilteringRequiredMap;

	/** The alias and index to index map. */
	private final ImmutableMap<String, String[]> aliasAndIndexToIndexMap;

	/**
	 * Instantiates a new meta data.
	 *
	 * @param version the version
	 * @param transientSettings the transient settings
	 * @param persistentSettings the persistent settings
	 * @param indices the indices
	 * @param templates the templates
	 */
	MetaData(long version, Settings transientSettings, Settings persistentSettings,
			ImmutableMap<String, IndexMetaData> indices, ImmutableMap<String, IndexTemplateMetaData> templates) {
		this.version = version;
		this.transientSettings = transientSettings;
		this.persistentSettings = persistentSettings;
		this.settings = ImmutableSettings.settingsBuilder().put(persistentSettings).put(transientSettings).build();
		this.indices = ImmutableMap.copyOf(indices);
		this.templates = templates;
		int totalNumberOfShards = 0;
		for (IndexMetaData indexMetaData : indices.values()) {
			totalNumberOfShards += indexMetaData.totalNumberOfShards();
		}
		this.totalNumberOfShards = totalNumberOfShards;

		List<String> allIndicesLst = Lists.newArrayList();
		for (IndexMetaData indexMetaData : indices.values()) {
			allIndicesLst.add(indexMetaData.index());
		}
		allIndices = allIndicesLst.toArray(new String[allIndicesLst.size()]);

		List<String> allOpenIndices = Lists.newArrayList();
		for (IndexMetaData indexMetaData : indices.values()) {
			if (indexMetaData.state() == IndexMetaData.State.OPEN) {
				allOpenIndices.add(indexMetaData.index());
			}
		}
		this.allOpenIndices = allOpenIndices.toArray(new String[allOpenIndices.size()]);

		MapBuilder<String, MapBuilder<String, AliasMetaData>> tmpAliasesMap = newMapBuilder();
		for (IndexMetaData indexMetaData : indices.values()) {
			String index = indexMetaData.index();
			for (AliasMetaData aliasMd : indexMetaData.aliases().values()) {
				MapBuilder<String, AliasMetaData> indexAliasMap = tmpAliasesMap.get(aliasMd.alias());
				if (indexAliasMap == null) {
					indexAliasMap = newMapBuilder();
					tmpAliasesMap.put(aliasMd.alias(), indexAliasMap);
				}
				indexAliasMap.put(index, aliasMd);
			}
		}
		MapBuilder<String, ImmutableMap<String, AliasMetaData>> aliases = newMapBuilder();
		for (Map.Entry<String, MapBuilder<String, AliasMetaData>> alias : tmpAliasesMap.map().entrySet()) {
			aliases.put(alias.getKey(), alias.getValue().immutableMap());
		}
		this.aliases = aliases.immutableMap();

		MapBuilder<String, MapBuilder<String, ImmutableSet<String>>> tmpAliasToIndexToSearchRoutingMap = newMapBuilder();
		for (IndexMetaData indexMetaData : indices.values()) {
			for (AliasMetaData aliasMd : indexMetaData.aliases().values()) {
				MapBuilder<String, ImmutableSet<String>> indexToSearchRoutingMap = tmpAliasToIndexToSearchRoutingMap
						.get(aliasMd.alias());
				if (indexToSearchRoutingMap == null) {
					indexToSearchRoutingMap = newMapBuilder();
					tmpAliasToIndexToSearchRoutingMap.put(aliasMd.alias(), indexToSearchRoutingMap);
				}
				if (aliasMd.searchRouting() != null) {
					indexToSearchRoutingMap.put(indexMetaData.index(),
							ImmutableSet.copyOf(Strings.splitStringByCommaToSet(aliasMd.searchRouting())));
				} else {
					indexToSearchRoutingMap.put(indexMetaData.index(), ImmutableSet.<String> of());
				}
			}
		}
		MapBuilder<String, ImmutableMap<String, ImmutableSet<String>>> aliasToIndexToSearchRoutingMap = newMapBuilder();
		for (Map.Entry<String, MapBuilder<String, ImmutableSet<String>>> alias : tmpAliasToIndexToSearchRoutingMap
				.map().entrySet()) {
			aliasToIndexToSearchRoutingMap.put(alias.getKey(), alias.getValue().immutableMap());
		}
		this.aliasToIndexToSearchRoutingMap = aliasToIndexToSearchRoutingMap.immutableMap();

		MapBuilder<String, ImmutableMap<String, Boolean>> filteringRequiredMap = newMapBuilder();
		for (IndexMetaData indexMetaData : indices.values()) {
			MapBuilder<String, Boolean> indexFilteringRequiredMap = newMapBuilder();

			indexFilteringRequiredMap.put(indexMetaData.index(), false);
			for (AliasMetaData aliasMetaData : indexMetaData.aliases().values()) {
				if (aliasMetaData.filter() != null) {
					indexFilteringRequiredMap.put(aliasMetaData.alias(), true);
				} else {
					indexFilteringRequiredMap.put(aliasMetaData.alias(), false);
				}
			}
			filteringRequiredMap.put(indexMetaData.index(), indexFilteringRequiredMap.immutableMap());
		}
		indexToAliasFilteringRequiredMap = filteringRequiredMap.immutableMap();

		MapBuilder<String, Set<String>> tmpAliasAndIndexToIndexBuilder = newMapBuilder();
		for (IndexMetaData indexMetaData : indices.values()) {
			Set<String> lst = tmpAliasAndIndexToIndexBuilder.get(indexMetaData.index());
			if (lst == null) {
				lst = newHashSet();
				tmpAliasAndIndexToIndexBuilder.put(indexMetaData.index(), lst);
			}
			lst.add(indexMetaData.index());

			for (String alias : indexMetaData.aliases().keySet()) {
				lst = tmpAliasAndIndexToIndexBuilder.get(alias);
				if (lst == null) {
					lst = newHashSet();
					tmpAliasAndIndexToIndexBuilder.put(alias, lst);
				}
				lst.add(indexMetaData.index());
			}
		}

		MapBuilder<String, String[]> aliasAndIndexToIndexBuilder = newMapBuilder();
		for (Map.Entry<String, Set<String>> entry : tmpAliasAndIndexToIndexBuilder.map().entrySet()) {
			aliasAndIndexToIndexBuilder.put(entry.getKey(),
					entry.getValue().toArray(new String[entry.getValue().size()]));
		}
		this.aliasAndIndexToIndexMap = aliasAndIndexToIndexBuilder.immutableMap();
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
	 * Settings.
	 *
	 * @return the settings
	 */
	public Settings settings() {
		return this.settings;
	}

	/**
	 * Transient settings.
	 *
	 * @return the settings
	 */
	public Settings transientSettings() {
		return this.transientSettings;
	}

	/**
	 * Persistent settings.
	 *
	 * @return the settings
	 */
	public Settings persistentSettings() {
		return this.persistentSettings;
	}

	/**
	 * Aliases.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, ImmutableMap<String, AliasMetaData>> aliases() {
		return this.aliases;
	}

	/**
	 * Gets the aliases.
	 *
	 * @return the aliases
	 */
	public ImmutableMap<String, ImmutableMap<String, AliasMetaData>> getAliases() {
		return aliases();
	}

	/**
	 * Concrete all indices.
	 *
	 * @return the string[]
	 */
	public String[] concreteAllIndices() {
		return allIndices;
	}

	/**
	 * Gets the concrete all indices.
	 *
	 * @return the concrete all indices
	 */
	public String[] getConcreteAllIndices() {
		return concreteAllIndices();
	}

	/**
	 * Concrete all open indices.
	 *
	 * @return the string[]
	 */
	public String[] concreteAllOpenIndices() {
		return allOpenIndices;
	}

	/**
	 * Gets the concrete all open indices.
	 *
	 * @return the concrete all open indices
	 */
	public String[] getConcreteAllOpenIndices() {
		return allOpenIndices;
	}

	/**
	 * Concrete indices.
	 *
	 * @param indices the indices
	 * @return the string[]
	 * @throws IndexMissingException the index missing exception
	 */
	public String[] concreteIndices(String[] indices) throws IndexMissingException {
		return concreteIndices(indices, false, false);
	}

	/**
	 * Concrete indices ignore missing.
	 *
	 * @param indices the indices
	 * @return the string[]
	 */
	public String[] concreteIndicesIgnoreMissing(String[] indices) {
		return concreteIndices(indices, true, false);
	}

	/**
	 * Resolve index routing.
	 *
	 * @param routing the routing
	 * @param aliasOrIndex the alias or index
	 * @return the string
	 */
	public String resolveIndexRouting(@Nullable String routing, String aliasOrIndex) {

		ImmutableMap<String, AliasMetaData> indexAliases = aliases.get(aliasOrIndex);
		if (indexAliases == null || indexAliases.isEmpty()) {
			return routing;
		}
		if (indexAliases.size() > 1) {
			throw new RebirthIllegalArgumentException("Alias [" + aliasOrIndex
					+ "] has more than one index associated with it [" + indexAliases.keySet()
					+ "], can't execute a single index op");
		}
		AliasMetaData aliasMd = indexAliases.values().iterator().next();
		if (aliasMd.indexRouting() != null) {
			if (routing != null) {
				if (!routing.equals(aliasMd.indexRouting())) {
					throw new RebirthIllegalArgumentException("Alias [" + aliasOrIndex
							+ "] has index routing associated with it [" + aliasMd.indexRouting()
							+ "], and was provided with routing value [" + routing + "], rejecting operation");
				}
			}
			routing = aliasMd.indexRouting();
		}
		if (routing != null) {
			if (routing.indexOf(',') != -1) {
				throw new RebirthIllegalArgumentException("index/alias [" + aliasOrIndex
						+ "] provided with routing value [" + routing
						+ "] that resolved to several routing values, rejecting operation");
			}
		}
		return routing;
	}

	/**
	 * Resolve search routing all indices.
	 *
	 * @param routing the routing
	 * @return the map
	 */
	private Map<String, Set<String>> resolveSearchRoutingAllIndices(String routing) {
		if (routing != null) {
			Set<String> r = Strings.splitStringByCommaToSet(routing);
			Map<String, Set<String>> routings = newHashMap();
			String[] concreteIndices = concreteAllIndices();
			for (String index : concreteIndices) {
				routings.put(index, r);
			}
			return routings;
		}
		return null;
	}

	/**
	 * Resolve search routing.
	 *
	 * @param routing the routing
	 * @param aliasOrIndex the alias or index
	 * @return the map
	 */
	public Map<String, Set<String>> resolveSearchRouting(@Nullable String routing, String aliasOrIndex) {
		Map<String, Set<String>> routings = null;
		Set<String> paramRouting = null;
		if (routing != null) {
			paramRouting = Strings.splitStringByCommaToSet(routing);
		}

		ImmutableMap<String, ImmutableSet<String>> indexToRoutingMap = aliasToIndexToSearchRoutingMap.get(aliasOrIndex);
		if (indexToRoutingMap != null && !indexToRoutingMap.isEmpty()) {

			for (Map.Entry<String, ImmutableSet<String>> indexRouting : indexToRoutingMap.entrySet()) {
				if (!indexRouting.getValue().isEmpty()) {

					Set<String> r = new THashSet<String>(indexRouting.getValue());
					if (paramRouting != null) {
						r.retainAll(paramRouting);
					}
					if (!r.isEmpty()) {
						if (routings == null) {
							routings = newHashMap();
						}
						routings.put(indexRouting.getKey(), r);
					}
				} else {

					if (paramRouting != null) {
						Set<String> r = new THashSet<String>(paramRouting);
						if (routings == null) {
							routings = newHashMap();
						}
						routings.put(indexRouting.getKey(), r);
					}
				}
			}
		} else {

			if (paramRouting != null) {
				routings = ImmutableMap.of(aliasOrIndex, paramRouting);
			}
		}
		return routings;
	}

	/**
	 * Resolve search routing.
	 *
	 * @param routing the routing
	 * @param aliasesOrIndices the aliases or indices
	 * @return the map
	 */
	public Map<String, Set<String>> resolveSearchRouting(@Nullable String routing, String[] aliasesOrIndices) {
		if (aliasesOrIndices == null || aliasesOrIndices.length == 0) {
			return resolveSearchRoutingAllIndices(routing);
		}

		if (aliasesOrIndices.length == 1) {
			if (aliasesOrIndices[0].equals("_all")) {
				return resolveSearchRoutingAllIndices(routing);
			} else {
				return resolveSearchRouting(routing, aliasesOrIndices[0]);
			}
		}

		Map<String, Set<String>> routings = null;
		Set<String> paramRouting = null;

		Set<String> norouting = newHashSet();
		if (routing != null) {
			paramRouting = Strings.splitStringByCommaToSet(routing);
		}

		for (String aliasOrIndex : aliasesOrIndices) {
			ImmutableMap<String, ImmutableSet<String>> indexToRoutingMap = aliasToIndexToSearchRoutingMap
					.get(aliasOrIndex);
			if (indexToRoutingMap != null && !indexToRoutingMap.isEmpty()) {
				for (Map.Entry<String, ImmutableSet<String>> indexRouting : indexToRoutingMap.entrySet()) {
					if (!norouting.contains(indexRouting.getKey())) {
						if (!indexRouting.getValue().isEmpty()) {

							if (routings == null) {
								routings = newHashMap();
							}
							Set<String> r = routings.get(indexRouting.getKey());
							if (r == null) {
								r = new THashSet<String>();
								routings.put(indexRouting.getKey(), r);
							}
							r.addAll(indexRouting.getValue());
							if (paramRouting != null) {
								r.retainAll(paramRouting);
							}
							if (r.isEmpty()) {
								routings.remove(indexRouting.getKey());
							}
						} else {

							if (!norouting.contains(indexRouting.getKey())) {
								norouting.add(indexRouting.getKey());
								if (paramRouting != null) {
									Set<String> r = new THashSet<String>(paramRouting);
									if (routings == null) {
										routings = newHashMap();
									}
									routings.put(indexRouting.getKey(), r);
								} else {
									if (routings != null) {
										routings.remove(indexRouting.getKey());
									}
								}
							}
						}
					}
				}
			} else {

				if (!norouting.contains(aliasOrIndex)) {
					norouting.add(aliasOrIndex);
					if (paramRouting != null) {
						Set<String> r = new THashSet<String>(paramRouting);
						if (routings == null) {
							routings = newHashMap();
						}
						routings.put(aliasOrIndex, r);
					} else {
						if (routings != null) {
							routings.remove(aliasOrIndex);
						}
					}
				}
			}

		}
		if (routings == null || routings.isEmpty()) {
			return null;
		}
		return routings;
	}

	/**
	 * Concrete indices.
	 *
	 * @param indices the indices
	 * @param ignoreMissing the ignore missing
	 * @param allOnlyOpen the all only open
	 * @return the string[]
	 * @throws IndexMissingException the index missing exception
	 */
	public String[] concreteIndices(String[] indices, boolean ignoreMissing, boolean allOnlyOpen)
			throws IndexMissingException {
		if (indices == null || indices.length == 0) {
			return allOnlyOpen ? concreteAllOpenIndices() : concreteAllIndices();
		}

		if (indices.length == 1) {
			String index = indices[0];
			if (index.length() == 0) {
				return allOnlyOpen ? concreteAllOpenIndices() : concreteAllIndices();
			}
			if (index.equals("_all")) {
				return allOnlyOpen ? concreteAllOpenIndices() : concreteAllIndices();
			}

			if (this.indices.containsKey(index)) {
				return indices;
			}
			String[] actualLst = aliasAndIndexToIndexMap.get(index);
			if (actualLst == null) {
				if (!ignoreMissing) {
					throw new IndexMissingException(new Index(index));
				} else {
					return Strings.EMPTY_ARRAY;
				}
			} else {
				return actualLst;
			}
		}

		boolean possiblyAliased = false;
		for (String index : indices) {
			if (!this.indices.containsKey(index)) {
				possiblyAliased = true;
				break;
			}
		}
		if (!possiblyAliased) {
			return indices;
		}

		Set<String> actualIndices = Sets.newHashSetWithExpectedSize(indices.length);
		for (String index : indices) {
			String[] actualLst = aliasAndIndexToIndexMap.get(index);
			if (actualLst == null) {
				if (!ignoreMissing) {
					throw new IndexMissingException(new Index(index));
				}
			} else {
				for (String x : actualLst) {
					actualIndices.add(x);
				}
			}
		}
		return actualIndices.toArray(new String[actualIndices.size()]);
	}

	/**
	 * Concrete index.
	 *
	 * @param index the index
	 * @return the string
	 * @throws IndexMissingException the index missing exception
	 * @throws RebirthIllegalArgumentException the rebirth illegal argument exception
	 */
	public String concreteIndex(String index) throws IndexMissingException, RebirthIllegalArgumentException {

		if (indices.containsKey(index)) {
			return index;
		}

		String[] lst = aliasAndIndexToIndexMap.get(index);
		if (lst == null) {
			throw new IndexMissingException(new Index(index));
		}
		if (lst.length > 1) {
			throw new RebirthIllegalArgumentException("Alias [" + index
					+ "] has more than one indices associated with it [" + Arrays.toString(lst)
					+ "], can't execute a single index op");
		}
		return lst[0];
	}

	/**
	 * Checks for index.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public boolean hasIndex(String index) {
		return indices.containsKey(index);
	}

	/**
	 * Checks for concrete index.
	 *
	 * @param index the index
	 * @return true, if successful
	 */
	public boolean hasConcreteIndex(String index) {
		return aliasAndIndexToIndexMap.containsKey(index);
	}

	/**
	 * Index.
	 *
	 * @param index the index
	 * @return the index meta data
	 */
	public IndexMetaData index(String index) {
		return indices.get(index);
	}

	/**
	 * Indices.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, IndexMetaData> indices() {
		return this.indices;
	}

	/**
	 * Gets the indices.
	 *
	 * @return the indices
	 */
	public ImmutableMap<String, IndexMetaData> getIndices() {
		return indices();
	}

	/**
	 * Templates.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, IndexTemplateMetaData> templates() {
		return this.templates;
	}

	/**
	 * Gets the templates.
	 *
	 * @return the templates
	 */
	public ImmutableMap<String, IndexTemplateMetaData> getTemplates() {
		return this.templates;
	}

	/**
	 * Total number of shards.
	 *
	 * @return the int
	 */
	public int totalNumberOfShards() {
		return this.totalNumberOfShards;
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
	 * Filtering aliases.
	 *
	 * @param index the index
	 * @param indices the indices
	 * @return the string[]
	 */
	public String[] filteringAliases(String index, String... indices) {
		if (indices == null || indices.length == 0) {
			return null;
		}

		if (indices.length == 1) {
			String alias = indices[0];

			if (alias.equals("_all")) {
				return null;
			}
			ImmutableMap<String, Boolean> aliasToFilteringRequiredMap = indexToAliasFilteringRequiredMap.get(index);
			if (aliasToFilteringRequiredMap == null) {

				throw new IndexMissingException(new Index(index));
			}
			Boolean filteringRequired = aliasToFilteringRequiredMap.get(alias);
			if (filteringRequired == null || !filteringRequired) {
				return null;
			}
			return new String[] { alias };
		}
		List<String> filteringAliases = null;
		for (String alias : indices) {
			ImmutableMap<String, Boolean> aliasToFilteringRequiredMap = indexToAliasFilteringRequiredMap.get(index);
			if (aliasToFilteringRequiredMap == null) {

				throw new IndexMissingException(new Index(index));
			}
			Boolean filteringRequired = aliasToFilteringRequiredMap.get(alias);

			if (filteringRequired != null) {
				if (filteringRequired) {

					if (filteringAliases == null) {
						filteringAliases = newArrayList();
					}
					filteringAliases.add(alias);
				} else {

					return null;
				}
			}
		}
		if (filteringAliases == null) {
			return null;
		}
		return filteringAliases.toArray(new String[filteringAliases.size()]);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public UnmodifiableIterator<IndexMetaData> iterator() {
		return indices.values().iterator();
	}

	/**
	 * Checks if is global state equals.
	 *
	 * @param metaData1 the meta data1
	 * @param metaData2 the meta data2
	 * @return true, if is global state equals
	 */
	public static boolean isGlobalStateEquals(MetaData metaData1, MetaData metaData2) {
		if (!metaData1.persistentSettings.equals(metaData2.persistentSettings))
			return false;
		if (!metaData1.templates.equals(metaData2.templates()))
			return false;
		return true;
	}

	/**
	 * Builder.
	 *
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * New meta data builder.
	 *
	 * @return the builder
	 */
	public static Builder newMetaDataBuilder() {
		return new Builder();
	}

	/**
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The version. */
		private long version;

		/** The transient settings. */
		private Settings transientSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;

		/** The persistent settings. */
		private Settings persistentSettings = ImmutableSettings.Builder.EMPTY_SETTINGS;

		/** The indices. */
		private MapBuilder<String, IndexMetaData> indices = newMapBuilder();

		/** The templates. */
		private MapBuilder<String, IndexTemplateMetaData> templates = newMapBuilder();

		/**
		 * Meta data.
		 *
		 * @param metaData the meta data
		 * @return the builder
		 */
		public Builder metaData(MetaData metaData) {
			this.transientSettings = metaData.transientSettings;
			this.persistentSettings = metaData.persistentSettings;
			this.version = metaData.version;
			this.indices.putAll(metaData.indices);
			this.templates.putAll(metaData.templates);
			return this;
		}

		/**
		 * Put.
		 *
		 * @param indexMetaDataBuilder the index meta data builder
		 * @return the builder
		 */
		public Builder put(IndexMetaData.Builder indexMetaDataBuilder) {

			indexMetaDataBuilder.version(indexMetaDataBuilder.version() + 1);
			IndexMetaData indexMetaData = indexMetaDataBuilder.build();
			indices.put(indexMetaData.index(), indexMetaData);
			return this;
		}

		/**
		 * Put.
		 *
		 * @param indexMetaData the index meta data
		 * @param incrementVersion the increment version
		 * @return the builder
		 */
		public Builder put(IndexMetaData indexMetaData, boolean incrementVersion) {
			if (indices.get(indexMetaData.index()) == indexMetaData) {
				return this;
			}

			if (incrementVersion) {
				indexMetaData = IndexMetaData.newIndexMetaDataBuilder(indexMetaData)
						.version(indexMetaData.version() + 1).build();
			}
			indices.put(indexMetaData.index(), indexMetaData);
			return this;
		}

		/**
		 * Gets the.
		 *
		 * @param index the index
		 * @return the index meta data
		 */
		public IndexMetaData get(String index) {
			return indices.get(index);
		}

		/**
		 * Removes the.
		 *
		 * @param index the index
		 * @return the builder
		 */
		public Builder remove(String index) {
			indices.remove(index);
			return this;
		}

		/**
		 * Removes the all indices.
		 *
		 * @return the builder
		 */
		public Builder removeAllIndices() {
			indices.clear();
			return this;
		}

		/**
		 * Put.
		 *
		 * @param template the template
		 * @return the builder
		 */
		public Builder put(IndexTemplateMetaData.Builder template) {
			return put(template.build());
		}

		/**
		 * Put.
		 *
		 * @param template the template
		 * @return the builder
		 */
		public Builder put(IndexTemplateMetaData template) {
			templates.put(template.name(), template);
			return this;
		}

		/**
		 * Removes the template.
		 *
		 * @param templateName the template name
		 * @return the builder
		 */
		public Builder removeTemplate(String templateName) {
			templates.remove(templateName);
			return this;
		}

		/**
		 * Update settings.
		 *
		 * @param settings the settings
		 * @param indices the indices
		 * @return the builder
		 */
		public Builder updateSettings(Settings settings, String... indices) {
			if (indices == null || indices.length == 0) {
				indices = this.indices.map().keySet().toArray(new String[this.indices.map().keySet().size()]);
			}
			for (String index : indices) {
				IndexMetaData indexMetaData = this.indices.get(index);
				if (indexMetaData == null) {
					throw new IndexMissingException(new Index(index));
				}
				put(IndexMetaData.newIndexMetaDataBuilder(indexMetaData).settings(
						settingsBuilder().put(indexMetaData.settings()).put(settings)));
			}
			return this;
		}

		/**
		 * Update number of replicas.
		 *
		 * @param numberOfReplicas the number of replicas
		 * @param indices the indices
		 * @return the builder
		 */
		public Builder updateNumberOfReplicas(int numberOfReplicas, String... indices) {
			if (indices == null || indices.length == 0) {
				indices = this.indices.map().keySet().toArray(new String[this.indices.map().keySet().size()]);
			}
			for (String index : indices) {
				IndexMetaData indexMetaData = this.indices.get(index);
				if (indexMetaData == null) {
					throw new IndexMissingException(new Index(index));
				}
				put(IndexMetaData.newIndexMetaDataBuilder(indexMetaData).numberOfReplicas(numberOfReplicas));
			}
			return this;
		}

		/**
		 * Transient settings.
		 *
		 * @return the settings
		 */
		public Settings transientSettings() {
			return this.transientSettings;
		}

		/**
		 * Transient settings.
		 *
		 * @param settings the settings
		 * @return the builder
		 */
		public Builder transientSettings(Settings settings) {
			this.transientSettings = settings;
			return this;
		}

		/**
		 * Persistent settings.
		 *
		 * @return the settings
		 */
		public Settings persistentSettings() {
			return this.persistentSettings;
		}

		/**
		 * Persistent settings.
		 *
		 * @param settings the settings
		 * @return the builder
		 */
		public Builder persistentSettings(Settings settings) {
			this.persistentSettings = settings;
			return this;
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
		 * @return the meta data
		 */
		public MetaData build() {
			return new MetaData(version, transientSettings, persistentSettings, indices.immutableMap(),
					templates.immutableMap());
		}

		/**
		 * To x content.
		 *
		 * @param metaData the meta data
		 * @return the string
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static String toXContent(MetaData metaData) throws IOException {
			XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
			builder.startObject();
			toXContent(metaData, builder, ToXContent.EMPTY_PARAMS);
			builder.endObject();
			return builder.string();
		}

		/**
		 * To x content.
		 *
		 * @param metaData the meta data
		 * @param builder the builder
		 * @param params the params
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void toXContent(MetaData metaData, XContentBuilder builder, ToXContent.Params params)
				throws IOException {
			builder.startObject("meta-data");

			builder.field("version", metaData.version());

			if (!metaData.persistentSettings().getAsMap().isEmpty()) {
				builder.startObject("settings");
				for (Map.Entry<String, String> entry : metaData.persistentSettings().getAsMap().entrySet()) {
					builder.field(entry.getKey(), entry.getValue());
				}
				builder.endObject();
			}

			builder.startObject("templates");
			for (IndexTemplateMetaData template : metaData.templates().values()) {
				IndexTemplateMetaData.Builder.toXContent(template, builder, params);
			}
			builder.endObject();

			if (!metaData.indices().isEmpty()) {
				builder.startObject("indices");
				for (IndexMetaData indexMetaData : metaData) {
					IndexMetaData.Builder.toXContent(indexMetaData, builder, params);
				}
				builder.endObject();
			}

			builder.endObject();
		}

		/**
		 * From x content.
		 *
		 * @param parser the parser
		 * @return the meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static MetaData fromXContent(XContentParser parser) throws IOException {
			Builder builder = new Builder();

			XContentParser.Token token = parser.currentToken();
			String currentFieldName = parser.currentName();
			if (!"meta-data".equals(currentFieldName)) {
				token = parser.nextToken();
				currentFieldName = parser.currentName();
				if (token == null) {

					return builder.build();
				}
			}

			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					if ("settings".equals(currentFieldName)) {
						ImmutableSettings.Builder settingsBuilder = settingsBuilder();
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							String key = parser.currentName();
							token = parser.nextToken();
							String value = parser.text();
							settingsBuilder.put(key, value);
						}
						builder.persistentSettings(settingsBuilder.build());
					} else if ("indices".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							builder.put(IndexMetaData.Builder.fromXContent(parser), false);
						}
					} else if ("templates".equals(currentFieldName)) {
						while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
							builder.put(IndexTemplateMetaData.Builder.fromXContent(parser));
						}
					}
				} else if (token.isValue()) {
					if ("version".equals(currentFieldName)) {
						builder.version = parser.longValue();
					}
				}
			}
			return builder.build();
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the meta data
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static MetaData readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder();
			builder.version = in.readLong();
			builder.transientSettings(readSettingsFromStream(in));
			builder.persistentSettings(readSettingsFromStream(in));
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.put(IndexMetaData.Builder.readFrom(in), false);
			}
			size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.put(IndexTemplateMetaData.Builder.readFrom(in));
			}
			return builder.build();
		}

		/**
		 * Write to.
		 *
		 * @param metaData the meta data
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(MetaData metaData, StreamOutput out) throws IOException {
			out.writeLong(metaData.version);
			writeSettingsToStream(metaData.transientSettings(), out);
			writeSettingsToStream(metaData.persistentSettings(), out);
			out.writeVInt(metaData.indices.size());
			for (IndexMetaData indexMetaData : metaData) {
				IndexMetaData.Builder.writeTo(indexMetaData, out);
			}
			out.writeVInt(metaData.templates.size());
			for (IndexTemplateMetaData template : metaData.templates.values()) {
				IndexTemplateMetaData.Builder.writeTo(template, out);
			}
		}
	}
}
