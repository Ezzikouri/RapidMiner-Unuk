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

package com.rapidminer.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.dialogs.ConfirmDialog;
import com.rapidminer.gui.tools.dialogs.MessageDialog;
import com.rapidminer.gui.tour.RapidMinerTour;
import com.rapidminer.gui.tour.TourChooser;
import com.rapidminer.gui.tour.TourManager;
import com.rapidminer.gui.tour.TourState;
import com.rapidminer.tools.I18N;

/**
 * Start the corresponding action.
 * 
 * @author Marco Boeck
 */
public class WelcomeTourAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private static Icon icon = null;

	private TourManager tourManager;

	static {
		icon = SwingTools.createIcon("48/" + I18N.getMessage(I18N.getGUIBundle(), "gui.action.welcome.tour.icon"));
	}

	public WelcomeTourAction() {
		super(I18N.getMessage(I18N.getGUIBundle(), "gui.action.welcome.tour.label"), icon);
		putValue(SHORT_DESCRIPTION, I18N.getMessage(I18N.getGUIBundle(), "gui.action.welcome.tour.tip"));
		//TODO: re-enable 
		tourManager = TourManager.getInstance();
//		checkTours();

	}

	public void actionPerformed(ActionEvent e) {
		new TourChooser().setVisible(true);
	}

	private void checkTours() {
		// check how much tours are not completed and remember the tourkey if and only if there is just one tour not completed
		String[] keys = tourManager.getTourkeys();
		String startKey = null;
		boolean stillSearching = true;
		for (int i = 0; i < keys.length; i++) {
			if (stillSearching) {
				if (tourManager.getTourState(keys[i]).equals(TourState.NOT_COMPLETED)) {
					if (startKey == null) {
						startKey = keys[i];
					} else {
						startKey = null;
						stillSearching = false;
					}
				}
			}
		}
//		//TODO: invokeLater
//		SwingUtilities.invokeLater(new Runnable() {
//
//			@Override
//			public void run() {
//				//ask whether we should start the tour if we found only one Key
//				if (startKey != null) {
//					ConfirmDialog dialog = new ConfirmDialog("new_tour_found", ConfirmDialog.YES_NO_OPTION, true, startKey);
//					dialog.setVisible(true);
//					if (dialog.getReturnOption() == ConfirmDialog.YES_OPTION) {
//						tourManager.startTour(startKey);
//					}
//					if (dialog.getDontAskAgainOption()) {
//						tourManager.setTourState(startKey, TourState.NEVER_ASK);
//					}
//				}
//			}
//		});
	}

}
