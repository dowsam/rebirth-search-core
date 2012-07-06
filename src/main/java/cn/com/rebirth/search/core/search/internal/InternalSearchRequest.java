/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalSearchRequest.java 2012-3-29 15:02:45 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.internal;

import java.io.IOException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.core.action.search.SearchType;
import cn.com.rebirth.search.core.cluster.routing.ShardRouting;
import cn.com.rebirth.search.core.search.Scroll;


/**
 * The Class InternalSearchRequest.
 *
 * @author l.xue.nong
 */
public class InternalSearchRequest implements Streamable {

    
    /** The index. */
    private String index;

    
    /** The shard id. */
    private int shardId;

    
    /** The number of shards. */
    private int numberOfShards;

    
    /** The search type. */
    private SearchType searchType;

    
    /** The scroll. */
    private Scroll scroll;

    
    /** The types. */
    private String[] types = Strings.EMPTY_ARRAY;

    
    /** The filtering aliases. */
    private String[] filteringAliases;

    
    /** The source. */
    private byte[] source;
    
    
    /** The source offset. */
    private int sourceOffset;
    
    
    /** The source length. */
    private int sourceLength;

    
    /** The extra source. */
    private byte[] extraSource;
    
    
    /** The extra source offset. */
    private int extraSourceOffset;
    
    
    /** The extra source length. */
    private int extraSourceLength;

    
    /** The now in millis. */
    private long nowInMillis;

    
    /**
     * Instantiates a new internal search request.
     */
    public InternalSearchRequest() {
    }

    
    /**
     * Instantiates a new internal search request.
     *
     * @param shardRouting the shard routing
     * @param numberOfShards the number of shards
     * @param searchType the search type
     */
    public InternalSearchRequest(ShardRouting shardRouting, int numberOfShards, SearchType searchType) {
        this(shardRouting.index(), shardRouting.id(), numberOfShards, searchType);
    }

    
    /**
     * Instantiates a new internal search request.
     *
     * @param index the index
     * @param shardId the shard id
     * @param numberOfShards the number of shards
     * @param searchType the search type
     */
    public InternalSearchRequest(String index, int shardId, int numberOfShards, SearchType searchType) {
        this.index = index;
        this.shardId = shardId;
        this.numberOfShards = numberOfShards;
        this.searchType = searchType;
    }

    
    /**
     * Index.
     *
     * @return the string
     */
    public String index() {
        return index;
    }

    
    /**
     * Shard id.
     *
     * @return the int
     */
    public int shardId() {
        return shardId;
    }

    
    /**
     * Search type.
     *
     * @return the search type
     */
    public SearchType searchType() {
        return this.searchType;
    }

    
    /**
     * Number of shards.
     *
     * @return the int
     */
    public int numberOfShards() {
        return numberOfShards;
    }

    
    /**
     * Source.
     *
     * @return the byte[]
     */
    public byte[] source() {
        return this.source;
    }

    
    /**
     * Source offset.
     *
     * @return the int
     */
    public int sourceOffset() {
        return sourceOffset;
    }

    
    /**
     * Source length.
     *
     * @return the int
     */
    public int sourceLength() {
        return sourceLength;
    }

    
    /**
     * Extra source.
     *
     * @return the byte[]
     */
    public byte[] extraSource() {
        return this.extraSource;
    }

    
    /**
     * Extra source offset.
     *
     * @return the int
     */
    public int extraSourceOffset() {
        return extraSourceOffset;
    }

    
    /**
     * Extra source length.
     *
     * @return the int
     */
    public int extraSourceLength() {
        return extraSourceLength;
    }

    
    /**
     * Source.
     *
     * @param source the source
     * @return the internal search request
     */
    public InternalSearchRequest source(byte[] source) {
        return source(source, 0, source.length);
    }

    
    /**
     * Source.
     *
     * @param source the source
     * @param offset the offset
     * @param length the length
     * @return the internal search request
     */
    public InternalSearchRequest source(byte[] source, int offset, int length) {
        this.source = source;
        this.sourceOffset = offset;
        this.sourceLength = length;
        return this;
    }

    
    /**
     * Extra source.
     *
     * @param extraSource the extra source
     * @param offset the offset
     * @param length the length
     * @return the internal search request
     */
    public InternalSearchRequest extraSource(byte[] extraSource, int offset, int length) {
        this.extraSource = extraSource;
        this.extraSourceOffset = offset;
        this.extraSourceLength = length;
        return this;
    }

    
    /**
     * Now in millis.
     *
     * @param nowInMillis the now in millis
     * @return the internal search request
     */
    public InternalSearchRequest nowInMillis(long nowInMillis) {
        this.nowInMillis = nowInMillis;
        return this;
    }

    
    /**
     * Now in millis.
     *
     * @return the long
     */
    public long nowInMillis() {
        return this.nowInMillis;
    }

    
    /**
     * Scroll.
     *
     * @return the scroll
     */
    public Scroll scroll() {
        return scroll;
    }

    
    /**
     * Scroll.
     *
     * @param scroll the scroll
     * @return the internal search request
     */
    public InternalSearchRequest scroll(Scroll scroll) {
        this.scroll = scroll;
        return this;
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
     * Filtering aliases.
     *
     * @param filteringAliases the filtering aliases
     */
    public void filteringAliases(String[] filteringAliases) {
        this.filteringAliases = filteringAliases;
    }

    
    /**
     * Types.
     *
     * @return the string[]
     */
    public String[] types() {
        return types;
    }

    
    /**
     * Types.
     *
     * @param types the types
     */
    public void types(String[] types) {
        this.types = types;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        index = in.readUTF();
        shardId = in.readVInt();
        searchType = SearchType.fromId(in.readByte());
        numberOfShards = in.readVInt();
        if (in.readBoolean()) {
            scroll = Scroll.readScroll(in);
        }

        BytesHolder bytes = in.readBytesReference();
        source = bytes.bytes();
        sourceOffset = bytes.offset();
        sourceLength = bytes.length();

        bytes = in.readBytesReference();
        extraSource = bytes.bytes();
        extraSourceOffset = bytes.offset();
        extraSourceLength = bytes.length();

        int typesSize = in.readVInt();
        if (typesSize > 0) {
            types = new String[typesSize];
            for (int i = 0; i < typesSize; i++) {
                types[i] = in.readUTF();
            }
        }
        int indicesSize = in.readVInt();
        if (indicesSize > 0) {
            filteringAliases = new String[indicesSize];
            for (int i = 0; i < indicesSize; i++) {
                filteringAliases[i] = in.readUTF();
            }
        } else {
            filteringAliases = null;
        }
        nowInMillis = in.readVLong();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(index);
        out.writeVInt(shardId);
        out.writeByte(searchType.id());
        out.writeVInt(numberOfShards);
        if (scroll == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            scroll.writeTo(out);
        }
        out.writeBytesHolder(source, sourceOffset, sourceLength);
        out.writeBytesHolder(extraSource, extraSourceOffset, extraSourceLength);
        out.writeVInt(types.length);
        for (String type : types) {
            out.writeUTF(type);
        }
        if (filteringAliases != null) {
            out.writeVInt(filteringAliases.length);
            for (String index : filteringAliases) {
                out.writeUTF(index);
            }
        } else {
            out.writeVInt(0);
        }
        out.writeVLong(nowInMillis);
    }
}
