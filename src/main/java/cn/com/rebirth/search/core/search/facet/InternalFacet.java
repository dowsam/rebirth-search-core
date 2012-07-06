/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core InternalFacet.java 2012-7-6 14:30:43 l.xue.nong$$
 */

package cn.com.rebirth.search.core.search.facet;

import java.io.IOException;

import cn.com.rebirth.commons.collect.MapBuilder;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;

import com.google.common.collect.ImmutableMap;

/**
 * The Interface InternalFacet.
 *
 * @author l.xue.nong
 */
public interface InternalFacet extends Facet, Streamable, ToXContent {

	/**
	 * Stream type.
	 *
	 * @return the string
	 */
	String streamType();

	/**
	 * The Interface Stream.
	 *
	 * @author l.xue.nong
	 */
	public static interface Stream {

		/**
		 * Read facet.
		 *
		 * @param type the type
		 * @param in the in
		 * @return the facet
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		Facet readFacet(String type, StreamInput in) throws IOException;
	}

	/**
	 * The Class Streams.
	 *
	 * @author l.xue.nong
	 */
	public static class Streams {

		/** The streams. */
		private static ImmutableMap<String, Stream> streams = ImmutableMap.of();

		/**
		 * Register stream.
		 *
		 * @param stream the stream
		 * @param types the types
		 */
		public static synchronized void registerStream(Stream stream, String... types) {
			MapBuilder<String, Stream> uStreams = MapBuilder.newMapBuilder(streams);
			for (String type : types) {
				uStreams.put(type, stream);
			}
			streams = uStreams.immutableMap();
		}

		/**
		 * Stream.
		 *
		 * @param type the type
		 * @return the stream
		 */
		public static Stream stream(String type) {
			return streams.get(type);
		}
	}
}
