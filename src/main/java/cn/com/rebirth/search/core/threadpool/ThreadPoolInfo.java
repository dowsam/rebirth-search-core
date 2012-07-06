/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core ThreadPoolInfo.java 2012-7-6 14:30:19 l.xue.nong$$
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
 * The Class ThreadPoolInfo.
 *
 * @author l.xue.nong
 */
public class ThreadPoolInfo implements Streamable, Iterable<ThreadPool.Info>, ToXContent {

	/** The infos. */
	private List<ThreadPool.Info> infos;

	/**
	 * Instantiates a new thread pool info.
	 */
	ThreadPoolInfo() {
	}

	/**
	 * Instantiates a new thread pool info.
	 *
	 * @param infos the infos
	 */
	public ThreadPoolInfo(List<ThreadPool.Info> infos) {
		this.infos = infos;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ThreadPool.Info> iterator() {
		return infos.iterator();
	}

	/**
	 * Read thread pool info.
	 *
	 * @param in the in
	 * @return the thread pool info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ThreadPoolInfo readThreadPoolInfo(StreamInput in) throws IOException {
		ThreadPoolInfo info = new ThreadPoolInfo();
		info.readFrom(in);
		return info;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		int size = in.readVInt();
		infos = new ArrayList<ThreadPool.Info>(size);
		for (int i = 0; i < size; i++) {
			ThreadPool.Info info = new ThreadPool.Info();
			info.readFrom(in);
			infos.add(info);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(infos.size());
		for (ThreadPool.Info info : infos) {
			info.writeTo(out);
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
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.THREAD_POOL);
		for (ThreadPool.Info info : infos) {
			info.toXContent(builder, params);
		}
		builder.endObject();
		return builder;
	}
}
