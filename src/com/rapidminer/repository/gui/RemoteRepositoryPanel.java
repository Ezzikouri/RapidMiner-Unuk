/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
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

package com.rapidminer.repository.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.rapidminer.gui.security.UserCredential;
import com.rapidminer.gui.security.Wallet;
import com.rapidminer.gui.tools.ProgressThread;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.remote.RemoteRepository;

/** Panel to add remote repositories
 * 
 * @author Simon Fischer, Nils Woehler
 *
 */
public class RemoteRepositoryPanel extends JPanel implements RepositoryConfigurationPanel {

	private static final long serialVersionUID = 1L;

	private static final ImageIcon OKAY_ICON = SwingTools.createIcon("24/ok.png");
	private static final ImageIcon ERROR_ICON = SwingTools.createIcon("24/error.png");
	private static final ImageIcon QUESTION_ICON = SwingTools.createIcon("24/symbol_questionmark.png");

	private final JTextField urlField = new JTextField("http://localhost:8080/", 30);
	private final JTextField aliasField = new JTextField("NewRepository", 30);
	private final JTextField userField = new JTextField(System.getProperty("user.name"), 20);
	private final JPasswordField passwordField = new JPasswordField(20);

	private final ResourceAction checkConnectionSettingsAction = new ResourceAction(false, "check_connection_settings") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			new ProgressThread("check_connection_settings", true) {

				@Override
				public void run() {
					getProgressListener().setTotal(100);

					getProgressListener().setCompleted(43);

					boolean configCorrect = RemoteRepository.checkConfiguration(urlField.getText(), userField.getText(), passwordField.getPassword());
					if (configCorrect) {
						checkButton.setIcon(OKAY_ICON);
					} else {
						// show error
						checkButton.setIcon(ERROR_ICON);
					}

					getProgressListener().complete();
				}
			}.start();
		}

	};

	private final JButton checkButton = new JButton(checkConnectionSettingsAction);

	private KeyListener resetCheckButtonKeyListener = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyReleased(KeyEvent e) {
			resetCheckButtonIcon();
		}

		@Override
		public void keyPressed(KeyEvent e) {}
	};

	public RemoteRepositoryPanel() {
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weighty = 1;
		c.weightx = .5;
		c.insets = new Insets(4, 4, 4, 4);

		// ALIAS
		c.gridwidth = GridBagConstraints.RELATIVE;
		JLabel label = new ResourceLabel("repositorydialog.alias");
		label.setLabelFor(aliasField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(aliasField, c);
		add(aliasField);

		// URL
		c.gridwidth = GridBagConstraints.RELATIVE;
		label = new ResourceLabel("repositorydialog.url");
		label.setLabelFor(urlField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(urlField, c);
		add(urlField);

		// USERNAME
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(10, 4, 4, 4);
		label = new ResourceLabel("repositorydialog.user");
		label.setLabelFor(userField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(userField, c);
		add(userField);

		// Password
		c.insets = new Insets(4, 4, 4, 4);
		c.gridwidth = GridBagConstraints.RELATIVE;
		label = new ResourceLabel("repositorydialog.password");
		label.setLabelFor(passwordField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(passwordField, c);
		add(passwordField);

		aliasField.selectAll();
		urlField.selectAll();
		userField.selectAll();

		userField.addKeyListener(resetCheckButtonKeyListener);
		passwordField.addKeyListener(resetCheckButtonKeyListener);
		urlField.addKeyListener(resetCheckButtonKeyListener);
	}

	@Override
	public void makeRepository() {
		final URL url;
		try {
			url = new URL(urlField.getText());
		} catch (MalformedURLException e) {
			SwingTools.showSimpleErrorMessage("illegal_url", e);
			return;
		}
		String alias = aliasField.getText().trim();
		if (alias.length() == 0) {
			alias = url.toString();
		}
		final String finalAlias = alias;

		ProgressThread pt = new ProgressThread("add_repository") {

			@Override
			public void run() {
				getProgressListener().setTotal(100);
				getProgressListener().setCompleted(10);
				Repository repository = new RemoteRepository(url, finalAlias, userField.getText(), passwordField.getPassword(), false);
				getProgressListener().setCompleted(90);
				RepositoryManager.getInstance(null).addRepository(repository);
				getProgressListener().setCompleted(100);
				getProgressListener().complete();
			}
		};
		pt.start();
	}

	@Override
	public void configureUIElementsFrom(Repository remote) {
		aliasField.setText(((RemoteRepository) remote).getAlias());
		urlField.setText(((RemoteRepository) remote).getBaseUrl().toString());
		userField.setText(((RemoteRepository) remote).getUsername());
		UserCredential credentials = Wallet.getInstance().getEntry(urlField.getText());
		if (credentials != null) {
			passwordField.setText(new String(credentials.getPassword()));
		}
	}

	@Override
	public boolean configure(final Repository repository) {
		URL url;
		try {
			url = new URL(urlField.getText());
		} catch (MalformedURLException e) {
			SwingTools.showSimpleErrorMessage("illegal_url", e);
			return false;
		}

		String userName = userField.getText();
		char[] password = passwordField.getPassword();

		((RemoteRepository) repository).rename(aliasField.getText());
		((RemoteRepository) repository).setBaseUrl(url);
		((RemoteRepository) repository).setUsername(userName);
		((RemoteRepository) repository).setPassword(password);

		UserCredential authenticationCredentials = new UserCredential(urlField.getText(), userName, password);
		Wallet.getInstance().registerCredentials(authenticationCredentials);
		Wallet.getInstance().saveCache();

		return true;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void setOkButton(JButton okButton) {
		// NOOP
	}

	@Override
	public List<AbstractButton> getAdditionalButtons() {
		LinkedList<AbstractButton> buttons = new LinkedList<AbstractButton>();
		buttons.add(checkButton);
		return buttons;
	}

	private void resetCheckButtonIcon() {
		checkButton.setIcon(QUESTION_ICON);
	}
}
