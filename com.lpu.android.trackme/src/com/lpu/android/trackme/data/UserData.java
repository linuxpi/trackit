package com.lpu.android.trackme.data;

import java.io.Serializable;

public class UserData implements Serializable{

	private String regId;
	private int uniqueKey;
	private double cordX;
	private double cordY;

	public UserData(String reg,int unKey){
		this(reg,unKey,0,0);
	}

	public UserData(String reg,int unKey,int x,int y){
		regId=reg;
		uniqueKey=unKey;

		cordY = y;
		cordX = x;
	}

	public String getRegId(){
		return regId;
		
	}

	public int getUniqueKey(){
		return uniqueKey;
	}

	public double getXCord(){
		return cordX;
	}
    
	public double getYCord(){
		return cordY;
	}
}