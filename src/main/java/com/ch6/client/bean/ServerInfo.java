package com.ch6.client.bean;

public class ServerInfo {

    private int port;
    private String address;
    private String sn;

    public ServerInfo(int port, String address, String sn) {
        this.port = port;
        this.address = address;
        this.sn = sn;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "port=" + port +
                ", address='" + address + '\'' +
                ", sn='" + sn + '\'' +
                '}';
    }
}
