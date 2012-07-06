/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core Translog.java 2012-7-6 14:30:46 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.translog;

import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.index.Term;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.exception.RebirthIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.lease.Releasable;
import cn.com.rebirth.search.core.index.engine.Engine;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;

/**
 * The Interface Translog.
 *
 * @author l.xue.nong
 */
public interface Translog extends IndexShardComponent {

	/** The Constant TRANSLOG_ID_KEY. */
	public static final String TRANSLOG_ID_KEY = "translog_id";

	/**
	 * Current id.
	 *
	 * @return the long
	 */
	long currentId();

	/**
	 * Estimated number of operations.
	 *
	 * @return the int
	 */
	int estimatedNumberOfOperations();

	/**
	 * Memory size in bytes.
	 *
	 * @return the long
	 */
	long memorySizeInBytes();

	/**
	 * Translog size in bytes.
	 *
	 * @return the long
	 */
	long translogSizeInBytes();

	/**
	 * New translog.
	 *
	 * @param id the id
	 * @throws TranslogException the translog exception
	 */
	void newTranslog(long id) throws TranslogException;

	/**
	 * New transient translog.
	 *
	 * @param id the id
	 * @throws TranslogException the translog exception
	 */
	void newTransientTranslog(long id) throws TranslogException;

	/**
	 * Make transient current.
	 */
	void makeTransientCurrent();

	/**
	 * Revert transient.
	 */
	void revertTransient();

	/**
	 * Adds the.
	 *
	 * @param operation the operation
	 * @return the location
	 * @throws TranslogException the translog exception
	 */
	Location add(Operation operation) throws TranslogException;

	/**
	 * Read.
	 *
	 * @param location the location
	 * @return the byte[]
	 */
	byte[] read(Location location);

	/**
	 * Snapshot.
	 *
	 * @return the snapshot
	 * @throws TranslogException the translog exception
	 */
	Snapshot snapshot() throws TranslogException;

	/**
	 * Snapshot.
	 *
	 * @param snapshot the snapshot
	 * @return the snapshot
	 */
	Snapshot snapshot(Snapshot snapshot);

	/**
	 * Clear unreferenced.
	 */
	void clearUnreferenced();

	/**
	 * Sync.
	 */
	void sync();

	/**
	 * Sync needed.
	 *
	 * @return true, if successful
	 */
	boolean syncNeeded();

	/**
	 * Sync on each operation.
	 *
	 * @param syncOnEachOperation the sync on each operation
	 */
	void syncOnEachOperation(boolean syncOnEachOperation);

	/**
	 * Close.
	 *
	 * @param delete the delete
	 */
	void close(boolean delete);

	/**
	 * The Class Location.
	 *
	 * @author l.xue.nong
	 */
	static class Location {

		/** The translog id. */
		public final long translogId;

		/** The translog location. */
		public final long translogLocation;

		/** The size. */
		public final int size;

		/**
		 * Instantiates a new location.
		 *
		 * @param translogId the translog id
		 * @param translogLocation the translog location
		 * @param size the size
		 */
		public Location(long translogId, long translogLocation, int size) {
			this.translogId = translogId;
			this.translogLocation = translogLocation;
			this.size = size;
		}
	}

	/**
	 * The Interface Snapshot.
	 *
	 * @author l.xue.nong
	 */
	static interface Snapshot extends Releasable {

		/**
		 * Translog id.
		 *
		 * @return the long
		 */
		long translogId();

		/**
		 * Position.
		 *
		 * @return the long
		 */
		long position();

		/**
		 * Length.
		 *
		 * @return the long
		 */
		long length();

		/**
		 * Estimated total operations.
		 *
		 * @return the int
		 */
		int estimatedTotalOperations();

		/**
		 * Checks for next.
		 *
		 * @return true, if successful
		 */
		boolean hasNext();

		/**
		 * Next.
		 *
		 * @return the operation
		 */
		Operation next();

		/**
		 * Seek forward.
		 *
		 * @param length the length
		 */
		void seekForward(long length);

		/**
		 * Stream.
		 *
		 * @return the input stream
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		InputStream stream() throws IOException;

		/**
		 * Length in bytes.
		 *
		 * @return the long
		 */
		long lengthInBytes();
	}

	/**
	 * The Interface Operation.
	 *
	 * @author l.xue.nong
	 */
	static interface Operation extends Streamable {

		/**
		 * The Enum Type.
		 *
		 * @author l.xue.nong
		 */
		static enum Type {

			/** The create. */
			CREATE((byte) 1),

			/** The save. */
			SAVE((byte) 2),

			/** The delete. */
			DELETE((byte) 3),

			/** The delete by query. */
			DELETE_BY_QUERY((byte) 4);

			/** The id. */
			private final byte id;

			/**
			 * Instantiates a new type.
			 *
			 * @param id the id
			 */
			private Type(byte id) {
				this.id = id;
			}

			/**
			 * Id.
			 *
			 * @return the byte
			 */
			public byte id() {
				return this.id;
			}

			/**
			 * From id.
			 *
			 * @param id the id
			 * @return the type
			 */
			public static Type fromId(byte id) {
				switch (id) {
				case 1:
					return CREATE;
				case 2:
					return SAVE;
				case 3:
					return DELETE;
				case 4:
					return DELETE_BY_QUERY;
				default:
					throw new IllegalArgumentException("No type mapped for [" + id + "]");
				}
			}
		}

		/**
		 * Op type.
		 *
		 * @return the type
		 */
		Type opType();

		/**
		 * Estimate size.
		 *
		 * @return the long
		 */
		long estimateSize();

		/**
		 * Read source.
		 *
		 * @param in the in
		 * @return the source
		 * @throws IOException Signals that an I/O exception has occurred.
		 */
		Source readSource(StreamInput in) throws IOException;
	}

	/**
	 * The Class Source.
	 *
	 * @author l.xue.nong
	 */
	static class Source {

		/** The source. */
		public final BytesHolder source;

		/** The routing. */
		public final String routing;

		/** The parent. */
		public final String parent;

		/** The timestamp. */
		public final long timestamp;

		/** The ttl. */
		public final long ttl;

		/**
		 * Instantiates a new source.
		 *
		 * @param source the source
		 * @param routing the routing
		 * @param parent the parent
		 * @param timestamp the timestamp
		 * @param ttl the ttl
		 */
		public Source(BytesHolder source, String routing, String parent, long timestamp, long ttl) {
			this.source = source;
			this.routing = routing;
			this.parent = parent;
			this.timestamp = timestamp;
			this.ttl = ttl;
		}
	}

	/**
	 * The Class Create.
	 *
	 * @author l.xue.nong
	 */
	static class Create implements Operation {

		/** The id. */
		private String id;

		/** The type. */
		private String type;

		/** The source. */
		private BytesHolder source;

		/** The routing. */
		private String routing;

		/** The parent. */
		private String parent;

		/** The timestamp. */
		private long timestamp;

		/** The ttl. */
		private long ttl;

		/** The version. */
		private long version;

		/**
		 * Instantiates a new creates the.
		 */
		public Create() {
		}

		/**
		 * Instantiates a new creates the.
		 *
		 * @param create the create
		 */
		public Create(Engine.Create create) {
			this.id = create.id();
			this.type = create.type();
			this.source = new BytesHolder(create.source(), create.sourceOffset(), create.sourceLength());
			this.routing = create.routing();
			this.parent = create.parent();
			this.timestamp = create.timestamp();
			this.ttl = create.ttl();
			this.version = create.version();
		}

		/**
		 * Instantiates a new creates the.
		 *
		 * @param type the type
		 * @param id the id
		 * @param source the source
		 */
		public Create(String type, String id, byte[] source) {
			this.id = id;
			this.type = type;
			this.source = new BytesHolder(source);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.CREATE;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#estimateSize()
		 */
		@Override
		public long estimateSize() {
			return ((id.length() + type.length()) * 2) + source.length() + 12;
		}

		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return this.id;
		}

		/**
		 * Source.
		 *
		 * @return the bytes holder
		 */
		public BytesHolder source() {
			return this.source;
		}

		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		/**
		 * Routing.
		 *
		 * @return the string
		 */
		public String routing() {
			return this.routing;
		}

		/**
		 * Parent.
		 *
		 * @return the string
		 */
		public String parent() {
			return this.parent;
		}

		/**
		 * Timestamp.
		 *
		 * @return the long
		 */
		public long timestamp() {
			return this.timestamp;
		}

		/**
		 * Ttl.
		 *
		 * @return the long
		 */
		public long ttl() {
			return this.ttl;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#readSource(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public Source readSource(StreamInput in) throws IOException {
			readFrom(in);
			return new Source(source, routing, parent, timestamp, ttl);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			int version = in.readVInt();
			id = in.readUTF();
			type = in.readUTF();
			source = in.readBytesReference();
			if (version >= 1) {
				if (in.readBoolean()) {
					routing = in.readUTF();
				}
			}
			if (version >= 2) {
				if (in.readBoolean()) {
					parent = in.readUTF();
				}
			}
			if (version >= 3) {
				this.version = in.readLong();
			}
			if (version >= 4) {
				this.timestamp = in.readLong();
			}
			if (version >= 5) {
				this.ttl = in.readLong();
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(5);
			out.writeUTF(id);
			out.writeUTF(type);
			out.writeBytesHolder(source);
			if (routing == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(routing);
			}
			if (parent == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(parent);
			}
			out.writeLong(version);
			out.writeLong(timestamp);
			out.writeLong(ttl);
		}
	}

	/**
	 * The Class Index.
	 *
	 * @author l.xue.nong
	 */
	static class Index implements Operation {

		/** The id. */
		private String id;

		/** The type. */
		private String type;

		/** The version. */
		private long version;

		/** The source. */
		private BytesHolder source;

		/** The routing. */
		private String routing;

		/** The parent. */
		private String parent;

		/** The timestamp. */
		private long timestamp;

		/** The ttl. */
		private long ttl;

		/**
		 * Instantiates a new index.
		 */
		public Index() {
		}

		/**
		 * Instantiates a new index.
		 *
		 * @param index the index
		 */
		public Index(Engine.Index index) {
			this.id = index.id();
			this.type = index.type();
			this.source = new BytesHolder(index.source(), index.sourceOffset(), index.sourceLength());
			this.routing = index.routing();
			this.parent = index.parent();
			this.version = index.version();
			this.timestamp = index.timestamp();
			this.ttl = index.ttl();
		}

		/**
		 * Instantiates a new index.
		 *
		 * @param type the type
		 * @param id the id
		 * @param source the source
		 */
		public Index(String type, String id, byte[] source) {
			this.type = type;
			this.id = id;
			this.source = new BytesHolder(source);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.SAVE;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#estimateSize()
		 */
		@Override
		public long estimateSize() {
			return ((id.length() + type.length()) * 2) + source.length() + 12;
		}

		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.type;
		}

		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return this.id;
		}

		/**
		 * Routing.
		 *
		 * @return the string
		 */
		public String routing() {
			return this.routing;
		}

		/**
		 * Parent.
		 *
		 * @return the string
		 */
		public String parent() {
			return this.parent;
		}

		/**
		 * Timestamp.
		 *
		 * @return the long
		 */
		public long timestamp() {
			return this.timestamp;
		}

		/**
		 * Ttl.
		 *
		 * @return the long
		 */
		public long ttl() {
			return this.ttl;
		}

		/**
		 * Source.
		 *
		 * @return the bytes holder
		 */
		public BytesHolder source() {
			return this.source;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#readSource(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public Source readSource(StreamInput in) throws IOException {
			readFrom(in);
			return new Source(source, routing, parent, timestamp, ttl);
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			int version = in.readVInt();
			id = in.readUTF();
			type = in.readUTF();
			source = in.readBytesReference();
			if (version >= 1) {
				if (in.readBoolean()) {
					routing = in.readUTF();
				}
			}
			if (version >= 2) {
				if (in.readBoolean()) {
					parent = in.readUTF();
				}
			}
			if (version >= 3) {
				this.version = in.readLong();
			}
			if (version >= 4) {
				this.timestamp = in.readLong();
			}
			if (version >= 5) {
				this.ttl = in.readLong();
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(5);
			out.writeUTF(id);
			out.writeUTF(type);
			out.writeBytesHolder(source);
			if (routing == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(routing);
			}
			if (parent == null) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeUTF(parent);
			}
			out.writeLong(version);
			out.writeLong(timestamp);
			out.writeLong(ttl);
		}
	}

	/**
	 * The Class Delete.
	 *
	 * @author l.xue.nong
	 */
	static class Delete implements Operation {

		/** The uid. */
		private Term uid;

		/** The version. */
		private long version;

		/**
		 * Instantiates a new delete.
		 */
		public Delete() {
		}

		/**
		 * Instantiates a new delete.
		 *
		 * @param delete the delete
		 */
		public Delete(Engine.Delete delete) {
			this(delete.uid());
			this.version = delete.version();
		}

		/**
		 * Instantiates a new delete.
		 *
		 * @param uid the uid
		 */
		public Delete(Term uid) {
			this.uid = uid;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.DELETE;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#estimateSize()
		 */
		@Override
		public long estimateSize() {
			return ((uid.field().length() + uid.text().length()) * 2) + 20;
		}

		/**
		 * Uid.
		 *
		 * @return the term
		 */
		public Term uid() {
			return this.uid;
		}

		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#readSource(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public Source readSource(StreamInput in) throws IOException {
			throw new RebirthIllegalStateException("trying to read doc source from delete operation");
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			int version = in.readVInt();
			uid = new Term(in.readUTF(), in.readUTF());
			if (version >= 1) {
				this.version = in.readLong();
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(1);
			out.writeUTF(uid.field());
			out.writeUTF(uid.text());
			out.writeLong(version);
		}
	}

	/**
	 * The Class DeleteByQuery.
	 *
	 * @author l.xue.nong
	 */
	static class DeleteByQuery implements Operation {

		/** The source. */
		private BytesHolder source;

		/** The filtering aliases. */
		@Nullable
		private String[] filteringAliases;

		/** The types. */
		private String[] types = Strings.EMPTY_ARRAY;

		/**
		 * Instantiates a new delete by query.
		 */
		public DeleteByQuery() {
		}

		/**
		 * Instantiates a new delete by query.
		 *
		 * @param deleteByQuery the delete by query
		 */
		public DeleteByQuery(Engine.DeleteByQuery deleteByQuery) {
			this(deleteByQuery.source(), deleteByQuery.filteringAliases(), deleteByQuery.types());
		}

		/**
		 * Instantiates a new delete by query.
		 *
		 * @param source the source
		 * @param filteringAliases the filtering aliases
		 * @param types the types
		 */
		public DeleteByQuery(BytesHolder source, String[] filteringAliases, String... types) {
			this.source = source;
			this.types = types == null ? Strings.EMPTY_ARRAY : types;
			this.filteringAliases = filteringAliases;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.DELETE_BY_QUERY;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#estimateSize()
		 */
		@Override
		public long estimateSize() {
			return source.length() + 8;
		}

		/**
		 * Source.
		 *
		 * @return the bytes holder
		 */
		public BytesHolder source() {
			return this.source;
		}

		/**
		 * Filtering aliases.
		 *
		 * @return the string[]
		 */
		public String[] filteringAliases() {
			return filteringAliases;
		}

		/**
		 * Types.
		 *
		 * @return the string[]
		 */
		public String[] types() {
			return this.types;
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.search.core.index.translog.Translog.Operation#readSource(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public Source readSource(StreamInput in) throws IOException {
			throw new RebirthIllegalStateException("trying to read doc source from delete_by_query operation");
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#readFrom(cn.com.rebirth.commons.io.stream.StreamInput)
		 */
		@Override
		public void readFrom(StreamInput in) throws IOException {
			int version = in.readVInt();
			source = in.readBytesReference();
			if (version < 2) {

				if (in.readBoolean()) {
					in.readUTF();
				}
			}
			int typesSize = in.readVInt();
			if (typesSize > 0) {
				types = new String[typesSize];
				for (int i = 0; i < typesSize; i++) {
					types[i] = in.readUTF();
				}
			}
			if (version >= 1) {
				int aliasesSize = in.readVInt();
				if (aliasesSize > 0) {
					filteringAliases = new String[aliasesSize];
					for (int i = 0; i < aliasesSize; i++) {
						filteringAliases[i] = in.readUTF();
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see cn.com.rebirth.commons.io.stream.Streamable#writeTo(cn.com.rebirth.commons.io.stream.StreamOutput)
		 */
		@Override
		public void writeTo(StreamOutput out) throws IOException {
			out.writeVInt(2);
			out.writeBytesHolder(source);
			out.writeVInt(types.length);
			for (String type : types) {
				out.writeUTF(type);
			}
			if (filteringAliases != null) {
				out.writeVInt(filteringAliases.length);
				for (String alias : filteringAliases) {
					out.writeUTF(alias);
				}
			} else {
				out.writeVInt(0);
			}
		}
	}
}
