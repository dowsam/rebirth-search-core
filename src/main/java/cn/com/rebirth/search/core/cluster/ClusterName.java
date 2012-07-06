/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterName.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.commons.settings.Settings;


/**
 * The Class ClusterName.
 *
 * @author l.xue.nong
 */
public class ClusterName implements Streamable {

	
	/** The Constant SETTING. */
	public static final String SETTING = "cluster.name";

	
	/** The Constant DEFAULT. */
	public static final ClusterName DEFAULT = new ClusterName("restartsearch".intern());

	
	/** The value. */
	private String value;

	
	/**
	 * Cluster name from settings.
	 *
	 * @param settings the settings
	 * @return the cluster name
	 */
	public static ClusterName clusterNameFromSettings(Settings settings) {
		return new ClusterName(settings.get("cluster.name", ClusterName.DEFAULT.value()));
	}

	
	/**
	 * Instantiates a new cluster name.
	 */
	private ClusterName() {

	}

	
	/**
	 * Instantiates a new cluster name.
	 *
	 * @param value the value
	 */
	public ClusterName(String value) {
		this.value = value.intern();
	}

	
	/**
	 * Value.
	 *
	 * @return the string
	 */
	public String value() {
		return this.value;
	}

	
	/**
	 * Read cluster name.
	 *
	 * @param in the in
	 * @return the cluster name
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterName readClusterName(StreamInput in) throws IOException {
		ClusterName clusterName = new ClusterName();
		clusterName.readFrom(in);
		return clusterName;
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		value = in.readUTF().intern();
	}

	
	/* (non-Javadoc)
	 * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeUTF(value);
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

		ClusterName that = (ClusterName) o;

		if (value != null ? !value.equals(that.value) : that.value != null)
			return false;

		return true;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value != null ? value.hashCode() : 0;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Cluster [" + value + "]";
	}
}
