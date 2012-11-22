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
package com.rapidminer.repository.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.RepositoryEntryTextField;
import com.rapidminer.gui.tools.ResourceActionAdapter;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.repository.DataEntry;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.MalformedRepositoryLocationException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Observable;
import com.rapidminer.tools.Observer;
import com.rapidminer.tools.ParameterService;

/**
 * A dialog that shows the repository tree. The static method {@link #selectLocation()} shows a dialog and returns the
 * location selected by the user.
 * 
 * @author Simon Fischer, Tobias Malbrecht
 * 
 */
public class RepositoryLocationChooser extends JPanel implements Observer<Boolean> {

	private static final long serialVersionUID = 1L;

	private final RepositoryTree tree;

	JLabel locationLabel;
	private final JTextField locationField = new JTextField(30);
	private final RepositoryEntryTextField locationFieldRepositoryEntry = new RepositoryEntryTextField();
	
	private boolean enforceValidRepositoryEntryName;

	private final RepositoryLocation resolveRelativeTo;

	private JCheckBox resolveBox;

	private final List<ChangeListener> listeners = new LinkedList<ChangeListener>();

	private final JLabel resultLabel = new JLabel();

	/** The entry the user last clicked on. (Not the selected entry, this is also influenced by the text field.) */
	private Entry currentEntry;

	private boolean folderSelected;
	
	private static class RepositoryLocationChooserDialog extends ButtonDialog implements Observer<Boolean> {
		private static final long serialVersionUID = -726540444296013310L;

		private RepositoryLocationChooser chooser = null;
		private String userSelection = null;
		
		private final JButton okButton;
		private final JButton cancelButton;
				
		public RepositoryLocationChooserDialog(RepositoryLocation resolveRelativeTo, String initialValue, final boolean allowEntries, final boolean allowFolders) {
			super("repository_chooser", true, new Object[]{});
			okButton = makeOkButton();
			chooser = new RepositoryLocationChooser(this, resolveRelativeTo, initialValue, allowEntries, allowFolders);
			chooser.tree.addRepositorySelectionListener(new RepositorySelectionListener() {
				@Override
				public void repositoryLocationSelected(RepositorySelectionEvent e) {
					// called on double click
					Entry entry = e.getEntry();
					if (allowEntries && entry instanceof DataEntry) {
						userSelection = entry.getLocation().toString();
						dispose();
					}
				}
			});	
			chooser.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					okButton.setEnabled(chooser.hasSelection(allowFolders) && (allowFolders || !chooser.folderSelected));
				}
			});
			okButton.setEnabled(chooser.hasSelection(allowFolders) && (allowFolders || !chooser.folderSelected));
			cancelButton = makeCancelButton();
			layoutDefault(chooser, NORMAL, okButton, cancelButton);
		}

		@Override
		protected void ok() {
			try {
				chooser.getRepositoryLocation();
				super.ok();
			} catch (MalformedRepositoryLocationException e) {
				SwingTools.showSimpleErrorMessage("malformed_repository_location", e, e.getMessage());
			}
		}

		@Override
		public void update(Observable<Boolean> observable, Boolean arg) {
			okButton.setEnabled(arg);
		}
	}


//	public RepositoryLocationChooser(RepositoryLocation resolveRelativeTo, String initialValue) {
//		this(null, resolveRelativeTo, initialValue);
//	}

	public RepositoryLocationChooser(Dialog owner, RepositoryLocation resolveRelativeTo, String initialValue) {
		this(owner, resolveRelativeTo, initialValue, true, false);
	}
	
	public RepositoryLocationChooser(Dialog owner, RepositoryLocation resolveRelativeTo, String initialValue, final boolean allowEntries, final boolean allowFolders) {
		this(owner, resolveRelativeTo, initialValue, allowEntries, allowFolders, false);
	}

	public RepositoryLocationChooser(Dialog owner, RepositoryLocation resolveRelativeTo, String initialValue, final boolean allowEntries, final boolean allowFolders, boolean enforceValidRepositoryEntryName) {
		if (initialValue != null) {
			try {
				RepositoryLocation repositoryLocation;
				if (resolveRelativeTo != null) {
					repositoryLocation = new RepositoryLocation(resolveRelativeTo, initialValue);
				} else {
					repositoryLocation = new RepositoryLocation(initialValue);
				}
				locationField.setText(repositoryLocation.getName());
				locationFieldRepositoryEntry.setText(repositoryLocation.getName());
				resultLabel.setText(repositoryLocation.toString());
			} catch (Exception e) {
			}
		}
		this.resolveRelativeTo = resolveRelativeTo;
		this.enforceValidRepositoryEntryName = enforceValidRepositoryEntryName;
		tree = new RepositoryTree(owner, !allowEntries);

		if (initialValue != null) {
			if (tree.expandIfExists(resolveRelativeTo, initialValue)) {
				locationField.setText("");
				locationFieldRepositoryEntry.setText("");
			}
		}
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if (e.getPath() != null) {
					currentEntry = (Entry) e.getPath().getLastPathComponent();
					if (currentEntry instanceof Folder) { //  && allowFolders)) {
//						locationField.setText("");
					} else if ((!(currentEntry instanceof Folder)) && allowEntries) {
//					if (true) {
//							//!(currentEntry instanceof Folder)) {
						locationField.setText(currentEntry.getLocation().getName());
						locationFieldRepositoryEntry.setText(currentEntry.getLocation().getName());
					}
					updateResult();
				}
			}
		});
		locationField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				updateResult();
			}

			@Override
			public void keyTyped(KeyEvent e) {
				TreePath selectionPath = tree.getSelectionPath();
				if (selectionPath != null) {
					Entry selectedEntry = (Entry) selectionPath.getLastPathComponent();
					if (!(selectedEntry instanceof Folder)) {
						tree.setSelectionPath(selectionPath.getParentPath());
					}
				}
			}
		});
		locationFieldRepositoryEntry.addObserver(this, true);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.gridwidth = GridBagConstraints.REMAINDER;

		JScrollPane treePane = new ExtendedJScrollPane(tree);
		treePane.setBorder(ButtonDialog.createBorder());
		add(treePane, c);

		c.insets = new Insets(ButtonDialog.GAP, 0, 0, ButtonDialog.GAP);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		locationLabel = new ResourceLabel("repository_chooser.entry_name");
		locationLabel.setLabelFor(locationField);
		add(locationLabel, c);

		c.weightx = 1;
		c.insets = new Insets(ButtonDialog.GAP, 0, 0, 0);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(locationField, c);
		add(locationFieldRepositoryEntry, c);
		if (enforceValidRepositoryEntryName) {
			locationField.setVisible(false);
		} else {
			locationFieldRepositoryEntry.setVisible(false);
		}

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		c.insets = new Insets(ButtonDialog.GAP, 0, 0, ButtonDialog.GAP);
		add(new ResourceLabel("repository_chooser.location"), c);
		c.weightx = 1;
		c.insets = new Insets(ButtonDialog.GAP, 0, 0, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(resultLabel, c);

		if (resolveRelativeTo != null) {
			resolveBox = new JCheckBox(new ResourceActionAdapter("repository_chooser.resolve", resolveRelativeTo.getAbsoluteLocation()));
			resolveBox.setSelected(ParameterService.getParameterValue(RapidMinerGUI.PROPERTY_RESOLVE_RELATIVE_REPOSITORY_LOCATIONS).equals("true"));
			add(resolveBox, c);
			resolveBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					updateResult();
				}
			});
		}
	}

	public String getRepositoryLocation() throws MalformedRepositoryLocationException {
		if (tree.getSelectionPath() != null) {
			Entry selectedEntry = (Entry) tree.getSelectionPath().getLastPathComponent();
			RepositoryLocation selectedLocation = selectedEntry.getLocation();
			if (selectedEntry instanceof Folder) {
				if (enforceValidRepositoryEntryName) {
					selectedLocation = new RepositoryLocation(selectedLocation, locationFieldRepositoryEntry.getText());
				} else {
					selectedLocation = new RepositoryLocation(selectedLocation, locationField.getText());
				}
			}
			if (RepositoryLocationChooser.this.resolveRelativeTo != null && resolveBox.isSelected()) {
				return selectedLocation.makeRelative(RepositoryLocationChooser.this.resolveRelativeTo);
			} else {
				return selectedLocation.getAbsoluteLocation();
			}
		} else {
			if (enforceValidRepositoryEntryName) {
				return locationFieldRepositoryEntry.getText();
			} else {
				return locationField.getText();
			}
		}
	}

	/** Same as {@link #hasSelection(boolean)} with parameter false. */
	public boolean hasSelection() {
		return hasSelection(false);
	}
	
	/** Returns true if the user entered a valid, non-empty repository location. */
	public boolean hasSelection(boolean allowFolders) {
		if (!allowFolders && ((enforceValidRepositoryEntryName && locationFieldRepositoryEntry.getText().isEmpty()) || (!enforceValidRepositoryEntryName && locationField.getText().isEmpty()))) {
			return false;
		} else {
			try {
				getRepositoryLocation();
				return true;
			} catch (MalformedRepositoryLocationException e) {
				//LogService.getRoot().warning("Malformed repository location: " + e);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
						"com.rapidminer.repository.gui.RepositoryLocationChooser.malformed_repository_location", 
						e),
						e);
				return false;
			}
		}
	}

	public boolean resolveRelative() {
		return resolveBox.isSelected();
	}
	
	public void setResolveRelative(boolean resolveRelative) {
		if (resolveBox != null) {
			if (!resolveRelative && resolveBox.isSelected()) {
				resolveBox.doClick();
			} else if (resolveRelative && !resolveBox.isSelected()) {
				resolveBox.doClick();
			}
		}
	}

	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	/**
	 * This will open a window to select a repository entry that is an entry or returns
	 * null if the user aborts the operation. Enforces a valid repository entry name.
	 */
	public static String selectEntry(RepositoryLocation resolveRelativeTo, Component c, boolean enforceValidRepositoryEntryName) {
		return selectLocation(resolveRelativeTo, null, c, true, false, false, enforceValidRepositoryEntryName);
	}

	/**
	 * This will open a window to select a repository entry that is an entry or returns
	 * null if the user aborts the operation.
	 */
	public static String selectEntry(RepositoryLocation resolveRelativeTo, Component c) {
		return selectLocation(resolveRelativeTo, null, c, true, false, false, true);
	}

	/**
	 * This will open a window to select a repository entry that is a folder or null if
	 * the user chooses to abort.
	 */
	public static String selectFolder(RepositoryLocation resolveRelativeTo, Component c) {
		return selectLocation(resolveRelativeTo, null, c, false, true);
	}

	public static String selectLocation(RepositoryLocation resolveRelativeTo, Component c) {
		return selectLocation(resolveRelativeTo, null, c, true, true);
	}
	
	public static String selectLocation(RepositoryLocation resolveRelativeTo, String initialValue, Component c, final boolean selectEntries, final boolean selectFolder) {
		return selectLocation(resolveRelativeTo, initialValue, c, selectEntries, selectFolder, false);
	}
	
	public static String selectLocation(RepositoryLocation resolveRelativeTo, String initialValue, Component c, final boolean selectEntries, final boolean selectFolder, final boolean forceDisableRelativeResolve) {
		return selectLocation(resolveRelativeTo, initialValue, c, selectEntries, selectFolder, forceDisableRelativeResolve, false);
	}

	public static String selectLocation(RepositoryLocation resolveRelativeTo, String initialValue, Component c, final boolean selectEntries, final boolean selectFolder, final boolean forceDisableRelativeResolve, final boolean enforceValidRepositoryEntryName) {
		final RepositoryLocationChooserDialog dialog = new RepositoryLocationChooserDialog(resolveRelativeTo, initialValue, selectEntries, selectFolder);
		if (forceDisableRelativeResolve) {
			dialog.chooser.setResolveRelative(false);
		}
		dialog.chooser.setEnforceValidRepositoryEntryName(enforceValidRepositoryEntryName);
		if (enforceValidRepositoryEntryName) {
			dialog.chooser.locationFieldRepositoryEntry.addObserver(dialog, true);
		}
		dialog.setVisible(true);

		// if user has used double click to submit
		if (dialog.userSelection != null) {
			return dialog.userSelection;
		}
		if (dialog.wasConfirmed()) {
			if (resolveRelativeTo != null && !forceDisableRelativeResolve) {
				ParameterService.setParameterValue(RapidMinerGUI.PROPERTY_RESOLVE_RELATIVE_REPOSITORY_LOCATIONS, dialog.chooser.resolveRelative() ? "true" : "false");
				ParameterService.saveParameters();
			}
			String text;
			try {
				text = dialog.chooser.getRepositoryLocation();
			} catch (MalformedRepositoryLocationException e) {
				// this should not happen since the dialog would not have disposed without an error message.
				throw new RuntimeException(e);
			}
			if (text.length() > 0) {
				return text;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private void updateResult() {
		try {
			String repositoryLocation = getRepositoryLocation();
			resultLabel.setText(repositoryLocation);
		} catch (MalformedRepositoryLocationException e) {
			//LogService.getRoot().log(Level.WARNING, "Malformed location: " + e, e);
			LogService.getRoot().log(Level.WARNING,
					I18N.getMessage(LogService.getRoot().getResourceBundle(), 
					"com.rapidminer.repository.gui.RepositoryLocationChooser.malformed_location", 
					e),
					e);
		}
		if ((currentEntry instanceof Folder) && !enforceValidRepositoryEntryName && locationField.getText().isEmpty()) {
			this.folderSelected = true;
		} if ((currentEntry instanceof Folder) && enforceValidRepositoryEntryName && locationFieldRepositoryEntry.getText().isEmpty()) {
			this.folderSelected = true;
		} else {
			this.folderSelected = false;
		}
		for (ChangeListener l : listeners) {
			l.stateChanged(new ChangeEvent(this));
		}
	}

	public boolean isEnforceValidRepositoryEntryName() {
		return enforceValidRepositoryEntryName;
	}

	public void setEnforceValidRepositoryEntryName(boolean enforceValidRepositoryEntryName) {
		this.enforceValidRepositoryEntryName = enforceValidRepositoryEntryName;
		this.locationLabel.setVisible(!enforceValidRepositoryEntryName);
		this.locationField.setVisible(!enforceValidRepositoryEntryName);
		this.locationFieldRepositoryEntry.setVisible(enforceValidRepositoryEntryName);
	}

	@Override
	public void update(Observable<Boolean> observable, Boolean arg) {
		updateResult();
	}
}
