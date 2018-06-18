package com.changyu.foryou.model;

import java.util.Date;


public class ChatMsg {
	
	private String fromId;
	
	private String toId;
	
	private String fromHeadUrl;
	
	private String toHeadUrl;

	private String msg;
	
	private Date time;
	
	private short isRead;
	
	private String relativeOrderId;
	
	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public String getFromHeadUrl() {
		return fromHeadUrl;
	}

	public void setFromHeadUrl(String fromHeadUrl) {
		this.fromHeadUrl = fromHeadUrl;
	}

	public String getToHeadUrl() {
		return toHeadUrl;
	}

	public void setToHeadUrl(String toHeadUrl) {
		this.toHeadUrl = toHeadUrl;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public short getIsRead() {
		return isRead;
	}

	public void setIsRead(short isRead) {
		this.isRead = isRead;
	}

	public String getRelativeOrderId() {
		return relativeOrderId;
	}

	public void setRelativeOrderId(String relativeOrderId) {
		this.relativeOrderId = relativeOrderId;
	}

}
