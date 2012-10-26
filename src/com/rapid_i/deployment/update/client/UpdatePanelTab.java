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
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.rapid_i.deployment.update.client.listmodels.AbstractPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.BookmarksPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.LicencedPackageListModel;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.gui.tools.ExtendedHTMLJEditorPane;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.components.LinkButton;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;


/**
 * 
 * @author Dominik Halfkann
 *
 */
public class UpdatePanelTab extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int LIST_WIDTH = 330;
	
	private UpdatePackagesModel updateModel;
	private AbstractPackageListModel model;
	UpdateServerAccount usAccount;
	
	private ExtendedHTMLJEditorPane displayPane;
	private final SelectForInstallationButton installButton;
	private LinkButton loginForInstallHint;
	private PackageDescriptor lastSelected = null;

	private JList packageList;
	
	private class SelectForInstallationButton extends JToggleButton implements Observer {
		
		private boolean purchaseFirst = false;
		
		private static final long serialVersionUID = 1L;

		public SelectForInstallationButton(Action a) {
			super(a);
		}
		
		public void setPurchaseFirst(boolean purchaseFirst) {
			this.purchaseFirst = purchaseFirst;
		}
		
		public boolean getPurchaseFirst() {
			return purchaseFirst;
		}

		@Override
		public void update(Observable o, Object arg) {
			if (o instanceof UpdatePackagesModel) {
				UpdatePackagesModel currentModel = (UpdatePackagesModel)o;
				if (arg != null && arg instanceof PackageDescriptor) {
					PackageDescriptor desc = (PackageDescriptor)arg;
					PackageDescriptor selectedDescriptor = (PackageDescriptor)getPackageList().getSelectedValue();
					if (selectedDescriptor != null && desc.getPackageId().equals(selectedDescriptor.getPackageId())) {
							this.setSelected(currentModel.isSelectedForInstallation(desc));
							if (this.isSelected()) {
								this.setIcon(SwingTools.createIcon("16/checkbox.png"));
							} else {
								this.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
							}
					}
				}
			}
		}
	}

	public UpdatePanelTab(UpdatePackagesModel updateModel, AbstractPackageListModel model, final UpdateServerAccount usAccount) {
		super(new GridBagLayout());
		
		this.updateModel = updateModel;
		this.model = model;
		this.usAccount = usAccount;
		this.usAccount.addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				updateDisplayPane();				
			}
		});
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, ButtonDialog.GAP);
		
		installButton = new SelectForInstallationButton(new ResourceAction(true, "update.select") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (installButton.getPurchaseFirst()) {
					try {
						PackageDescriptor selectedDescriptor = (PackageDescriptor)getPackageList().getSelectedValue();
						String url = UpdateManager.getBaseUrl() + "/faces/product_details.xhtml?productId=" + selectedDescriptor.getPackageId();
						Desktop.getDesktop().browse(new URI(url));
					} catch (Exception e1) {
						SwingTools.showVerySimpleErrorMessage("cannot_open_browser");
					}

				} else {
					PackageDescriptor selectedDescriptor = (PackageDescriptor)getPackageList().getSelectedValue();
					UpdatePanelTab.this.updateModel.toggleSelesctionForInstallation(selectedDescriptor);
					getModel().updateView(selectedDescriptor);
				}
			}
		});
		installButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!installButton.getPurchaseFirst()) {
					if (installButton.isSelected()) {
						installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));
					} else {
						installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
					}
				}
			}
		});
		installButton.setEnabled(false);
		updateModel.addObserver(installButton);

		displayPane = new ExtendedHTMLJEditorPane("text/html", "");
		displayPane.installDefaultStylesheet();
		((HTMLEditorKit) displayPane.getEditorKit()).getStyleSheet().addRule("a  {text-decoration:underline; color:blue;}");
		
		setDefaultDescription();
		
		displayPane.setEditable(false);

		displayPane.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (Exception e1) {
						SwingTools.showVerySimpleErrorMessage("cannot_open_browser");
					}
				}
			}
		});
		
		loginForInstallHint = new LinkButton(new AbstractAction(){
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				usAccount.login();
			}
			
		});

		packageList = createUpdateList();
		JScrollPane updateListScrollPane = new ExtendedJScrollPane(packageList);
		updateListScrollPane.setMinimumSize(new Dimension(LIST_WIDTH, 100));
		updateListScrollPane.setPreferredSize(new Dimension(LIST_WIDTH, 100));
		updateListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		Component topPanel = makeTopPanel();
		if (topPanel != null) {
			JPanel leftPanel = new JPanel(new BorderLayout());
			leftPanel.add(updateListScrollPane, BorderLayout.CENTER);
			leftPanel.add(topPanel, BorderLayout.NORTH);
			add(leftPanel, c);
		} else {
			add(updateListScrollPane, c);
		}
		

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, 0);

		JScrollPane jScrollPane = new ExtendedJScrollPane(displayPane);
		jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.add(jScrollPane, BorderLayout.CENTER);

		JPanel extensionButtonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		extensionButtonPane.setBackground(Color.white);

		extensionButtonPane.add(loginForInstallHint);
		extensionButtonPane.add(installButton);
		descriptionPanel.add(extensionButtonPane, BorderLayout.SOUTH);

		add(descriptionPanel, c);		
	}
	
	protected Component makeTopPanel() {
		return null;
	}

	private JList createUpdateList() {
		JList updateList = new JList(model);
		updateList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					updateDisplayPane();
				}
			}

		});
		updateList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					PackageDescriptor selectedDescriptor = (PackageDescriptor)getPackageList().getSelectedValue();
					UpdatePanelTab.this.updateModel.toggleSelesctionForInstallation(selectedDescriptor);
					getModel().updateView(selectedDescriptor);
				}
			}
		});
		updateList.setCellRenderer(new UpdateListCellRenderer(updateModel));
		return updateList;
	}

	
	protected JList getPackageList() {
		return packageList;
	}

	public void selectNotify() {		
		if (model instanceof BookmarksPackageListModel || model instanceof LicencedPackageListModel) {
			usAccount.login();
		}
		model.update();		
	}

	
	public AbstractPackageListModel getModel() {
		return model;
	}
	
	private void setDefaultDescription() {
		
		new Thread("Load Default Description") {

			@Override
			public void run() {
				try {
					displayPane.setPage("http://rapid-i.com/rapidminer_news/");
				} catch (Exception e) {
					displayPane.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update_welcome_message.text", UpdateManager.getBaseUrl()));
				}

			}
		}.start();
	}

	private void updateDisplayPane() {
		Object selectedValue = packageList.getSelectedValue();
		PackageDescriptor desc = null;
		if (selectedValue instanceof PackageDescriptor) {
			desc = (PackageDescriptor) selectedValue;
			lastSelected = desc;
		} else if (lastSelected != null) {
			desc = lastSelected;
		}
		if (desc != null) {
			
			installButton.setEnabled(true);
			StyleSheet css = ExtendedHTMLJEditorPane.makeDefaultStylesheet();
			css.addRule("a  {text-decoration:underline; color:blue;}");
			HTMLDocument doc = new HTMLDocument(css);
			displayPane.setDocument(doc);
			displayPane.setText(updateModel.toString(desc));
			//displayPane.installDefaultStylesheet();
			
			displayPane.setCaretPosition(0);

			installButton.setSelected(updateModel.isSelectedForInstallation(desc));

			boolean isInstalled = false;
			ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
			if (ext != null) {
				isInstalled = true;
				String installed = ext.getLatestInstalledVersion();
				if (installed != null) {
					boolean upToDate = installed.compareTo(desc.getVersion()) >= 0;
					if (upToDate) {
						installButton.setEnabled(false);
					}
				}
			}						
			
			if (desc.isRestricted() && !isInstalled) {
				if (!usAccount.isLoggedIn()) {
					// restricted, uninstalled, not logged in
					installButton.setVisible(false);
					loginForInstallHint.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.label.update.need_to_log_in.label"));
				} else if (updateModel.isPurchased(desc)) {
					// restricted, purchased but not installed yet
					installButton.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.select.label"));
					installButton.getAction().putValue(Action.MNEMONIC_KEY, (int)I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.select.mne").toUpperCase().charAt(0));
					installButton.setPurchaseFirst(false);
					installButton.setVisible(true);
					loginForInstallHint.setText("");
					
					if (updateModel.isSelectedForInstallation(desc)) {
						installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));
					} else {
						installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
					}
				} else {
					// restricted, not purchased
					installButton.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.purchase.label"));
					installButton.setIcon(SwingTools.createIcon("16/shopping_cart_empty.png"));
					installButton.getAction().putValue(Action.MNEMONIC_KEY, (int)I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.purchase.mne").toUpperCase().charAt(0));
					
					
					installButton.setPurchaseFirst(true);
				}				
			} else {
				// not restricted / restricted but already installed
				installButton.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.select.label"));
				installButton.getAction().putValue(Action.MNEMONIC_KEY, (int)I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.select.mne").toUpperCase().charAt(0));
				installButton.setPurchaseFirst(false);
				installButton.setVisible(true);
				loginForInstallHint.setText("");
				
				if (updateModel.isSelectedForInstallation(desc)) {
					installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));
				} else {
					installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
				}
			}
		}
	}

	
	@Override
	public void removeNotify() {
		super.removeNotify();
		usAccount.deleteObservers();
		updateModel.deleteObservers();
	}
}