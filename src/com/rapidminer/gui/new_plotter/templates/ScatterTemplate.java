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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.new_plotter.configuration.DataTableColumn;
import com.rapidminer.gui.new_plotter.configuration.DefaultDimensionConfig;
import com.rapidminer.gui.new_plotter.configuration.DimensionConfig.PlotDimension;
import com.rapidminer.gui.new_plotter.configuration.LegendConfiguration.LegendPosition;
import com.rapidminer.gui.new_plotter.configuration.PlotConfiguration;
import com.rapidminer.gui.new_plotter.configuration.RangeAxisConfig;
import com.rapidminer.gui.new_plotter.configuration.SeriesFormat;
import com.rapidminer.gui.new_plotter.configuration.ValueSource;
import com.rapidminer.gui.new_plotter.templates.gui.PlotterTemplatePanel;
import com.rapidminer.gui.new_plotter.templates.gui.ScatterTemplatePanel;
import com.rapidminer.gui.new_plotter.templates.style.PlotterStyleProvider;
import com.rapidminer.gui.new_plotter.templates.style.ColorScheme.ColorRGB;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.math.function.aggregation.AbstractAggregationFunction.AggregationFunctionType;

/**
 * The template for a scatter plot. For the GUI, see {@link ScatterTemplatePanel}.
 * 
 * @author Marco Boeck
 * 
 */
public class ScatterTemplate extends PlotterTemplate {

	private static final String JITTER_ELEMENT = "jitter";

	private static final String COLOR_LOGARITHMIC_ELEMENT = "colorLogarithmic";

	private static final String Y_AXIS_LOGARITHMIC_ELEMENT = "yAxisLogarithmic";

	private static final String X_AXIS_LOGARITHMIC_ELEMENT = "xAxisLogarithmic";

	private static final String COLOR_COLUMN_ELEMENT = "colorColumn";

	private static final String Y_AXIS_COLUMN_ELEMENT = "yAxisColumn";

	private static final String X_AXIS_COLUMN_ELEMENT = "xAxisColumn";
	

	/** the current {@link RangeAxisConfig} */
	private RangeAxisConfig currentRangeAxisConfig;

	/** the name of the x-axis column */
	private String xAxisColumn;

	/** the name of the y-axis column */
	private String yAxisColumn;

	/** the name of the color column */
	private String colorColumn;

	/** if true, the x-axis will be logarithmic */
	private boolean xAxisLogarithmic;

	/** if true, the y-axis will be logarithmic */
	private boolean yAxisLogarithmic;

	/** if true, the color will be logarithmic */
	private boolean colorLogarithmic;

	/** the jitter value for the plot */
	private int jitter;
	
	/** the {@link ScatterTemplatePanel} instance */
	private transient ScatterTemplatePanel scatterPanel;
	
	
	/**
	 * Creates a new {@link ScatterTemplate}. This template allows easy configuration of the scatter
	 * chart for the plotter.
	 */
	public ScatterTemplate() {
		// value when "None" is selected
		String noSelection = I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.column.empty_selection.label");
		xAxisColumn = noSelection;
		yAxisColumn = noSelection;
		colorColumn = noSelection;

		xAxisLogarithmic = false;
		yAxisLogarithmic = false;
		colorLogarithmic = false;

		jitter = 0;
		
		scatterPanel = new ScatterTemplatePanel(this);
	}

	@Override
	public String getChartType() {
		return ScatterTemplate.getI18NName();
	}

	@Override
	public PlotterTemplatePanel getTemplateConfigurationPanel() {
		return scatterPanel;
	}

	@Override
	protected void dataUpdated(final DataTable dataTable) {
		// clear possible existing data
		currentRangeAxisConfig = null;
		scatterPanel.updatePlotInstance(getPlotInstance());
	}

	/**
	 * Set the name of the column which will be used as the domain (X) axis.
	 * 
	 * @param columnName
	 */
	public void setXAxisColum(String columnName) {
		if (columnName == null) {
			throw new IllegalArgumentException("columnName must not be null!");
		}
		xAxisColumn = columnName;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the name of the domain (X) axis column.
	 * 
	 * @return
	 */
	public String getXAxisColumn() {
		return xAxisColumn;
	}

	/**
	 * Set the name of the column which will be used as the range (Y) axis.
	 * 
	 * @param columnName
	 */
	public void setYAxisColum(String columnName) {
		if (columnName == null) {
			throw new IllegalArgumentException("columnName must not be null!");
		}
		yAxisColumn = columnName;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the name of the range (Y) axis column.
	 * 
	 * @return
	 */
	public String getYAxisColumn() {
		return yAxisColumn;
	}

	/**
	 * Set the name of the column which will be used as the color.
	 * 
	 * @param columnName
	 */
	public void setColorColum(String columnName) {
		if (columnName == null) {
			throw new IllegalArgumentException("columnName must not be null!");
		}
		colorColumn = columnName;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the name of the column used for the color.
	 * 
	 * @return
	 */
	public String getColorColumn() {
		return colorColumn;
	}

	/**
	 * Sets whether the domain (X) axis should be logarithmic or not.
	 * 
	 * @param log
	 */
	public void setXAxisLogarithmic(boolean log) {
		xAxisLogarithmic = log;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns whether the domain (X) axis is logarithmic or not.
	 * 
	 * @return
	 */
	public boolean isXAxisLogarithmic() {
		return xAxisLogarithmic;
	}

	/**
	 * Sets whether the range (Y) axis should be logarithmic or not.
	 * 
	 * @param log
	 */
	public void setYAxisLogarithmic(boolean log) {
		yAxisLogarithmic = log;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns whether the range (Y) axis is logarithmic or not.
	 * 
	 * @return
	 */
	public boolean isYAxisLogarithmic() {
		return yAxisLogarithmic;
	}

	/**
	 * Sets whether the color should be logarithmic or not.
	 * 
	 * @param log
	 */
	public void setColorLogarithmic(boolean log) {
		colorLogarithmic = log;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns whether the color is logarithmic or not.
	 * 
	 * @return
	 */
	public boolean isColorLogarithmic() {
		return colorLogarithmic;
	}

	/**
	 * Sets the jitter for the plot.
	 * 
	 * @param jitter
	 *            0 <= {@code jitter} <= 100
	 */
	public void setJitter(int jitter) {
		if (jitter < 0 || jitter > 100) {
			throw new IllegalArgumentException("jitter must be between 0 and 100!");
		}
		this.jitter = jitter;

		setChanged();
		notifyObservers();
		updatePlotConfiguration();
	}

	/**
	 * Returns the current jitter setting for the plot.
	 * 
	 * @return
	 */
	public int getJitter() {
		return jitter;
	}
	
	public static String getI18NName() {
		return I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.scatter.name"); 
	}

	@Override
	protected void updatePlotConfiguration() {
		// don't do anything if updates are suspended due to batch updating
		if (suspendUpdates) {
			return;
		}
		
		PlotConfiguration plotConfiguration = plotInstance.getMasterPlotConfiguration();

		// value when "None" is selected
		String noSelection = I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.column.empty_selection.label");
		
		// stop event processing
		boolean plotConfigurationProcessedEvents = plotConfiguration.isProcessingEvents();
		plotConfiguration.setProcessEvents(false);

		// x axis column selection
		if (!xAxisColumn.equals(noSelection)) {
			plotConfiguration.getDimensionConfig(PlotDimension.DOMAIN).setDataTableColumn(new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(xAxisColumn)));
			plotConfiguration.getDimensionConfig(PlotDimension.DOMAIN).setLogarithmic(xAxisLogarithmic);
		} else {
			// remove config
			if (currentRangeAxisConfig != null) {
				plotConfiguration.removeRangeAxisConfig(currentRangeAxisConfig);
				currentRangeAxisConfig = null;
			}
			plotConfiguration.setProcessEvents(plotConfigurationProcessedEvents);
			return;
		}

		// y axis column selection
		if (!yAxisColumn.equals(noSelection)) {
			RangeAxisConfig newRangeAxisConfig = new RangeAxisConfig(yAxisColumn, plotConfiguration);
			ValueSource valueSource;
			valueSource = new ValueSource(plotConfiguration, new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(yAxisColumn)), AggregationFunctionType.count,
					false);
			SeriesFormat sFormat = new SeriesFormat();
			valueSource.setSeriesFormat(sFormat);
			newRangeAxisConfig.addValueSource(valueSource, null);
			newRangeAxisConfig.setLogarithmicAxis(yAxisLogarithmic);

			// remove old config
			if (currentRangeAxisConfig != null) {
				plotConfiguration.removeRangeAxisConfig(currentRangeAxisConfig);
			}
			// add new config
			plotConfiguration.addRangeAxisConfig(newRangeAxisConfig);
			// remember the new config so we can remove it later again
			currentRangeAxisConfig = newRangeAxisConfig;
		} else {
			// remove config
			if (currentRangeAxisConfig != null) {
				plotConfiguration.removeRangeAxisConfig(currentRangeAxisConfig);
				currentRangeAxisConfig = null;
			}
		}

		// color column selection
		if (!colorColumn.equals(noSelection)) {
			plotConfiguration.setDimensionConfig(PlotDimension.COLOR, null);
			DefaultDimensionConfig dimConfig = new DefaultDimensionConfig(plotConfiguration, new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(colorColumn)),
					PlotDimension.COLOR);
			dimConfig.setLogarithmic(colorLogarithmic);
			plotConfiguration.setDimensionConfig(PlotDimension.COLOR, dimConfig);
		} else {
			plotConfiguration.setDimensionConfig(PlotDimension.COLOR, null);
		}

		// general settings
		plotConfiguration.setAxesFont(styleProvider.getAxesFont());
		plotConfiguration.setTitleFont(styleProvider.getTitleFont());
		plotConfiguration.getLegendConfiguration().setLegendFont(styleProvider.getLegendFont());
		plotConfiguration.addColorSchemeAndSetActive(styleProvider.getColorScheme());
		plotConfiguration.getLegendConfiguration().setLegendPosition(LegendPosition.BOTTOM);
		plotConfiguration.setFrameBackgroundColor(ColorRGB.convertToColor(styleProvider.getFrameBackgroundColor()));
		plotConfiguration.setPlotBackgroundColor(ColorRGB.convertToColor(styleProvider.getPlotBackgroundColor()));
		plotConfiguration.setTitleText(styleProvider.getTitleText());
		
		// continue event processing
		plotConfiguration.setProcessEvents(plotConfigurationProcessedEvents);
	}

	@Override
	public Element writeToXML(Document document) {
		Element template = document.createElement(TEMPLATE_ELEMENT);
		template.setAttribute(NAME_ELEMENT, getChartType());
		Element setting;
		
		setting = document.createElement(X_AXIS_COLUMN_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, xAxisColumn);
		template.appendChild(setting);
		
		setting = document.createElement(Y_AXIS_COLUMN_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, yAxisColumn);
		template.appendChild(setting);
		
		setting = document.createElement(COLOR_COLUMN_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, colorColumn);
		template.appendChild(setting);
		
		setting = document.createElement(X_AXIS_LOGARITHMIC_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, String.valueOf(xAxisLogarithmic));
		template.appendChild(setting);
		
		setting = document.createElement(Y_AXIS_LOGARITHMIC_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, String.valueOf(yAxisLogarithmic));
		template.appendChild(setting);
		
		setting = document.createElement(COLOR_LOGARITHMIC_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, String.valueOf(colorLogarithmic));
		template.appendChild(setting);
		
		setting = document.createElement(JITTER_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, String.valueOf(jitter));
		template.appendChild(setting);
		
		template.appendChild(styleProvider.createXML(document));
		
		return template;
	}
	
	@Override
	public void loadFromXML(Element templateElement) {
		suspendUpdates = true;
		
		for (int i=0; i<templateElement.getChildNodes().getLength(); i++) {
			Node node = templateElement.getChildNodes().item(i);
			if (node instanceof Element) {
				Element setting = (Element) node;
				
				if (setting.getNodeName().equals(X_AXIS_COLUMN_ELEMENT)) {
					setXAxisColum(setting.getAttribute(VALUE_ATTRIBUTE));
				} else if (setting.getNodeName().equals(Y_AXIS_COLUMN_ELEMENT)) {
					setYAxisColum(setting.getAttribute(VALUE_ATTRIBUTE));
				} else if (setting.getNodeName().equals(COLOR_COLUMN_ELEMENT)) {
					setColorColum(setting.getAttribute(VALUE_ATTRIBUTE));
				} else if (setting.getNodeName().equals(X_AXIS_LOGARITHMIC_ELEMENT)) {
					setXAxisLogarithmic(Boolean.parseBoolean(setting.getAttribute(VALUE_ATTRIBUTE)));
				} else if (setting.getNodeName().equals(Y_AXIS_LOGARITHMIC_ELEMENT)) {
					setYAxisLogarithmic(Boolean.parseBoolean(setting.getAttribute(VALUE_ATTRIBUTE)));
				} else if (setting.getNodeName().equals(COLOR_LOGARITHMIC_ELEMENT)) {
					setColorLogarithmic(Boolean.parseBoolean(setting.getAttribute(VALUE_ATTRIBUTE)));
				} else if (setting.getNodeName().equals(JITTER_ELEMENT)) {
					try {
						setJitter(Integer.parseInt(setting.getAttribute(VALUE_ATTRIBUTE)));
					} catch (NumberFormatException e) {
						LogService.getRoot().warning("Could not restore jitter setting for scatter template!");
					}
				} else if (setting.getNodeName().equals(PlotterStyleProvider.STYLE_ELEMENT)) {
					styleProvider.loadFromXML(setting);
				}
			}
		}
		
		suspendUpdates = false;
		updatePlotConfiguration();
	}
}
