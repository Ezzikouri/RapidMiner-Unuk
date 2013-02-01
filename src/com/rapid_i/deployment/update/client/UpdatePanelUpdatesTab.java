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
package com.rapid_i.deployment.update.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.rapid_i.deployment.update.client.listmodels.AbstractPackageListModel;
import com.rapid_i.deployment.update.client.listmodels.UpdatesPackageListModel;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.I18N;

/**
 * 
 * @author Dominik Halfkann
 *
 */
public class UpdatePanelUpdatesTab extends UpdatePanelTab {

	private static final long serialVersionUID = 1L;
	private JButton updateAllButton;
	
	public final Action selectAllAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			updateModel.markAllPackages(listModel.getAllPackageNames(), listModel.getCache());
			getModel().updateView();
			updateAllButton.setEnabled(false);
		}

	};

	public UpdatePanelUpdatesTab(UpdatePackagesModel updateModel, PackageDescriptorCache packageDescriptorCache, UpdateServerAccount usAccount) {
		this(updateModel, new UpdatesPackageListModel(packageDescriptorCache), usAccount);
	}

	private UpdatePanelUpdatesTab(UpdatePackagesModel updateModel, AbstractPackageListModel model, UpdateServerAccount usAccount) {
		super(updateModel, model, usAccount);
	}

	@Override
	protected JComponent makeBottomPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setMinimumSize(new Dimension(100,35));
		panel.setPreferredSize(new Dimension(100,35));
		panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

		updateAllButton = new JButton(I18N.getMessage(I18N.getGUIBundle(), "gui.dialog.update.tab.updates.update_all_button"));
		updateAllButton.setIcon(SwingTools.createIcon("16/checks.png"));
		updateAllButton.setEnabled(false);
		listModel.addListDataListener(new ListDataListener(){

			@Override
			public void intervalAdded(ListDataEvent e) {}

			@Override
			public void intervalRemoved(ListDataEvent e) {}

			@Override
			public void contentsChanged(ListDataEvent e) {
				if (listModel.getAllPackageNames().size() > 0) {
					updateAllButton.setEnabled(true);
				} else {
					updateAllButton.setEnabled(false);
				}
			}
			
		});
		updateAllButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAllAction.actionPerformed(null);
			}
		});

		panel.add(updateAllButton);
		return panel;
	}
}
