/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core InternalFacets.java 2012-3-29 15:01:41 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.facet;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.io.stream.Streamable;
import cn.com.rebirth.search.commons.xcontent.ToXContent;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentBuilderString;
import cn.com.rebirth.search.commons.xcontent.ToXContent.Params;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


/**
 * The Class InternalFacets.
 *
 * @author l.xue.nong
 */
public class InternalFacets implements Facets, Streamable, ToXContent, Iterable<Facet> {

    
    /** The facets. */
    private List<Facet> facets = ImmutableList.of();

    
    /** The facets as map. */
    private Map<String, Facet> facetsAsMap;

    
    /**
     * Instantiates a new internal facets.
     */
    private InternalFacets() {

    }

    
    /**
     * Instantiates a new internal facets.
     *
     * @param facets the facets
     */
    public InternalFacets(List<Facet> facets) {
        this.facets = facets;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Facet> iterator() {
        return facets.iterator();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facets#facets()
     */
    public List<Facet> facets() {
        return facets;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facets#getFacets()
     */
    public Map<String, Facet> getFacets() {
        return facetsAsMap();
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facets#facetsAsMap()
     */
    public Map<String, Facet> facetsAsMap() {
        if (facetsAsMap != null) {
            return facetsAsMap;
        }
        Map<String, Facet> facetsAsMap = newHashMap();
        for (Facet facet : facets) {
            facetsAsMap.put(facet.name(), facet);
        }
        this.facetsAsMap = facetsAsMap;
        return facetsAsMap;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facets#facet(java.lang.Class, java.lang.String)
     */
    @Override
    public <T extends Facet> T facet(Class<T> facetType, String name) {
        return facetType.cast(facet(name));
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.core.search.facet.Facets#facet(java.lang.String)
     */
    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends Facet> T facet(String name) {
        return (T) facetsAsMap().get(name);
    }

    
    /**
     * The Class Fields.
     *
     * @author l.xue.nong
     */
    static final class Fields {
        
        
        /** The Constant FACETS. */
        static final XContentBuilderString FACETS = new XContentBuilderString("facets");
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.xcontent.ToXContent#toXContent(cn.com.summall.search.commons.xcontent.XContentBuilder, cn.com.summall.search.commons.xcontent.ToXContent.Params)
     */
    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.FACETS);
        for (Facet facet : facets) {
            ((InternalFacet) facet).toXContent(builder, params);
        }
        builder.endObject();
        return builder;
    }

    
    /**
     * Read facets.
     *
     * @param in the in
     * @return the internal facets
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static InternalFacets readFacets(StreamInput in) throws IOException {
        InternalFacets result = new InternalFacets();
        result.readFrom(in);
        return result;
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#readFrom(cn.com.summall.search.commons.io.stream.StreamInput)
     */
    @Override
    public void readFrom(StreamInput in) throws IOException {
        int size = in.readVInt();
        if (size == 0) {
            facets = ImmutableList.of();
            facetsAsMap = ImmutableMap.of();
        } else {
            facets = Lists.newArrayListWithCapacity(size);
            for (int i = 0; i < size; i++) {
                String type = in.readUTF();
                Facet facet = InternalFacet.Streams.stream(type).readFacet(type, in);
                facets.add(facet);
            }
        }
    }

    
    /* (non-Javadoc)
     * @see cn.com.summall.search.commons.io.stream.Streamable#writeTo(cn.com.summall.search.commons.io.stream.StreamOutput)
     */
    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(facets.size());
        for (Facet facet : facets) {
            InternalFacet internalFacet = (InternalFacet) facet;
            out.writeUTF(internalFacet.streamType());
            internalFacet.writeTo(out);
        }
    }
}

