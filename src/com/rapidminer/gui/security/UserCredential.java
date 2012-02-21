package com.rapidminer.gui.security;

import java.net.PasswordAuthentication;

/** The user credentials stored in a {@link Wallet}. Each username belongs to one URL. 
 * 
 * @author Miguel Büscher
 *
 */
public class UserCredential {
	
	private String url;
	private String user;
	private char[] password;

	public UserCredential(String url, String user, char[] password) {
		super();
		this.url = url;
		this.user = user;
		this.password = password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}
	
	public String getURL() {		 
		 return url; 
	 }
	
	public String getUsername() {
		 return user;
	 }
	
	public char[] getPassword() {
		 return password;
	 }
	
	public PasswordAuthentication makePasswordAuthentication() {
		return new PasswordAuthentication(getUsername(), getPassword());
	}

	@Override
	public UserCredential clone() {
		return new UserCredential(getURL(), getUsername(), getPassword());
	}
}
