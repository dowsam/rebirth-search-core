/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core BulkItemResponse.java 2012-7-6 14:28:50 l.xue.nong$$
 */

package cn.com.rebirth.search.core.action.bulk;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.ActionResponse;
import cn.com.rebirth.search.core.action.delete.DeleteResponse;
import cn.com.rebirth.search.core.action.index.IndexResponse;

/**
 * The Class BulkItemResponse.
 *
 * @author l.xue.nong
 */
public class BulkItemResponse implements Streamable {

	/**
	 * The Class Failure.
	 *
	 * @author l.xue.nong
	 */
	public static class Failure {

		/** The index. */
		private final String index;

		/** The type. */
		private final String type;

		/** The id. */
		private final String id;

		/** The message. */
		private final String message;

		/**
		 * Instantiates a new failure.
		 *
		 * @param index the index
		 * @param type the type
		 * @param id the id
		 * @param message the message
		 */
		public Failure(String index, String type, String id, String message) {
			this.index = index;
			this.type = type;
			this.id = id;
			this.message = message;
		}

		/**
		 * Index.
		 *
		 * @return the string
		 */
		public String index() {
			return this.index;
		}

		/**
		 * Gets the index.
		 *
		 * @return the index
		 */
		public String getIndex() {
			return index();
		}

		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return type;
		}

		/**
		 * Gets the type.
		 *
		 * @return the type
		 */
		public String getType() {
			return type();
		}

		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return id;
		}

		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		public String getId() {
			return this.id;
		}

		/**
		 * Message.
		 *
		 * @return the string
		 */
		public String message() {
			return this.message;
		}

		/**
		 * Gets the message.
		 *
		 * @return the message
		 */
		public String getMessage() {
			return message();
		}
	}

	/** The id. */
	private int id;

	/** The op type. */
	private String opType;

	/** The response. */
	private ActionResponse response;

	/** The failure. */
	private Failure failure;

	/**
	 * Instantiates a new bulk item response.
	 */
	BulkItemResponse() {

	}

	/**
	 * Instantiates a new bulk item response.
	 *
	 * @param id the id
	 * @param opType the op type
	 * @param response the response
	 */
	public BulkItemResponse(int id, String opType, ActionResponse response) {
		this.id = id;
		this.opType = opType;
		this.response = response;
	}

	/**
	 * Instantiates a new bulk item response.
	 *
	 * @param id the id
	 * @param opType the op type
	 * @param failure the failure
	 */
	public BulkItemResponse(int id, String opType, Failure failure) {
		this.id = id;
		this.opType = opType;
		this.failure = failure;
	}

	/**
	 * Item id.
	 *
	 * @return the int
	 */
	public int itemId() {
		return id;
	}

	/**
	 * Op type.
	 *
	 * @return the string
	 */
	public String opType() {
		return this.opType;
	}

	/**
	 * Index.
	 *
	 * @return the string
	 */
	public String index() {
		if (failure != null) {
			return failure.index();
		}
		if (response instanceof IndexResponse) {
			return ((IndexResponse) response).index();
		} else if (response instanceof DeleteResponse) {
			return ((DeleteResponse) response).index();
		}
		return null;
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public String getIndex() {
		return index();
	}

	/**
	 * Type.
	 *
	 * @return the string
	 */
	public String type() {
		if (failure != null) {
			return failure.type();
		}
		if (response instanceof IndexResponse) {
			return ((IndexResponse) response).type();
		} else if (response instanceof DeleteResponse) {
			return ((DeleteResponse) response).type();
		}
		return null;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return this.type();
	}

	/**
	 * Id.
	 *
	 * @return the string
	 */
	public String id() {
		if (failure != null) {
			return failure.id();
		}
		if (response instanceof IndexResponse) {
			return ((IndexResponse) response).id();
		} else if (response instanceof DeleteResponse) {
			return ((DeleteResponse) response).id();
		}
		return null;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id();
	}

	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		if (failure != null) {
			return -1;
		}
		if (response instanceof IndexResponse) {
			return ((IndexResponse) response).version();
		} else if (response instanceof DeleteResponse) {
			return ((DeleteResponse) response).version();
		}
		return -1;
	}

	/**
	 * Response.
	 *
	 * @param <T> the generic type
	 * @return the t
	 */
	public <T extends ActionResponse> T response() {
		return (T) response;
	}

	/**
	 * Failed.
	 *
	 * @return true, if successful
	 */
	public boolean failed() {
		return failure != null;
	}

	/**
	 * Checks if is failed.
	 *
	 * @return true, if is failed
	 */
	public boolean isFailed() {
		return failed();
	}

	/**
	 * Failure message.
	 *
	 * @return the string
	 */
	public String failureMessage() {
		if (failure != null) {
			return failure.message();
		}
		return null;
	}

	/**
	 * Gets the failure message.
	 *
	 * @return the failure message
	 */
	public String getFailureMessage() {
		return failureMessage();
	}

	/**
	 * Failure.
	 *
	 * @return the failure
	 */
	public Failure failure() {
		return this.failure;
	}

	/**
	 * Gets the failure.
	 *
	 * @return the failure
	 */
	public Failure getFailure() {
		return failure();
	}

	/**
	 * Read bulk item.
	 *
	 * @param in the in
	 * @return the bulk item response
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static BulkItemResponse readBulkItem(StreamInput in) throws IOException {
		BulkItemResponse response = new BulkItemResponse();
		response.readFrom(in);
		return response;
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
	 */
	@Override
	public void readFrom(StreamInput in) throws IOException {
		id = in.readVInt();
		opType = in.readUTF();

		byte type = in.readByte();
		if (type == 0) {
			response = new IndexResponse();
			response.readFrom(in);
		} else if (type == 1) {
			response = new DeleteResponse();
			response.readFrom(in);
		}

		if (in.readBoolean()) {
			failure = new Failure(in.readUTF(), in.readUTF(), in.readUTF(), in.readUTF());
		}
	}

	/* (non-Javadoc)
	 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
	 */
	@Override
	public void writeTo(StreamOutput out) throws IOException {
		out.writeVInt(id);
		out.writeUTF(opType);
		if (response == null) {
			out.writeByte((byte) 2);
		} else {
			if (response instanceof IndexResponse) {
				out.writeByte((byte) 0);
			} else if (response instanceof DeleteResponse) {
				out.writeByte((byte) 1);
			}
			response.writeTo(out);
		}
		if (failure == null) {
			out.writeBoolean(false);
		} else {
			out.writeBoolean(true);
			out.writeUTF(failure.index());
			out.writeUTF(failure.type());
			out.writeUTF(failure.id());
			out.writeUTF(failure.message());
		}
	}
}
