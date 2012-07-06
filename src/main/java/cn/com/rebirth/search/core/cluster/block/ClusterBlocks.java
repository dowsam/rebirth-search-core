/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ClusterBlocks.java 2012-7-6 14:29:57 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.block;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.rest.RestStatus;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * The Class ClusterBlocks.
 *
 * @author l.xue.nong
 */
public class ClusterBlocks {

	/** The Constant EMPTY_CLUSTER_BLOCK. */
	public static final ClusterBlocks EMPTY_CLUSTER_BLOCK = new ClusterBlocks(ImmutableSet.<ClusterBlock> of(),
			ImmutableMap.<String, ImmutableSet<ClusterBlock>> of());

	/** The global. */
	private final ImmutableSet<ClusterBlock> global;

	/** The indices blocks. */
	private final ImmutableMap<String, ImmutableSet<ClusterBlock>> indicesBlocks;

	/** The level holders. */
	private final ImmutableLevelHolder[] levelHolders;

	/**
	 * Instantiates a new cluster blocks.
	 *
	 * @param global the global
	 * @param indicesBlocks the indices blocks
	 */
	ClusterBlocks(ImmutableSet<ClusterBlock> global, ImmutableMap<String, ImmutableSet<ClusterBlock>> indicesBlocks) {
		this.global = global;
		this.indicesBlocks = indicesBlocks;

		levelHolders = new ImmutableLevelHolder[ClusterBlockLevel.values().length];
		for (ClusterBlockLevel level : ClusterBlockLevel.values()) {
			ImmutableSet.Builder<ClusterBlock> globalBuilder = ImmutableSet.builder();
			for (ClusterBlock block : global) {
				if (block.contains(level)) {
					globalBuilder.add(block);
				}
			}

			ImmutableMap.Builder<String, ImmutableSet<ClusterBlock>> indicesBuilder = ImmutableMap.builder();
			for (Map.Entry<String, ImmutableSet<ClusterBlock>> entry : indicesBlocks.entrySet()) {
				ImmutableSet.Builder<ClusterBlock> indexBuilder = ImmutableSet.builder();
				for (ClusterBlock block : entry.getValue()) {
					if (block.contains(level)) {
						indexBuilder.add(block);
					}
				}

				indicesBuilder.put(entry.getKey(), indexBuilder.build());
			}

			levelHolders[level.id()] = new ImmutableLevelHolder(globalBuilder.build(), indicesBuilder.build());
		}
	}

	/**
	 * Global.
	 *
	 * @return the immutable set
	 */
	public ImmutableSet<ClusterBlock> global() {
		return global;
	}

	/**
	 * Indices.
	 *
	 * @return the immutable map
	 */
	public ImmutableMap<String, ImmutableSet<ClusterBlock>> indices() {
		return indicesBlocks;
	}

	/**
	 * Global.
	 *
	 * @param level the level
	 * @return the immutable set
	 */
	public ImmutableSet<ClusterBlock> global(ClusterBlockLevel level) {
		return levelHolders[level.id()].global();
	}

	/**
	 * Indices.
	 *
	 * @param level the level
	 * @return the immutable map
	 */
	public ImmutableMap<String, ImmutableSet<ClusterBlock>> indices(ClusterBlockLevel level) {
		return levelHolders[level.id()].indices();
	}

	/**
	 * Disable state persistence.
	 *
	 * @return true, if successful
	 */
	public boolean disableStatePersistence() {
		for (ClusterBlock clusterBlock : global) {
			if (clusterBlock.disableStatePersistence()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks for global block.
	 *
	 * @param block the block
	 * @return true, if successful
	 */
	public boolean hasGlobalBlock(ClusterBlock block) {
		return global.contains(block);
	}

	/**
	 * Checks for global block.
	 *
	 * @param status the status
	 * @return true, if successful
	 */
	public boolean hasGlobalBlock(RestStatus status) {
		for (ClusterBlock clusterBlock : global) {
			if (clusterBlock.status().equals(status)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks for index block.
	 *
	 * @param index the index
	 * @param block the block
	 * @return true, if successful
	 */
	public boolean hasIndexBlock(String index, ClusterBlock block) {
		return indicesBlocks.containsKey(index) && indicesBlocks.get(index).contains(block);
	}

	/**
	 * Global blocked raise exception.
	 *
	 * @param level the level
	 * @throws ClusterBlockException the cluster block exception
	 */
	public void globalBlockedRaiseException(ClusterBlockLevel level) throws ClusterBlockException {
		ClusterBlockException blockException = globalBlockedException(level);
		if (blockException != null) {
			throw blockException;
		}
	}

	/**
	 * Global blocked exception.
	 *
	 * @param level the level
	 * @return the cluster block exception
	 */
	public ClusterBlockException globalBlockedException(ClusterBlockLevel level) {
		if (global(level).isEmpty()) {
			return null;
		}
		return new ClusterBlockException(ImmutableSet.copyOf(global(level)));
	}

	/**
	 * Index blocked raise exception.
	 *
	 * @param level the level
	 * @param index the index
	 * @throws ClusterBlockException the cluster block exception
	 */
	public void indexBlockedRaiseException(ClusterBlockLevel level, String index) throws ClusterBlockException {
		ClusterBlockException blockException = indexBlockedException(level, index);
		if (blockException != null) {
			throw blockException;
		}
	}

	/**
	 * Index blocked exception.
	 *
	 * @param level the level
	 * @param index the index
	 * @return the cluster block exception
	 */
	public ClusterBlockException indexBlockedException(ClusterBlockLevel level, String index) {
		if (!indexBlocked(level, index)) {
			return null;
		}
		ImmutableSet.Builder<ClusterBlock> builder = ImmutableSet.builder();
		builder.addAll(global(level));
		ImmutableSet<ClusterBlock> indexBlocks = indices(level).get(index);
		if (indexBlocks != null) {
			builder.addAll(indexBlocks);
		}
		return new ClusterBlockException(builder.build());
	}

	/**
	 * Index blocked.
	 *
	 * @param level the level
	 * @param index the index
	 * @return true, if successful
	 */
	public boolean indexBlocked(ClusterBlockLevel level, String index) {
		if (!global(level).isEmpty()) {
			return true;
		}
		ImmutableSet<ClusterBlock> indexBlocks = indices(level).get(index);
		if (indexBlocks != null && !indexBlocks.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Indices blocked exception.
	 *
	 * @param level the level
	 * @param indices the indices
	 * @return the cluster block exception
	 */
	public ClusterBlockException indicesBlockedException(ClusterBlockLevel level, String[] indices) {
		boolean indexIsBlocked = false;
		for (String index : indices) {
			if (indexBlocked(level, index)) {
				indexIsBlocked = true;
			}
		}
		if (!indexIsBlocked) {
			return null;
		}
		ImmutableSet.Builder<ClusterBlock> builder = ImmutableSet.builder();
		builder.addAll(global(level));
		for (String index : indices) {
			ImmutableSet<ClusterBlock> indexBlocks = indices(level).get(index);
			if (indexBlocks != null) {
				builder.addAll(indexBlocks);
			}
		}
		return new ClusterBlockException(builder.build());
	}

	/**
	 * The Class ImmutableLevelHolder.
	 *
	 * @author l.xue.nong
	 */
	static class ImmutableLevelHolder {

		/** The Constant EMPTY. */
		static final ImmutableLevelHolder EMPTY = new ImmutableLevelHolder(ImmutableSet.<ClusterBlock> of(),
				ImmutableMap.<String, ImmutableSet<ClusterBlock>> of());

		/** The global. */
		private final ImmutableSet<ClusterBlock> global;

		/** The indices. */
		private final ImmutableMap<String, ImmutableSet<ClusterBlock>> indices;

		/**
		 * Instantiates a new immutable level holder.
		 *
		 * @param global the global
		 * @param indices the indices
		 */
		ImmutableLevelHolder(ImmutableSet<ClusterBlock> global, ImmutableMap<String, ImmutableSet<ClusterBlock>> indices) {
			this.global = global;
			this.indices = indices;
		}

		/**
		 * Global.
		 *
		 * @return the immutable set
		 */
		public ImmutableSet<ClusterBlock> global() {
			return global;
		}

		/**
		 * Indices.
		 *
		 * @return the immutable map
		 */
		public ImmutableMap<String, ImmutableSet<ClusterBlock>> indices() {
			return indices;
		}
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
	 * The Class Builder.
	 *
	 * @author l.xue.nong
	 */
	public static class Builder {

		/** The global. */
		private Set<ClusterBlock> global = Sets.newHashSet();

		/** The indices. */
		private Map<String, Set<ClusterBlock>> indices = Maps.newHashMap();

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
		}

		/**
		 * Blocks.
		 *
		 * @param blocks the blocks
		 * @return the builder
		 */
		public Builder blocks(ClusterBlocks blocks) {
			global.addAll(blocks.global());
			for (Map.Entry<String, ImmutableSet<ClusterBlock>> entry : blocks.indices().entrySet()) {
				if (!indices.containsKey(entry.getKey())) {
					indices.put(entry.getKey(), Sets.<ClusterBlock> newHashSet());
				}
				indices.get(entry.getKey()).addAll(entry.getValue());
			}
			return this;
		}

		/**
		 * Adds the global block.
		 *
		 * @param block the block
		 * @return the builder
		 */
		public Builder addGlobalBlock(ClusterBlock block) {
			global.add(block);
			return this;
		}

		/**
		 * Removes the global block.
		 *
		 * @param block the block
		 * @return the builder
		 */
		public Builder removeGlobalBlock(ClusterBlock block) {
			global.remove(block);
			return this;
		}

		/**
		 * Adds the index block.
		 *
		 * @param index the index
		 * @param block the block
		 * @return the builder
		 */
		public Builder addIndexBlock(String index, ClusterBlock block) {
			if (!indices.containsKey(index)) {
				indices.put(index, Sets.<ClusterBlock> newHashSet());
			}
			indices.get(index).add(block);
			return this;
		}

		/**
		 * Removes the index blocks.
		 *
		 * @param index the index
		 * @return the builder
		 */
		public Builder removeIndexBlocks(String index) {
			if (!indices.containsKey(index)) {
				return this;
			}
			indices.remove(index);
			return this;
		}

		/**
		 * Removes the index block.
		 *
		 * @param index the index
		 * @param block the block
		 * @return the builder
		 */
		public Builder removeIndexBlock(String index, ClusterBlock block) {
			if (!indices.containsKey(index)) {
				return this;
			}
			indices.get(index).remove(block);
			if (indices.get(index).isEmpty()) {
				indices.remove(index);
			}
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the cluster blocks
		 */
		public ClusterBlocks build() {
			ImmutableMap.Builder<String, ImmutableSet<ClusterBlock>> indicesBuilder = ImmutableMap.builder();
			for (Map.Entry<String, Set<ClusterBlock>> entry : indices.entrySet()) {
				indicesBuilder.put(entry.getKey(), ImmutableSet.copyOf(entry.getValue()));
			}
			return new ClusterBlocks(ImmutableSet.copyOf(global), indicesBuilder.build());
		}

		/**
		 * Read cluster blocks.
		 *
		 * @param in the in
		 * @return the cluster blocks
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static ClusterBlocks readClusterBlocks(StreamInput in) throws IOException {
			ImmutableSet<ClusterBlock> global = readBlockSet(in);
			ImmutableMap.Builder<String, ImmutableSet<ClusterBlock>> indicesBuilder = ImmutableMap.builder();
			int size = in.readVInt();
			for (int j = 0; j < size; j++) {
				indicesBuilder.put(in.readUTF().intern(), readBlockSet(in));
			}
			return new ClusterBlocks(global, indicesBuilder.build());
		}

		/**
		 * Write cluster blocks.
		 *
		 * @param blocks the blocks
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeClusterBlocks(ClusterBlocks blocks, StreamOutput out) throws IOException {
			writeBlockSet(blocks.global(), out);
			out.writeVInt(blocks.indices().size());
			for (Map.Entry<String, ImmutableSet<ClusterBlock>> entry : blocks.indices().entrySet()) {
				out.writeUTF(entry.getKey());
				writeBlockSet(entry.getValue(), out);
			}
		}

		/**
		 * Write block set.
		 *
		 * @param blocks the blocks
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private static void writeBlockSet(ImmutableSet<ClusterBlock> blocks, StreamOutput out) throws IOException {
			out.writeVInt(blocks.size());
			for (ClusterBlock block : blocks) {
				block.writeTo(out);
			}
		}

		/**
		 * Read block set.
		 *
		 * @param in the in
		 * @return the immutable set
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		private static ImmutableSet<ClusterBlock> readBlockSet(StreamInput in) throws IOException {
			ImmutableSet.Builder<ClusterBlock> builder = ImmutableSet.builder();
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.add(ClusterBlock.readClusterBlock(in));
			}
			return builder.build();
		}
	}
}
