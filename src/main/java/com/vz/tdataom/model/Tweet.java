package com.vz.tdataom.model;

public class Tweet implements java.io.Serializable {

	private static final long serialVersionUID = -7612780773183083396L;
	
	private String id ;
	private String postedTime;
	private String body ;
    private Gnip gnip;
    private Actor actor;
	
    public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Actor getActor() {
		return actor;
	}
	public void setActor(Actor actor) {
		this.actor = actor;
	}
	public String getPostedTime() {
		return postedTime;
	}
	public void setPostedTime(String postedTime) {
		this.postedTime = postedTime;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Gnip getGnip() {
		return gnip;
	}
	public void setGnip(Gnip gnip) {
		this.gnip = gnip;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
    
    
 
    

}
