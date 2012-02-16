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

package com.rapidminer.gui.new_plotter.templates;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.new_plotter.configuration.DataTableColumn;
import com.rapidminer.gui.new_plotter.configuration.DimensionConfig;
import com.rapidminer.gui.new_plotter.configuration.DimensionConfig.PlotDimension;
import com.rapidminer.gui.new_plotter.configuration.LegendConfiguration.LegendPosition;
import com.rapidminer.gui.new_plotter.configuration.LineFormat.LineStyle;
import com.rapidminer.gui.new_plotter.configuration.PlotConfiguration;
import com.rapidminer.gui.new_plotter.configuration.RangeAxisConfig;
import com.rapidminer.gui.new_plotter.configuration.SeriesFormat;
import com.rapidminer.gui.new_plotter.configuration.SeriesFormat.ItemShape;
import com.rapidminer.gui.new_plotter.configuration.SeriesFormat.VisualizationType;
import com.rapidminer.gui.new_plotter.configuration.ValueSource;
import com.rapidminer.gui.new_plotter.data.PlotInstance;
import com.rapidminer.gui.new_plotter.templates.SeriesTemplate.DataTableWithIndexDelegate;
import com.rapidminer.gui.new_plotter.templates.gui.PlotterTemplatePanel;
import com.rapidminer.gui.new_plotter.templates.gui.SeriesMultipleTemplatePanel;
import com.rapidminer.gui.new_plotter.templates.style.PlotterStyleProvider;
import com.rapidminer.gui.new_plotter.templates.style.ColorScheme.ColorRGB;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.math.function.aggregation.AbstractAggregationFunction.AggregationFunctionType;

/**
 * The template for a series multiple plot.
 * 
 * @author Marco Boeck
 * 
 */
public class SeriesMultipleTemplate extends PlotterTemplate {

	private static final String PLOT_NAME_ELEMENT = "plotName";

	private static final String PLOT_NAMES_ELEMENT = "plotNames";

	private static final String INDEX_NAME_ELEMENT = "indexName";
	
	
	/** the current {@link DataTable} */
	private DataTable currentDataTable;

	/** the current {@link RangeAxisConfig}s */
	private List<RangeAxisConfig> currentRangeAxisConfigsList;

	/** the name of the index column */
	private String indexName;

	/** the names of the plots to show */
	private Object[] plotNames;
	
	/** the {@link SeriesMultipleTemplatePanel} instance */
	private transient SeriesMultipleTemplatePanel seriesMultiplePanel;
	

	/**
	 * Creates a new {@link SeriesMultipleTemplate}. This template allows easy configuration of the
	 * histogram chart for the plotter.
	 */
	public SeriesMultipleTemplate() {
		currentRangeAxisConfigsList = new LinkedList<RangeAxisConfig>();

		// value when "None" is selected
		String noSelection = I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.column.empty_selection.label");
		indexName = noSelection;
		plotNames = new Object[0];
		
		seriesMultiplePanel = new SeriesMultipleTemplatePanel(this);
	}

	@Override
	public String getChartType() {
		return SeriesMultipleTemplate.getI18NName();
	}

	@Override
	public PlotterTemplatePanel getTemplateConfigurationPanel() {
		return seriesMultiplePanel;
	}

	/**
	 * Sets the name for the index dimension column.
	 * 
	 * @param columnName
	 */
	public void setIndexDimensionName(String columnName) {
		indexName = columnName;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the name of the index dimension column.
	 * 
	 * @return
	 */
	public String getIndexDimensionName() {
		return indexName;
	}

	/**
	 * Sets the currently selected plots by their name.
	 * 
	 * @param plotNames
	 */
	public void setPlotSelection(Object[] plotNames) {
		this.plotNames = plotNames;

		updatePlotConfiguration();
		setChanged();
		notifyObservers();
	}

	/**
	 * Returns the currently selected plots.
	 * 
	 * @return
	 */
	public Object[] getPlotSelection() {
		return plotNames;
	}

	@Override
	protected void dataUpdated(final DataTable dataTable) {
		// add artifical index column if needed
		if (dataTable.getColumnIndex(SeriesTemplate.ARTIFICAL_INDEX_COLUMN) == -1) {
			currentDataTable = new DataTableWithIndexDelegate(dataTable);

			PlotConfiguration plotConfiguration = new PlotConfiguration(new DataTableColumn(currentDataTable, 0));
			PlotInstance plotInstance = new PlotInstance(plotConfiguration, currentDataTable);
			setPlotInstance(plotInstance);
		}

		// clear possible existing data
		currentRangeAxisConfigsList.clear();
	}
	
	public static String getI18NName() {
		return I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.series_multiple.name");
	}

	@Override
	protected void updatePlotConfiguration() {
		// don't do anything if updates are suspended due to batch updating
		if (suspendUpdates) {
			return;
		}
		
		PlotConfiguration plotConfiguration = getPlotConfiguration();
		
		// stop event processing
		boolean plotConfigurationProcessedEvents = plotConfiguration.isProcessingEvents();
		plotConfiguration.setProcessEvents(false);
		
		// remove old config(s)
		for (RangeAxisConfig rAConfig : currentRangeAxisConfigsList) {
			plotConfiguration.removeRangeAxisConfig(rAConfig);
		}
		currentRangeAxisConfigsList.clear();
		
		// no selection?
		if (plotNames.length == 0) {
			plotConfiguration.setProcessEvents(plotConfigurationProcessedEvents);
			return;
		}

		// value when "None" is selected
		String noSelection = I18N.getMessage(I18N.getGUIBundle(), "gui.plotter.column.empty_selection.label");
		DataTableColumn indexColumn;
		if (indexName.equals(noSelection)) {
			indexColumn = new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(SeriesTemplate.ARTIFICAL_INDEX_COLUMN));
		} else {
			indexColumn = new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(indexName));
		}
		DimensionConfig domainDimensionConfig = plotConfiguration.getDimensionConfig(PlotDimension.DOMAIN);
		domainDimensionConfig.setDataTableColumn(indexColumn);

		int indexOfPlots = 0;
		for (Object plot : plotNames) {
			String plotName = String.valueOf(plot);
			RangeAxisConfig newRangeAxisConfig = new RangeAxisConfig(plotName, plotConfiguration);
			ValueSource valueSource;
			DataTableColumn aDataTableColumn = new DataTableColumn(currentDataTable, currentDataTable.getColumnIndex(plotName));
			valueSource = new ValueSource(plotConfiguration, aDataTableColumn, AggregationFunctionType.count, false);
			SeriesFormat sFormat = new SeriesFormat();
			sFormat.setSeriesType(VisualizationType.LINES_AND_SHAPES);
			sFormat.setLineStyle(LineStyle.SOLID);
			sFormat.setItemShape(ItemShape.NONE);
			sFormat.setLineWidth(1.5f);
			ColorRGB yAxisColor = styleProvider.getColorScheme().getColors().get(indexOfPlots++ % styleProvider.getColorScheme().getColors().size());
			sFormat.setItemColor(ColorRGB.convertToColor(yAxisColor));
			valueSource.setSeriesFormat(sFormat);
			newRangeAxisConfig.addValueSource(valueSource, null);

			// add new config(s)
			plotConfiguration.addRangeAxisConfig(newRangeAxisConfig);
			// remember the new config so we can remove it later again
			currentRangeAxisConfigsList.add(newRangeAxisConfig);
		}

		// general settings
		plotConfiguration.setAxesFont(styleProvider.getAxesFont());
		plotConfiguration.setTitleFont(styleProvider.getTitleFont());
		plotConfiguration.getLegendConfiguration().setLegendFont(styleProvider.getLegendFont());
		plotConfiguration.addColorSchemeAndSetActive(styleProvider.getColorScheme());
		plotConfiguration.getLegendConfiguration().setLegendPosition(LegendPosition.BOTTOM);
		plotConfiguration.setFrameBackgroundColor(ColorRGB.convertToColor(styleProvider.getFrameBackgroundColor()));
		plotConfiguration.setPlotBackgroundColor(ColorRGB.convertToColor(styleProvider.getPlotBackgroundColor()));
		
		// continue event processing
		plotConfiguration.setProcessEvents(plotConfigurationProcessedEvents);
	}
	
	@Override
	public Element writeToXML(Document document) {
		Element template = document.createElement(PlotterTemplate.TEMPLATE_ELEMENT);
		template.setAttribute(PlotterTemplate.NAME_ELEMENT, getChartType());
		Element setting;
		
		setting = document.createElement(INDEX_NAME_ELEMENT);
		setting.setAttribute(VALUE_ATTRIBUTE, String.valueOf(indexName));
		template.appendChild(setting);
		
		setting = document.createElement(PLOT_NAMES_ELEMENT);
		for (Object key : plotNames) {
			Element plotNameElement = document.createElement(PLOT_NAME_ELEMENT);
			plotNameElement.setAttribute(VALUE_ATTRIBUTE, String.valueOf(key));
			setting.appendChild(plotNameElement);
		}
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
				
				if (setting.getNodeName().equals(PLOT_NAMES_ELEMENT)) {
					List<Object> plotNamesList = new LinkedList<Object>();
					for (int j=0; j<setting.getChildNodes().getLength(); j++) {
						Node plotNode = setting.getChildNodes().item(j);
						if (plotNode instanceof Element) {
							Element plotNameElement = (Element) plotNode;
							
							if (plotNameElement.getNodeName().equals(PLOT_NAME_ELEMENT)) {
								plotNamesList.add(plotNameElement.getAttribute(VALUE_ATTRIBUTE));
							}
						}
					}
					setPlotSelection(plotNamesList.toArray());
				} else if (setting.getNodeName().equals(INDEX_NAME_ELEMENT)) {
					setIndexDimensionName(setting.getAttribute(VALUE_ATTRIBUTE));
				} else if (setting.getNodeName().equals(PlotterStyleProvider.STYLE_ELEMENT)) {
					styleProvider.loadFromXML(setting);
				}
			}
		}

		suspendUpdates = false;
		updatePlotConfiguration();
	}
}
