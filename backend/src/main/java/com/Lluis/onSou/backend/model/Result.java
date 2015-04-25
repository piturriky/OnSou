package com.Lluis.onSou.backend.model;

/**
 * Created by Lluís on 25/04/2015.
 */
public class Result {
    public boolean status;
    public String msg;
    public Object obj;

    public Result(){}

    public Result(boolean status){
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
