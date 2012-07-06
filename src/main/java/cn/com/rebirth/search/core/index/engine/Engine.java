/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Engine.java 2012-3-29 15:01:42 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.engine;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.ExtendedIndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.exception.RestartException;
import cn.com.rebirth.commons.unit.ByteSizeValue;
import cn.com.rebirth.commons.unit.TimeValue;
import cn.com.rebirth.search.commons.component.CloseableComponent;
import cn.com.rebirth.search.commons.lease.Releasable;
import cn.com.rebirth.search.commons.lucene.uid.UidField;
import cn.com.rebirth.search.core.index.VersionType;
import cn.com.rebirth.search.core.index.deletionpolicy.SnapshotIndexCommit;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.ParsedDocument;
import cn.com.rebirth.search.core.index.mapper.internal.UidFieldMapper;
import cn.com.rebirth.search.core.index.shard.IndexShardComponent;
import cn.com.rebirth.search.core.index.shard.ShardId;
import cn.com.rebirth.search.core.index.translog.Translog;


/**
 * The Interface Engine.
 *
 * @author l.xue.nong
 */
public interface Engine extends IndexShardComponent, CloseableComponent {

	
	/** The INACTIV e_ shar d_ indexin g_ buffer. */
	static ByteSizeValue INACTIVE_SHARD_INDEXING_BUFFER = ByteSizeValue.parseBytesSizeValue("500kb");

	
	/**
	 * Default refresh interval.
	 *
	 * @return the time value
	 */
	TimeValue defaultRefreshInterval();

	
	/**
	 * Enable gc deletes.
	 *
	 * @param enableGcDeletes the enable gc deletes
	 */
	void enableGcDeletes(boolean enableGcDeletes);

	
	/**
	 * Update indexing buffer size.
	 *
	 * @param indexingBufferSize the indexing buffer size
	 */
	void updateIndexingBufferSize(ByteSizeValue indexingBufferSize);

	
	/**
	 * Adds the failed engine listener.
	 *
	 * @param listener the listener
	 */
	void addFailedEngineListener(FailedEngineListener listener);

	
	/**
	 * Start.
	 *
	 * @throws EngineException the engine exception
	 */
	void start() throws EngineException;

	
	/**
	 * Creates the.
	 *
	 * @param create the create
	 * @throws EngineException the engine exception
	 */
	void create(Create create) throws EngineException;

	
	/**
	 * Index.
	 *
	 * @param index the index
	 * @throws EngineException the engine exception
	 */
	void index(Index index) throws EngineException;

	
	/**
	 * Delete.
	 *
	 * @param delete the delete
	 * @throws EngineException the engine exception
	 */
	void delete(Delete delete) throws EngineException;

	
	/**
	 * Delete.
	 *
	 * @param delete the delete
	 * @throws EngineException the engine exception
	 */
	void delete(DeleteByQuery delete) throws EngineException;

	
	/**
	 * Gets the.
	 *
	 * @param get the get
	 * @return the gets the result
	 * @throws EngineException the engine exception
	 */
	GetResult get(Get get) throws EngineException;

	
	/**
	 * Searcher.
	 *
	 * @return the searcher
	 * @throws EngineException the engine exception
	 */
	Searcher searcher() throws EngineException;

	
	/**
	 * Segments.
	 *
	 * @return the list
	 */
	List<Segment> segments();

	
	/**
	 * Refresh needed.
	 *
	 * @return true, if successful
	 */
	boolean refreshNeeded();

	
	/**
	 * Possible merge needed.
	 *
	 * @return true, if successful
	 */
	boolean possibleMergeNeeded();

	
	/**
	 * Maybe merge.
	 *
	 * @throws EngineException the engine exception
	 */
	void maybeMerge() throws EngineException;

	
	/**
	 * Refresh.
	 *
	 * @param refresh the refresh
	 * @throws EngineException the engine exception
	 */
	void refresh(Refresh refresh) throws EngineException;

	
	/**
	 * Flush.
	 *
	 * @param flush the flush
	 * @throws EngineException the engine exception
	 * @throws FlushNotAllowedEngineException the flush not allowed engine exception
	 */
	void flush(Flush flush) throws EngineException, FlushNotAllowedEngineException;

	
	/**
	 * Optimize.
	 *
	 * @param optimize the optimize
	 * @throws EngineException the engine exception
	 */
	void optimize(Optimize optimize) throws EngineException;

	
	/**
	 * Snapshot.
	 *
	 * @param <T> the generic type
	 * @param snapshotHandler the snapshot handler
	 * @return the t
	 * @throws EngineException the engine exception
	 */
	<T> T snapshot(SnapshotHandler<T> snapshotHandler) throws EngineException;

	
	/**
	 * Recover.
	 *
	 * @param recoveryHandler the recovery handler
	 * @throws EngineException the engine exception
	 */
	void recover(RecoveryHandler recoveryHandler) throws EngineException;

	
	/**
	 * The listener interface for receiving failedEngine events.
	 * The class that is interested in processing a failedEngine
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addFailedEngineListener<code> method. When
	 * the failedEngine event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see FailedEngineEvent
	 */
	static interface FailedEngineListener {

		
		/**
		 * On failed engine.
		 *
		 * @param shardId the shard id
		 * @param t the t
		 */
		void onFailedEngine(ShardId shardId, Throwable t);
	}

	
	/**
	 * The Interface RecoveryHandler.
	 *
	 * @author l.xue.nong
	 */
	static interface RecoveryHandler {

		
		/**
		 * Phase1.
		 *
		 * @param snapshot the snapshot
		 * @throws SumMallSearchException the sum mall search exception
		 */
		void phase1(SnapshotIndexCommit snapshot) throws RestartException;

		
		/**
		 * Phase2.
		 *
		 * @param snapshot the snapshot
		 * @throws SumMallSearchException the sum mall search exception
		 */
		void phase2(Translog.Snapshot snapshot) throws RestartException;

		
		/**
		 * Phase3.
		 *
		 * @param snapshot the snapshot
		 * @throws SumMallSearchException the sum mall search exception
		 */
		void phase3(Translog.Snapshot snapshot) throws RestartException;
	}

	
	/**
	 * The Interface SnapshotHandler.
	 *
	 * @param <T> the generic type
	 * @author l.xue.nong
	 */
	static interface SnapshotHandler<T> {

		
		/**
		 * Snapshot.
		 *
		 * @param snapshotIndexCommit the snapshot index commit
		 * @param translogSnapshot the translog snapshot
		 * @return the t
		 * @throws EngineException the engine exception
		 */
		T snapshot(SnapshotIndexCommit snapshotIndexCommit, Translog.Snapshot translogSnapshot) throws EngineException;
	}

	
	/**
	 * The Interface Searcher.
	 *
	 * @author l.xue.nong
	 */
	static interface Searcher extends Releasable {

		
		/**
		 * Reader.
		 *
		 * @return the index reader
		 */
		IndexReader reader();

		
		/**
		 * Searcher.
		 *
		 * @return the extended index searcher
		 */
		ExtendedIndexSearcher searcher();
	}

	
	/**
	 * The Class Refresh.
	 *
	 * @author l.xue.nong
	 */
	static class Refresh {

		
		/** The wait for operations. */
		private final boolean waitForOperations;

		
		/** The force. */
		private boolean force = false;

		
		/**
		 * Instantiates a new refresh.
		 *
		 * @param waitForOperations the wait for operations
		 */
		public Refresh(boolean waitForOperations) {
			this.waitForOperations = waitForOperations;
		}

		
		/**
		 * Force.
		 *
		 * @param force the force
		 * @return the refresh
		 */
		public Refresh force(boolean force) {
			this.force = force;
			return this;
		}

		
		/**
		 * Force.
		 *
		 * @return true, if successful
		 */
		public boolean force() {
			return this.force;
		}

		
		/**
		 * Wait for operations.
		 *
		 * @return true, if successful
		 */
		public boolean waitForOperations() {
			return waitForOperations;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "waitForOperations[" + waitForOperations + "]";
		}
	}

	
	/**
	 * The Class Flush.
	 *
	 * @author l.xue.nong
	 */
	static class Flush {

		
		/** The full. */
		private boolean full = false;

		
		/** The refresh. */
		private boolean refresh = false;

		
		/** The force. */
		private boolean force = false;

		
		/**
		 * Refresh.
		 *
		 * @return true, if successful
		 */
		public boolean refresh() {
			return this.refresh;
		}

		
		/**
		 * Refresh.
		 *
		 * @param refresh the refresh
		 * @return the flush
		 */
		public Flush refresh(boolean refresh) {
			this.refresh = refresh;
			return this;
		}

		
		/**
		 * Full.
		 *
		 * @return true, if successful
		 */
		public boolean full() {
			return this.full;
		}

		
		/**
		 * Full.
		 *
		 * @param full the full
		 * @return the flush
		 */
		public Flush full(boolean full) {
			this.full = full;
			return this;
		}

		
		/**
		 * Force.
		 *
		 * @return true, if successful
		 */
		public boolean force() {
			return this.force;
		}

		
		/**
		 * Force.
		 *
		 * @param force the force
		 * @return the flush
		 */
		public Flush force(boolean force) {
			this.force = force;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "full[" + full + "], refresh[" + refresh + "], force[" + force + "]";
		}
	}

	
	/**
	 * The Class Optimize.
	 *
	 * @author l.xue.nong
	 */
	static class Optimize {

		
		/** The wait for merge. */
		private boolean waitForMerge = true;

		
		/** The max num segments. */
		private int maxNumSegments = -1;

		
		/** The only expunge deletes. */
		private boolean onlyExpungeDeletes = false;

		
		/** The flush. */
		private boolean flush = false;

		
		/** The refresh. */
		private boolean refresh = false;

		
		/**
		 * Instantiates a new optimize.
		 */
		public Optimize() {
		}

		
		/**
		 * Wait for merge.
		 *
		 * @return true, if successful
		 */
		public boolean waitForMerge() {
			return waitForMerge;
		}

		
		/**
		 * Wait for merge.
		 *
		 * @param waitForMerge the wait for merge
		 * @return the optimize
		 */
		public Optimize waitForMerge(boolean waitForMerge) {
			this.waitForMerge = waitForMerge;
			return this;
		}

		
		/**
		 * Max num segments.
		 *
		 * @return the int
		 */
		public int maxNumSegments() {
			return maxNumSegments;
		}

		
		/**
		 * Max num segments.
		 *
		 * @param maxNumSegments the max num segments
		 * @return the optimize
		 */
		public Optimize maxNumSegments(int maxNumSegments) {
			this.maxNumSegments = maxNumSegments;
			return this;
		}

		
		/**
		 * Only expunge deletes.
		 *
		 * @return true, if successful
		 */
		public boolean onlyExpungeDeletes() {
			return onlyExpungeDeletes;
		}

		
		/**
		 * Only expunge deletes.
		 *
		 * @param onlyExpungeDeletes the only expunge deletes
		 * @return the optimize
		 */
		public Optimize onlyExpungeDeletes(boolean onlyExpungeDeletes) {
			this.onlyExpungeDeletes = onlyExpungeDeletes;
			return this;
		}

		
		/**
		 * Flush.
		 *
		 * @return true, if successful
		 */
		public boolean flush() {
			return flush;
		}

		
		/**
		 * Flush.
		 *
		 * @param flush the flush
		 * @return the optimize
		 */
		public Optimize flush(boolean flush) {
			this.flush = flush;
			return this;
		}

		
		/**
		 * Refresh.
		 *
		 * @return true, if successful
		 */
		public boolean refresh() {
			return refresh;
		}

		
		/**
		 * Refresh.
		 *
		 * @param refresh the refresh
		 * @return the optimize
		 */
		public Optimize refresh(boolean refresh) {
			this.refresh = refresh;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "waitForMerge[" + waitForMerge + "], maxNumSegments[" + maxNumSegments + "], onlyExpungeDeletes["
					+ onlyExpungeDeletes + "], flush[" + flush + "], refresh[" + refresh + "]";
		}
	}

	
	/**
	 * The Interface Operation.
	 *
	 * @author l.xue.nong
	 */
	static interface Operation {

		
		/**
		 * The Enum Type.
		 *
		 * @author l.xue.nong
		 */
		static enum Type {

			
			/** The CREATE. */
			CREATE,

			
			/** The INDEX. */
			INDEX,

			
			/** The DELETE. */
			DELETE
		}

		
		/**
		 * The Enum Origin.
		 *
		 * @author l.xue.nong
		 */
		static enum Origin {

			
			/** The PRIMARY. */
			PRIMARY,

			
			/** The REPLICA. */
			REPLICA,

			
			/** The RECOVERY. */
			RECOVERY
		}

		
		/**
		 * Op type.
		 *
		 * @return the type
		 */
		Type opType();

		
		/**
		 * Origin.
		 *
		 * @return the origin
		 */
		Origin origin();
	}

	
	/**
	 * The Interface IndexingOperation.
	 *
	 * @author l.xue.nong
	 */
	static interface IndexingOperation extends Operation {

		
		/**
		 * Parsed doc.
		 *
		 * @return the parsed document
		 */
		ParsedDocument parsedDoc();

		
		/**
		 * Docs.
		 *
		 * @return the list
		 */
		List<Document> docs();

		
		/**
		 * Doc mapper.
		 *
		 * @return the document mapper
		 */
		DocumentMapper docMapper();
	}

	
	/**
	 * The Class Create.
	 *
	 * @author l.xue.nong
	 */
	static class Create implements IndexingOperation {

		
		/** The doc mapper. */
		private final DocumentMapper docMapper;

		
		/** The uid. */
		private final Term uid;

		
		/** The doc. */
		private final ParsedDocument doc;

		
		/** The version. */
		private long version;

		
		/** The version type. */
		private VersionType versionType = VersionType.INTERNAL;

		
		/** The origin. */
		private Origin origin = Origin.PRIMARY;

		
		/** The start time. */
		private long startTime;

		
		/** The end time. */
		private long endTime;

		
		/**
		 * Instantiates a new creates the.
		 *
		 * @param docMapper the doc mapper
		 * @param uid the uid
		 * @param doc the doc
		 */
		public Create(DocumentMapper docMapper, Term uid, ParsedDocument doc) {
			this.docMapper = docMapper;
			this.uid = uid;
			this.doc = doc;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#docMapper()
		 */
		public DocumentMapper docMapper() {
			return this.docMapper;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.CREATE;
		}

		
		/**
		 * Origin.
		 *
		 * @param origin the origin
		 * @return the creates the
		 */
		public Create origin(Origin origin) {
			this.origin = origin;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#origin()
		 */
		@Override
		public Origin origin() {
			return this.origin;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#parsedDoc()
		 */
		public ParsedDocument parsedDoc() {
			return this.doc;
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
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.doc.type();
		}

		
		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return this.doc.id();
		}

		
		/**
		 * Routing.
		 *
		 * @return the string
		 */
		public String routing() {
			return this.doc.routing();
		}

		
		/**
		 * Timestamp.
		 *
		 * @return the long
		 */
		public long timestamp() {
			return this.doc.timestamp();
		}

		
		/**
		 * Ttl.
		 *
		 * @return the long
		 */
		public long ttl() {
			return this.doc.ttl();
		}

		
		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		
		/**
		 * Version.
		 *
		 * @param version the version
		 * @return the creates the
		 */
		public Create version(long version) {
			this.version = version;
			return this;
		}

		
		/**
		 * Version type.
		 *
		 * @return the version type
		 */
		public VersionType versionType() {
			return this.versionType;
		}

		
		/**
		 * Version type.
		 *
		 * @param versionType the version type
		 * @return the creates the
		 */
		public Create versionType(VersionType versionType) {
			this.versionType = versionType;
			return this;
		}

		
		/**
		 * Parent.
		 *
		 * @return the string
		 */
		public String parent() {
			return this.doc.parent();
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#docs()
		 */
		public List<Document> docs() {
			return this.doc.docs();
		}

		
		/**
		 * Analyzer.
		 *
		 * @return the analyzer
		 */
		public Analyzer analyzer() {
			return this.doc.analyzer();
		}

		
		/**
		 * Source.
		 *
		 * @return the byte[]
		 */
		public byte[] source() {
			return this.doc.source();
		}

		
		/**
		 * Source offset.
		 *
		 * @return the int
		 */
		public int sourceOffset() {
			return this.doc.sourceOffset();
		}

		
		/**
		 * Source length.
		 *
		 * @return the int
		 */
		public int sourceLength() {
			return this.doc.sourceLength();
		}

		
		/**
		 * Uid field.
		 *
		 * @return the uid field
		 */
		public UidField uidField() {
			return (UidField) doc.rootDoc().getFieldable(UidFieldMapper.NAME);
		}

		
		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 * @return the creates the
		 */
		public Create startTime(long startTime) {
			this.startTime = startTime;
			return this;
		}

		
		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		
		/**
		 * End time.
		 *
		 * @param endTime the end time
		 * @return the creates the
		 */
		public Create endTime(long endTime) {
			this.endTime = endTime;
			return this;
		}

		
		/**
		 * End time.
		 *
		 * @return the long
		 */
		public long endTime() {
			return this.endTime;
		}
	}

	
	/**
	 * The Class Index.
	 *
	 * @author l.xue.nong
	 */
	static class Index implements IndexingOperation {

		
		/** The doc mapper. */
		private final DocumentMapper docMapper;

		
		/** The uid. */
		private final Term uid;

		
		/** The doc. */
		private final ParsedDocument doc;

		
		/** The version. */
		private long version;

		
		/** The version type. */
		private VersionType versionType = VersionType.INTERNAL;

		
		/** The origin. */
		private Origin origin = Origin.PRIMARY;

		
		/** The start time. */
		private long startTime;

		
		/** The end time. */
		private long endTime;

		
		/**
		 * Instantiates a new index.
		 *
		 * @param docMapper the doc mapper
		 * @param uid the uid
		 * @param doc the doc
		 */
		public Index(DocumentMapper docMapper, Term uid, ParsedDocument doc) {
			this.docMapper = docMapper;
			this.uid = uid;
			this.doc = doc;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#docMapper()
		 */
		public DocumentMapper docMapper() {
			return this.docMapper;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.INDEX;
		}

		
		/**
		 * Origin.
		 *
		 * @param origin the origin
		 * @return the index
		 */
		public Index origin(Origin origin) {
			this.origin = origin;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#origin()
		 */
		@Override
		public Origin origin() {
			return this.origin;
		}

		
		/**
		 * Uid.
		 *
		 * @return the term
		 */
		public Term uid() {
			return this.uid;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#parsedDoc()
		 */
		public ParsedDocument parsedDoc() {
			return this.doc;
		}

		
		/**
		 * Version.
		 *
		 * @param version the version
		 * @return the index
		 */
		public Index version(long version) {
			this.version = version;
			return this;
		}

		
		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		
		/**
		 * Version type.
		 *
		 * @param versionType the version type
		 * @return the index
		 */
		public Index versionType(VersionType versionType) {
			this.versionType = versionType;
			return this;
		}

		
		/**
		 * Version type.
		 *
		 * @return the version type
		 */
		public VersionType versionType() {
			return this.versionType;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.IndexingOperation#docs()
		 */
		public List<Document> docs() {
			return this.doc.docs();
		}

		
		/**
		 * Analyzer.
		 *
		 * @return the analyzer
		 */
		public Analyzer analyzer() {
			return this.doc.analyzer();
		}

		
		/**
		 * Id.
		 *
		 * @return the string
		 */
		public String id() {
			return this.doc.id();
		}

		
		/**
		 * Type.
		 *
		 * @return the string
		 */
		public String type() {
			return this.doc.type();
		}

		
		/**
		 * Routing.
		 *
		 * @return the string
		 */
		public String routing() {
			return this.doc.routing();
		}

		
		/**
		 * Parent.
		 *
		 * @return the string
		 */
		public String parent() {
			return this.doc.parent();
		}

		
		/**
		 * Timestamp.
		 *
		 * @return the long
		 */
		public long timestamp() {
			return this.doc.timestamp();
		}

		
		/**
		 * Ttl.
		 *
		 * @return the long
		 */
		public long ttl() {
			return this.doc.ttl();
		}

		
		/**
		 * Source.
		 *
		 * @return the byte[]
		 */
		public byte[] source() {
			return this.doc.source();
		}

		
		/**
		 * Source offset.
		 *
		 * @return the int
		 */
		public int sourceOffset() {
			return this.doc.sourceOffset();
		}

		
		/**
		 * Source length.
		 *
		 * @return the int
		 */
		public int sourceLength() {
			return this.doc.sourceLength();
		}

		
		/**
		 * Uid field.
		 *
		 * @return the uid field
		 */
		public UidField uidField() {
			return (UidField) doc.rootDoc().getFieldable(UidFieldMapper.NAME);
		}

		
		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 * @return the index
		 */
		public Index startTime(long startTime) {
			this.startTime = startTime;
			return this;
		}

		
		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		
		/**
		 * End time.
		 *
		 * @param endTime the end time
		 * @return the index
		 */
		public Index endTime(long endTime) {
			this.endTime = endTime;
			return this;
		}

		
		/**
		 * End time.
		 *
		 * @return the long
		 */
		public long endTime() {
			return this.endTime;
		}
	}

	
	/**
	 * The Class Delete.
	 *
	 * @author l.xue.nong
	 */
	static class Delete implements Operation {

		
		/** The type. */
		private final String type;

		
		/** The id. */
		private final String id;

		
		/** The uid. */
		private final Term uid;

		
		/** The version. */
		private long version;

		
		/** The version type. */
		private VersionType versionType = VersionType.INTERNAL;

		
		/** The origin. */
		private Origin origin = Origin.PRIMARY;

		
		/** The not found. */
		private boolean notFound;

		
		/** The start time. */
		private long startTime;

		
		/** The end time. */
		private long endTime;

		
		/**
		 * Instantiates a new delete.
		 *
		 * @param type the type
		 * @param id the id
		 * @param uid the uid
		 */
		public Delete(String type, String id, Term uid) {
			this.type = type;
			this.id = id;
			this.uid = uid;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#opType()
		 */
		@Override
		public Type opType() {
			return Type.DELETE;
		}

		
		/**
		 * Origin.
		 *
		 * @param origin the origin
		 * @return the delete
		 */
		public Delete origin(Origin origin) {
			this.origin = origin;
			return this;
		}

		
		/* (non-Javadoc)
		 * @see cn.com.summall.search.core.index.engine.Engine.Operation#origin()
		 */
		@Override
		public Origin origin() {
			return this.origin;
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
		 * @param version the version
		 * @return the delete
		 */
		public Delete version(long version) {
			this.version = version;
			return this;
		}

		
		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		
		/**
		 * Version type.
		 *
		 * @param versionType the version type
		 * @return the delete
		 */
		public Delete versionType(VersionType versionType) {
			this.versionType = versionType;
			return this;
		}

		
		/**
		 * Version type.
		 *
		 * @return the version type
		 */
		public VersionType versionType() {
			return this.versionType;
		}

		
		/**
		 * Not found.
		 *
		 * @return true, if successful
		 */
		public boolean notFound() {
			return this.notFound;
		}

		
		/**
		 * Not found.
		 *
		 * @param notFound the not found
		 * @return the delete
		 */
		public Delete notFound(boolean notFound) {
			this.notFound = notFound;
			return this;
		}

		
		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 * @return the delete
		 */
		public Delete startTime(long startTime) {
			this.startTime = startTime;
			return this;
		}

		
		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		
		/**
		 * End time.
		 *
		 * @param endTime the end time
		 * @return the delete
		 */
		public Delete endTime(long endTime) {
			this.endTime = endTime;
			return this;
		}

		
		/**
		 * End time.
		 *
		 * @return the long
		 */
		public long endTime() {
			return this.endTime;
		}
	}

	
	/**
	 * The Class DeleteByQuery.
	 *
	 * @author l.xue.nong
	 */
	static class DeleteByQuery {

		
		/** The query. */
		private final Query query;

		
		/** The source. */
		private final BytesHolder source;

		
		/** The filtering aliases. */
		private final String[] filteringAliases;

		
		/** The alias filter. */
		private final Filter aliasFilter;

		
		/** The types. */
		private final String[] types;

		
		/** The start time. */
		private long startTime;

		
		/** The end time. */
		private long endTime;

		
		/**
		 * Instantiates a new delete by query.
		 *
		 * @param query the query
		 * @param source the source
		 * @param filteringAliases the filtering aliases
		 * @param aliasFilter the alias filter
		 * @param types the types
		 */
		public DeleteByQuery(Query query, BytesHolder source, @Nullable String[] filteringAliases,
				@Nullable Filter aliasFilter, String... types) {
			this.query = query;
			this.source = source;
			this.types = types;
			this.filteringAliases = filteringAliases;
			this.aliasFilter = aliasFilter;
		}

		
		/**
		 * Query.
		 *
		 * @return the query
		 */
		public Query query() {
			return this.query;
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
		 * Types.
		 *
		 * @return the string[]
		 */
		public String[] types() {
			return this.types;
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
		 * Alias filter.
		 *
		 * @return the filter
		 */
		public Filter aliasFilter() {
			return aliasFilter;
		}

		
		/**
		 * Start time.
		 *
		 * @param startTime the start time
		 * @return the delete by query
		 */
		public DeleteByQuery startTime(long startTime) {
			this.startTime = startTime;
			return this;
		}

		
		/**
		 * Start time.
		 *
		 * @return the long
		 */
		public long startTime() {
			return this.startTime;
		}

		
		/**
		 * End time.
		 *
		 * @param endTime the end time
		 * @return the delete by query
		 */
		public DeleteByQuery endTime(long endTime) {
			this.endTime = endTime;
			return this;
		}

		
		/**
		 * End time.
		 *
		 * @return the long
		 */
		public long endTime() {
			return this.endTime;
		}
	}

	
	/**
	 * The Class Get.
	 *
	 * @author l.xue.nong
	 */
	static class Get {

		
		/** The realtime. */
		private final boolean realtime;

		
		/** The uid. */
		private final Term uid;

		
		/** The load source. */
		private boolean loadSource = true;

		
		/**
		 * Instantiates a new gets the.
		 *
		 * @param realtime the realtime
		 * @param uid the uid
		 */
		public Get(boolean realtime, Term uid) {
			this.realtime = realtime;
			this.uid = uid;
		}

		
		/**
		 * Realtime.
		 *
		 * @return true, if successful
		 */
		public boolean realtime() {
			return this.realtime;
		}

		
		/**
		 * Uid.
		 *
		 * @return the term
		 */
		public Term uid() {
			return uid;
		}

		
		/**
		 * Load source.
		 *
		 * @return true, if successful
		 */
		public boolean loadSource() {
			return this.loadSource;
		}

		
		/**
		 * Load source.
		 *
		 * @param loadSource the load source
		 * @return the gets the
		 */
		public Get loadSource(boolean loadSource) {
			this.loadSource = loadSource;
			return this;
		}
	}

	
	/**
	 * The Class GetResult.
	 *
	 * @author l.xue.nong
	 */
	static class GetResult {

		
		/** The exists. */
		private final boolean exists;

		
		/** The version. */
		private final long version;

		
		/** The source. */
		private final Translog.Source source;

		
		/** The doc id and version. */
		private final UidField.DocIdAndVersion docIdAndVersion;

		
		/** The searcher. */
		private final Searcher searcher;

		
		/** The Constant NOT_EXISTS. */
		public static final GetResult NOT_EXISTS = new GetResult(false, -1, null);

		
		/**
		 * Instantiates a new gets the result.
		 *
		 * @param exists the exists
		 * @param version the version
		 * @param source the source
		 */
		public GetResult(boolean exists, long version, @Nullable Translog.Source source) {
			this.source = source;
			this.exists = exists;
			this.version = version;
			this.docIdAndVersion = null;
			this.searcher = null;
		}

		
		/**
		 * Instantiates a new gets the result.
		 *
		 * @param searcher the searcher
		 * @param docIdAndVersion the doc id and version
		 */
		public GetResult(Searcher searcher, UidField.DocIdAndVersion docIdAndVersion) {
			this.exists = true;
			this.source = null;
			this.version = docIdAndVersion.version;
			this.docIdAndVersion = docIdAndVersion;
			this.searcher = searcher;
		}

		
		/**
		 * Exists.
		 *
		 * @return true, if successful
		 */
		public boolean exists() {
			return exists;
		}

		
		/**
		 * Version.
		 *
		 * @return the long
		 */
		public long version() {
			return this.version;
		}

		
		/**
		 * Source.
		 *
		 * @return the translog. source
		 */
		@Nullable
		public Translog.Source source() {
			return source;
		}

		
		/**
		 * Searcher.
		 *
		 * @return the searcher
		 */
		public Searcher searcher() {
			return this.searcher;
		}

		
		/**
		 * Doc id and version.
		 *
		 * @return the uid field. doc id and version
		 */
		public UidField.DocIdAndVersion docIdAndVersion() {
			return docIdAndVersion;
		}

		
		/**
		 * Release.
		 */
		public void release() {
			if (searcher != null) {
				searcher.release();
			}
		}
	}

}
