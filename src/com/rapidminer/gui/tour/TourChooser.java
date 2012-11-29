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

package com.rapidminer.gui.tour;

import java.awt.Component;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.Tools;

/**
 * This class creates a dialog in which the user can choose a Tour (e.g. RapidMinerTour) and starts the Tour.
 * It also displays the progress-state of the Tours with yellow and grey stars.
 * 
 * @author Thilo Kamradt
 *
 */
public class TourChooser extends ButtonDialog {

	private static final long serialVersionUID = 1L;

	private TourManager tourManager;
	private JList list;

	public TourChooser() {
		super("Tour");
		tourManager = TourManager.getInstance();
		super.layoutDefault(makeTable(), LARGE, makeOkButton("tour.startTour"), makeCloseButton());
		super.setSize(455, 500);
	}

	@Override
	protected void ok() {
		IntroductoryTour choosenTour = (IntroductoryTour) list.getSelectedValue();
		if (choosenTour != null) {
			choosenTour.startTour();
			super.ok();
		}

	}

	protected JComponent makeTable() {
		list = new JList(new AbstractListModel() {

			private static final long serialVersionUID = 1L;

			@Override
			public int getSize() {
				return tourManager.size();
			}

			@Override
			public Object getElementAt(int index) {
				return tourManager.get(index);
			}
		});

		list.setCellRenderer(new DefaultListCellRenderer() {

			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				// ICON 	
				renderer.setIcon(new ImageIcon(Tools.getResource("rapidminer_frame_icon_48.png"), ""));
				String tourKey = ((IntroductoryTour) value).getKey();
				String description = I18N.getMessage(I18N.getGUIBundle(), "gui.tour." + tourKey + ".description");
				String statusValue = "<br>";
				//make numbers
				int current = tourManager.getProgress(tourKey);
				int max = ((IntroductoryTour) value).getSize();
				//statusValue = statusValue + current + "/" + max + " <br>";
				// make stars
				for (int i = 0; i < current; i++) {
					statusValue = statusValue + "<img src=\"" + Tools.getResource("icons/16/star_yellow.png") + "\"/>";
				}
				for (int i = 0; i < (max - current); i++) {
					statusValue = statusValue + "<img src=\"" + Tools.getResource("icons/16/star_grey.png") + "\"/>";
				}
				renderer.setText("<html><div style=\"width:300px\">" + "<h3 style=\"padding-left:5px;color:" + (isSelected ? "white" : "black") + ";\">" + tourKey + "</h3><p style=\"padding-left:5px;\">" + description + "</p><p style=\"padding-left:5px;\">" + statusValue + "</p></div></html>");
				return renderer;
			}
		});
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSelectedIndex(0);
		if(list.getModel().getSize()>3) {
			//add JSrcollPane
			JScrollPane scroll = new JScrollPane(list);
			scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			return scroll;
		}
		return list;
	}

}
