package com.flyingpig.demo.common;


public class Result {
    public int getStatus() {
        return status;
    }

    public Result() {
    }

    public Result(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //增删改
    public static Result success() {
        return new Result(200, "success", null);
    }

    //查询 成功响应
    public static Result success(Object data) {
        return new Result(200, "success", data);
    }

    //失败响应
    public static Result error(Integer code,String msg) {
        return new Result(code, msg, null);
    }

    public static Result error(String msg) {
        return new Result(500, msg, null);
    }


    private int status;
    private String message;

    Object data;



}
