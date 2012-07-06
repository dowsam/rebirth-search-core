/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ShardValidateQueryRequest.java 2012-3-29 15:01:23 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.admin.indices.validate.query;

import java.io.IOException;

import cn.com.rebirth.commons.BytesHolder;
import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.search.core.action.support.broadcast.BroadcastShardOperationRequest;


/**
 * The Class ShardValidateQueryRequest.
 *
 * @author l.xue.nong
 */
class ShardValidateQueryRequest extends BroadcastShardOperationRequest {

    
    /** The query source. */
    private BytesHolder querySource;

    
    /** The types. */
    private String[] types = Strings.EMPTY_ARRAY;

    
    /** The filtering aliases. */
    @Nullable
    private String[] filteringAliases;

    
    /**
     * Instantiates a new shard validate query request.
     */
    ShardValidateQueryRequest() {

    }

    
    /**
     * Instantiates a new shard validate query request.
     *
     * @param index the index
     * @param shardId the shard id
     * @param filteringAliases the filtering aliases
     * @param request the request
     */
    public ShardValidateQueryRequest(String index, int shardId, @Nullable String[] filteringAliases, ValidateQueryRequest request) {
        super(index, shardId);
        this.querySource = request.querySource();
        this.types = request.types();
        this.filteringAliases = filteringAliases;
    }

    
    /**
     * Query source.
     *
     * @return the bytes holder
     */
    public BytesHolder querySource() {
        return querySource;
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

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        querySource = in.readBytesReference();

        int typesSize = in.readVInt();
        if (typesSize > 0) {
            types = new String[typesSize];
            for (int i = 0; i < typesSize; i++) {
                types[i] = in.readUTF();
            }
        }
        int aliasesSize = in.readVInt();
        if (aliasesSize > 0) {
            filteringAliases = new String[aliasesSize];
            for (int i = 0; i < aliasesSize; i++) {
                filteringAliases[i] = in.readUTF();
            }
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.action.support.broadcast.BroadcastShardOperationRequest#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeBytesHolder(querySource);

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
