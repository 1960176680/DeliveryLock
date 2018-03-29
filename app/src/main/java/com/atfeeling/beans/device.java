package com.atfeeling.beans;
/**
 * Created by anxulei on 2018/3/11.
 */

public class device  {
    private String name;
    private String deviceId;
    private String rssi;
    private String state;
    private String electricity;
    private String isOpen;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }



    public device(String name, String deviceId, String rssi, String state, String electricity, String isOpen) {
        this.name = name;
        this.deviceId = deviceId;
        this.rssi = rssi;
        this.state = state;
        this.electricity = electricity;
        this.isOpen = isOpen;
    }

    public String getName() {
        return name;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRssi() {
        return rssi;
    }

    public String getElectricity() {
        return electricity;
    }

    public String getIsOpen() {
        return isOpen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public void setElectricity(String electricity) {
        this.electricity = electricity;
    }

    public void setIsOpen(String isOpen) {
        this.isOpen = isOpen;
    }
}
