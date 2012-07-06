/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core NetworkStats.java 2012-7-6 14:30:17 l.xue.nong$$
 */

package cn.com.rebirth.search.core.monitor.network;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;

/**
 * The Class NetworkStats.
 *
 * @author l.xue.nong
 */
public class NetworkStats implements Streamable, Serializable, ToXContent {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -795567624836554915L;

	/** The timestamp. */
	long timestamp;

	/** The tcp. */
	Tcp tcp = null;

	/**
	 * Instantiates a new network stats.
	 */
	NetworkStats() {

	}

	/**
	 * The Class Fields.
	 *
	 * @author l.xue.nong
	 */
	static final class Fields {

		/** The Constant NETWORK. */
		static final XContentBuilderString NETWORK = new XContentBuilderString("network");

		/** The Constant TCP. */
		static final XContentBuilderString TCP = new XContentBuilderString("tcp");

		/** The Constant ACTIVE_OPENS. */
		static final XContentBuilderString ACTIVE_OPENS = new XContentBuilderString("active_opens");

		/** The Constant PASSIVE_OPENS. */
		static final XContentBuilderString PASSIVE_OPENS = new XContentBuilderString("passive_opens");

		/** The Constant CURR_ESTAB. */
		static final XContentBuilderString CURR_ESTAB = new XContentBuilderString("curr_estab");

		/** The Constant IN_SEGS. */
		static final XContentBuilderString IN_SEGS = new XContentBuilderString("in_segs");

		/** The Constant OUT_SEGS. */
		static final XContentBuilderString OUT_SEGS = new XContentBuilderString("out_segs");

		/** The Constant RETRANS_SEGS. */
		static final XContentBuilderString RETRANS_SEGS = new XContentBuilderString("retrans_segs");

		/** The Constant ESTAB_RESETS. */
		static final XContentBuilderString ESTAB_RESETS = new XContentBuilderString("estab_resets");

		/** The Constant ATTEMPT_FAILS. */
		static final XContentBuilderString ATTEMPT_FAILS = new XContentBuilderString("attempt_fails");

		/** The Constant IN_ERRS. */
		static final XContentBuilderString IN_ERRS = new XContentBuilderString("in_errs");

		/** The Constant OUT_RSTS. */
		static final XContentBuilderString OUT_RSTS = new XContentBuilderString("out_rsts");
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.search.commons.xcontent.ToXContent#toXContent(cn.com.rebirth.search.commons.xcontent.XContentBuilder, cn.com.rebirth.search.commons.xcontent.ToXContent.Params)
	 */
	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(Fields.NETWORK);
		if (tcp != null) {
			builder.startObject(Fields.TCP);
			builder.field(Fields.ACTIVE_OPENS, tcp.getActiveOpens());
			builder.field(Fields.PASSIVE_OPENS, tcp.getPassiveOpens());
			builder.field(Fields.CURR_ESTAB, tcp.getCurrEstab());
			builder.field(Fields.IN_SEGS, tcp.getInSegs());
			builder.field(Fields.OUT_SEGS, tcp.getOutSegs());
			builder.field(Fields.RETRANS_SEGS, tcp.getRetransSegs());
			builder.field(Fields.ESTAB_RESETS, tcp.getEstabResets());
			builder.field(Fields.ATTEMPT_FAILS, tcp.getAttemptFails());
			builder.field(Fields.IN_ERRS, tcp.getInErrs());
			builder.field(Fields.OUT_RSTS, tcp.getOutRsts());
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}

	/**
	 * Read network stats.
	 *
	 * @param in the in
	 * @return the network stats
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static NetworkStats readNetworkStats(StreamInput in) throws IOException {
		NetworkStats stats = new NetworkStats();
		stats.readFrom(in);
		return stats;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		timestamp = in.readVLong();
		if (in.readBoolean()) {
			tcp = Tcp.readNetworkTcp(in);
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVLong(timestamp);
		if (tcp == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			tcp.writeTo(out);
		}
	}

	/**
	 * Timestamp.
	 *
	 * @return the long
	 */
	public long timestamp() {
		return timestamp;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp();
	}

	/**
	 * Tcp.
	 *
	 * @return the tcp
	 */
	public Tcp tcp() {
		return tcp;
	}

	/**
	 * Gets the tcp.
	 *
	 * @return the tcp
	 */
	public Tcp getTcp() {
		return tcp();
	}

	/**
	 * The Class Tcp.
	 *
	 * @author l.xue.nong
	 */
	public static class Tcp implements Serializable, Streamable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 5016461756099190388L;

		/** The active opens. */
		long activeOpens;

		/** The passive opens. */
		long passiveOpens;

		/** The attempt fails. */
		long attemptFails;

		/** The estab resets. */
		long estabResets;

		/** The curr estab. */
		long currEstab;

		/** The in segs. */
		long inSegs;

		/** The out segs. */
		long outSegs;

		/** The retrans segs. */
		long retransSegs;

		/** The in errs. */
		long inErrs;

		/** The out rsts. */
		long outRsts;

		/**
		 * Read network tcp.
		 *
		 * @param in the in
		 * @return the tcp
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		public static Tcp readNetworkTcp(StreamInput in) throws IOException {
			Tcp tcp = new Tcp();
			tcp.readFrom(in);
			return tcp;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			activeOpens = in.readLong();
			passiveOpens = in.readLong();
			attemptFails = in.readLong();
			estabResets = in.readLong();
			currEstab = in.readLong();
			inSegs = in.readLong();
			outSegs = in.readLong();
			retransSegs = in.readLong();
			inErrs = in.readLong();
			outRsts = in.readLong();
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeLong(activeOpens);
			out.writeLong(passiveOpens);
			out.writeLong(attemptFails);
			out.writeLong(estabResets);
			out.writeLong(currEstab);
			out.writeLong(inSegs);
			out.writeLong(outSegs);
			out.writeLong(retransSegs);
			out.writeLong(inErrs);
			out.writeLong(outRsts);
		}

		/**
		 * Active opens.
		 *
		 * @return the long
		 */
		public long activeOpens() {
			return this.activeOpens;
		}

		/**
		 * Gets the active opens.
		 *
		 * @return the active opens
		 */
		public long getActiveOpens() {
			return activeOpens();
		}

		/**
		 * Passive opens.
		 *
		 * @return the long
		 */
		public long passiveOpens() {
			return passiveOpens;
		}

		/**
		 * Gets the passive opens.
		 *
		 * @return the passive opens
		 */
		public long getPassiveOpens() {
			return passiveOpens();
		}

		/**
		 * Attempt fails.
		 *
		 * @return the long
		 */
		public long attemptFails() {
			return attemptFails;
		}

		/**
		 * Gets the attempt fails.
		 *
		 * @return the attempt fails
		 */
		public long getAttemptFails() {
			return attemptFails();
		}

		/**
		 * Estab resets.
		 *
		 * @return the long
		 */
		public long estabResets() {
			return estabResets;
		}

		/**
		 * Gets the estab resets.
		 *
		 * @return the estab resets
		 */
		public long getEstabResets() {
			return estabResets();
		}

		/**
		 * Curr estab.
		 *
		 * @return the long
		 */
		public long currEstab() {
			return currEstab;
		}

		/**
		 * Gets the curr estab.
		 *
		 * @return the curr estab
		 */
		public long getCurrEstab() {
			return currEstab();
		}

		/**
		 * In segs.
		 *
		 * @return the long
		 */
		public long inSegs() {
			return inSegs;
		}

		/**
		 * Gets the in segs.
		 *
		 * @return the in segs
		 */
		public long getInSegs() {
			return inSegs();
		}

		/**
		 * Out segs.
		 *
		 * @return the long
		 */
		public long outSegs() {
			return outSegs;
		}

		/**
		 * Gets the out segs.
		 *
		 * @return the out segs
		 */
		public long getOutSegs() {
			return outSegs();
		}

		/**
		 * Retrans segs.
		 *
		 * @return the long
		 */
		public long retransSegs() {
			return retransSegs;
		}

		/**
		 * Gets the retrans segs.
		 *
		 * @return the retrans segs
		 */
		public long getRetransSegs() {
			return retransSegs();
		}

		/**
		 * In errs.
		 *
		 * @return the long
		 */
		public long inErrs() {
			return inErrs;
		}

		/**
		 * Gets the in errs.
		 *
		 * @return the in errs
		 */
		public long getInErrs() {
			return inErrs();
		}

		/**
		 * Out rsts.
		 *
		 * @return the long
		 */
		public long outRsts() {
			return outRsts;
		}

		/**
		 * Gets the out rsts.
		 *
		 * @return the out rsts
		 */
		public long getOutRsts() {
			return outRsts();
		}
	}
}
