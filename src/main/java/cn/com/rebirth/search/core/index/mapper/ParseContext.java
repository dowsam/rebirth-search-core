/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ParseContext.java 2012-3-29 15:01:17 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.settings.Settings;
import cn.com.rebirth.search.commons.lucene.all.AllEntries;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.core.index.analysis.AnalysisService;
import cn.com.rebirth.search.core.index.mapper.object.RootObjectMapper;


/**
 * The Class ParseContext.
 *
 * @author l.xue.nong
 */
public class ParseContext {

    
    /** The doc mapper. */
    private final DocumentMapper docMapper;

    
    /** The doc mapper parser. */
    private final DocumentMapperParser docMapperParser;

    
    /** The path. */
    private final ContentPath path;

    
    /** The parser. */
    private XContentParser parser;

    
    /** The document. */
    private Document document;

    
    /** The documents. */
    private List<Document> documents = new ArrayList<Document>();

    
    /** The analyzer. */
    private Analyzer analyzer;

    
    /** The index. */
    private final String index;

    
    /** The index settings. */
    @Nullable
    private final Settings indexSettings;

    
    /** The source to parse. */
    private SourceToParse sourceToParse;
    
    
    /** The source. */
    private byte[] source;
    
    
    /** The source offset. */
    private int sourceOffset;
    
    
    /** The source length. */
    private int sourceLength;

    
    /** The id. */
    private String id;

    
    /** The listener. */
    private DocumentMapper.ParseListener listener;

    
    /** The uid. */
    private String uid;

    
    /** The string builder. */
    private StringBuilder stringBuilder = new StringBuilder();

    
    /** The ignored values. */
    private Map<String, String> ignoredValues = new HashMap<String, String>();

    
    /** The mappers added. */
    private boolean mappersAdded = false;

    
    /** The external value set. */
    private boolean externalValueSet;

    
    /** The external value. */
    private Object externalValue;

    
    /** The all entries. */
    private AllEntries allEntries = new AllEntries();

    
    /**
     * Instantiates a new parses the context.
     *
     * @param index the index
     * @param indexSettings the index settings
     * @param docMapperParser the doc mapper parser
     * @param docMapper the doc mapper
     * @param path the path
     */
    public ParseContext(String index, @Nullable Settings indexSettings, DocumentMapperParser docMapperParser, DocumentMapper docMapper, ContentPath path) {
        this.index = index;
        this.indexSettings = indexSettings;
        this.docMapper = docMapper;
        this.docMapperParser = docMapperParser;
        this.path = path;
    }

    
    /**
     * Reset.
     *
     * @param parser the parser
     * @param document the document
     * @param source the source
     * @param listener the listener
     */
    public void reset(XContentParser parser, Document document, SourceToParse source, DocumentMapper.ParseListener listener) {
        this.parser = parser;
        this.document = document;
        if (document != null) {
            this.documents = new ArrayList<Document>();
            this.documents.add(document);
        } else {
            this.documents = null;
        }
        this.analyzer = null;
        this.uid = null;
        this.id = null;
        this.sourceToParse = source;
        this.source = source == null ? null : sourceToParse.source();
        this.sourceOffset = source == null ? 0 : sourceToParse.sourceOffset();
        this.sourceLength = source == null ? 0 : sourceToParse.sourceLength();
        this.path.reset();
        this.mappersAdded = false;
        this.listener = listener == null ? DocumentMapper.ParseListener.EMPTY : listener;
        this.allEntries = new AllEntries();
        this.ignoredValues.clear();
    }

    
    /**
     * Flyweight.
     *
     * @return true, if successful
     */
    public boolean flyweight() {
        return sourceToParse.flyweight();
    }

    
    /**
     * Doc mapper parser.
     *
     * @return the document mapper parser
     */
    public DocumentMapperParser docMapperParser() {
        return this.docMapperParser;
    }

    
    /**
     * Mappers added.
     *
     * @return true, if successful
     */
    public boolean mappersAdded() {
        return this.mappersAdded;
    }

    
    /**
     * Added mapper.
     */
    public void addedMapper() {
        this.mappersAdded = true;
    }

    
    /**
     * Index.
     *
     * @return the string
     */
    public String index() {
        return this.index;
    }

    
    /**
     * Index settings.
     *
     * @return the settings
     */
    @Nullable
    public Settings indexSettings() {
        return this.indexSettings;
    }

    
    /**
     * Type.
     *
     * @return the string
     */
    public String type() {
        return sourceToParse.type();
    }

    
    /**
     * Source to parse.
     *
     * @return the source to parse
     */
    public SourceToParse sourceToParse() {
        return this.sourceToParse;
    }

    
    /**
     * Source.
     *
     * @return the byte[]
     */
    public byte[] source() {
        return source;
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
     * Source.
     *
     * @param source the source
     * @param offset the offset
     * @param length the length
     */
    public void source(byte[] source, int offset, int length) {
        this.source = source;
        this.sourceOffset = offset;
        this.sourceLength = length;
    }

    
    /**
     * Path.
     *
     * @return the content path
     */
    public ContentPath path() {
        return this.path;
    }

    
    /**
     * Parser.
     *
     * @return the x content parser
     */
    public XContentParser parser() {
        return this.parser;
    }

    
    /**
     * Listener.
     *
     * @return the document mapper. parse listener
     */
    public DocumentMapper.ParseListener listener() {
        return this.listener;
    }

    
    /**
     * Root doc.
     *
     * @return the document
     */
    public Document rootDoc() {
        return documents.get(0);
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
     * Doc.
     *
     * @return the document
     */
    public Document doc() {
        return this.document;
    }

    
    /**
     * Adds the doc.
     *
     * @param doc the doc
     */
    public void addDoc(Document doc) {
        this.documents.add(doc);
    }

    
    /**
     * Switch doc.
     *
     * @param doc the doc
     * @return the document
     */
    public Document switchDoc(Document doc) {
        Document prev = this.document;
        this.document = doc;
        return prev;
    }

    
    /**
     * Root.
     *
     * @return the root object mapper
     */
    public RootObjectMapper root() {
        return docMapper.root();
    }

    
    /**
     * Doc mapper.
     *
     * @return the document mapper
     */
    public DocumentMapper docMapper() {
        return this.docMapper;
    }

    
    /**
     * Analysis service.
     *
     * @return the analysis service
     */
    public AnalysisService analysisService() {
        return docMapperParser.analysisService;
    }

    
    /**
     * Id.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    
    /**
     * Ignored value.
     *
     * @param indexName the index name
     * @param value the value
     */
    public void ignoredValue(String indexName, String value) {
        ignoredValues.put(indexName, value);
    }

    
    /**
     * Ignored value.
     *
     * @param indexName the index name
     * @return the string
     */
    public String ignoredValue(String indexName) {
        return ignoredValues.get(indexName);
    }

    
    /**
     * Id.
     *
     * @param id the id
     */
    public void id(String id) {
        this.id = id;
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
     * Uid.
     *
     * @param uid the uid
     */
    public void uid(String uid) {
        this.uid = uid;
    }

    
    /**
     * Include in all.
     *
     * @param includeInAll the include in all
     * @param mapper the mapper
     * @return true, if successful
     */
    public boolean includeInAll(Boolean includeInAll, FieldMapper mapper) {
        return includeInAll(includeInAll, mapper.index());
    }

    
    /**
     * Include in all.
     *
     * @param specificIncludeInAll the specific include in all
     * @param index the index
     * @return true, if successful
     */
    private boolean includeInAll(Boolean specificIncludeInAll, Field.Index index) {
        if (!docMapper.allFieldMapper().enabled()) {
            return false;
        }
        
        if (specificIncludeInAll == null) {
            return index != Field.Index.NO;
        }
        return specificIncludeInAll;
    }

    
    /**
     * All entries.
     *
     * @return the all entries
     */
    public AllEntries allEntries() {
        return this.allEntries;
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
     * Analyzer.
     *
     * @param analyzer the analyzer
     */
    public void analyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    
    /**
     * External value.
     *
     * @param externalValue the external value
     */
    public void externalValue(Object externalValue) {
        this.externalValueSet = true;
        this.externalValue = externalValue;
    }

    
    /**
     * External value set.
     *
     * @return true, if successful
     */
    public boolean externalValueSet() {
        return this.externalValueSet;
    }

    
    /**
     * External value.
     *
     * @return the object
     */
    public Object externalValue() {
        externalValueSet = false;
        return externalValue;
    }

    
    /**
     * String builder.
     *
     * @return the string builder
     */
    public StringBuilder stringBuilder() {
        stringBuilder.setLength(0);
        return this.stringBuilder;
    }
}
