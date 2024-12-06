package com.flyingpig.demo.controller;


public class Result {
    private Integer code;//响应码，token过期返回0，正确返回1，异常返回2
    private String msg;//响应信息，描述字符串
    private Object data;//返回的数据

    //增删改
    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    //查询 成功响应

}