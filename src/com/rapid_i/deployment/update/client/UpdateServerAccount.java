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
import com.rapidminer.gui.tools.ProgressThread;
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
		setChanged();
		notifyObservers();
	}
	
	public static void setPasswordAuthentication(PasswordAuthentication pa) {
		upateServerPA = pa;
	}
	
	/** Shows the login dialog and notifies observers when the status changed.**/
	public void login(final UpdatePackagesModel updateModel) {
		new ProgressThread("log_in_to_updateserver", false) {
			public void run() {
				try {
					while (true) {
						
						if (loggedIn) {
							return;
						}
						
						UpdateManager.clearAccountSerive();
						
						PasswordAuthentication pa = PasswordDialog.getPasswordAuthentication(UpdateManager.getUpdateServerURI("").toString(), false, false);
		
						boolean clickedOk = pa != null;
						if (clickedOk) {
							//user hit "ok"
							upateServerPA = pa;
		
							getProgressListener().setCompleted(10);
							//check the provided login data
							try {
								UpdateManager.getAccountService();
							} catch (Exception e) {
								LogService.getRoot().log(Level.WARNING, "Failed to login: "+e, e);
								// wrong login data
								continue;
							}
							getProgressListener().setCompleted(50);
							loggedIn = true;
							updateModel.updatePurchasedPackages();
							getProgressListener().setCompleted(90);
							setChanged();
							notifyObservers(null);
							getProgressListener().setCompleted(100);
							return;
						} else {
							//user hit "cancel"
							upateServerPA = null;
							setChanged();
							notifyObservers(null);
							return;
						}
						
					}
				} catch (URISyntaxException e) {
					return;
				}
			}
		}.start();
	}
	
	public void logout(final UpdatePackagesModel updateModel) {
		new ProgressThread("log_out_frm_updateserver", false) {
			public void run() {
				UpdateManager.clearAccountSerive();
				upateServerPA = null;
				loggedIn = false;
				updateModel.clearPurchasedPackages();
				setChanged();
				notifyObservers(null);
			}
		}.start();
	}
	
	public void updatePurchasedPackages(final UpdatePackagesModel updateModel) {
		new ProgressThread("fetching_updates", false) {
			public void run() {
				getProgressListener().setCompleted(10);
				updateModel.updatePurchasedPackages();
				getProgressListener().setCompleted(75);
				setChanged();
				notifyObservers(null);
				getProgressListener().setCompleted(100);
			}
		}.start();
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public String getUserName() {
		return upateServerPA != null ? upateServerPA.getUserName() : null;
	}

	public char[] getPassword() {
		return upateServerPA.getPassword();
	}
	
}
