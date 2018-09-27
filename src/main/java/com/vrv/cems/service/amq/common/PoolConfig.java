package com.vrv.cems.service.amq.common;

public class PoolConfig {


    private String URL ;
    private String userName ;
    private String password ;

    private int maxConnection ;
    private int minConnection ;
    private int maxSesstionPerConnection ;
    private int minSesstionPerConnection ;
    private int maxProducerPerSession ;
    private int minProducerPerSession ;
    private int maxConsumerPerSession ;
    private int minConsumerPerSession ;

    public PoolConfig() {
    }

    public PoolConfig(String URL, String userName, String password, int maxConnection, int minConnection, int maxSesstionPerConnection, int minSesstionPerConnection, int maxProducerPerSession, int minProducerPerSession, int maxConsumerPerSession, int minConsumerPerSession) {
        this.URL = URL;
        this.userName = userName;
        this.password = password;
        this.maxConnection = maxConnection;
        this.minConnection = minConnection;
        this.maxSesstionPerConnection = maxSesstionPerConnection;
        this.minSesstionPerConnection = minSesstionPerConnection;
        this.maxProducerPerSession = maxProducerPerSession;
        this.minProducerPerSession = minProducerPerSession;
        this.maxConsumerPerSession = maxConsumerPerSession;
        this.minConsumerPerSession = minConsumerPerSession;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(int maxConnection) {
        this.maxConnection = maxConnection;
    }

    public int getMinConnection() {
        return minConnection;
    }

    public void setMinConnection(int minConnection) {
        this.minConnection = minConnection;
    }

    public int getMaxSesstionPerConnection() {
        return maxSesstionPerConnection;
    }

    public void setMaxSesstionPerConnection(int maxSesstionPerConnection) {
        this.maxSesstionPerConnection = maxSesstionPerConnection;
    }

    public int getMinSesstionPerConnection() {
        return minSesstionPerConnection;
    }

    public void setMinSesstionPerConnection(int minSesstionPerConnection) {
        this.minSesstionPerConnection = minSesstionPerConnection;
    }

    public int getMaxProducerPerSession() {
        return maxProducerPerSession;
    }

    public void setMaxProducerPerSession(int maxProducerPerSession) {
        this.maxProducerPerSession = maxProducerPerSession;
    }

    public int getMinProducerPerSession() {
        return minProducerPerSession;
    }

    public void setMinProducerPerSession(int minProducerPerSession) {
        this.minProducerPerSession = minProducerPerSession;
    }

    public int getMaxConsumerPerSession() {
        return maxConsumerPerSession;
    }

    public void setMaxConsumerPerSession(int maxConsumerPerSession) {
        this.maxConsumerPerSession = maxConsumerPerSession;
    }

    public int getMinConsumerPerSession() {
        return minConsumerPerSession;
    }

    public void setMinConsumerPerSession(int minConsumerPerSession) {
        this.minConsumerPerSession = minConsumerPerSession;
    }
}
