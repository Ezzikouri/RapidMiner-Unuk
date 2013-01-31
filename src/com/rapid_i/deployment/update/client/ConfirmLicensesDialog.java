
package com.rapid_i.deployment.update.client;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.deployment.client.wsimport.PackageDescriptor;
import com.rapidminer.deployment.client.wsimport.UpdateService;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ResourceAction;
import com.rapidminer.gui.tools.ResourceLabel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;

public class ConfirmLicensesDialog extends ButtonDialog {

	private static final long serialVersionUID = 4276757146820898347L;
	private JButton okButton;
	private JRadioButton accept, reject;
	private JEditorPane licensePane = new JEditorPane("text/html", "");
	private static final int LIST_WIDTH = 330;
	private JList selectedforInstallList;
	private JList dependentPackages;
	private ResourceLabel licenseLabel;

	public ConfirmLicensesDialog(List<PackageDescriptor> selectedList, List<PackageDescriptor> dependencyList, int numberOfTotalPackages) {
		super("confirm_licenses", "updates");
		setModal(true);

		JPanel main = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.weightx = 1;
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;

		c.gridx = 0;
		c.weighty = 0.1;
		ResourceLabel label = new ResourceLabel("selected_packages");
		main.add(label, c);

		selectedforInstallList = new JList(selectedList.toArray());
		dependentPackages = new JList(dependencyList.toArray());
		
    	selectedforInstallList.setFixedCellHeight(50);
		dependentPackages.setFixedCellHeight(40);

		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 1;
		JScrollPane selectedForInstallPane = new ExtendedJScrollPane(selectedforInstallList);
		selectedForInstallPane.setMinimumSize(new Dimension(LIST_WIDTH, 100));
		selectedForInstallPane.setPreferredSize(new Dimension(LIST_WIDTH, 100));
		selectedForInstallPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		selectedforInstallList.addListSelectionListener(new LicenseListSelectionListener(dependentPackages));
		selectedforInstallList.setCellRenderer(new PackageListCellRenderer(35,9));
		selectedForInstallPane.setBorder(BorderFactory.createLineBorder(SwingTools.LIGHTEST_BLUE));
		main.add(selectedForInstallPane, c);

		c.gridx = 0;
		c.gridy = 2;
		c.weighty = 0.2;
		ResourceLabel dependentLabel = new ResourceLabel("dependent_packages");
		main.add(dependentLabel, c);

		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		JScrollPane dependentPackagesPane = new ExtendedJScrollPane(dependentPackages);
		dependentPackagesPane.setMinimumSize(new Dimension(LIST_WIDTH, 100));
		dependentPackagesPane.setPreferredSize(new Dimension(LIST_WIDTH, 100));
		dependentPackagesPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		dependentPackages.addListSelectionListener(new LicenseListSelectionListener(selectedforInstallList));
		dependentPackages.setCellRenderer(new PackageListCellRenderer(25,8));
		dependentPackagesPane.setBorder(BorderFactory.createLineBorder(SwingTools.LIGHTEST_BLUE));
		main.add(dependentPackagesPane, c);
		
	
		c.gridx = 1;
		c.gridy = 0;
		c.weighty = 1;
		licenseLabel = new ResourceLabel("license_label");
		main.add(licenseLabel, c);
		
		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		c.gridheight = 6;
		licensePane.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(licensePane);
		scrollPane.setPreferredSize(new Dimension(400, 400));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createLineBorder(SwingTools.LIGHTEST_BLUE));
		main.add(scrollPane, c);

		accept = new JRadioButton(new ResourceAction("accept_license") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons();
			}
		});

		c.gridx = GridBagConstraints.REMAINDER;
		c.gridy = GridBagConstraints.REMAINDER;
		reject = new JRadioButton(new ResourceAction("reject_license") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				enableButtons();
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(accept);
		group.add(reject);
		reject.setSelected(true);

		JPanel radioPanel = new JPanel(new GridBagLayout());
		c.gridx = 0;
		c.gridy = 0;
		radioPanel.add(accept, c);

		c.gridx = 1;
		c.gridy = 0;
		radioPanel.add(reject, c);

		c.gridx = 1;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weighty = 0.2;
		main.add(radioPanel, c);

		
		okButton = this.makeOkButton("update.install");
		okButton.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.action.update.install.label", numberOfTotalPackages));
		okButton.setEnabled(false);
		

		layoutDefault(main, HUGE, okButton, makeCancelButton("skip_install"));
	}

	private void setInitialSelection() {
		selectedforInstallList.setSelectedIndex(0);
	}

	@Override
	protected void ok() {
		// TODO Auto-generated method stub
		super.ok();
	}

	@Override
	protected void cancel() {
		// TODO Auto-generated method stub
		super.cancel();
	}

	private void enableButtons() {
		okButton.setEnabled(accept.isSelected());
	}

	private void setLicensePaneContent(PackageDescriptor desc) throws MalformedURLException, URISyntaxException {
		UpdateService service = UpdateManager.getService();
		String licence = service.getLicenseTextHtml(desc.getLicenseName());
		licensePane.setText(licence);
		licensePane.setCaretPosition(0);
		licenseLabel.setText(I18N.getMessage(I18N.getGUIBundle(), "gui.label.license_label.label", desc.getName()));
	}

	/** Returns true iff the user chooses to confirm the license. 
	 * @param numberOfTotalPackages 
	 * @param updateModel */
	public static boolean confirm(List<PackageDescriptor> selectedList, List<PackageDescriptor> dependencyList, int numberOfTotalPackages) {
		ConfirmLicensesDialog d = new ConfirmLicensesDialog(selectedList, dependencyList,numberOfTotalPackages);
		d.setInitialSelection();
		d.setVisible(true);
		return d.wasConfirmed();
	}

	private class LicenseListSelectionListener implements ListSelectionListener {

		private JList otherList;

		public LicenseListSelectionListener(JList otherList) {
			this.otherList = otherList;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				JList source = (JList) e.getSource();
				if (!source.isSelectionEmpty()) {
					/* 
					* clear selection in the other list 
					 */
					if (!otherList.isSelectionEmpty()) {
						otherList.clearSelection();
					}

					PackageDescriptor desc = null;
					Object selectedValue = ((JList) source).getSelectedValue();
					desc = (PackageDescriptor) selectedValue;

					try {
						setLicensePaneContent(desc);

					} catch (Exception e1) {
						e1.printStackTrace();
						SwingTools.showSimpleErrorMessage("error_installing_update", e1, e1.getMessage());
					}

				}

			}
		}
	}
}
