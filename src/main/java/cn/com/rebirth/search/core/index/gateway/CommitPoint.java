/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core CommitPoint.java 2012-3-29 15:01:26 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.gateway;

import java.util.List;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.search.core.index.store.StoreFileMetaData;

import com.google.common.collect.ImmutableList;


/**
 * The Class CommitPoint.
 *
 * @author l.xue.nong
 */
public class CommitPoint {

	
	/** The Constant NULL. */
	public static final CommitPoint NULL = new CommitPoint(-1, "_null_", Type.GENERATED,
			ImmutableList.<CommitPoint.FileInfo> of(), ImmutableList.<CommitPoint.FileInfo> of());

	
	/**
	 * The Class FileInfo.
	 *
	 * @author l.xue.nong
	 */
	public static class FileInfo {

		
		/** The name. */
		private final String name;

		
		/** The physical name. */
		private final String physicalName;

		
		/** The length. */
		private final long length;

		
		/** The checksum. */
		private final String checksum;

		
		/**
		 * Instantiates a new file info.
		 *
		 * @param name the name
		 * @param physicalName the physical name
		 * @param length the length
		 * @param checksum the checksum
		 */
		public FileInfo(String name, String physicalName, long length, String checksum) {
			this.name = name;
			this.physicalName = physicalName;
			this.length = length;
			this.checksum = checksum;
		}

		
		/**
		 * Name.
		 *
		 * @return the string
		 */
		public String name() {
			return name;
		}

		
		/**
		 * Physical name.
		 *
		 * @return the string
		 */
		public String physicalName() {
			return this.physicalName;
		}

		
		/**
		 * Length.
		 *
		 * @return the long
		 */
		public long length() {
			return length;
		}

		
		/**
		 * Checksum.
		 *
		 * @return the string
		 */
		@Nullable
		public String checksum() {
			return checksum;
		}

		
		/**
		 * Checks if is same.
		 *
		 * @param md the md
		 * @return true, if is same
		 */
		public boolean isSame(StoreFileMetaData md) {
			if (checksum == null || md.checksum() == null) {
				return false;
			}
			return length == md.length() && checksum.equals(md.checksum());
		}
	}

	
	/**
	 * The Enum Type.
	 *
	 * @author l.xue.nong
	 */
	public static enum Type {

		
		/** The GENERATED. */
		GENERATED,

		
		/** The SAVED. */
		SAVED
	}

	
	/** The version. */
	private final long version;

	
	/** The name. */
	private final String name;

	
	/** The type. */
	private final Type type;

	
	/** The index files. */
	private final ImmutableList<FileInfo> indexFiles;

	
	/** The translog files. */
	private final ImmutableList<FileInfo> translogFiles;

	
	/**
	 * Instantiates a new commit point.
	 *
	 * @param version the version
	 * @param name the name
	 * @param type the type
	 * @param indexFiles the index files
	 * @param translogFiles the translog files
	 */
	public CommitPoint(long version, String name, Type type, List<FileInfo> indexFiles, List<FileInfo> translogFiles) {
		this.version = version;
		this.name = name;
		this.type = type;
		this.indexFiles = ImmutableList.copyOf(indexFiles);
		this.translogFiles = ImmutableList.copyOf(translogFiles);
	}

	
	/**
	 * Version.
	 *
	 * @return the long
	 */
	public long version() {
		return version;
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
	 * Type.
	 *
	 * @return the type
	 */
	public Type type() {
		return this.type;
	}

	
	/**
	 * Index files.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<FileInfo> indexFiles() {
		return this.indexFiles;
	}

	
	/**
	 * Translog files.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<FileInfo> translogFiles() {
		return this.translogFiles;
	}

	
	/**
	 * Contain physical index file.
	 *
	 * @param physicalName the physical name
	 * @return true, if successful
	 */
	public boolean containPhysicalIndexFile(String physicalName) {
		return findPhysicalIndexFile(physicalName) != null;
	}

	
	/**
	 * Find physical index file.
	 *
	 * @param physicalName the physical name
	 * @return the commit point. file info
	 */
	public CommitPoint.FileInfo findPhysicalIndexFile(String physicalName) {
		for (FileInfo file : indexFiles) {
			if (file.physicalName().equals(physicalName)) {
				return file;
			}
		}
		return null;
	}

	
	/**
	 * Find name file.
	 *
	 * @param name the name
	 * @return the commit point. file info
	 */
	public CommitPoint.FileInfo findNameFile(String name) {
		CommitPoint.FileInfo fileInfo = findNameIndexFile(name);
		if (fileInfo != null) {
			return fileInfo;
		}
		return findNameTranslogFile(name);
	}

	
	/**
	 * Find name index file.
	 *
	 * @param name the name
	 * @return the commit point. file info
	 */
	public CommitPoint.FileInfo findNameIndexFile(String name) {
		for (FileInfo file : indexFiles) {
			if (file.name().equals(name)) {
				return file;
			}
		}
		return null;
	}

	
	/**
	 * Find name translog file.
	 *
	 * @param name the name
	 * @return the commit point. file info
	 */
	public CommitPoint.FileInfo findNameTranslogFile(String name) {
		for (FileInfo file : translogFiles) {
			if (file.name().equals(name)) {
				return file;
			}
		}
		return null;
	}
}
