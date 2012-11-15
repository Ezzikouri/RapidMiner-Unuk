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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.components.LinkButton;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.NetTools;

/**
 * 
 * @author Simon Fischer
 * 
 */
public class UpdateDialog extends ButtonDialog {

	private static final long serialVersionUID = 1L;
	static {
		NetTools.init();
	}
	public static final Action UPDATE_ACTION = new ResourceAction("update_manager") {

		private static final long serialVersionUID = 1L;
		{
			setCondition(EDIT_IN_PROGRESS, DONT_CARE);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			showUpdateDialog();
		}
	};

	private WindowListener windowListener = new WindowListener() {
		public void windowActivated(WindowEvent e) {
			UpdateServerAccount account = UpdateManager.getUpdateServerAccount();
			account.updatePurchasedPackages(updateModel);
	    }

		@Override
		public void windowOpened(WindowEvent e) {}

		@Override
		public void windowClosing(WindowEvent e) {}

		@Override
		public void windowClosed(WindowEvent e) {}

		@Override
		public void windowIconified(WindowEvent e) {}

		@Override
		public void windowDeiconified(WindowEvent e) {}

		@Override
		public void windowDeactivated(WindowEvent e) {}
	};
	
	private final UpdateService service;

	private final UpdatePanel ulp;
	
	private static UpdatePackagesModel updateModel;

	private static class USAcountInfoButton extends LinkButton implements Observer {

		private static final long serialVersionUID = 1L;

		public USAcountInfoButton() {
			super(new AbstractAction("") {

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					UpdateServerAccount account = UpdateManager.getUpdateServerAccount();
					if (account.isLoggedIn()) {
						account.logout();
					} else {
						account.login(updateModel);
					}

				}
			});

			Dimension size = new Dimension(300, 24);
			this.setSize(size);
			this.setMaximumSize(size);
			this.setPreferredSize(size);
		}

		@Override
		public void update(Observable obs, Object arg) {
			if (obs instanceof UpdateServerAccount) {
				UpdateServerAccount account = (UpdateServerAccount) obs;
				if (account.isLoggedIn()) {
					this.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.account_button.logged_in", account.getUserName()));
				} else {
					this.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.account_button.logged_out"));
				}
			}
		}
	}
	
	private class InstallButton extends JButton implements Observer {
		private static final long serialVersionUID = 1L;

		InstallButton(Action a) {
			super(a);
		}
		
		@Override
		public void update(Observable o, Object arg) {
			if (o instanceof UpdatePackagesModel) {
				UpdatePackagesModel currentModel = (UpdatePackagesModel)o;
				if (currentModel.getInstallationList() != null && currentModel.getInstallationList().size() > 0) {
					this.setEnabled(true);
				} else {
					this.setEnabled(false);
				}
			}
		}
		
	}

	private USAcountInfoButton accountInfoButton = new USAcountInfoButton();

	public UpdateDialog(UpdateService service, List<PackageDescriptor> descriptors, String[] preselectedExtensions) {
		super("update");
		setModal(true);
		this.service = service;
		UpdateServerAccount usAccount = UpdateManager.getUpdateServerAccount();
		usAccount.addObserver(accountInfoButton);
		updateModel = new UpdatePackagesModel(descriptors, usAccount);
		ulp = new UpdatePanel(this, descriptors, preselectedExtensions, usAccount, updateModel);
		layoutDefault(ulp, LARGE, makeOkButton("update.install"), makeCloseButton());
		this.addWindowListener(windowListener);
	}

	@Override
	protected JButton makeOkButton(String i18nKey) {
		
		Action okAction = new ResourceAction(i18nKey) {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				wasConfirmed = true;
				ok();
			}			
		};
		InstallButton button = new InstallButton(okAction);
		getRootPane().setDefaultButton(button);

		button.setEnabled(false);
		updateModel.addObserver(button);
		return button;
	}
	
	@Override
	/** Overriding makeButtonPanel in order to display account information. **/
	protected JPanel makeButtonPanel(AbstractButton... buttons) {
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel buttonPanelRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		for (AbstractButton button : buttons) {
			if (button != null) {
				buttonPanelRight.add(button);
			}
		}
		buttonPanel.add(buttonPanelRight, BorderLayout.CENTER);
		JPanel buttonPanelLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 2 * GAP));
		buttonPanelLeft.add(accountInfoButton, false);
		buttonPanel.add(buttonPanelLeft, BorderLayout.WEST);
		return buttonPanel;
	}

	public static void showUpdateDialog(final String... preselectedExtensions) {
		final UpdateService service;
		try {
			service = UpdateManager.getService();
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("failed_update_server", e, UpdateManager.getBaseUrl());
			return;
		}

		final List<PackageDescriptor> descriptors = new LinkedList<PackageDescriptor>();
		new UpdateDialog(service, descriptors, preselectedExtensions).setVisible(true);
	}

	public void startUpdate(final List<PackageDescriptor> downloadList) {
		new ProgressThread("installing_updates", true) {

			@Override
			public void run() {
				try {
					getProgressListener().setTotal(100);
					getProgressListener().setCompleted(10);

					// Download licenses
					Map<String, String> licenses = new HashMap<String, String>();
					for (PackageDescriptor desc : downloadList) {
						String license = licenses.get(desc.getLicenseName());
						if (license == null) {
							license = service.getLicenseText(desc.getLicenseName());
							licenses.put(desc.getLicenseName(), license);
						}
					}

					// Confirm licenses
					getProgressListener().setCompleted(20);
					List<PackageDescriptor> acceptedList = new LinkedList<PackageDescriptor>();
					for (PackageDescriptor desc : downloadList) {
						if (ConfirmLicenseDialog.confirm(desc, licenses.get(desc.getLicenseName()))) {
							acceptedList.add(desc);
						}
					}

					if (!acceptedList.isEmpty()) {
						UpdateManager um = new UpdateManager(service);
						int result = um.performUpdates(acceptedList, getProgressListener());
						UpdateDialog.this.dispose();
						if (SwingTools.showConfirmDialog((result == 1 ? "update.complete_restart" : "update.complete_restart1"), ConfirmDialog.YES_NO_OPTION, result) == ConfirmDialog.YES_OPTION) {
							RapidMinerGUI.getMainFrame().exit(true);
						}
					}
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("error_installing_update", e, e.getMessage());
				} finally {					
					getProgressListener().complete();
				}
			}
		}.start();
	}

	@Override
	protected void ok() {
		ulp.startUpdate();
	}
}
