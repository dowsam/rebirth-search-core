/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core ReduceSearchPhaseException.java 2012-3-29 15:02:53 l.xue.nong$$
 */


package cn.com.rebirth.search.core.action.search;


/**
 * The Class ReduceSearchPhaseException.
 *
 * @author l.xue.nong
 */
public class ReduceSearchPhaseException extends SearchPhaseExecutionException {

    /**
     * Instantiates a new reduce search phase exception.
     *
     * @param phaseName the phase name
     * @param msg the msg
     * @param shardFailures the shard failures
     */
    public ReduceSearchPhaseException(String phaseName, String msg, ShardSearchFailure[] shardFailures) {
        super(phaseName, "[reduce] " + msg, shardFailures);
    }

    /**
     * Instantiates a new reduce search phase exception.
     *
     * @param phaseName the phase name
     * @param msg the msg
     * @param cause the cause
     * @param shardFailures the shard failures
     */
    public ReduceSearchPhaseException(String phaseName, String msg, Throwable cause, ShardSearchFailure[] shardFailures) {
        super(phaseName, "[reduce] " + msg, cause, shardFailures);
    }
}
