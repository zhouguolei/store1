package com.yijiagou.pojo;

/**
 * Created by zgl on 17-8-15.
 */
public class UserAndDevice {
    private String uname;
    private String deviceid;
    private String devicetype;

    public UserAndDevice() {
    }

    public UserAndDevice(String uname, String deviceid, String devicetype) {
        this.uname = uname;
        this.deviceid = deviceid;
        this.devicetype = devicetype;
    }

    public UserAndDevice(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getDevicetype() {
        return devicetype;
    }

    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }
}
