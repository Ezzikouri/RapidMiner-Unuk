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
package com.rapidminer.gui.new_plotter.templates;

import java.util.Observable;
import java.util.Observer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.new_plotter.configuration.PlotConfiguration;
import com.rapidminer.gui.new_plotter.data.PlotInstance;
import com.rapidminer.gui.new_plotter.engine.jfreechart.JFreeChartPlotEngine;
import com.rapidminer.gui.new_plotter.templates.gui.PlotterTemplatePanel;
import com.rapidminer.gui.new_plotter.templates.style.PlotterStyleProvider;

/**
 * Abstract class which all templates for the new plotters have to extend.
 * 
 * @author Marco Boeck
 *
 */
public abstract class PlotterTemplate extends Observable implements Observer {
	
	/** the current {@link DataTable} */
	protected DataTable currentDataTable;
	
	/** the {@link PlotConfiguration} for the template */
	protected PlotInstance plotInstance;
	
	/** the {@link PlotterStyleProvider} for the template */
	protected PlotterStyleProvider styleProvider;
	
	/** the {@link JFreeChartPlotEngine} instance */
	protected JFreeChartPlotEngine plotEngine;
	
	/** if true, will not update plot configuration despite changes */
	protected transient boolean suspendUpdates;
	
	/** the GUI used to display the given template */
	protected transient PlotterTemplatePanel guiPanel;
	
	
	public static final String TEMPLATE_ELEMENT = "template";

	public static final String NAME_ELEMENT = "name";
	
	public static final String VALUE_ATTRIBUTE = "value";
	
	
	/**
	 * Standard constructor.
	 */
	public PlotterTemplate() {
		// empty
	}
	
	/**
	 * Sets the {@link PlotterStyleProvider} for this {@link PlotterTemplate}.
	 * @param styleProvider
	 */
	public void setStyleProvider(PlotterStyleProvider styleProvider) {
		this.styleProvider = styleProvider;
		this.styleProvider.addObserver(this);
	}
	
	/**
	 * Gets the {@link PlotterStyleProvider} for this {@link PlotterTemplate}.
	 * @return
	 */
	public PlotterStyleProvider getStyleProvider() {
		return this.styleProvider;
	}
	
	/**
	 * Gets the name of the chart for the {@link PlotterTemplate}. This is the name which will
	 * be displayed in the chart type combobox.
	 * @return the chart type
	 */
	public abstract String getChartType();
	
	/**
	 * Gets the {@link PlotterTemplatePanel} which can be used to configurate the {@link PlotterTemplate}.
	 * @return the configuration {@link PlotterTemplatePanel}
	 */
	public PlotterTemplatePanel getTemplateConfigurationPanel() {
		return guiPanel;
	}
	
	/**
	 * Gets the {@link PlotConfiguration} object used to setup the plotter for this specific chart type.
	 * @return the {@link PlotConfiguration}
	 */
	public PlotConfiguration getPlotConfiguration() {
		return plotInstance.getMasterPlotConfiguration();
	}
	
	/**
	 * Gets the {@link PlotInstance} object for this template.
	 * @return the {@link PlotInstance}
	 */
	public PlotInstance getPlotInstance() {
		return plotInstance;
	}
	
	@Override
	public String toString() {
		return getChartType();
	}
	
	/**
	 * Set the {@link PlotInstance} object for the template.
	 * @param plotConfig
	 */
	public void setPlotInstance(PlotInstance plotInstance) {
		if (plotInstance == null) {
			throw new IllegalArgumentException("PlotInstance must not be null!");
		}
		
		this.plotInstance = plotInstance;
		if (guiPanel != null) {
			guiPanel.updatePlotInstance(plotInstance);
		}
	}
	
	/**
	 * Call to notify the plotter template that the data has changed.
	 * @param dataTable the new data
	 */
	public synchronized void fireDataUpdated(final DataTable dataTable) {
		this.currentDataTable = dataTable;
		dataUpdated(dataTable);
		
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Sets the {@link JFreeChartPlotEngine} instance.
	 * @param plotEngine
	 */
	public void setPlotEngine(JFreeChartPlotEngine plotEngine) {
		this.plotEngine = plotEngine;
	}
	
	/**
	 * Gets the current {@link DataTable} used by this {@link PlotterTemplate}.
	 * @return
	 */
	public DataTable getDataTable() {
		return currentDataTable;
	}
	
	/**
	 * Handles the template specific handling when the data changes.
	 * @param plotEngine the {@link JFreeChartPlotEngine} instance
	 * @param dataTable the new data
	 */
	protected abstract void dataUpdated(final DataTable dataTable);
	
	/**
	 * Returns the I18N name of the {@link PlotterTemplate} as displayed in the GUI.
	 * @return the I18N name
	 */
	public static String getI18NName() {
		throw new IllegalAccessError("method must be implemented by each template so this method will be hidden!");
	}
	
	/**
	 * Updates the current plot depending on the template.
	 */
	protected abstract void updatePlotConfiguration();
	
	/**
	 * Gets the {@link JFreeChartPlotEngine} instance.
	 */
	public JFreeChartPlotEngine getPlotEngine() {
		return plotEngine;
	}
	
	/**
	 * Writes all {@link PlotterTemplate} settings to the given {@link Document}.
	 * @param document the {@link Document} where all settings are written to
	 * @return the template {@link Element}
	 */
	public abstract Element writeToXML(Document document);
	
	/**
	 * Loads all {@link PlotterTemplate} settings from the given {@link Element}.
	 * @param templateElement the {@link Element} where all settings are loaded from
	 */
	public abstract void loadFromXML(Element templateElement);
	
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof PlotterStyleProvider) {
			updatePlotConfiguration();
		}
	}

	/**
	 * Forces an update of the chart.
	 */
	public void forceUpdate() {
		updatePlotConfiguration();
	}

}
