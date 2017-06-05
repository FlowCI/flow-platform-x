package com.flow.platform.util.mos;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by gy@fir.im on 01/06/2017.
 * Copyright fir.im
 */
public class Instance implements Serializable {

    public final static String STATUS_DEPLOYING = "deploying";
    public final static String STATUS_DISK = "disk";
    public final static String STATUS_READY = "ready";
    public final static String STATUS_RUNNING = "running";

    private Integer cpu;

    private Integer memory;

    private String secGroupId;

    private String secGroupName;

    @SerializedName("instanceName")
    private String name;

    private String keypairName;

    private String instanceId;

    private String instanceType;

    private String instanceTypeId;

    private String eips;

    private Date createdAt;

    private String billingType;

    private String ipAddresses;

    private String status;

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public String getSecGroupId() {
        return secGroupId;
    }

    public void setSecGroupId(String secGroupId) {
        this.secGroupId = secGroupId;
    }

    public String getSecGroupName() {
        return secGroupName;
    }

    public void setSecGroupName(String secGroupName) {
        this.secGroupName = secGroupName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKeypairName() {
        return keypairName;
    }

    public void setKeypairName(String keypairName) {
        this.keypairName = keypairName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getInstanceTypeId() {
        return instanceTypeId;
    }

    public void setInstanceTypeId(String instanceTypeId) {
        this.instanceTypeId = instanceTypeId;
    }

    public String getEips() {
        return eips;
    }

    public void setEips(String eips) {
        this.eips = eips;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getBillingType() {
        return billingType;
    }

    public void setBillingType(String billingType) {
        this.billingType = billingType;
    }

    public String getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(String ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
