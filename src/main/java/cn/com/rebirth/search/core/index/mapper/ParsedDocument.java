/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ParsedDocument.java 2012-3-29 15:02:05 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;

import java.util.Arrays;
import java.util.List;


/**
 * The Class ParsedDocument.
 *
 * @author l.xue.nong
 */
public class ParsedDocument {

    /** The uid. */
    private final String uid;

    /** The id. */
    private final String id;

    /** The type. */
    private final String type;

    /** The routing. */
    private final String routing;

    /** The timestamp. */
    private final long timestamp;

    /** The ttl. */
    private final long ttl;

    /** The documents. */
    private final List<Document> documents;

    /** The analyzer. */
    private final Analyzer analyzer;

    /** The source. */
    private final byte[] source;
    
    /** The source offset. */
    private final int sourceOffset;
    
    /** The source length. */
    private final int sourceLength;

    /** The mappers added. */
    private boolean mappersAdded;

    /** The parent. */
    private String parent;

    /**
     * Instantiates a new parsed document.
     *
     * @param uid the uid
     * @param id the id
     * @param type the type
     * @param routing the routing
     * @param timestamp the timestamp
     * @param ttl the ttl
     * @param document the document
     * @param analyzer the analyzer
     * @param source the source
     * @param mappersAdded the mappers added
     */
    public ParsedDocument(String uid, String id, String type, String routing, long timestamp, long ttl, Document document, Analyzer analyzer, byte[] source, boolean mappersAdded) {
        this(uid, id, type, routing, timestamp, ttl, Arrays.asList(document), analyzer, source, 0, source.length, mappersAdded);
    }

    /**
     * Instantiates a new parsed document.
     *
     * @param uid the uid
     * @param id the id
     * @param type the type
     * @param routing the routing
     * @param timestamp the timestamp
     * @param ttl the ttl
     * @param documents the documents
     * @param analyzer the analyzer
     * @param source the source
     * @param sourceOffset the source offset
     * @param sourceLength the source length
     * @param mappersAdded the mappers added
     */
    public ParsedDocument(String uid, String id, String type, String routing, long timestamp, long ttl, List<Document> documents, Analyzer analyzer, byte[] source, int sourceOffset, int sourceLength, boolean mappersAdded) {
        this.uid = uid;
        this.id = id;
        this.type = type;
        this.routing = routing;
        this.timestamp = timestamp;
        this.ttl = ttl;
        this.documents = documents;
        this.source = source;
        this.sourceOffset = sourceOffset;
        this.sourceLength = sourceLength;
        this.analyzer = analyzer;
        this.mappersAdded = mappersAdded;
    }

    /**
     * Uid.
     *
     * @return the string
     */
    public String uid() {
        return this.uid;
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
     * Root doc.
     *
     * @return the document
     */
    public Document rootDoc() {
        return documents.get(documents.size() - 1);
    }

    /**
     * Docs.
     *
     * @return the list
     */
    public List<Document> docs() {
        return this.documents;
    }

    /**
     * Analyzer.
     *
     * @return the analyzer
     */
    public Analyzer analyzer() {
        return this.analyzer;
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
        return this.sourceOffset;
    }

    /**
     * Source length.
     *
     * @return the int
     */
    public int sourceLength() {
        return this.sourceLength;
    }

    /**
     * Parent.
     *
     * @param parent the parent
     * @return the parsed document
     */
    public ParsedDocument parent(String parent) {
        this.parent = parent;
        return this;
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
     * Mappers added.
     *
     * @return true, if successful
     */
    public boolean mappersAdded() {
        return mappersAdded;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Document ").append("uid[").append(uid).append("] doc [").append(documents).append("]");
        return sb.toString();
    }
}
