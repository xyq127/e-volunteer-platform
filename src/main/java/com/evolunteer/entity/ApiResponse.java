package com.evolunteer.entity;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {

    private static final int CODE_SUCCESS = 100;

    private static final int CODE_FAIL = 200;

    private int code;

    private String msg;

    private Map<String, Object> extend = new HashMap<>();

    public static ApiResponse success() {
        ApiResponse result = new ApiResponse();
        result.setCode(CODE_SUCCESS);
        result.setMsg("处理成功！");
        return result;
    }

    public static ApiResponse success(String msg) {
        ApiResponse result = success();
        result.setMsg(msg);
        return result;
    }

    public static ApiResponse fail() {
        ApiResponse result = new ApiResponse();
        result.setCode(CODE_FAIL);
        result.setMsg("处理失败！");
        return result;
    }

    public static ApiResponse fail(String msg) {
        ApiResponse result = fail();
        result.setMsg(msg);
        return result;
    }

    public ApiResponse add(String key, Object value) {
        this.extend.put(key, value);
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, Object> extend) {
        this.extend = extend;
    }
}
