/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core FieldMappers.java 2012-3-29 15:01:30 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * The Class FieldMappers.
 *
 * @author l.xue.nong
 */
public class FieldMappers implements Iterable<FieldMapper> {

    /** The field mappers. */
    private final FieldMapper[] fieldMappers;
    
    /** The field mappers as list. */
    private final List<FieldMapper> fieldMappersAsList;

    /**
     * Instantiates a new field mappers.
     */
    public FieldMappers() {
        this.fieldMappers = new FieldMapper[0];
        this.fieldMappersAsList = Arrays.asList(fieldMappers);
    }

    /**
     * Instantiates a new field mappers.
     *
     * @param fieldMapper the field mapper
     */
    public FieldMappers(FieldMapper fieldMapper) {
        this.fieldMappers = new FieldMapper[]{fieldMapper};
        this.fieldMappersAsList = Arrays.asList(this.fieldMappers);
    }

    /**
     * Instantiates a new field mappers.
     *
     * @param fieldMappers the field mappers
     */
    private FieldMappers(FieldMapper[] fieldMappers) {
        this.fieldMappers = fieldMappers;
        this.fieldMappersAsList = Arrays.asList(this.fieldMappers);
    }

    /**
     * Mapper.
     *
     * @return the field mapper
     */
    public FieldMapper mapper() {
        if (fieldMappers.length == 0) {
            return null;
        }
        return fieldMappers[0];
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return fieldMappers.length == 0;
    }

    /**
     * Mappers.
     *
     * @return the list
     */
    public List<FieldMapper> mappers() {
        return this.fieldMappersAsList;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<FieldMapper> iterator() {
        return fieldMappersAsList.iterator();
    }

    
    /**
     * Concat.
     *
     * @param mapper the mapper
     * @return the field mappers
     */
    public FieldMappers concat(FieldMapper mapper) {
        FieldMapper[] newMappers = new FieldMapper[fieldMappers.length + 1];
        System.arraycopy(fieldMappers, 0, newMappers, 0, fieldMappers.length);
        newMappers[fieldMappers.length] = mapper;
        return new FieldMappers(newMappers);
    }

    /**
     * Removes the.
     *
     * @param mapper the mapper
     * @return the field mappers
     */
    public FieldMappers remove(FieldMapper mapper) {
        ArrayList<FieldMapper> list = new ArrayList<FieldMapper>(fieldMappers.length);
        for (FieldMapper fieldMapper : fieldMappers) {
            if (!fieldMapper.equals(mapper)) { 
                list.add(fieldMapper);
            }
        }
        return new FieldMappers(list.toArray(new FieldMapper[list.size()]));
    }
}
