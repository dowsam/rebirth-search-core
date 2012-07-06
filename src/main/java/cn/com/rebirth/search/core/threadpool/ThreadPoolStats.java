/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ThreadPoolStats.java 2012-3-29 15:00:50 l.xue.nong$$
 */


package cn.com.rebirth.search.core.threadpool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;


/**
 * The Class ThreadPoolStats.
 *
 * @author l.xue.nong
 */
public class ThreadPoolStats implements Streamable, ToXContent, Iterable<ThreadPoolStats.Stats> {

	
	/**
	 * The Class Stats.
	 *
	 * @author l.xue.nong
	 */
	public static class Stats implements Streamable, ToXContent {

		
		/** The name. */
		private String name;

		
		/** The threads. */
		private int threads;

		
		/** The queue. */
		private int queue;

		
		/** The active. */
		private int active;

		
		/**
		 * Instantiates a new stats.
		 */
		Stats() {

		}

		
		/**
		 * Instantiates a new stats.
		 *
		 * @param name the name
		 * @param threads the threads
		 * @param queue the queue
		 * @param active the active
		 */
		public Stats(String name, int threads, int queue, int active) {
			this.name = name;
			this.threads = threads;
			this.queue = queue;
			this.active = active;
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
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		
		/**
		 * Threads.
		 *
		 * @return the int
		 */
		public int threads() {
			return this.threads;
		}

		
		/**
		 * Gets the threads.
		 *
		 * @return the threads
		 */
		public int getThreads() {
			return this.threads;
		}

		
		/**
		 * Queue.
		 *
		 * @return the int
		 */
		public int queue() {
			return this.queue;
		}

		
		/**
		 * Gets the queue.
		 *
		 * @return the queue
		 */
		public int getQueue() {
			return this.queue;
		}

		
		/**
		 * Active.
		 *
		 * @return the int
		 */
		public int active() {
			return this.active;
		}

		
		/**
		 * Gets the active.
		 *
		 * @return the active
		 */
		public int getActive() {
			return this.active;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			name = in.readUTF();
			threads = in.readInt();
			queue = in.readInt();
			active = in.readInt();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeUTF(name);
			out.writeInt(threads);
			out.writeInt(queue);
			out.writeInt(active);
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
		 */
		@Override
		public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
			builder.startObject(name, XContentBuilder.FieldCaseConversion.NONE);
			if (threads != -1) {
				builder.field(Fields.THREADS, threads);
			}
			if (queue != -1) {
				builder.field(Fields.QUEUE, queue);
			}
			if (active != -1) {
				builder.field(Fields.ACTIVE, active);
			}
			builder.endObject();
			return builder;
		}
	}

	
	/** The stats. */
	private List<Stats> stats;

	
	/**
	 * Instantiates a new thread pool stats.
	 */
	ThreadPoolStats() {

	}

	
	/**
	 * Instantiates a new thread pool stats.
	 *
	 * @param stats the stats
	 */
	public ThreadPoolStats(List<Stats> stats) {
		this.stats = stats;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Stats> iterator() {
		return stats.iterator();
	}

	
	/**
	 * Read thread pool stats.
	 *
	 * @param in the in
	 * @return the thread pool stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ThreadPoolStats readThreadPoolStats(StreamInput in) throws IOException {
		ThreadPoolStats stats = new ThreadPoolStats();
		stats.readFrom(in);
		return stats;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		stats = new ArrayList<Stats>(size);
		for (int i = 0; i < size; i++) {
			Stats stats1 = new Stats();
			stats1.readFrom(in);
			stats.add(stats1);
		}
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(stats.size());
		for (Stats stat : stats) {
			stat.writeTo(out);
		}
	}

	
	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		
		/** The Constant THREAD_POOL. */
		static final XContentBuilderString THREAD_POOL = new XContentBuilderString("thread_pool");

		
		/** The Constant THREADS. */
		static final XContentBuilderString THREADS = new XContentBuilderString("threads");

		
		/** The Constant QUEUE. */
		static final XContentBuilderString QUEUE = new XContentBuilderString("queue");

		
		/** The Constant ACTIVE. */
		static final XContentBuilderString ACTIVE = new XContentBuilderString("active");
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
		builder.startObject(Fields.THREAD_POOL);
		for (Stats stat : stats) {
			stat.toXContent(builder, params);
		}
		builder.endObject();
		return builder;
	}
}
