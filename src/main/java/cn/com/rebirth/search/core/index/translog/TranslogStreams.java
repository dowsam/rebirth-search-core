/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core TranslogStreams.java 2012-7-6 14:30:35 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.commons.io.stream.BytesStreamInput;

/**
 * The Class TranslogStreams.
 *
 * @author l.xue.nong
 */
public class TranslogStreams {

	/**
	 * Read translog operation.
	 *
	 * @param in the in
	 * @return the translog. operation
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Translog.Operation readTranslogOperation(StreamInput in) throws IOException {
		Translog.Operation.Type type = Translog.Operation.Type.fromId(in.readByte());
		Translog.Operation operation;
		switch (type) {
		case CREATE:
			operation = new Translog.Create();
			break;
		case DELETE:
			operation = new Translog.Delete();
			break;
		case DELETE_BY_QUERY:
			operation = new Translog.DeleteByQuery();
			break;
		case SAVE:
			operation = new Translog.Index();
			break;
		default:
			throw new IOException("No type for [" + type + "]");
		}
		operation.readFrom(in);
		return operation;
	}

	/**
	 * Read source.
	 *
	 * @param data the data
	 * @return the translog. source
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Translog.Source readSource(byte[] data) throws IOException {
		BytesStreamInput in = new BytesStreamInput(data, false);
		in.readInt();
		Translog.Operation.Type type = Translog.Operation.Type.fromId(in.readByte());
		Translog.Operation operation;
		switch (type) {
		case CREATE:
			operation = new Translog.Create();
			break;
		case DELETE:
			operation = new Translog.Delete();
			break;
		case DELETE_BY_QUERY:
			operation = new Translog.DeleteByQuery();
			break;
		case SAVE:
			operation = new Translog.Index();
			break;
		default:
			throw new IOException("No type for [" + type + "]");
		}
		return operation.readSource(in);
	}

	/**
	 * Write translog operation.
	 *
	 * @param out the out
	 * @param op the op
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeTranslogOperation(StreamOutput out, Translog.Operation op) throws IOException {
		out.writeByte(op.opType().id());
		op.writeTo(out);
	}
}
