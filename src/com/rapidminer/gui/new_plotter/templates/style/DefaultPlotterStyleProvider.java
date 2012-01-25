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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import com.rapidminer.gui.new_plotter.templates.style.ColorScheme.ColorRGB;

/**
 * The default {@link PlotterStyleProvider}. Lets the user choose a font
 * for the axes, title and legend, and lets him choose a color scheme for the plot.
 * 
 * @author Marco Boeck
 *
 */
public class DefaultPlotterStyleProvider extends PlotterStyleProvider {
	
	/** the list containing all {@link ColorScheme}s. */
	private List<ColorScheme> listOfColorSchemes;
	
	/** the list containing all default {@link ColorScheme}s. */
	private List<ColorScheme> listOfDefaultColorSchemes;
	
	/** the synchronized object */
	private Object synchronizeColorSchemeListObject = new Object();
	
	/** the index pointing to the currently used color scheme */
	private int colorSchemeIndex;
	
	/** the current axes font */
	private Font axesFont;
	
	/** the current legend font */
	private Font legendFont;
	
	/** the current title font */
	private Font titleFont;
	
	/** the font size for the font buttons */
	public static final int FONT_SIZE_DEFAULT = 12;
	
	
	
	/**
	 * Creates a new {@link DefaultPlotterStyleProvider}.
	 */
	public DefaultPlotterStyleProvider() {
		listOfColorSchemes = new LinkedList<ColorScheme>();
		listOfDefaultColorSchemes = new LinkedList<ColorScheme>();
		colorSchemeIndex = 0;
		
		/*
		 * default color schemes are defined here
		 */
		List<ColorRGB> listOfColors = new LinkedList<ColorRGB>();
		listOfColors.add(new ColorRGB(222, 217, 26));
		listOfColors.add(new ColorRGB(219, 138, 47));
		listOfColors.add(new ColorRGB(217, 26, 21));
		listOfColors.add(new ColorRGB(83, 70, 255));
		listOfColors.add(new ColorRGB(156, 217, 84));
		ColorScheme cs = new ColorScheme("Colorful", listOfColors);
		listOfColorSchemes.add(cs);
		listOfDefaultColorSchemes.add(cs);
		
		listOfColors = new LinkedList<ColorRGB>();
		listOfColors.add(new ColorRGB(94, 173, 0));
		listOfColors.add(new ColorRGB(255, 188, 10));
		listOfColors.add(new ColorRGB(189, 39, 53));
		listOfColors.add(new ColorRGB(255, 119, 0));
		listOfColors.add(new ColorRGB(81, 17, 84));
		cs = new ColorScheme("Forest", listOfColors);
		listOfColorSchemes.add(cs);
		listOfDefaultColorSchemes.add(cs);
		/*
		 * end default color schemes
		 */
		
		axesFont = new Font("Dialog", Font.PLAIN, 10);
		legendFont = new Font("Dialog", Font.PLAIN, 10);
		titleFont = new Font("Dialog", Font.PLAIN, 10);
	}

	

	@Override
	public JPanel getStyleProviderPanel() {
		return new DefaultPlotterStyleProviderGUI(this);
	}

	@Override
	public Font getAxesFont() {
		return axesFont;
	}
	
	/**
	 * Sets the axes {@link Font}.
	 * @param axesFont
	 */
	public void setAxesFont(Font axesFont) {
		if (axesFont == null) {
			throw new IllegalArgumentException("axesFont must not be null!");
		}
		
		this.axesFont = axesFont;
		setChanged();
		notifyObservers();
	}

	@Override
	public Font getLegendFont() {
		return legendFont;
	}
	
	/**
	 * Sets the legend {@link Font}.
	 * @param legendFont
	 */
	public void setLegendFont(Font legendFont) {
		if (legendFont == null) {
			throw new IllegalArgumentException("legendFont must not be null!");
		}
		
		this.legendFont = legendFont;
		setChanged();
		notifyObservers();
	}

	@Override
	public Font getTitleFont() {
		return titleFont;
	}
	
	/**
	 * Sets the title {@link Font}.
	 * @param titleFont
	 */
	public void setTitleFont(Font titleFont) {
		if (titleFont == null) {
			throw new IllegalArgumentException("titleFont must not be null!");
		}
		
		this.titleFont = titleFont;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Set the index of the currently selected {@link ColorScheme}.
	 * @param index
	 */
	public void setSelectedColorSchemeIndex(int index) {
		synchronized(synchronizeColorSchemeListObject) {
			if (index < 0 || index >= listOfColorSchemes.size()) {
				throw new IllegalArgumentException("index must be >= 0 and <= number of available color schemes!");
			}
		}
		
		colorSchemeIndex = index;
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Returns the index of the currently selected {@link ColorScheme}.
	 * @return
	 */
	public int getSelectedColorSchemeIndex() {
		return colorSchemeIndex;
	}
	
	/**
	 * Returns the list of available {@link ColorScheme}s. Notice that this list is a copy
	 * so direct modification of the {@link DefaultPlotterStyleProvider} is not possible.
	 * @return
	 */
	public List<ColorScheme> getColorSchemes() {
		List<ColorScheme> newList;
		synchronized(synchronizeColorSchemeListObject) {
			newList = new LinkedList<ColorScheme>(listOfColorSchemes);
		}
		return newList;
	}
	
	/**
	 * Appends the given {@link ColorScheme} to the list of available {@link ColorScheme}s.
	 * Duplicates are not allowed and will result in an {@link IllegalArgumentException}.
	 * @param colorScheme
	 */
	public void addColorScheme(ColorScheme colorScheme) {
		addColorScheme(colorScheme, listOfColorSchemes.size());
	}
	
	/**
	 * Adds the given {@link ColorScheme} to the list of available {@link ColorScheme}s at the specified index.
	 * Note that inserting before the default ColorSchemes is not supported and will result in an {@link IllegalArgumentException}.
	 * Duplicates are not allowed and will result in an {@link IllegalArgumentException}.
	 * @param colorScheme
	 */
	public void addColorScheme(ColorScheme colorScheme, int index) {
		if (colorScheme == null) {
			throw new IllegalArgumentException("colorScheme must not be null!");
		}
		if (index < listOfDefaultColorSchemes.size()) {
			throw new IllegalArgumentException("Cannot add a ColorScheme before the default ColorSchemes!");
		}
		index = Math.min(listOfColorSchemes.size(), index);
		
		synchronized(synchronizeColorSchemeListObject) {
			if (!listOfColorSchemes.contains(colorScheme)) {
				listOfColorSchemes.add(index, colorScheme);
			} else {
				throw new IllegalArgumentException("duplicate colorScheme not allowed!");
			}
		}
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Removes the given {@link ColorScheme} with the given index.
	 * Will throw an {@link IllegalArgumentException} if trying to remove a default {@link ColorScheme}.
	 * @param index
	 */
	public void removeColorScheme(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("index must not be < 0!");
		}
		if (index < listOfDefaultColorSchemes.size()) {
			throw new IllegalArgumentException("Cannot remove a default ColorScheme!");
		}
		
		synchronized(synchronizeColorSchemeListObject) {
			listOfColorSchemes.remove(index);
		}
	}
	
	/**
	 * Removes the given {@link ColorScheme}.
	 * Will throw an {@link IllegalArgumentException} if trying to remove a default {@link ColorScheme}.
	 * @param colorScheme
	 */
	public void removeColorScheme(ColorScheme colorScheme) {
		synchronized(synchronizeColorSchemeListObject) {
			if (listOfDefaultColorSchemes.contains(colorScheme)) {
				throw new IllegalArgumentException("Cannot remove a default ColorScheme!");
			}
			
			listOfColorSchemes.remove(colorScheme);
		}
	}

	@Override
	public ColorScheme getColorScheme() {
		ColorScheme scheme;
		synchronized(synchronizeColorSchemeListObject) {
			scheme = listOfColorSchemes.get(colorSchemeIndex);
		}
		return scheme;
	}
}
