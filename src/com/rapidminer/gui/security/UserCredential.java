package com.rapidminer.gui.security;

/** The Usercredentials for the PasswordManager. 
 * 
 * @author Miguel Büscher
 *
 */
public class UserCredential {
	
	private String url;
	private String user;
	private String password;
	
	public UserCredential() {

	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getURL() {		 
		 return url; 
	 }
	
	public String getUsername() {
		 return user;
	 }
	
	public String getPassword() {
		 return password;
	 }

}
