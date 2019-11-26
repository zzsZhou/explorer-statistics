package com.github.ontio.explorer.statistics.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    private Integer code;

    private String msg;

    private Object result;

    public Response(Result result, Object content) {
        this.code = result.code();
        this.msg = result.msg();
        this.result = content;
    }

    public Response(Result result) {
        this.code = result.code();
        this.msg = result.msg();
        this.result = "";
    }

}
