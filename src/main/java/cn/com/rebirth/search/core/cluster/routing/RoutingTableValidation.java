/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RoutingTableValidation.java 2012-7-6 14:29:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.cluster.routing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The Class RoutingTableValidation.
 *
 * @author l.xue.nong
 */
public class RoutingTableValidation implements Serializable, Streamable {

	/** The valid. */
	private boolean valid = true;

	/** The failures. */
	private List<String> failures;

	/** The indices failures. */
	private Map<String, List<String>> indicesFailures;

	/**
	 * Instantiates a new routing table validation.
	 */
	public RoutingTableValidation() {
	}

	/**
	 * Valid.
	 *
	 * @return true, if successful
	 */
	public boolean valid() {
		return valid;
	}

	/**
	 * All failures.
	 *
	 * @return the list
	 */
	public List<String> allFailures() {
		if (failures().isEmpty() && indicesFailures().isEmpty()) {
			return ImmutableList.of();
		}
		List<String> allFailures = newArrayList(failures());
		for (Map.Entry<String, List<String>> entry : indicesFailures().entrySet()) {
			for (String failure : entry.getValue()) {
				allFailures.add("Index [" + entry.getKey() + "]: " + failure);
			}
		}
		return allFailures;
	}

	/**
	 * Failures.
	 *
	 * @return the list
	 */
	public List<String> failures() {
		if (failures == null) {
			return ImmutableList.of();
		}
		return failures;
	}

	/**
	 * Indices failures.
	 *
	 * @return the map
	 */
	public Map<String, List<String>> indicesFailures() {
		if (indicesFailures == null) {
			return ImmutableMap.of();
		}
		return indicesFailures;
	}

	/**
	 * Index failures.
	 *
	 * @param index the index
	 * @return the list
	 */
	public List<String> indexFailures(String index) {
		if (indicesFailures == null) {
			return ImmutableList.of();
		}
		List<String> indexFailures = indicesFailures.get(index);
		if (indexFailures == null) {
			return ImmutableList.of();
		}
		return indexFailures;
	}

	/**
	 * Adds the failure.
	 *
	 * @param failure the failure
	 */
	public void addFailure(String failure) {
		valid = false;
		if (failures == null) {
			failures = newArrayList();
		}
		failures.add(failure);
	}

	/**
	 * Adds the index failure.
	 *
	 * @param index the index
	 * @param failure the failure
	 */
	public void addIndexFailure(String index, String failure) {
		valid = false;
		if (indicesFailures == null) {
			indicesFailures = newHashMap();
		}
		List<String> indexFailures = indicesFailures.get(index);
		if (indexFailures == null) {
			indexFailures = Lists.newArrayList();
			indicesFailures.put(index, indexFailures);
		}
		indexFailures.add(failure);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return allFailures().toString();
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		valid = in.readBoolean();
		int size = in.readVInt();
		if (size == 0) {
			failures = ImmutableList.of();
		} else {
			failures = Lists.newArrayListWithCapacity(size);
			for (int i = 0; i < size; i++) {
				failures.add(in.readUTF());
			}
		}
		size = in.readVInt();
		if (size == 0) {
			indicesFailures = ImmutableMap.of();
		} else {
			indicesFailures = newHashMap();
			for (int i = 0; i < size; i++) {
				String index = in.readUTF();
				int size2 = in.readVInt();
				List<String> indexFailures = newArrayListWithCapacity(size2);
				for (int j = 0; j < size2; j++) {
					indexFailures.add(in.readUTF());
				}
				indicesFailures.put(index, indexFailures);
			}
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeBoolean(valid);
		if (failures == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(failures.size());
			for (String failure : failures) {
				out.writeUTF(failure);
			}
		}
		if (indicesFailures == null) {
			out.writeVInt(0);
		} else {
			out.writeVInt(indicesFailures.size());
			for (Map.Entry<String, List<String>> entry : indicesFailures.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeVInt(entry.getValue().size());
				for (String failure : entry.getValue()) {
					out.writeUTF(failure);
				}
			}
		}
	}
}
