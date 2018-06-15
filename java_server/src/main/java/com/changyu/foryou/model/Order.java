package com.changyu.foryou.model;

import java.util.Date;

public class Order {
	private String orderId;
	
	private String cityName;
	
	private String DistrictName;
	
    private String fromAddr;
    
    private String fromAddrDetai;
    
    private String fromAddrLongitude;
    
    private String fromAddrLatitude;
    
    private String toAddr;
    
    private String toAddrDetai;
    
    private String toAddrLongitude;
    
    private String toAddrLatitude;

    private String createUser;
    
    private Date createTime;
    
    private Date departTime;
    
    private Date rcvTime;
    
    private short orderType;
    
    private short orderStatus;
    
    private Float orderPrice;
    
    private String records;
    
    private String receiveUser;
    
    private short startByPassenger;
    
    private short starByDriver;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getFromAddr() {
		return fromAddr;
	}

	public void setFromAddr(String fromAddr) {
		this.fromAddr = fromAddr;
	}

	public String getFromAddrDetai() {
		return fromAddrDetai;
	}

	public void setFromAddrDetai(String fromAddrDetai) {
		this.fromAddrDetai = fromAddrDetai;
	}

	public String getFromAddrLongitude() {
		return fromAddrLongitude;
	}

	public void setFromAddrLongitude(String fromAddrLongitude) {
		this.fromAddrLongitude = fromAddrLongitude;
	}

	public String getFromAddrLatitude() {
		return fromAddrLatitude;
	}

	public void setFromAddrLatitude(String fromAddrLatitude) {
		this.fromAddrLatitude = fromAddrLatitude;
	}

	public String getToAddr() {
		return toAddr;
	}

	public void setToAddr(String toAddr) {
		this.toAddr = toAddr;
	}

	public String getToAddrDetai() {
		return toAddrDetai;
	}

	public void setToAddrDetai(String toAddrDetai) {
		this.toAddrDetai = toAddrDetai;
	}

	public String getToAddrLongitude() {
		return toAddrLongitude;
	}

	public void setToAddrLongitude(String toAddrLongitude) {
		this.toAddrLongitude = toAddrLongitude;
	}

	public String getToAddrLatitude() {
		return toAddrLatitude;
	}

	public void setToAddrLatitude(String toAddrLatitude) {
		this.toAddrLatitude = toAddrLatitude;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getDepartTime() {
		return departTime;
	}

	public void setDepartTime(Date departTime) {
		this.departTime = departTime;
	}

	public short getOrderType() {
		return orderType;
	}

	public void setOrderType(short orderType) {
		this.orderType = orderType;
	}

	public Short getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Short orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Float getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(Float orderPrice) {
		this.orderPrice = orderPrice;
	}

	public String getRecords() {
		return records;
	}

	public void setRecords(String records) {
		this.records = records;
	}

	public String getReceiveUser() {
		return receiveUser;
	}

	public void setReceiveUser(String receiveUser) {
		this.receiveUser = receiveUser;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getDistrictName() {
		return DistrictName;
	}

	public void setDistrictName(String districtName) {
		DistrictName = districtName;
	}

	public short getStartByPassenger() {
		return startByPassenger;
	}

	public void setStartByPassenger(short startByPassenger) {
		this.startByPassenger = startByPassenger;
	}

	public short getStarByDriver() {
		return starByDriver;
	}

	public void setStarByDriver(short starByDriver) {
		this.starByDriver = starByDriver;
	}

	public Date getRcvTime() {
		return rcvTime;
	}

	public void setRcvTime(Date rcvTime) {
		this.rcvTime = rcvTime;
	}

}
