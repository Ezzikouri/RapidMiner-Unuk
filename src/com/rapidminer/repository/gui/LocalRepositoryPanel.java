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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.itextpdf.text.Font;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceActionAdapter;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.repository.Repository;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.RepositoryManager;
import com.rapidminer.repository.local.LocalRepository;
import com.rapidminer.tools.I18N;
/**
 * @author Simon Fischer
 */
public class LocalRepositoryPanel extends JPanel implements RepositoryConfigurationPanel {

	private static final long serialVersionUID = 1L;
	
	private final JTextField fileField = new JTextField(30);
	private final JTextField aliasField = new JTextField("NewLocalRepository", 30);
	private final JCheckBox standardLocation = new JCheckBox(new ResourceActionAdapter("repositorydialog.use_standard_location"));
	private final JLabel errorLabel = new JLabel(" ");
	
	private JButton chooseFileButton;

	private JButton finishButton;

	/** The optional finish button will be disabled when invalid information is entered. */
	public LocalRepositoryPanel(JButton finishButton) {
		this.finishButton = finishButton;
		
		standardLocation.setSelected(true);
		errorLabel.setForeground(Color.red);
		errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD));
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weighty = 0;
		c.weightx = .5;
		c.insets = new Insets(4, 4, 4, 4);
		c.fill = GridBagConstraints.HORIZONTAL;

		// ALIAS
		c.gridwidth = 1;
		JLabel label = new ResourceLabel("repositorydialog.alias");		
		label.setLabelFor(aliasField);
		gbl.setConstraints(label, c);
		add(label);		

		c.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(aliasField, c);
		add(aliasField);

		// URL
		c.gridwidth = 1;
		c.gridheight = 2;
		label = new ResourceLabel("repositorydialog.root_directory");
		label.setLabelFor(fileField);
		gbl.setConstraints(label, c);
		add(label);

		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(standardLocation, c);

		c.gridwidth = GridBagConstraints.RELATIVE;
		gbl.setConstraints(fileField, c);
		add(fileField);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		chooseFileButton = new JButton(new ResourceAction(true, "choose_file") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.chooseFile(LocalRepositoryPanel.this, null, true, true, (String)null, null);
				if (file != null) {
					fileField.setText(file.toString());
				}
			}			
		});
		add(chooseFileButton, c);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(errorLabel, c);
		
		JPanel dummy = new JPanel();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		gbl.setConstraints(dummy, c);
		add(dummy);
		
		aliasField.selectAll();
		fileField.selectAll();
		
		aliasField.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				aliasUpdated();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				aliasUpdated();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				aliasUpdated();				
			}
		});
		fileField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				dumpSettingsCheck();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				dumpSettingsCheck();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				dumpSettingsCheck();
			}
		});
		standardLocation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableFileField();
				aliasUpdated();
			}
		});
		enableFileField();
		aliasUpdated();
	}

	@Override
	public void makeRepository() {
		File file = new File(fileField.getText());
		file.mkdir();
		String alias = aliasField.getText().trim();
		if (alias.length() == 0) {
			alias = file.getName();
		}
		try {
			RepositoryManager.getInstance(null).addRepository(new LocalRepository(alias, file));
		} catch (RepositoryException e) {
			SwingTools.showSimpleErrorMessage("cannot_create_repository", e);
		}
	}

	@Override
	public void configureUIElementsFrom(Repository repository) {
		aliasField.setText(((LocalRepository) repository).getName());
		fileField.setText(((LocalRepository) repository).getRoot().getAbsolutePath());
	}

	@Override
	public boolean configure(Repository repository) {
		((LocalRepository) repository).setRoot(new File(fileField.getText()));
		((LocalRepository) repository).rename(aliasField.getText());
		return true;
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	/** Sets the folder based on the alias.
	 *  TODO: Enable/disable finish button for illegal values */
	private void aliasUpdated() {
		if (standardLocation.isSelected()) {
			fileField.setText(LocalRepository.getDefaultRepositoryFolder(aliasField.getText()).toString());
		}
		dumpSettingsCheck();
	}

	/** Enables or disable the file field iff {@link #standardLocation} is deselected. */
	private void enableFileField() {
		boolean enabled = !standardLocation.isSelected();
		fileField.setEnabled(enabled);
		chooseFileButton.setEnabled(enabled);
		dumpSettingsCheck();
	}

	private void dumpSettingsCheck() {
		String key = checkSettings();
		if (key == null) {
			// TODO: enable ok button
			errorLabel.setText(" ");
			if (finishButton != null) {
				finishButton.setEnabled(true);
			}
		} else {
			// TODO: disable ok button
			errorLabel.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.repositorydialog.error."+key));
			if (finishButton != null) {
				finishButton.setEnabled(false);
			}
		}
	}
	
	/** Checks the current settings and returns an I18N key if settings are incorrect. */
	private String checkSettings() {
		if (aliasField.getText().isEmpty()) {
			return "alias_cannot_be_empty";
		}
		if (!RepositoryLocation.isNameValid(aliasField.getText())) {
			return "alias_invalid";
		}
		if (fileField.getText().isEmpty()) {
			return "folder_cannot_be_empty";
		}
		File file = new File(fileField.getText());
		if (file.exists() && !file.isDirectory()) {
			return "root_is_not_a_directory";
		}
		if (file.exists() && !file.canWrite()) {
			return "root_is_not_writable";
		}
		
		while (!file.exists()) {
			file = file.getParentFile();
			if (file == null) {
				return "cannot_determine_root";
			}
		}
		if (!file.canWrite()) {
			return "cannot_create_root_folder";
		}
		return null;
	}
	
	@Override
	public void setOkButton(JButton okButton) {
		this.finishButton = okButton;		
	}
}
