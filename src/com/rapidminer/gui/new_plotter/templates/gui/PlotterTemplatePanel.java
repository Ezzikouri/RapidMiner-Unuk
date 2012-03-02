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
package com.rapidminer.gui.new_plotter.templates.gui;

import java.util.Observer;

import javax.swing.JPanel;

import com.rapidminer.gui.new_plotter.data.PlotInstance;
import com.rapidminer.gui.new_plotter.templates.PlotterTemplate;

/**
 * This class is the abstract superclass for all {@link PlotterTemplate} GUIs.
 * 
 * @author Marco Boeck
 *
 */
public abstract class PlotterTemplatePanel extends JPanel implements Observer {
	
	private static final long serialVersionUID = -7451641816924895335L;
	
	
	/**
	 * Standard constructor. Adds the {@link PlotterTemplatePanel} as an {@link Observer} to
	 * the {@link PlotterTemplate}.
	 */
	public PlotterTemplatePanel(final PlotterTemplate template) {
		template.addObserver(this);
	}
	
	/**
	 * Call this method each time the {@link PlotInstance} changes.
	 * @param plotInstance
	 */
	public abstract void updatePlotInstance(final PlotInstance plotInstance);
}
