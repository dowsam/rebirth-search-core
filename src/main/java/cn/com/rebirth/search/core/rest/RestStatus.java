/*
 * Copyright (c) 2005-2012 www.summall.com.cn All rights reserved
 * Info:summall-search-core RestStatus.java 2012-3-29 15:01:55 l.xue.nong$$
 */


package cn.com.rebirth.search.core.rest;

import java.io.IOException;

import cn.com.rebirth.commons.io.stream.StreamInput;
import cn.com.rebirth.commons.io.stream.StreamOutput;


/**
 * The Enum RestStatus.
 *
 * @author l.xue.nong
 */
public enum RestStatus {
    
    
    /** The CONTINUE. */
    CONTINUE(100),
    
    
    /** The SWITCHIN g_ protocols. */
    SWITCHING_PROTOCOLS(101),
    
    
    /** The OK. */
    OK(200),
    
    
    /** The CREATED. */
    CREATED(201),
    
    
    /** The ACCEPTED. */
    ACCEPTED(202),
    
    
    /** The NO n_ authoritativ e_ information. */
    NON_AUTHORITATIVE_INFORMATION(203),
    
    
    /** The N o_ content. */
    NO_CONTENT(204),
    
    
    /** The RESE t_ content. */
    RESET_CONTENT(205),
    
    
    /** The PARTIA l_ content. */
    PARTIAL_CONTENT(206),
    
    
    /** The MULT i_ status. */
    MULTI_STATUS(207),
    
    
    /** The MULTIPL e_ choices. */
    MULTIPLE_CHOICES(300),
    
    
    /** The MOVE d_ permanently. */
    MOVED_PERMANENTLY(301),
    
    
    /** The FOUND. */
    FOUND(302),
    
    
    /** The SE e_ other. */
    SEE_OTHER(303),
    
    
    /** The NO t_ modified. */
    NOT_MODIFIED(304),
    
    
    /** The US e_ proxy. */
    USE_PROXY(305),
    
    
    /** The TEMPORAR y_ redirect. */
    TEMPORARY_REDIRECT(307),
    
    
    /** The BA d_ request. */
    BAD_REQUEST(400),
    
    
    /** The UNAUTHORIZED. */
    UNAUTHORIZED(401),
    
    
    /** The PAYMEN t_ required. */
    PAYMENT_REQUIRED(402),
    
    
    /** The FORBIDDEN. */
    FORBIDDEN(403),
    
    
    /** The NO t_ found. */
    NOT_FOUND(404),
    
    
    /** The METHO d_ no t_ allowed. */
    METHOD_NOT_ALLOWED(405),
    
    
    /** The NO t_ acceptable. */
    NOT_ACCEPTABLE(406),
    
    
    /** The PROX y_ authentication. */
    PROXY_AUTHENTICATION(407),
    
    
    /** The REQUES t_ timeout. */
    REQUEST_TIMEOUT(408),
    
    
    /** The CONFLICT. */
    CONFLICT(409),
    
    
    /** The GONE. */
    GONE(410),
    
    
    /** The LENGT h_ required. */
    LENGTH_REQUIRED(411),
    
    
    /** The PRECONDITIO n_ failed. */
    PRECONDITION_FAILED(412),
    
    
    /** The REQUES t_ entit y_ to o_ large. */
    REQUEST_ENTITY_TOO_LARGE(413),
    
    
    /** The REQUES t_ ur i_ to o_ long. */
    REQUEST_URI_TOO_LONG(414),
    
    
    /** The UNSUPPORTE d_ medi a_ type. */
    UNSUPPORTED_MEDIA_TYPE(415),
    
    
    /** The REQUESTE d_ rang e_ no t_ satisfied. */
    REQUESTED_RANGE_NOT_SATISFIED(416),
    
    
    /** The EXPECTATIO n_ failed. */
    EXPECTATION_FAILED(417),
    
    
    /** The UNPROCESSABL e_ entity. */
    UNPROCESSABLE_ENTITY(422),
    
    
    /** The LOCKED. */
    LOCKED(423),
    
    
    /** The FAILE d_ dependency. */
    FAILED_DEPENDENCY(424),
    
    
    /** The INTERNA l_ serve r_ error. */
    INTERNAL_SERVER_ERROR(500),
    
    
    /** The NO t_ implemented. */
    NOT_IMPLEMENTED(501),
    
    
    /** The BA d_ gateway. */
    BAD_GATEWAY(502),
    
    
    /** The SERVIC e_ unavailable. */
    SERVICE_UNAVAILABLE(503),
    
    
    /** The GATEWA y_ timeout. */
    GATEWAY_TIMEOUT(504),
    
    
    /** The HTT p_ versio n_ no t_ supported. */
    HTTP_VERSION_NOT_SUPPORTED(505),
    
    
    /** The INSUFFICIEN t_ storage. */
    INSUFFICIENT_STORAGE(506);


    
    /** The status. */
    private int status;

    
    /**
     * Instantiates a new rest status.
     *
     * @param status the status
     */
    RestStatus(int status) {
        this.status = (short) status;
    }

    
    /**
     * Gets the status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    
    /**
     * Read from.
     *
     * @param in the in
     * @return the rest status
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static RestStatus readFrom(StreamInput in) throws IOException {
        return RestStatus.valueOf(in.readUTF());
    }

    
    /**
     * Write to.
     *
     * @param out the out
     * @param status the status
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeTo(StreamOutput out, RestStatus status) throws IOException {
        out.writeUTF(status.name());
    }
}