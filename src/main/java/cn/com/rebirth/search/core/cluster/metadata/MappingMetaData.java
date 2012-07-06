/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core MappingMetaData.java 2012-3-29 15:01:10 l.xue.nong$$
 */


package cn.com.rebirth.search.core.cluster.metadata;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import cn.com.rebirth.commons.Nullable;
import cn.com.rebirth.commons.Strings;
import cn.com.rebirth.commons.compress.CompressedString;
import cn.com.rebirth.commons.exception.RestartIllegalStateException;
import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;
import cn.com.rebirth.commons.joda.FormatDateTimeFormatter;
import cn.com.rebirth.commons.joda.Joda;
import cn.com.rebirth.search.commons.xcontent.XContentBuilder;
import cn.com.rebirth.search.commons.xcontent.XContentFactory;
import cn.com.rebirth.search.commons.xcontent.XContentHelper;
import cn.com.rebirth.search.commons.xcontent.XContentParser;
import cn.com.rebirth.search.commons.xcontent.support.XContentMapValues;
import cn.com.rebirth.search.core.action.TimestampParsingException;
import cn.com.rebirth.search.core.index.mapper.DocumentMapper;
import cn.com.rebirth.search.core.index.mapper.internal.TimestampFieldMapper;


/**
 * The Class MappingMetaData.
 *
 * @author l.xue.nong
 */
public class MappingMetaData {

    
    /**
     * The Class Id.
     *
     * @author l.xue.nong
     */
    public static class Id {

        
        /** The Constant EMPTY. */
        public static final Id EMPTY = new Id(null);

        
        /** The path. */
        private final String path;

        
        /** The path elements. */
        private final String[] pathElements;

        
        /**
         * Instantiates a new id.
         *
         * @param path the path
         */
        public Id(String path) {
            this.path = path;
            if (path == null) {
                pathElements = Strings.EMPTY_ARRAY;
            } else {
                pathElements = Strings.delimitedListToStringArray(path, ".");
            }
        }

        
        /**
         * Checks for path.
         *
         * @return true, if successful
         */
        public boolean hasPath() {
            return path != null;
        }

        
        /**
         * Path.
         *
         * @return the string
         */
        public String path() {
            return this.path;
        }

        
        /**
         * Path elements.
         *
         * @return the string[]
         */
        public String[] pathElements() {
            return this.pathElements;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Id id = (Id) o;

            if (path != null ? !path.equals(id.path) : id.path != null) return false;
            if (!Arrays.equals(pathElements, id.pathElements)) return false;

            return true;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (pathElements != null ? Arrays.hashCode(pathElements) : 0);
            return result;
        }
    }

    
    /**
     * The Class Routing.
     *
     * @author l.xue.nong
     */
    public static class Routing {

        
        /** The Constant EMPTY. */
        public static final Routing EMPTY = new Routing(false, null);

        
        /** The required. */
        private final boolean required;

        
        /** The path. */
        private final String path;

        
        /** The path elements. */
        private final String[] pathElements;

        
        /**
         * Instantiates a new routing.
         *
         * @param required the required
         * @param path the path
         */
        public Routing(boolean required, String path) {
            this.required = required;
            this.path = path;
            if (path == null) {
                pathElements = Strings.EMPTY_ARRAY;
            } else {
                pathElements = Strings.delimitedListToStringArray(path, ".");
            }
        }

        
        /**
         * Required.
         *
         * @return true, if successful
         */
        public boolean required() {
            return required;
        }

        
        /**
         * Checks for path.
         *
         * @return true, if successful
         */
        public boolean hasPath() {
            return path != null;
        }

        
        /**
         * Path.
         *
         * @return the string
         */
        public String path() {
            return this.path;
        }

        
        /**
         * Path elements.
         *
         * @return the string[]
         */
        public String[] pathElements() {
            return this.pathElements;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Routing routing = (Routing) o;

            if (required != routing.required) return false;
            if (path != null ? !path.equals(routing.path) : routing.path != null) return false;
            if (!Arrays.equals(pathElements, routing.pathElements)) return false;

            return true;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = (required ? 1 : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (pathElements != null ? Arrays.hashCode(pathElements) : 0);
            return result;
        }
    }

    
    /**
     * The Class Timestamp.
     *
     * @author l.xue.nong
     */
    public static class Timestamp {

        
        /**
         * Parses the string timestamp.
         *
         * @param timestampAsString the timestamp as string
         * @param dateTimeFormatter the date time formatter
         * @return the string
         * @throws TimestampParsingException the timestamp parsing exception
         */
        public static String parseStringTimestamp(String timestampAsString, FormatDateTimeFormatter dateTimeFormatter) throws TimestampParsingException {
            long ts;
            try {
                ts = Long.parseLong(timestampAsString);
            } catch (NumberFormatException e) {
                try {
                    ts = dateTimeFormatter.parser().parseMillis(timestampAsString);
                } catch (RuntimeException e1) {
                    throw new TimestampParsingException(timestampAsString);
                }
            }
            return String.valueOf(ts);
        }


        
        /** The Constant EMPTY. */
        public static final Timestamp EMPTY = new Timestamp(false, null, TimestampFieldMapper.DEFAULT_DATE_TIME_FORMAT);

        
        /** The enabled. */
        private final boolean enabled;

        
        /** The path. */
        private final String path;

        
        /** The format. */
        private final String format;

        
        /** The path elements. */
        private final String[] pathElements;

        
        /** The date time formatter. */
        private final FormatDateTimeFormatter dateTimeFormatter;

        
        /**
         * Instantiates a new timestamp.
         *
         * @param enabled the enabled
         * @param path the path
         * @param format the format
         */
        public Timestamp(boolean enabled, String path, String format) {
            this.enabled = enabled;
            this.path = path;
            if (path == null) {
                pathElements = Strings.EMPTY_ARRAY;
            } else {
                pathElements = Strings.delimitedListToStringArray(path, ".");
            }
            this.format = format;
            this.dateTimeFormatter = Joda.forPattern(format);
        }

        
        /**
         * Enabled.
         *
         * @return true, if successful
         */
        public boolean enabled() {
            return enabled;
        }

        
        /**
         * Checks for path.
         *
         * @return true, if successful
         */
        public boolean hasPath() {
            return path != null;
        }

        
        /**
         * Path.
         *
         * @return the string
         */
        public String path() {
            return this.path;
        }

        
        /**
         * Path elements.
         *
         * @return the string[]
         */
        public String[] pathElements() {
            return this.pathElements;
        }

        
        /**
         * Format.
         *
         * @return the string
         */
        public String format() {
            return this.format;
        }

        
        /**
         * Date time formatter.
         *
         * @return the format date time formatter
         */
        public FormatDateTimeFormatter dateTimeFormatter() {
            return this.dateTimeFormatter;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Timestamp timestamp = (Timestamp) o;

            if (enabled != timestamp.enabled) return false;
            if (dateTimeFormatter != null ? !dateTimeFormatter.equals(timestamp.dateTimeFormatter) : timestamp.dateTimeFormatter != null)
                return false;
            if (format != null ? !format.equals(timestamp.format) : timestamp.format != null) return false;
            if (path != null ? !path.equals(timestamp.path) : timestamp.path != null) return false;
            if (!Arrays.equals(pathElements, timestamp.pathElements)) return false;

            return true;
        }

        
        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            int result = (enabled ? 1 : 0);
            result = 31 * result + (path != null ? path.hashCode() : 0);
            result = 31 * result + (format != null ? format.hashCode() : 0);
            result = 31 * result + (pathElements != null ? Arrays.hashCode(pathElements) : 0);
            result = 31 * result + (dateTimeFormatter != null ? dateTimeFormatter.hashCode() : 0);
            return result;
        }
    }

    
    /** The type. */
    private final String type;

    
    /** The source. */
    private final CompressedString source;

    
    /** The id. */
    private Id id;
    
    
    /** The routing. */
    private Routing routing;
    
    
    /** The timestamp. */
    private Timestamp timestamp;

    
    /**
     * Instantiates a new mapping meta data.
     *
     * @param docMapper the doc mapper
     */
    public MappingMetaData(DocumentMapper docMapper) {
        this.type = docMapper.type();
        this.source = docMapper.mappingSource();
        this.id = new Id(docMapper.idFieldMapper().path());
        this.routing = new Routing(docMapper.routingFieldMapper().required(), docMapper.routingFieldMapper().path());
        this.timestamp = new Timestamp(docMapper.timestampFieldMapper().enabled(), docMapper.timestampFieldMapper().path(), docMapper.timestampFieldMapper().dateTimeFormatter().format());
    }

    
    /**
     * Instantiates a new mapping meta data.
     *
     * @param mapping the mapping
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MappingMetaData(CompressedString mapping) throws IOException {
        this.source = mapping;
        Map<String, Object> mappingMap = XContentHelper.createParser(mapping.compressed(), 0, mapping.compressed().length).mapOrderedAndClose();
        if (mappingMap.size() != 1) {
            throw new RestartIllegalStateException("Can't derive type from mapping, no root type: " + mapping.string());
        }
        this.type = mappingMap.keySet().iterator().next();
        initMappers((Map<String, Object>) mappingMap.get(this.type));
    }

    
    /**
     * Instantiates a new mapping meta data.
     *
     * @param mapping the mapping
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MappingMetaData(Map<String, Object> mapping) throws IOException {
        this(mapping.keySet().iterator().next(), mapping);
    }

    
    /**
     * Instantiates a new mapping meta data.
     *
     * @param type the type
     * @param mapping the mapping
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public MappingMetaData(String type, Map<String, Object> mapping) throws IOException {
        this.type = type;
        XContentBuilder mappingBuilder = XContentFactory.jsonBuilder().map(mapping);
        this.source = new CompressedString(mappingBuilder.underlyingBytes(), 0, mappingBuilder.underlyingBytesLength());
        Map<String, Object> withoutType = mapping;
        if (mapping.size() == 1 && mapping.containsKey(type)) {
            withoutType = (Map<String, Object>) mapping.get(type);
        }
        initMappers(withoutType);
    }

    
    /**
     * Inits the mappers.
     *
     * @param withoutType the without type
     */
    private void initMappers(Map<String, Object> withoutType) {
        if (withoutType.containsKey("_id")) {
            String path = null;
            Map<String, Object> routingNode = (Map<String, Object>) withoutType.get("_id");
            for (Map.Entry<String, Object> entry : routingNode.entrySet()) {
                String fieldName = Strings.toUnderscoreCase(entry.getKey());
                Object fieldNode = entry.getValue();
                if (fieldName.equals("path")) {
                    path = fieldNode.toString();
                }
            }
            this.id = new Id(path);
        } else {
            this.id = Id.EMPTY;
        }
        if (withoutType.containsKey("_routing")) {
            boolean required = false;
            String path = null;
            Map<String, Object> routingNode = (Map<String, Object>) withoutType.get("_routing");
            for (Map.Entry<String, Object> entry : routingNode.entrySet()) {
                String fieldName = Strings.toUnderscoreCase(entry.getKey());
                Object fieldNode = entry.getValue();
                if (fieldName.equals("required")) {
                    required = XContentMapValues.nodeBooleanValue(fieldNode);
                } else if (fieldName.equals("path")) {
                    path = fieldNode.toString();
                }
            }
            this.routing = new Routing(required, path);
        } else {
            this.routing = Routing.EMPTY;
        }
        if (withoutType.containsKey("_timestamp")) {
            boolean enabled = false;
            String path = null;
            String format = TimestampFieldMapper.DEFAULT_DATE_TIME_FORMAT;
            Map<String, Object> timestampNode = (Map<String, Object>) withoutType.get("_timestamp");
            for (Map.Entry<String, Object> entry : timestampNode.entrySet()) {
                String fieldName = Strings.toUnderscoreCase(entry.getKey());
                Object fieldNode = entry.getValue();
                if (fieldName.equals("enabled")) {
                    enabled = XContentMapValues.nodeBooleanValue(fieldNode);
                } else if (fieldName.equals("path")) {
                    path = fieldNode.toString();
                } else if (fieldName.equals("format")) {
                    format = fieldNode.toString();
                }
            }
            this.timestamp = new Timestamp(enabled, path, format);
        } else {
            this.timestamp = Timestamp.EMPTY;
        }
    }

    
    /**
     * Instantiates a new mapping meta data.
     *
     * @param type the type
     * @param source the source
     * @param id the id
     * @param routing the routing
     * @param timestamp the timestamp
     */
    public MappingMetaData(String type, CompressedString source, Id id, Routing routing, Timestamp timestamp) {
        this.type = type;
        this.source = source;
        this.id = id;
        this.routing = routing;
        this.timestamp = timestamp;
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
     * Source.
     *
     * @return the compressed string
     */
    public CompressedString source() {
        return this.source;
    }

    
    /**
     * Source as map.
     *
     * @return the map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Map<String, Object> sourceAsMap() throws IOException {
        Map<String, Object> mapping = XContentHelper.convertToMap(source.compressed(), 0, source.compressed().length, true).v2();
        if (mapping.size() == 1 && mapping.containsKey(type())) {
            
            mapping = (Map<String, Object>) mapping.get(type());
        }
        return mapping;
    }

    
    /**
     * Gets the source as map.
     *
     * @return the source as map
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Map<String, Object> getSourceAsMap() throws IOException {
        return sourceAsMap();
    }

    
    /**
     * Id.
     *
     * @return the id
     */
    public Id id() {
        return this.id;
    }

    
    /**
     * Routing.
     *
     * @return the routing
     */
    public Routing routing() {
        return this.routing;
    }

    
    /**
     * Timestamp.
     *
     * @return the timestamp
     */
    public Timestamp timestamp() {
        return this.timestamp;
    }

    
    /**
     * Creates the parse context.
     *
     * @param id the id
     * @param routing the routing
     * @param timestamp the timestamp
     * @return the parses the context
     */
    public ParseContext createParseContext(@Nullable String id, @Nullable String routing, @Nullable String timestamp) {
        return new ParseContext(
                id == null && id().hasPath(),
                routing == null && routing().hasPath(),
                timestamp == null && timestamp().hasPath()
        );
    }

    
    /**
     * Parses the.
     *
     * @param parser the parser
     * @param parseContext the parse context
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void parse(XContentParser parser, ParseContext parseContext) throws IOException {
        innerParse(parser, parseContext);
    }

    
    /**
     * Inner parse.
     *
     * @param parser the parser
     * @param context the context
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void innerParse(XContentParser parser, ParseContext context) throws IOException {
        if (!context.parsingStillNeeded()) {
            return;
        }

        XContentParser.Token t = parser.currentToken();
        if (t == null) {
            t = parser.nextToken();
        }
        if (t == XContentParser.Token.START_OBJECT) {
            t = parser.nextToken();
        }
        String idPart = context.idParsingStillNeeded() ? id().pathElements()[context.locationId] : null;
        String routingPart = context.routingParsingStillNeeded() ? routing().pathElements()[context.locationRouting] : null;
        String timestampPart = context.timestampParsingStillNeeded() ? timestamp().pathElements()[context.locationTimestamp] : null;

        for (; t == XContentParser.Token.FIELD_NAME; t = parser.nextToken()) {
            
            String fieldName = parser.currentName();
            
            t = parser.nextToken();
            boolean incLocationId = false;
            boolean incLocationRouting = false;
            boolean incLocationTimestamp = false;
            if (context.idParsingStillNeeded() && fieldName.equals(idPart)) {
                if (context.locationId + 1 == id.pathElements().length) {
                    context.id = parser.textOrNull();
                    context.idResolved = true;
                } else {
                    incLocationId = true;
                }
            }
            if (context.routingParsingStillNeeded() && fieldName.equals(routingPart)) {
                if (context.locationRouting + 1 == routing.pathElements().length) {
                    context.routing = parser.textOrNull();
                    context.routingResolved = true;
                } else {
                    incLocationRouting = true;
                }
            }
            if (context.timestampParsingStillNeeded() && fieldName.equals(timestampPart)) {
                if (context.locationTimestamp + 1 == timestamp.pathElements().length) {
                    context.timestamp = parser.textOrNull();
                    context.timestampResolved = true;
                } else {
                    incLocationTimestamp = true;
                }
            }

            if (incLocationId || incLocationRouting || incLocationTimestamp) {
                if (t == XContentParser.Token.START_OBJECT) {
                    context.locationId += incLocationId ? 1 : 0;
                    context.locationRouting += incLocationRouting ? 1 : 0;
                    context.locationTimestamp += incLocationTimestamp ? 1 : 0;
                    innerParse(parser, context);
                    context.locationId -= incLocationId ? 1 : 0;
                    context.locationRouting -= incLocationRouting ? 1 : 0;
                    context.locationTimestamp -= incLocationTimestamp ? 1 : 0;
                }
            } else {
                parser.skipChildren();
            }

            if (!context.parsingStillNeeded()) {
                return;
            }
        }
    }

    
    /**
     * Write to.
     *
     * @param mappingMd the mapping md
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeTo(MappingMetaData mappingMd, StreamOutput out) throws IOException {
        out.writeUTF(mappingMd.type());
        mappingMd.source().writeTo(out);
        
        if (mappingMd.id().hasPath()) {
            out.writeBoolean(true);
            out.writeUTF(mappingMd.id().path());
        } else {
            out.writeBoolean(false);
        }
        
        out.writeBoolean(mappingMd.routing().required());
        if (mappingMd.routing().hasPath()) {
            out.writeBoolean(true);
            out.writeUTF(mappingMd.routing().path());
        } else {
            out.writeBoolean(false);
        }
        
        out.writeBoolean(mappingMd.timestamp().enabled());
        if (mappingMd.timestamp().hasPath()) {
            out.writeBoolean(true);
            out.writeUTF(mappingMd.timestamp().path());
        } else {
            out.writeBoolean(false);
        }
        out.writeUTF(mappingMd.timestamp().format());
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappingMetaData that = (MappingMetaData) o;

        if (!id.equals(that.id)) return false;
        if (!routing.equals(that.routing)) return false;
        if (!source.equals(that.source)) return false;
        if (!timestamp.equals(that.timestamp)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + routing.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }

    
    /**
     * Read from.
     *
     * @param in the in
     * @return the mapping meta data
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static MappingMetaData readFrom(StreamInput in) throws IOException {
        String type = in.readUTF();
        CompressedString source = CompressedString.readCompressedString(in);
        
        Id id = new Id(in.readBoolean() ? in.readUTF() : null);
        
        Routing routing = new Routing(in.readBoolean(), in.readBoolean() ? in.readUTF() : null);
        
        Timestamp timestamp = new Timestamp(in.readBoolean(), in.readBoolean() ? in.readUTF() : null, in.readUTF());
        return new MappingMetaData(type, source, id, routing, timestamp);
    }

    
    /**
     * The Class ParseResult.
     *
     * @author l.xue.nong
     */
    public static class ParseResult {
        
        
        /** The routing. */
        public final String routing;
        
        
        /** The routing resolved. */
        public final boolean routingResolved;
        
        
        /** The timestamp. */
        public final String timestamp;
        
        
        /** The timestamp resolved. */
        public final boolean timestampResolved;

        
        /**
         * Instantiates a new parses the result.
         *
         * @param routing the routing
         * @param routingResolved the routing resolved
         * @param timestamp the timestamp
         * @param timestampResolved the timestamp resolved
         */
        public ParseResult(String routing, boolean routingResolved, String timestamp, boolean timestampResolved) {
            this.routing = routing;
            this.routingResolved = routingResolved;
            this.timestamp = timestamp;
            this.timestampResolved = timestampResolved;
        }
    }

    
    /**
     * The Class ParseContext.
     *
     * @author l.xue.nong
     */
    public static class ParseContext {
        
        
        /** The should parse id. */
        final boolean shouldParseId;
        
        
        /** The should parse routing. */
        final boolean shouldParseRouting;
        
        
        /** The should parse timestamp. */
        final boolean shouldParseTimestamp;

        
        /** The location id. */
        int locationId = 0;
        
        
        /** The location routing. */
        int locationRouting = 0;
        
        
        /** The location timestamp. */
        int locationTimestamp = 0;
        
        
        /** The id resolved. */
        boolean idResolved;
        
        
        /** The routing resolved. */
        boolean routingResolved;
        
        
        /** The timestamp resolved. */
        boolean timestampResolved;
        
        
        /** The id. */
        String id;
        
        
        /** The routing. */
        String routing;
        
        
        /** The timestamp. */
        String timestamp;

        
        /**
         * Instantiates a new parses the context.
         *
         * @param shouldParseId the should parse id
         * @param shouldParseRouting the should parse routing
         * @param shouldParseTimestamp the should parse timestamp
         */
        public ParseContext(boolean shouldParseId, boolean shouldParseRouting, boolean shouldParseTimestamp) {
            this.shouldParseId = shouldParseId;
            this.shouldParseRouting = shouldParseRouting;
            this.shouldParseTimestamp = shouldParseTimestamp;
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
         * Should parse id.
         *
         * @return true, if successful
         */
        public boolean shouldParseId() {
            return shouldParseId;
        }

        
        /**
         * Id resolved.
         *
         * @return true, if successful
         */
        public boolean idResolved() {
            return idResolved;
        }

        
        /**
         * Id parsing still needed.
         *
         * @return true, if successful
         */
        public boolean idParsingStillNeeded() {
            return shouldParseId && !idResolved;
        }

        
        /**
         * Routing.
         *
         * @return the string
         */
        public String routing() {
            return routing;
        }

        
        /**
         * Should parse routing.
         *
         * @return true, if successful
         */
        public boolean shouldParseRouting() {
            return shouldParseRouting;
        }

        
        /**
         * Routing resolved.
         *
         * @return true, if successful
         */
        public boolean routingResolved() {
            return routingResolved;
        }

        
        /**
         * Routing parsing still needed.
         *
         * @return true, if successful
         */
        public boolean routingParsingStillNeeded() {
            return shouldParseRouting && !routingResolved;
        }

        
        /**
         * Timestamp.
         *
         * @return the string
         */
        public String timestamp() {
            return timestamp;
        }

        
        /**
         * Should parse timestamp.
         *
         * @return true, if successful
         */
        public boolean shouldParseTimestamp() {
            return shouldParseTimestamp;
        }

        
        /**
         * Timestamp resolved.
         *
         * @return true, if successful
         */
        public boolean timestampResolved() {
            return timestampResolved;
        }

        
        /**
         * Timestamp parsing still needed.
         *
         * @return true, if successful
         */
        public boolean timestampParsingStillNeeded() {
            return shouldParseTimestamp && !timestampResolved;
        }

        
        /**
         * Should parse.
         *
         * @return true, if successful
         */
        public boolean shouldParse() {
            return shouldParseId || shouldParseRouting || shouldParseTimestamp;
        }

        
        /**
         * Parsing still needed.
         *
         * @return true, if successful
         */
        public boolean parsingStillNeeded() {
            return idParsingStillNeeded() || routingParsingStillNeeded() || timestampParsingStillNeeded();
        }
    }
}
