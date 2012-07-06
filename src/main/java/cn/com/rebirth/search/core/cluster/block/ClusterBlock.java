/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ClusterBlock.java 2012-3-29 15:02:52 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.block;

import java.io.IOException;
import java.io.Serializable;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;
import cn.com.rebirth.search.core.rest.RestStatus;


/**
 * The Class ClusterBlock.
 *
 * @author l.xue.nong
 */
public class ClusterBlock implements Serializable, Streamable, ToXContent {

    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1214123015693655305L;

	
    /** The id. */
    private int id;

    
    /** The description. */
    private String description;

    
    /** The levels. */
    private ClusterBlockLevel[] levels;

    
    /** The retryable. */
    private boolean retryable;

    
    /** The disable state persistence. */
    private boolean disableStatePersistence = false;

    
    /** The status. */
    private RestStatus status;

    
    /**
     * Instantiates a new cluster block.
     */
    ClusterBlock() {
    }

    
    /**
     * Instantiates a new cluster block.
     *
     * @param id the id
     * @param description the description
     * @param retryable the retryable
     * @param disableStatePersistence the disable state persistence
     * @param status the status
     * @param levels the levels
     */
    public ClusterBlock(int id, String description, boolean retryable, boolean disableStatePersistence, RestStatus status, ClusterBlockLevel... levels) {
        this.id = id;
        this.description = description;
        this.retryable = retryable;
        this.disableStatePersistence = disableStatePersistence;
        this.status = status;
        this.levels = levels;
    }

    
    /**
     * Id.
     *
     * @return the int
     */
    public int id() {
        return this.id;
    }

    
    /**
     * Description.
     *
     * @return the string
     */
    public String description() {
        return this.description;
    }

    
    /**
     * Status.
     *
     * @return the rest status
     */
    public RestStatus status() {
        return this.status;
    }

    
    /**
     * Levels.
     *
     * @return the cluster block level[]
     */
    public ClusterBlockLevel[] levels() {
        return this.levels;
    }

    
    /**
     * Contains.
     *
     * @param level the level
     * @return true, if successful
     */
    public boolean contains(ClusterBlockLevel level) {
        for (ClusterBlockLevel testLevel : levels) {
            if (testLevel == level) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * Retryable.
     *
     * @return true, if successful
     */
    public boolean retryable() {
        return this.retryable;
    }

    
    /**
     * Disable state persistence.
     *
     * @return true, if successful
     */
    public boolean disableStatePersistence() {
        return this.disableStatePersistence;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Integer.toString(id));
        builder.field("description", description);
        builder.field("retryable", retryable);
        if (disableStatePersistence) {
            builder.field("disable_state_persistence", disableStatePersistence);
        }
        builder.startArray("levels");
        for (ClusterBlockLevel level : levels) {
            builder.value(level.name().toLowerCase());
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    
    /**
     * Read cluster block.
     *
     * @param in the in
     * @return the cluster block
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ClusterBlock readClusterBlock(StreamInput in) throws IOException {
        ClusterBlock block = new ClusterBlock();
        block.readFrom(in);
        return block;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        id = in.readVInt();
        description = in.readUTF();
        levels = new ClusterBlockLevel[in.readVInt()];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = ClusterBlockLevel.fromId(in.readVInt());
        }
        retryable = in.readBoolean();
        disableStatePersistence = in.readBoolean();
        status = RestStatus.readFrom(in);
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(id);
        out.writeUTF(description);
        out.writeVInt(levels.length);
        for (ClusterBlockLevel level : levels) {
            out.writeVInt(level.id());
        }
        out.writeBoolean(retryable);
        out.writeBoolean(disableStatePersistence);
        RestStatus.writeTo(out, status);
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append(",").append(description).append(", blocks ");
        for (ClusterBlockLevel level : levels) {
            sb.append(level.name()).append(",");
        }
        return sb.toString();
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClusterBlock that = (ClusterBlock) o;

        if (id != that.id) return false;

        return true;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id;
    }
}
