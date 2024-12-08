package com.flyingpig.demo.controller;


public class Result {

    private int status;
    private String message;



    public Result(int status, String message){
        this.status = status;
        this.message = message;
    }

    // Getter 和 Setter 方法
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
