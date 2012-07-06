/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ObjectMappers.java 2012-3-29 15:01:13 l.xue.nong$$
 */


package cn.com.rebirth.search.core.index.mapper;

import cn.com.rebirth.search.core.index.mapper.object.ObjectMapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;


/**
 * The Class ObjectMappers.
 *
 * @author l.xue.nong
 */
public class ObjectMappers implements Iterable<ObjectMapper> {

    
    /** The object mappers. */
    private final ImmutableList<ObjectMapper> objectMappers;

    
    /**
     * Instantiates a new object mappers.
     */
    public ObjectMappers() {
        this.objectMappers = ImmutableList.of();
    }

    
    /**
     * Instantiates a new object mappers.
     *
     * @param objectMapper the object mapper
     */
    public ObjectMappers(ObjectMapper objectMapper) {
        this(new ObjectMapper[]{objectMapper});
    }

    
    /**
     * Instantiates a new object mappers.
     *
     * @param objectMappers the object mappers
     */
    public ObjectMappers(ObjectMapper[] objectMappers) {
        if (objectMappers == null) {
            objectMappers = new ObjectMapper[0];
        }
        this.objectMappers = ImmutableList.copyOf(Iterators.forArray(objectMappers));
    }

    
    /**
     * Instantiates a new object mappers.
     *
     * @param objectMappers the object mappers
     */
    public ObjectMappers(ImmutableList<ObjectMapper> objectMappers) {
        this.objectMappers = objectMappers;
    }

    
    /**
     * Mapper.
     *
     * @return the object mapper
     */
    public ObjectMapper mapper() {
        if (objectMappers.isEmpty()) {
            return null;
        }
        return objectMappers.get(0);
    }

    
    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    public boolean isEmpty() {
        return objectMappers.isEmpty();
    }

    
    /**
     * Mappers.
     *
     * @return the immutable list
     */
    public ImmutableList<ObjectMapper> mappers() {
        return this.objectMappers;
    }

    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public UnmodifiableIterator<ObjectMapper> iterator() {
        return objectMappers.iterator();
    }

    
    /**
     * Concat.
     *
     * @param mapper the mapper
     * @return the object mappers
     */
    public ObjectMappers concat(ObjectMapper mapper) {
        return new ObjectMappers(new ImmutableList.Builder<ObjectMapper>().addAll(objectMappers).add(mapper).build());
    }

    
    /**
     * Concat.
     *
     * @param mappers the mappers
     * @return the object mappers
     */
    public ObjectMappers concat(ObjectMappers mappers) {
        return new ObjectMappers(new ImmutableList.Builder<ObjectMapper>().addAll(objectMappers).addAll(mappers).build());
    }

    
    /**
     * Removes the.
     *
     * @param mappers the mappers
     * @return the object mappers
     */
    public ObjectMappers remove(Iterable<ObjectMapper> mappers) {
        ImmutableList.Builder<ObjectMapper> builder = new ImmutableList.Builder<ObjectMapper>();
        for (ObjectMapper objectMapper : objectMappers) {
            boolean found = false;
            for (ObjectMapper mapper : mappers) {
                if (objectMapper == mapper) { 
                    found = true;
                }
            }
            if (!found) {
                builder.add(objectMapper);
            }
        }
        return new ObjectMappers(builder.build());
    }

    
    /**
     * Removes the.
     *
     * @param mapper the mapper
     * @return the object mappers
     */
    public ObjectMappers remove(ObjectMapper mapper) {
        ImmutableList.Builder<ObjectMapper> builder = new ImmutableList.Builder<ObjectMapper>();
        for (ObjectMapper objectMapper : objectMappers) {
            if (objectMapper != mapper) { 
                builder.add(objectMapper);
            }
        }
        return new ObjectMappers(builder.build());
    }
}
