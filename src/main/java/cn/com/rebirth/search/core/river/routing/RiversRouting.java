/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core RiversRouting.java 2012-7-6 14:30:37 l.xue.nong$$
 */

package cn.com.rebirth.search.core.river.routing;

import java.io.IOException;
import java.util.Iterator;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.river.RiverName;

import com.google.common.collect.ImmutableMap;

/**
 * The Class RiversRouting.
 *
 * @author l.xue.nong
 */
public class RiversRouting implements Iterable<RiverRouting> {

	/** The Constant EMPTY. */
	public static final RiversRouting EMPTY = RiversRouting.builder().build();

	/** The rivers. */
	private final ImmutableMap<RiverName, RiverRouting> rivers;

	/**
	 * Instantiates a new rivers routing.
	 *
	 * @param rivers the rivers
	 */
	private RiversRouting(ImmutableMap<RiverName, RiverRouting> rivers) {
		this.rivers = rivers;
	}

	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return rivers.isEmpty();
	}

	/**
	 * Routing.
	 *
	 * @param riverName the river name
	 * @return the river routing
	 */
	public RiverRouting routing(RiverName riverName) {
		return rivers.get(riverName);
	}

	/**
	 * Checks for river by name.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	public boolean hasRiverByName(String name) {
		for (RiverName riverName : rivers.keySet()) {
			if (riverName.name().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<RiverRouting> iterator() {
		return rivers.values().iterator();
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

		/** The rivers. */
		private MapBuilder<RiverName, RiverRouting> rivers = MapBuilder.newMapBuilder();

		/**
		 * Routing.
		 *
		 * @param routing the routing
		 * @return the builder
		 */
		public Builder routing(RiversRouting routing) {
			rivers.putAll(routing.rivers);
			return this;
		}

		/**
		 * Put.
		 *
		 * @param routing the routing
		 * @return the builder
		 */
		public Builder put(RiverRouting routing) {
			rivers.put(routing.riverName(), routing);
			return this;
		}

		/**
		 * Removes the.
		 *
		 * @param routing the routing
		 * @return the builder
		 */
		public Builder remove(RiverRouting routing) {
			rivers.remove(routing.riverName());
			return this;
		}

		/**
		 * Removes the.
		 *
		 * @param riverName the river name
		 * @return the builder
		 */
		public Builder remove(RiverName riverName) {
			rivers.remove(riverName);
			return this;
		}

		/**
		 * Remote.
		 *
		 * @param riverName the river name
		 * @return the builder
		 */
		public Builder remote(String riverName) {
			for (RiverName name : rivers.map().keySet()) {
				if (name.name().equals(riverName)) {
					rivers.remove(name);
				}
			}
			return this;
		}

		/**
		 * Builds the.
		 *
		 * @return the rivers routing
		 */
		public RiversRouting build() {
			return new RiversRouting(rivers.immutableMap());
		}

		/**
		 * Read from.
		 *
		 * @param in the in
		 * @return the rivers routing
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static RiversRouting readFrom(StreamInput in) throws IOException {
			Builder builder = new Builder();
			int size = in.readVInt();
			for (int i = 0; i < size; i++) {
				builder.put(RiverRouting.readRiverRouting(in));
			}
			return builder.build();
		}

		/**
		 * Write to.
		 *
		 * @param routing the routing
		 * @param out the out
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static void writeTo(RiversRouting routing, StreamOutput out) throws IOException {
			out.writeVInt(routing.rivers.size());
			for (RiverRouting riverRouting : routing) {
				riverRouting.writeTo(out);
			}
		}
	}
}
