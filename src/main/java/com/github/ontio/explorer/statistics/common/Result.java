package com.github.ontio.explorer.statistics.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Result {

    SUCCESS(0, "SUCCESS"),
    BAD_REQUEST(6400, "Bad Request"),
    INTERNAL_SERVER_ERROR(6500, "Internal Server Error"),
    NOT_FOUND(6404, "Not Found");

    private int code;

    private String msg;

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }
}
