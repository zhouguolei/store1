package com.yijiagou.pojo;

/**
 * Created by zgl on 17-8-11.
 */
public class UrlAppinfo {
    private String id;
    private String info;

    public UrlAppinfo() {
    }

    public UrlAppinfo(String id, String info) {
        this.id = id;
        this.info = info;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
