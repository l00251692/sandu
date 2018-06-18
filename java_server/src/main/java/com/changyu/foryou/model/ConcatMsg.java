package com.changyu.foryou.model;

import java.util.Date;

public class ConcatMsg {
	
	private String concatId;
	
	private String fromId;
	
	private String toId;
	
	private String msg;
	
	private Date time;
	
	private short unReadNum;

	public String getConcatId() {
		return concatId;
	}

	public void setConcatId(String concatId) {
		this.concatId = concatId;
	}

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

	public short getUnReadNum() {
		return unReadNum;
	}

	public void setUnReadNum(short unReadNum) {
		this.unReadNum = unReadNum;
	}
	

}
