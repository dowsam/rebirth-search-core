/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core Uid.java 2012-3-29 15:01:56 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;


/**
 * The Class Uid.
 *
 * @author l.xue.nong
 */
public final class Uid {

    /** The Constant DELIMITER. */
    public static final char DELIMITER = '#';

    /** The type. */
    private final String type;

    /** The id. */
    private final String id;

    /**
     * Instantiates a new uid.
     *
     * @param type the type
     * @param id the id
     */
    public Uid(String type, String id) {
        this.type = type;
        this.id = id;
    }

    /**
     * Type.
     *
     * @return the string
     */
    public String type() {
        return type;
    }

    /**
     * Id.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Uid uid = (Uid) o;

        if (id != null ? !id.equals(uid.id) : uid.id != null) return false;
        if (type != null ? !type.equals(uid.type) : uid.type != null) return false;

        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return type + DELIMITER + id;
    }

    /**
     * Type prefix.
     *
     * @param type the type
     * @return the string
     */
    public static String typePrefix(String type) {
        return type + DELIMITER;
    }

    /**
     * Id from uid.
     *
     * @param uid the uid
     * @return the string
     */
    public static String idFromUid(String uid) {
        int delimiterIndex = uid.indexOf(DELIMITER); 
        return uid.substring(delimiterIndex + 1);
    }

    /**
     * Type from uid.
     *
     * @param uid the uid
     * @return the string
     */
    public static String typeFromUid(String uid) {
        int delimiterIndex = uid.indexOf(DELIMITER); 
        return uid.substring(0, delimiterIndex);
    }

    /**
     * Creates the uid.
     *
     * @param uid the uid
     * @return the uid
     */
    public static Uid createUid(String uid) {
        int delimiterIndex = uid.indexOf(DELIMITER); 
        return new Uid(uid.substring(0, delimiterIndex), uid.substring(delimiterIndex + 1));
    }

    /**
     * Creates the uid.
     *
     * @param type the type
     * @param id the id
     * @return the string
     */
    public static String createUid(String type, String id) {
        return createUid(new StringBuilder(), type, id);
    }

    /**
     * Creates the uid.
     *
     * @param sb the sb
     * @param type the type
     * @param id the id
     * @return the string
     */
    public static String createUid(StringBuilder sb, String type, String id) {
        return sb.append(type).append(DELIMITER).append(id).toString();
    }
}
