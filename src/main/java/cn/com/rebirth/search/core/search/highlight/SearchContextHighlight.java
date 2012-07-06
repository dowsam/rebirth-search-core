/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core SearchContextHighlight.java 2012-3-29 15:01:12 l.xue.nong$$
 */


package cn.com.rebirth.search.core.search.highlight;

import java.util.List;


/**
 * The Class SearchContextHighlight.
 *
 * @author l.xue.nong
 */
public class SearchContextHighlight {

    /** The fields. */
    private final List<Field> fields;

    /**
     * Instantiates a new search context highlight.
     *
     * @param fields the fields
     */
    public SearchContextHighlight(List<Field> fields) {
        this.fields = fields;
    }

    /**
     * Fields.
     *
     * @return the list
     */
    public List<Field> fields() {
        return fields;
    }

    /**
     * The Class Field.
     *
     * @author l.xue.nong
     */
    public static class Field {

        /** The field. */
        private final String field;

        /** The fragment char size. */
        private int fragmentCharSize = -1;

        /** The number of fragments. */
        private int numberOfFragments = -1;

        /** The fragment offset. */
        private int fragmentOffset = -1;

        /** The encoder. */
        private String encoder;

        /** The pre tags. */
        private String[] preTags;

        /** The post tags. */
        private String[] postTags;

        /** The score ordered. */
        private Boolean scoreOrdered;

        /** The highlight filter. */
        private Boolean highlightFilter;

        /** The require field match. */
        private Boolean requireFieldMatch;

        /** The boundary max scan. */
        private int boundaryMaxScan = -1;
        
        /** The boundary chars. */
        private char[] boundaryChars = null;

        /**
         * Instantiates a new field.
         *
         * @param field the field
         */
        public Field(String field) {
            this.field = field;
        }

        /**
         * Field.
         *
         * @return the string
         */
        public String field() {
            return field;
        }

        /**
         * Fragment char size.
         *
         * @return the int
         */
        public int fragmentCharSize() {
            return fragmentCharSize;
        }

        /**
         * Fragment char size.
         *
         * @param fragmentCharSize the fragment char size
         */
        public void fragmentCharSize(int fragmentCharSize) {
            this.fragmentCharSize = fragmentCharSize;
        }

        /**
         * Number of fragments.
         *
         * @return the int
         */
        public int numberOfFragments() {
            return numberOfFragments;
        }

        /**
         * Number of fragments.
         *
         * @param numberOfFragments the number of fragments
         */
        public void numberOfFragments(int numberOfFragments) {
            this.numberOfFragments = numberOfFragments;
        }

        /**
         * Fragment offset.
         *
         * @return the int
         */
        public int fragmentOffset() {
            return fragmentOffset;
        }

        /**
         * Fragment offset.
         *
         * @param fragmentOffset the fragment offset
         */
        public void fragmentOffset(int fragmentOffset) {
            this.fragmentOffset = fragmentOffset;
        }

        /**
         * Encoder.
         *
         * @return the string
         */
        public String encoder() {
            return encoder;
        }

        /**
         * Encoder.
         *
         * @param encoder the encoder
         */
        public void encoder(String encoder) {
            this.encoder = encoder;
        }

        /**
         * Pre tags.
         *
         * @return the string[]
         */
        public String[] preTags() {
            return preTags;
        }

        /**
         * Pre tags.
         *
         * @param preTags the pre tags
         */
        public void preTags(String[] preTags) {
            this.preTags = preTags;
        }

        /**
         * Post tags.
         *
         * @return the string[]
         */
        public String[] postTags() {
            return postTags;
        }

        /**
         * Post tags.
         *
         * @param postTags the post tags
         */
        public void postTags(String[] postTags) {
            this.postTags = postTags;
        }

        /**
         * Score ordered.
         *
         * @return the boolean
         */
        public Boolean scoreOrdered() {
            return scoreOrdered;
        }

        /**
         * Score ordered.
         *
         * @param scoreOrdered the score ordered
         */
        public void scoreOrdered(boolean scoreOrdered) {
            this.scoreOrdered = scoreOrdered;
        }

        /**
         * Highlight filter.
         *
         * @return the boolean
         */
        public Boolean highlightFilter() {
            return highlightFilter;
        }

        /**
         * Highlight filter.
         *
         * @param highlightFilter the highlight filter
         */
        public void highlightFilter(boolean highlightFilter) {
            this.highlightFilter = highlightFilter;
        }

        /**
         * Require field match.
         *
         * @return the boolean
         */
        public Boolean requireFieldMatch() {
            return requireFieldMatch;
        }

        /**
         * Require field match.
         *
         * @param requireFieldMatch the require field match
         */
        public void requireFieldMatch(boolean requireFieldMatch) {
            this.requireFieldMatch = requireFieldMatch;
        }

        /**
         * Boundary max scan.
         *
         * @return the int
         */
        public int boundaryMaxScan() {
            return boundaryMaxScan;
        }

        /**
         * Boundary max scan.
         *
         * @param boundaryMaxScan the boundary max scan
         */
        public void boundaryMaxScan(int boundaryMaxScan) {
            this.boundaryMaxScan = boundaryMaxScan;
        }

        /**
         * Boundary chars.
         *
         * @return the char[]
         */
        public char[] boundaryChars() {
            return boundaryChars;
        }

        /**
         * Boundary chars.
         *
         * @param boundaryChars the boundary chars
         */
        public void boundaryChars(char[] boundaryChars) {
            this.boundaryChars = boundaryChars;
        }
    }
}
