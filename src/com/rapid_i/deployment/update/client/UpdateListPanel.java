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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import com.rapid_i.deployment.update.client.listmodels.AbstractPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.BookmarksPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.LicencedPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.SearchPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.TopDownloadsPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.TopRatedPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.UpdatesPackageListModel;
import com.rapidminer.RapidMiner;
import com.rapidminer.deployment.client.wsimport.AccountService;
import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.tools.ExtendedHTMLJEditorPane;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJToolBar;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.plugin.Dependency;

/**
 * 
 * @author Simon Fischer
 * 
 */
public class UpdateListPanel extends JPanel {
	
	private static final int LIST_WIDTH = 330;

	private final PackageDescriptorCache packageDescriptorCache = new PackageDescriptorCache();

	private final Map<PackageDescriptor, Boolean> selectionMap = new HashMap<PackageDescriptor, Boolean>();
	private final Map<PackageDescriptor, List<Dependency>> dependencyMap = new HashMap<PackageDescriptor, List<Dependency>>();
	/** Read the comment of {@link #isPurchased(PackageDescriptor)}. */
	private final Set<String> purchasedPackages = new HashSet<String>();

	private final UpdateDialog updateDialog;
	
	private List<JList> packageLists = new ArrayList<JList>();

	private static final long serialVersionUID = 1L;
	
	
	final JTextField searchField = new JTextField(12);
	final SearchPackageListModel searchModel = new SearchPackageListModel(packageDescriptorCache);
	JList resultList;
	
	private JTabbedPane updatesTabbedPane = new JTabbedPane();

	private final List<PackageDescriptor> descriptors;

	private final JLabel sizeLabel = new JLabel();
	
	private Document defaultDescriptionDocument = null;
	
	private String defaultDocumentContent = "";
	
	private List<ExtendedHTMLJEditorPane> displayPanes = new ArrayList<ExtendedHTMLJEditorPane>();

	public UpdateListPanel(UpdateDialog dialog, List<PackageDescriptor> descriptors, String[] preselectedExtensions) {
		
		final ExtendedHTMLJEditorPane displayPane = new ExtendedHTMLJEditorPane("text/html", "");
		displayPane.installDefaultStylesheet();
		displayPane.setEditable(false);
		new Thread("Load Default Description") {
			@Override
			public void run() {
				setDefaultDescription(displayPane);
				//System.out.println("-------------------------------------------");
				//System.out.println("Text: " + displayPane.getText());
				//System.out.println("-------------------------------------------");
				defaultDescriptionDocument = displayPane.getDocument();

					//defaultDocumentContent = defaultDescriptionDocument.getText(0, defaultDescriptionDocument.getLength()-1);
					//System.out.println("-------------------------------------------");
					//System.out.println("Content: " + defaultDocumentContent);
					//System.out.println("-------------------------------------------");
					updateDefaultDescription();

				
			}
				
		}.start();
		defaultDescriptionDocument = displayPane.getDocument();
		
		
		for (String pE : preselectedExtensions) {
			for (PackageDescriptor desc : descriptors) {
				if (desc.getPackageId().equals(pE)) {
					selectionMap.put(desc, true);
				}
			}
		}
		for (PackageDescriptor desc : descriptors) {
			if (desc.getDependencies() != null) {
				List<Dependency> dep = Dependency.parse(desc.getDependencies());
				if (!dep.isEmpty()) {
					dependencyMap.put(desc, dep);
				}
			}
		}
		this.updateDialog = dialog;
		this.descriptors = descriptors;

		updateSize();

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(800, 320));
		setMinimumSize(new Dimension(800, 320));
		
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.search"), createSerchListPanel());
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.updates"), createUpdateListPanel(new UpdatesPackageListModel(packageDescriptorCache)));
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.top_downloads"), createUpdateListPanel(new TopDownloadsPackageListModel(packageDescriptorCache)));
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.top_rated"), createUpdateListPanel(new TopRatedPackageListModel(packageDescriptorCache)));
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.purchased"), createUpdateListPanel(new LicencedPackageListModel(packageDescriptorCache)));
		updatesTabbedPane.add(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.bookmarks"), createUpdateListPanel(new BookmarksPackageListModel(packageDescriptorCache)));
		
		updatesTabbedPane.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				getPackageListModel(getCurrentUpdateList()).update();
			}
		});
	
		add(updatesTabbedPane, BorderLayout.CENTER);
		
		getPackageListModel(packageLists.get(0)).update();
	}
	
	private JList getCurrentUpdateList() {
		return packageLists.get(updatesTabbedPane.getSelectedIndex());
	}
	
	private AbstractPackageListModel getPackageListModel(JList list) {
		return (AbstractPackageListModel)list.getModel();
	}
	
	private void updateDefaultDescription() {
		for (ExtendedHTMLJEditorPane displayPane : displayPanes) {
			displayPane.setDocument(defaultDescriptionDocument);
		}
	}
	
	private JPanel createUpdateListPanel(AbstractPackageListModel model) {
		
		
		
		JPanel updateListPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, ButtonDialog.GAP);
		
		final JToggleButton installButton = new JToggleButton(new ResourceAction(true, "update.select") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				toggleSelection();	
			}
		});
		installButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (installButton.isSelected()) {
					installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));	
				} else {
					installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
				}
			}
		});
		installButton.setEnabled(false);
		
		final ExtendedHTMLJEditorPane displayPane = new ExtendedHTMLJEditorPane("text/html", "");
		displayPanes.add(displayPane);
		displayPane.setDocument(defaultDescriptionDocument);
		displayPane.getDocument().addDocumentListener(new DocumentListener(){

			@Override
			public void insertUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				System.out.println("inserted");
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				System.out.println("removed");
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// TODO Auto-generated method stub
				System.out.println("changed");
			}
			
		});
		
		//setDefaultDescription(displayPane);
		
		displayPane.installDefaultStylesheet();
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
		
		JList packageList = createUpdateList(model, displayPane, installButton);
		packageLists.add(packageList);
		JScrollPane updateListScrollPane = new ExtendedJScrollPane(packageList);
		updateListScrollPane.setMinimumSize(new Dimension(LIST_WIDTH,100));
		updateListScrollPane.setPreferredSize(new Dimension(LIST_WIDTH,100));
		updateListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		updateListPanel.add(updateListScrollPane, c);
		
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

		extensionButtonPane.add(installButton);
		descriptionPanel.add(extensionButtonPane, BorderLayout.SOUTH);
		
		updateListPanel.add(descriptionPanel, c);
		
		return updateListPanel;
	}
	
	private JList createUpdateList(AbstractPackageListModel model, final ExtendedHTMLJEditorPane displayPane, final JToggleButton installButton) {
		final JList updateList = new JList(model);
		updateList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					Object selectedValue = updateList.getSelectedValue();
					if (selectedValue instanceof PackageDescriptor) {
						
						installButton.setEnabled(true);
						
						PackageDescriptor desc = (PackageDescriptor) selectedValue;
						displayPane.setDocument(new HTMLDocument());
						displayPane.setText(UpdateListPanel.this.toString(desc));
						displayPane.setCaretPosition(0);
						
						installButton.setSelected(isSelected(desc));
						if (isSelected((PackageDescriptor)getCurrentUpdateList().getSelectedValue())) {
							installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));
						} else {
							installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
						}

						ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
						if (ext != null) {
							String installed = ext.getLatestInstalledVersion();
							if (installed != null) {
								boolean upToDate = installed.compareTo(desc.getVersion()) >= 0;
								if (upToDate) {
									installButton.setEnabled(false);
								} 
							}
							}
					}
					
				}
			}
		});
		updateList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					toggleSelection();
				}
			}
		});
		updateList.setCellRenderer(new UpdateListCellRenderer(this));
		return updateList;
	}
	
	private JPanel createSerchListPanel() {
		
		JPanel panel = new JPanel(new BorderLayout());
		
		
		JToolBar toolBar = new ExtendedJToolBar();
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        toolBar.setFloatable(false);
        
        searchField.setToolTipText(I18N.getMessage(I18N.getGUIBundle(), "gui.field.update.search.tip"));
        
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
              int key = e.getKeyCode();
              if (key == KeyEvent.VK_ENTER) {
            	  searchAction.actionPerformed(null);
            	  e.consume();
                 }
              }
            }
         );
        
        JButton searchButton = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.search.search_button"));
        searchButton.addActionListener(searchAction);
        
        toolBar.add(searchField);
        toolBar.add(searchButton);
        
        panel.add(toolBar, BorderLayout.NORTH);
        
		JPanel updateListPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, ButtonDialog.GAP);
		
		final JToggleButton installButton = new JToggleButton(new ResourceAction(true, "update.select") {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleSelection();
			}			
		});
		installButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (installButton.isSelected()) {
					installButton.setIcon(SwingTools.createIcon("16/checkbox.png"));	
				} else {
					installButton.setIcon(SwingTools.createIcon("16/checkbox_unchecked.png"));
				}
			}
		});
		installButton.setEnabled(false);
		
		// TODO: Marker - Welcome Message set
		final ExtendedHTMLJEditorPane displayPane = new ExtendedHTMLJEditorPane("text/html", "");
		displayPanes.add(displayPane);
		
		displayPane.installDefaultStylesheet();
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
		
		resultList = createUpdateList(searchModel, displayPane, installButton);
		resultList.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				System.out.println("selection changed.");
				if (resultList.isSelectionEmpty()) {
					System.out.println("And Selection is empty");
					new Thread("Load Default Description") {
						@Override
						public void run() {
							if (displayPane.getDocument() == defaultDescriptionDocument) System.out.println("same document already!");
							displayPane.setDocument(defaultDescriptionDocument);
							//setDefaultDescription(displayPane);
							System.out.println("changing text done!");
						}
							
					}.start();
				}
			}
		});
		packageLists.add(resultList);
		JScrollPane updateListScrollPane = new ExtendedJScrollPane(resultList);
		updateListScrollPane.setMinimumSize(new Dimension(LIST_WIDTH,100));
		updateListScrollPane.setPreferredSize(new Dimension(LIST_WIDTH,100));
		updateListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(updateListScrollPane, BorderLayout.CENTER);
		updateListPanel.add(panel, c);
		
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

		
		extensionButtonPane.add(installButton);
		descriptionPanel.add(extensionButtonPane, BorderLayout.SOUTH);
		
		updateListPanel.add(descriptionPanel, c);
		
		return updateListPanel;
	}
	
	private void setDefaultDescription(ExtendedHTMLJEditorPane editor) {
		
		
		
		try {
			//editor.setPage("http://rapid-i.com/rapidminer_news/bla");
			editor.setPage("http://rapid-i.com/rapidminer_news/");
			/*defaultDescription = buf.toString();
			System.out.println("------------------------------------------");
			System.out.println(defaultDescription);
			System.out.println("------------------------------------------");*/
			
			//return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html><head></head><body>test</body></html>";
			//return defaultDescription;
		} catch (Exception e) {
			editor.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update_welcome_message.text", UpdateManager.getBaseUrl()));
		}
	}
	
	public final Action searchAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		private String oldSearch ="";
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String value = searchField.getText();
			if (value != null && !value.equals(oldSearch)) {
				searchModel.search(value);
				oldSearch = value;
				resultList.clearSelection();
			}
		}
		
	};

	private String toString(PackageDescriptor descriptor) {
		StringBuilder b = new StringBuilder("<html>");
		b.append("<span style=\"font-size:14px;\">").append(descriptor.getName() + "</span>");
		Date date = new Date(descriptor.getCreationTime().toGregorianCalendar().getTimeInMillis());
		b.append("<hr noshade=\"true\" style=\"margin-bottom:8px;\"/><p style=\"margin-bottom:8px;\"><strong>Version ").append(descriptor.getVersion()).append(", released ").append(Tools.formatDate(date));
		b.append(", ").append(Tools.formatBytes(descriptor.getSize())).append("</strong></p>");
		if ((descriptor.getDependencies() != null) && !descriptor.getDependencies().isEmpty()) {
			b.append("<div style=\"margin-bottom:8px;\">Depends on: " + descriptor.getDependencies() + "</div>");
		}
		b.append("<div style=\"margin-bottom:8px;\">").append(descriptor.getLongDescription()).append("</div>");
		// Before you are shocked, read the comment of isPurchased() :-)
		if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(descriptor.getLicenseName())) {
			if (isPurchased(descriptor)) {
				b.append("<p>You have purchased this package. However, you cannot install this extension with this version of RapidMiner. Please upgrade first.</p>");
			} else {
				try {
					b.append("<p><a href=" + UpdateManager.getUpdateServerURI("/shop/" + descriptor.getPackageId()).toString() + ">Order this extension.</a></p><p>You cannot install this extension with this pre-release of RapidMiner. Please upgrade first.</p>");
				} catch (URISyntaxException e) {
				}
			}
		}
		b.append("<p><a href=\""+UpdateManager.getBaseUrl()+"/faces/product_details.xhtml?productId="+descriptor.getPackageId()+"\">Extension homepage</a></p>");
		b.append("</html>");
		return b.toString();
	}

	public boolean isSelected(PackageDescriptor desc) {
		Boolean selected = selectionMap.get(desc);
		return (selected != null) && selected.booleanValue();
	}

	private void updateSize() {
		int totalSize = getTotalSize();
		if (totalSize > 0) {
			sizeLabel.setText("Total download size: " + Tools.formatBytes(totalSize) + " (This may be less, if incremental updates are possible.)");
		} else {
			sizeLabel.setText(" ");
		}
	}

	private int getTotalSize() {
		int totalSize = 0;
		for (Map.Entry<PackageDescriptor, Boolean> entry : selectionMap.entrySet()) {
			if (entry.getValue()) {
				totalSize += entry.getKey().getSize();
			}
		}
		return totalSize;
	}

	public void startUpdate() {
		final List<PackageDescriptor> downloadList = new LinkedList<PackageDescriptor>();
		for (Entry<PackageDescriptor, Boolean> entry : selectionMap.entrySet()) {
			if (entry.getValue()) {
				downloadList.add(entry.getKey());
			}
		}
		updateDialog.startUpdate(downloadList);
	}

	private boolean isUpToDate(PackageDescriptor desc) {
		ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
		if (ext != null) {
			String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
			String myVersion = ManagedExtension.normalizeVersion(ext.getLatestInstalledVersion());
			if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void toggleSelection() {
		PackageDescriptor desc = (PackageDescriptor) getCurrentUpdateList().getSelectedValue();
		if (desc != null) {
			boolean select = !isSelected(desc);
			if (isUpToDate(desc)) {
				select = false;
			}
			if (desc.getPackageTypeName().equals("RAPIDMINER_PLUGIN")) {
				if (select) {
					resolveDependencies(desc);
				}
			} else if (desc.getPackageTypeName().equals("STAND_ALONE")) {
				String longVersion = RapidMiner.getLongVersion();
				String myVersion = ManagedExtension.normalizeVersion(longVersion);
				String remoteVersion = ManagedExtension.normalizeVersion(desc.getVersion());
				if ((myVersion != null) && (remoteVersion.compareTo(myVersion) <= 0)) {
					select = false;
				}
			}
			if (UpdateManager.COMMERCIAL_LICENSE_NAME.equals(desc.getLicenseName()) && !isPurchased(desc)) {
				select = false;
				SwingTools.showMessageDialog("purchase_package", desc.getName());
			}
			selectionMap.put(desc, select);
			getPackageListModel(getCurrentUpdateList()).update(desc);
		}
		updateSize();
	}

	private void resolveDependencies(PackageDescriptor desc) {
		List<Dependency> deps = dependencyMap.get(desc);
		if (deps != null) {
			for (Dependency dep : deps) {
				for (PackageDescriptor other : descriptors) {
					if (other.getPackageId().equals(dep.getPluginExtensionId())) {
						Boolean selected = selectionMap.get(other);
						boolean selectedB = (selected != null) && selected.booleanValue();
						if (!selectedB && !isUpToDate(other)) {
							selectionMap.put(other, true);
							resolveDependencies(other);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * Currently, this is an unused feature. There are no extensions that can be purchased. Don't be afraid, RapidMiner
	 * is, and will always be, open source and free. However, future extensions like connectors to SAP or other data
	 * sources requiring proprietary drivers with expensive license fees may only be available on a commercial basis,
	 * for obvious reasons :-)
	 */
	public boolean isPurchased(PackageDescriptor desc) {
		return purchasedPackages.contains(desc.getPackageId());
	}
	
	/**
	 *  Connects to rapidupdate.de to fetch the bookmarks and automatically select them.
	 */
	public void fetchBookmarks() {
		// TODO: Do in progress thread
		List<String> bookmarks;
		try {
			AccountService accountService = UpdateManager.getAccountService();
			bookmarks = accountService.getBookmarkedProducts("rapidminer");
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("error_accessing_marketplace_account", e, e.toString());
			return;
		}

		Map<String,PackageDescriptor> packDescById = new HashMap<String,PackageDescriptor>();
		for (PackageDescriptor desc : descriptors) {
			packDescById.put(desc.getPackageId(), desc);
		}
		//for (PackageDescriptor desc : descriptors) {
		for (String bookmarkedId : bookmarks) {
			PackageDescriptor desc = packDescById.get(bookmarkedId);
			//if (bookmarks.contains(desc.getPackageId())) {
			//LogService.getRoot().log(Level.INFO, "Looking up "+bookmarkedId);
			LogService.getRoot().log(Level.INFO, "com.rapid_i.de.deployement.update.client.UpdateListPanel.looking_up", bookmarkedId);
			if (desc == null) {
				//LogService.getRoot().log(Level.INFO, "Bookmarked package "+bookmarkedId+" was unlisted. Fetching now.");
				LogService.getRoot().log(Level.INFO, "com.rapid_i.de.deployement.update.client.UpdateListPanel.fetching_bookmarked_package", bookmarkedId);
				String rmPlatform = "ANY"; //Launcher.getPlatform();
				try {
					UpdateService updateService = UpdateManager.getService();					
					String latestRMVersion = updateService.getLatestVersion(bookmarkedId, rmPlatform);
					desc = updateService.getPackageInfo(bookmarkedId, latestRMVersion, rmPlatform);
					if (desc != null) {
						packDescById.put(desc.getPackageId(), desc);
						getPackageListModel(getCurrentUpdateList()).add(desc);
					}
				} catch (Exception e) {
					SwingTools.showSimpleErrorMessage("error_during_update", e);
					return;
				}
			}
			if (desc != null) {
				ManagedExtension ext = ManagedExtension.get(desc.getPackageId());
				final boolean upToDate;
				if (ext == null) {
					upToDate = false;
				} else {
					String installed = ext.getLatestInstalledVersion();
					if (installed != null) {
						upToDate = installed.compareTo(desc.getVersion()) >= 0;
					} else {
						upToDate = false;
					}
				}
				if (!upToDate) {
					selectionMap.put(desc, true);
					resolveDependencies(desc);
				}
				continue;
			}
		}
		getCurrentUpdateList().repaint();
	}

//	public static AccountService getAccountService() {
//		AccountServiceService ass = new AccountServiceService();
//		AccountService accountService = ass.getAccountServicePort();
//		return accountService;
//	}
}
