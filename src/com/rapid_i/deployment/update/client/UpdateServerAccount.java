/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */

package com.rapid_i.deployment.update.client;

import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Observable;
import java.util.logging.Level;

import com.rapidminer.gui.tools.PasswordDialog;
import com.rapidminer.tools.GlobalAuthenticator;
import com.rapidminer.tools.LogService;

/**
 * Observable class which stores information about the currently active user account for the Update Server.
 * 
 * @author Dominik Halfkann
 */
public class UpdateServerAccount extends Observable {

	private static PasswordAuthentication upateServerPA = null;
	
	private boolean loggedIn = false;
	
	static {
		GlobalAuthenticator.registerServerAuthenticator(new GlobalAuthenticator.URLAuthenticator() {

			@Override
			public PasswordAuthentication getAuthentication(URL url) {
				try {
					if (url.toString().startsWith(UpdateManager.getUpdateServerURI("").toString())) {
						return upateServerPA != null ? upateServerPA : new PasswordAuthentication("", new char[] {});
					} else {
						return null;
					}
				} catch (URISyntaxException e) {
					return null;
				}
			}

			@Override
			public String getName() {
				return "UpdateService authenticator.";
			}

			@Override
			public String toString() {
				return getName();
			}
		});
	}
	
	public void forceNotifyObservers() {
		if (isAccountServiceLoggedIn()) loggedIn = true;
		setChanged();
		notifyObservers();
	}
	
	private boolean isAccountServiceLoggedIn() {
		return loggedIn;
	}
	
	public static void setPasswordAuthentication(PasswordAuthentication pa) {
		upateServerPA = pa;
	}
	
	/** Shows the login dialog and notifies observers when the status changed.
	 * @return true when the user logged in successfully, false otherwise **/
	public boolean login() {
		try {
			while (true) {
				
				if (isAccountServiceLoggedIn()) {
					loggedIn = true;
					return true;
				}
				
				PasswordAuthentication pa = PasswordDialog.getPasswordAuthentication(UpdateManager.getUpdateServerURI("").toString(), false, false);

				loggedIn = pa != null;
				if (loggedIn) {
					//user hit "ok"
					upateServerPA = pa;

					//check the provided login data
					try {
						UpdateManager.getAccountService();
					} catch (Exception e) {
						LogService.getRoot().log(Level.WARNING, "Failed to login: "+e, e);
						// wrong login data
						loggedIn = false;
						continue;
					}
					//updateAccountInfoButton(pa.getUserName());
					setChanged();
					notifyObservers(null);
					return true;
				} else {
					//user hit "cancel"
					upateServerPA = null;
					setChanged();
					notifyObservers(null);
					return false;
				}
				
			}
		} catch (URISyntaxException e) {
			return false;
		}
	}
	
	public void logout() {
		UpdateManager.clearAccountSerive();
		upateServerPA = null;
		loggedIn = false;
		setChanged();
		notifyObservers(null);
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public String getUserName() {
		return upateServerPA != null ? upateServerPA.getUserName() : null;
	}
	
}
