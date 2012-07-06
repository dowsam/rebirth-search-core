/*
 * Copyright (c) 2005-2012 www.china-cti.com All rights reserved
 * Info:rebirth-search-core CommitPoints.java 2012-7-6 14:29:06 l.xue.nong$$
 */

package cn.com.rebirth.search.core.index.gateway;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.XContentType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * The Class CommitPoints.
 *
 * @author l.xue.nong
 */
public class CommitPoints implements Iterable<CommitPoint> {

	/** The commit points. */
	private final ImmutableList<CommitPoint> commitPoints;

	/**
	 * Instantiates a new commit points.
	 *
	 * @param commitPoints the commit points
	 */
	public CommitPoints(List<CommitPoint> commitPoints) {
		Collections.sort(commitPoints, new Comparator<CommitPoint>() {
			@Override
			public int compare(CommitPoint o1, CommitPoint o2) {
				return (o2.version() < o1.version() ? -1 : (o2.version() == o1.version() ? 0 : 1));
			}
		});
		this.commitPoints = ImmutableList.copyOf(commitPoints);
	}

	/**
	 * Commits.
	 *
	 * @return the immutable list
	 */
	public ImmutableList<CommitPoint> commits() {
		return this.commitPoints;
	}

	/**
	 * Checks for version.
	 *
	 * @param version the version
	 * @return true, if successful
	 */
	public boolean hasVersion(long version) {
		for (CommitPoint commitPoint : commitPoints) {
			if (commitPoint.version() == version) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find physical index file.
	 *
	 * @param physicalName the physical name
	 * @return the commit point. file info
	 */
	public CommitPoint.FileInfo findPhysicalIndexFile(String physicalName) {
		for (CommitPoint commitPoint : commitPoints) {
			CommitPoint.FileInfo fileInfo = commitPoint.findPhysicalIndexFile(physicalName);
			if (fileInfo != null) {
				return fileInfo;
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
		for (CommitPoint commitPoint : commitPoints) {
			CommitPoint.FileInfo fileInfo = commitPoint.findNameFile(name);
			if (fileInfo != null) {
				return fileInfo;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<CommitPoint> iterator() {
		return commitPoints.iterator();
	}

	/**
	 * To x content.
	 *
	 * @param commitPoint the commit point
	 * @return the byte[]
	 * @throws Exception the exception
	 */
	public static byte[] toXContent(CommitPoint commitPoint) throws Exception {
		XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
		builder.startObject();
		builder.field("version", commitPoint.version());
		builder.field("name", commitPoint.name());
		builder.field("type", commitPoint.type().toString());

		builder.startObject("index_files");
		for (CommitPoint.FileInfo fileInfo : commitPoint.indexFiles()) {
			builder.startObject(fileInfo.name());
			builder.field("physical_name", fileInfo.physicalName());
			builder.field("length", fileInfo.length());
			if (fileInfo.checksum() != null) {
				builder.field("checksum", fileInfo.checksum());
			}
			builder.endObject();
		}
		builder.endObject();

		builder.startObject("translog_files");
		for (CommitPoint.FileInfo fileInfo : commitPoint.translogFiles()) {
			builder.startObject(fileInfo.name());
			builder.field("physical_name", fileInfo.physicalName());
			builder.field("length", fileInfo.length());
			builder.endObject();
		}
		builder.endObject();

		builder.endObject();
		return builder.copiedBytes();
	}

	/**
	 * From x content.
	 *
	 * @param data the data
	 * @return the commit point
	 * @throws Exception the exception
	 */
	public static CommitPoint fromXContent(byte[] data) throws Exception {
		XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(data);
		try {
			String currentFieldName = null;
			XContentParser.Token token = parser.nextToken();
			if (token == null) {

				throw new IOException("No commit point data");
			}
			long version = -1;
			String name = null;
			CommitPoint.Type type = null;
			List<CommitPoint.FileInfo> indexFiles = Lists.newArrayList();
			List<CommitPoint.FileInfo> translogFiles = Lists.newArrayList();
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
				} else if (token == XContentParser.Token.START_OBJECT) {
					List<CommitPoint.FileInfo> files = null;
					if ("index_files".equals(currentFieldName) || "indexFiles".equals(currentFieldName)) {
						files = indexFiles;
					} else if ("translog_files".equals(currentFieldName) || "translogFiles".equals(currentFieldName)) {
						files = translogFiles;
					} else {
						throw new IOException("Can't handle object with name [" + currentFieldName + "]");
					}
					while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
						if (token == XContentParser.Token.FIELD_NAME) {
							currentFieldName = parser.currentName();
						} else if (token == XContentParser.Token.START_OBJECT) {
							String fileName = currentFieldName;
							String physicalName = null;
							long size = -1;
							String checksum = null;
							while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
								if (token == XContentParser.Token.FIELD_NAME) {
									currentFieldName = parser.currentName();
								} else if (token.isValue()) {
									if ("physical_name".equals(currentFieldName)
											|| "physicalName".equals(currentFieldName)) {
										physicalName = parser.text();
									} else if ("length".equals(currentFieldName)) {
										size = parser.longValue();
									} else if ("checksum".equals(currentFieldName)) {
										checksum = parser.text();
									}
								}
							}
							if (physicalName == null) {
								throw new IOException("Malformed commit, missing physical_name for [" + fileName + "]");
							}
							if (size == -1) {
								throw new IOException("Malformed commit, missing length for [" + fileName + "]");
							}
							files.add(new CommitPoint.FileInfo(fileName, physicalName, size, checksum));
						}
					}
				} else if (token.isValue()) {
					if ("version".equals(currentFieldName)) {
						version = parser.longValue();
					} else if ("name".equals(currentFieldName)) {
						name = parser.text();
					} else if ("type".equals(currentFieldName)) {
						type = CommitPoint.Type.valueOf(parser.text());
					}
				}
			}

			if (version == -1) {
				throw new IOException("Malformed commit, missing version");
			}
			if (name == null) {
				throw new IOException("Malformed commit, missing name");
			}
			if (type == null) {
				throw new IOException("Malformed commit, missing type");
			}

			return new CommitPoint(version, name, type, indexFiles, translogFiles);
		} finally {
			parser.close();
		}
	}
}
