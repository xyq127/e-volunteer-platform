package com.evolunteer.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 统一响应结果类：控制器统一返回该对象，code 为 100 表示处理成功、200 表示处理失败，
 * msg 为提示信息，extend 存放返回给页面的业务数据。
 */
public class Msg {

    /**
     * 处理成功状态码
     */
    private static final int CODE_SUCCESS = 100;

    /**
     * 处理失败状态码
     */
    private static final int CODE_FAIL = 200;

    /**
     * 状态码
     */
    private int code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回给页面的业务数据
     */
    private Map<String, Object> extend = new HashMap<>();

    /**
     * 构造处理成功的响应结果
     *
     * @return 处理成功的响应结果
     */
    public static Msg success() {
        Msg result = new Msg();
        result.setCode(CODE_SUCCESS);
        result.setMsg("处理成功！");
        return result;
    }

    /**
     * 构造处理失败的响应结果
     *
     * @return 处理失败的响应结果
     */
    public static Msg fail() {
        Msg result = new Msg();
        result.setCode(CODE_FAIL);
        result.setMsg("处理失败！");
        return result;
    }

    /**
     * 追加返回给页面的业务数据
     *
     * @param key   数据名称
     * @param value 数据内容
     * @return 当前响应结果
     */
    public Msg add(String key, Object value) {
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
