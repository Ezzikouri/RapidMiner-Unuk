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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import com.rapidminer.gui.tools.dialogs.ButtonDialog;
import com.rapidminer.tools.I18N;


/**
 * @author Thilo Kamradt
 *
 */
public class TourChooser extends ButtonDialog {

	private TourManager tourManager;
	private JList list;
	public TourChooser() {
		super("Tour");
		tourManager = TourManager.getInstance();
		list = makeTable();
		super.layoutDefault((JComponent)list, LARGE, makeOkButton("tour.startTour"), makeCloseButton());
	}

	@Override
	protected void ok() {
		IntroductoryTour choosenTour = (IntroductoryTour)list.getSelectedValue();
		if(choosenTour!= null) {
			choosenTour.startTour();
			super.ok();
		}
		
	}
	
	protected JList makeTable() {
		JList tourList = new JList(new AbstractListModel() {
			@Override
			public int getSize() {
				return tourManager.size();
			}

			@Override
			public Object getElementAt(int index) {
				// TODO Auto-generated method stub
				return tourManager.get(index);
			}			
		});
		
		
		tourList.setCellRenderer(new DefaultListCellRenderer() {			
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				// ICON 	renderer.setIcon(;);
				String description = I18N.getMessage(I18N.getGUIBundle(),"gui.tour."+((IntroductoryTour)value).getKey()+".description");
				renderer.setText("<html>" + "<h3><font color=black>" + ((IntroductoryTour)value).getKey() + "</font></h3>"+description+"</html>");
				return renderer;
			}
		});
		tourList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return tourList;
	}

	
}
