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
package com.rapidminer.gui.viewer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.datatable.DataTableExampleSetAdapter;
import com.rapidminer.datatable.DataTableListener;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.new_plotter.configuration.PlotConfiguration;
import com.rapidminer.gui.new_plotter.data.PlotInstance;
import com.rapidminer.gui.new_plotter.gui.ChartConfigurationPanel;
import com.rapidminer.gui.new_plotter.gui.AbstractConfigurationPanel.DatasetTransformationType;
import com.rapidminer.gui.new_plotter.integration.ExpertDataTableRenderer;
import com.rapidminer.gui.new_plotter.integration.PlotConfigurationHistory;
import com.rapidminer.gui.plotter.Plotter;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.plotter.PlotterConfigurationModel;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.operator.IOObject;
import com.rapidminer.report.Tableable;

import edu.uci.ics.jung.visualization.layout.CachingLayout;

/**
 * Can be used to display (parts of) the data by means of a JTable.
 * 
 * @author Ingo Mierswa
 */
public class DataTableViewer extends JPanel implements Tableable, DataTableListener {
    
    private static final long serialVersionUID = 6878549119308753961L;
    
    
	public static final String TABLE_MODE = "TABLE";
	
	public static final String PLOT_MODE = "PLOT";

	private static final String ADVANCED_MODE = "ADVANCED";

    private JLabel generalInfo = new JLabel();
    
    private DataTableViewerTable dataTableViewerTable;
    
    private PlotterPanel plotterPanel;

	private JPanel tablePanel;

	private ChartConfigurationPanel advancedPanel;
	
	private PlotterConfigurationModel plotterSettings;

	public static enum ViewState {
		TABLE,
		PLOT,
		ADVANCED
	}

	private ViewState viewState;

	public ViewState getViewState() {
		return viewState;
	}

	public void setViewState(ViewState viewState) {
		this.viewState = viewState;
	}

	// removes all other panels when drawing the one of the currentstate
	private void removePanels(ViewState currentState) {
		if (currentState.equals(ViewState.TABLE)) {
			remove(plotterPanel);
			remove(advancedPanel);
		}
		if (currentState.equals(ViewState.PLOT)) {
			remove(tablePanel);
			remove(advancedPanel);
		}
		if (currentState.equals(ViewState.ADVANCED)) {
			remove(tablePanel);
			remove(plotterPanel);
		}

	}

	public DataTableViewer(DataTable dataTable) {
    	this(dataTable, PlotterConfigurationModel.DATA_SET_PLOTTER_SELECTION, true, TABLE_MODE, false);
    }
    
    public DataTableViewer(DataTable dataTable, boolean showPlotter) {
    	this(dataTable, PlotterConfigurationModel.DATA_SET_PLOTTER_SELECTION, showPlotter, TABLE_MODE, false);
    }
    
    public DataTableViewer(DataTable dataTable, boolean showPlotter, String startMode) {
    	this(dataTable, PlotterConfigurationModel.DATA_SET_PLOTTER_SELECTION, showPlotter, startMode, false);
    }

    public DataTableViewer(DataTable dataTable, LinkedHashMap<String, Class<? extends Plotter>> availablePlotters) {
    	this(dataTable, availablePlotters, true, TABLE_MODE, false);
    }
    
	public DataTableViewer(DataTable dataTable, LinkedHashMap<String, Class<? extends Plotter>> availablePlotters, boolean showPlotter, String tableMode, boolean autoResize) {

		super(new BorderLayout());

		// Build table view
        this.dataTableViewerTable = new DataTableViewerTable(autoResize);
		this.tablePanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(generalInfo);
        tablePanel.add(infoPanel, BorderLayout.NORTH);
		JScrollPane tableScrollPane = new ExtendedJScrollPane(dataTableViewerTable);
		tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        
		// Build cards
		CardLayout cards = new CardLayout();
		JPanel mainPanel = new JPanel(cards);
		add(mainPanel, BorderLayout.CENTER);

		// Add and select table as default
		mainPanel.add(tablePanel, TABLE_MODE);
		cards.show(mainPanel, TABLE_MODE);
        
		// Add plotters and radio buttons if desired
        if (showPlotter) {
        	this.plotterSettings = new PlotterConfigurationModel(availablePlotters, dataTable);
        	this.plotterPanel = new PlotterPanel(plotterSettings);
			DataTable plotData = plotterSettings.getDataTable();

			// preface to create ChartConfigationPanel:
			ExampleSet exampleSet = DataTableExampleSetAdapter.createExampleSetFromDataTable(plotData);
			Map<DatasetTransformationType, PlotConfiguration> plotConfigurationMap = PlotConfigurationHistory.getPlotConfigurationMap((IOObject) exampleSet, plotData);
			PlotInstance plotInstance = new PlotInstance(plotConfigurationMap.get(DatasetTransformationType.ORIGINAL), plotData);
			this.advancedPanel = new ChartConfigurationPanel(true, plotInstance, plotData, plotConfigurationMap.get(DatasetTransformationType.DE_PIVOTED));

        	// toggle radio button for views
        	final JRadioButton tableButton = new JRadioButton("Table View", true);
        	tableButton.setToolTipText("Toggles to the table view of this model data.");
        	tableButton.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {
        			if (tableButton.isSelected()) {
						setViewState(ViewState.TABLE);
						removePanels(getViewState());
						add(tablePanel, BorderLayout.CENTER);
						repaint();
					}
        		}
        	});
        	final JRadioButton plotButton = new JRadioButton("Plot View", false);
        	plotButton.setToolTipText("Toggles to the plotter view of this model.");
        	plotButton.addActionListener(new ActionListener() {
        		public void actionPerformed(ActionEvent e) {

        			if (plotButton.isSelected()) {
						setViewState(ViewState.PLOT);
						removePanels(getViewState());
        				add(plotterPanel, BorderLayout.CENTER);
						repaint();
        			}
        		}
        	});

			final JRadioButton advancedChartsButton = new JRadioButton("Advanced Charts", false);
			advancedChartsButton.setToolTipText("Toggles to the advanced plot view of this model.");
			advancedChartsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (advancedChartsButton.isSelected()) {
						setViewState(ViewState.ADVANCED);
						removePanels(getViewState());
						add(advancedPanel, BorderLayout.CENTER);
						repaint();
					}
				}
			});


            ButtonGroup group = new ButtonGroup();
        	group.add(tableButton);
        	group.add(plotButton);
			group.add(advancedChartsButton);
            JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        	togglePanel.add(tableButton);	
        	togglePanel.add(plotButton);
			togglePanel.add(advancedChartsButton);

        	add(togglePanel, BorderLayout.NORTH);
        	// init correct mode
        	if(tableMode == TABLE_MODE) {
					add(tablePanel);
					tableButton.setSelected(true);
					setViewState(ViewState.TABLE);
        	} else if(tableMode == PLOT_MODE){
					add(plotterPanel);
					plotButton.setSelected(true);
					setViewState(ViewState.PLOT);
			} else if (tableMode == ADVANCED_MODE) {
					add(advancedPanel);
					advancedChartsButton.setSelected(true);
					setViewState(ViewState.ADVANCED);

        	}
        } // end if (showPlotter)

        setDataTable(dataTable);
    }

    public DataTable getDataTable() {
    	return plotterSettings.getDataTable();
    }
    
    public PlotterPanel getPlotterPanel() {
    	return plotterPanel;
    }
    
    public DataTableViewerTable getTable() {
        return dataTableViewerTable;
    }
    
    public void setDataTable(DataTable dataTable) {
        dataTableViewerTable.setDataTable(dataTable);
        if (plotterSettings != null) {
        	plotterSettings.setDataTable(dataTable);
        }
        
        // add listener for correct row count
        dataTable.addDataTableListener(this);
        dataTableUpdated(dataTable);
    }
    
	public void dataTableUpdated(DataTable dataTable) {
        generalInfo.setText(dataTable.getName() + " (" + dataTable.getNumberOfRows() + " rows, " + dataTable.getNumberOfColumns() + " columns)");	
	}
    
	public void prepareReporting() {
		dataTableViewerTable.prepareReporting();
	}
	
	public void finishReporting() {
		dataTableViewerTable.finishReporting();
	}
	
    public String getColumnName(int columnIndex) {
    	return dataTableViewerTable.getColumnName(columnIndex);
    }

	public String getCell(int row, int column) {
		return dataTableViewerTable.getCell(row, column);
	}

	public int getColumnNumber() {
		return dataTableViewerTable.getColumnNumber();
	}

	public int getRowNumber() {
		return dataTableViewerTable.getRowNumber();
	}
	
	public boolean isFirstLineHeader() { return false; }
	
	public boolean isFirstColumnHeader() { return false; }
}
