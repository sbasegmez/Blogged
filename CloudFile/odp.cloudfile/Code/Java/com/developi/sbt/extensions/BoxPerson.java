package com.developi.sbt.extensions;

/**
 * BoxPerson is the representation of a person identity on Basecamp.
 */

import java.io.Serializable;

import com.ibm.commons.util.io.json.JsonJavaObject;

public class BoxPerson implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String emailAddress; //email_address
	private String avatarUrl; //avatar_url
	
	private boolean empty;
	
	public BoxPerson() {
		setEmpty(true);
	}
	
	public BoxPerson(JsonJavaObject data) {
		this();
		setData(data);
	}
	
	/**
	 * Constructing a BoxPerson object from a JSON data
	 */
	public void setData(JsonJavaObject data) {
		if(data!=null) {
			this.id=data.getAsString("id");
			this.name = data.getAsString("name");
			this.emailAddress = data.getAsString("login");
			this.avatarUrl = data.getAsString("avatar_url");
			empty=false;
		}
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getAvatarUrl() {
		return avatarUrl;
	}
	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}

	public boolean isEmpty() {
		return empty;
	}

}
