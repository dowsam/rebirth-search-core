/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core StoreFileMetaData.java 2012-3-29 15:02:37 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.store;

import java.io.IOException;

import org.apache.lucene.store.Directory;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;


/**
 * The Class StoreFileMetaData.
 *
 * @author l.xue.nong
 */
public class StoreFileMetaData implements Streamable {

    
    /** The name. */
    private String name;

    
    /** The last modified. */
    private long lastModified;

    
    /** The length. */
    private long length;

    
    /** The checksum. */
    private String checksum;

    
    /** The directory. */
    private transient Directory directory;

    
    /**
     * Instantiates a new store file meta data.
     */
    StoreFileMetaData() {
    }

    
    /**
     * Instantiates a new store file meta data.
     *
     * @param name the name
     * @param length the length
     * @param lastModified the last modified
     * @param checksum the checksum
     */
    public StoreFileMetaData(String name, long length, long lastModified, String checksum) {
        this(name, length, lastModified, checksum, null);
    }

    
    /**
     * Instantiates a new store file meta data.
     *
     * @param name the name
     * @param length the length
     * @param lastModified the last modified
     * @param checksum the checksum
     * @param directory the directory
     */
    public StoreFileMetaData(String name, long length, long lastModified, String checksum, @Nullable Directory directory) {
        this.name = name;
        this.lastModified = lastModified;
        this.length = length;
        this.checksum = checksum;
        this.directory = directory;
    }

    
    /**
     * Directory.
     *
     * @return the directory
     */
    public Directory directory() {
        return this.directory;
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
     * Last modified.
     *
     * @return the long
     */
    public long lastModified() {
        return this.lastModified;
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
        return this.checksum;
    }

    
    /**
     * Checks if is same.
     *
     * @param other the other
     * @return true, if is same
     */
    public boolean isSame(StoreFileMetaData other) {
        if (checksum == null || other.checksum == null) {
            return false;
        }
        return length == other.length && checksum.equals(other.checksum);
    }

    
    /**
     * Read store file meta data.
     *
     * @param in the in
     * @return the store file meta data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static StoreFileMetaData readStoreFileMetaData(StreamInput in) throws IOException {
        StoreFileMetaData md = new StoreFileMetaData();
        md.readFrom(in);
        return md;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "name [" + name + "], length [" + length + "], checksum [" + checksum + "]";
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        length = in.readVLong();
        if (in.readBoolean()) {
            checksum = in.readUTF();
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeVLong(length);
        if (checksum == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(checksum);
        }
    }
}
