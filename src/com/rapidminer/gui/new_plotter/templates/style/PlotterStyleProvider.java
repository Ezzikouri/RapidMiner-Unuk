/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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
package com.rapidminer.gui.new_plotter.templates.style;

import java.awt.Font;
import java.util.Observable;

import javax.swing.JPanel;

/**
 * Abstract class which all style providers for the new plotter templates have to extend.
 * 
 * @author Marco Boeck
 *
 */
public abstract class PlotterStyleProvider extends Observable {
	
	/**
	 * Return the {@link JPanel} where the user can change the color/font settings.
	 * @return
	 */
	public abstract JPanel getStyleProviderPanel();
	
	/**
	 * Returns the {@link Font} which the user chose for the axes.
	 * @return
	 */
	public abstract Font getAxesFont();
	
	/**
	 * Returns the {@link Font} which the user chose for the legend.
	 * @return
	 */
	public abstract Font getLegendFont();
	
	/**
	 * Returns the {@link Font} which the user chose for the title.
	 * @return
	 */
	public abstract Font getTitleFont();
	
	/**
	 * Returns a {@link ColorScheme} instance which will be used to color the plot(s).
	 * Each plot will use one of the colors in the order provided, if more plots than
	 * colors exist, it will start from the beginning.
	 * @return
	 */
	public abstract ColorScheme getColorScheme();
}
