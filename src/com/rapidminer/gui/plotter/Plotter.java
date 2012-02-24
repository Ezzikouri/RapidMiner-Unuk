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
package com.rapidminer.gui.plotter;

import java.awt.Point;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import com.rapidminer.datatable.DataTable;
import com.rapidminer.gui.plotter.PlotterConfigurationModel.PlotterChangedListener;
import com.rapidminer.gui.plotter.PlotterConfigurationModel.PlotterSettingsChangedListener;
import com.rapidminer.gui.plotter.conditions.BasicPlotterCondition;
import com.rapidminer.gui.plotter.conditions.PlotterCondition;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.report.Renderable;


/**
 * An interface for all data available plotters. Plotters which implements this
 * interface can be used together with a plotter panel which is automatically
 * created and / or adapted.
 * 
 * @author Ingo Mierswa
 */
public interface Plotter extends Renderable, PlotterSettingsChangedListener, PlotterChangedListener {
    
	/**
	 * Indicates that no plot values can be selected by the user.
	 */
	public static final int NO_SELECTION = -1;
	
	/**
	 * Indicates that only single values can be selected by the user for a
	 * dimension.
	 */
	public static final int SINGLE_SELECTION = 0;

	/**
	 * Indicates that multiple values can be selected by the user for a
	 * dimension.
	 */
	public static final int MULTIPLE_SELECTION = 1;

	/** Returns the name of this plotter. */
	public String getPlotterName();
	
	/** Sets the data table for this plotter. */
	public void setDataTable(DataTable dataTable);

	/** Returns the plotter component. */
	public JComponent getPlotter();
	
	/** Returns the plotter component for rendering purposes like reporting. In most cases this will
	 *  be the same component but sometimes a plotter might want to left out additional control 
	 *  elements etc. */
	public JComponent getRenderComponent();
	
	
	/** Forces the generation of a plotter which might be necessary for reporting plotters which
	 *  are usually generated by user interaction. Plotter which are only generated by user interaction
	 *  (for example the SOM plotter or the scatter plot matrix) should calculate the plot by 
	 *  overwriting this method. */
	public void forcePlotGeneration();
	
	/** Indicates if the plotter is currently under a process of value adjustments. Might give 
	 *  implementing plotters a hint that graphical updates should not be performed until all
	 *  settings are made. */
	public void stopUpdates(boolean value);
	
    /** Returns the condition for data tables. Plotters which are able to plot all data tables
     *  should not return null but {@link BasicPlotterCondition}. */
    public PlotterCondition getPlotterCondition();
    
	/**
	 * Returns a small icon for the line type with the given index. May return
	 * null.
	 */
	public Icon getIcon(int index);

	/**
	 * Returns the number of axes beside the value dimension, i. e. 1 for a 2D
	 * and 2 for a 3D plot.
	 */
	public int getNumberOfAxes();

	/**
	 * Returns a label for the axis with the given index, e.g.
	 * &quot;x-Axis&quot;.
	 */
	public String getAxisName(int index);

	/**
	 * Returns the selection type for the value axis, i.e. one out of
	 * NO_SELECTION, SINGLE_SELECTION, or MULTIPLE_SELECTION.
	 */
	public int getValuePlotSelectionType();

	/** Returns true if the plotter can handle jitter settings, i.e. a small random pertubation of
	 *  data point positions. */
	public boolean canHandleJitter();
	
	/** Indicates if the jittering can be handled continously. */
	public boolean canHandleContinousJittering();
	
	/** Sets the amount of Jitter and initiates a repaint. */
	public void setJitter(int jitter);
	
	/** Can handle zooming. */
	public boolean canHandleZooming();
	
	/** Reacts to zoom setting changed. The given value lies between 1 and 100. The initial value is 1. */
	public void setZooming(int zooming);
	
	/** Returns the initial zoom factor. */
	public int getInitialZoomFactor();

	/** Returns true if this plotter provides an options dialog. */
	public boolean hasOptionsDialog();

	/** Opens an options dialog. */
	public void showOptionsDialog();

	/** Returns the component of index index for interaction if plotter provides 
	 *  one. If no component with the index exists, it returns null */
	public JComponent getOptionsComponent(int index);

	/** Returns true, if plotter has capability to save an image on his own. If false,
	 *  RapidMiner will provide a Save Image button.
	 */
	public boolean hasSaveImageButton();

	/** Returns true, if plotter provides coordinates to show. If returns false 
	 * Label for coordinates will be removed
	 */
	public boolean isProvidingCoordinates();
    
	/** 
	 * Returns the name of the plotting axe. If returns null, default "Plots" should be used.
	 */
	public String getPlotName();
	
	/** Indicates if the plotter can save the data. */
	public boolean isSaveable();

	/** Should invoke the saving of the plotted data (file dialog,...). */
	public void save();

	/** Maps the given data dimension on the given plotterAxis. */
	public void setAxis(int plotterAxis, int dimension);

	/** Returns the data dimension which is used for the given axis or -1 if no axis is used. */
	public int getAxis(int axis);

	/**
	 * Sets if the given data dimension should be plotted in the value
	 * dimension.
	 */
	public void setPlotColumn(int dimension, boolean plot);

	/** Returns true if the given dimension should be plotted. */
	public boolean getPlotColumn(int dimension);

	/** Indicates where the mouse is in the plotter component. */
	public void setMousePosInDataSpace(int mouseX, int mouseY);

	/** Returns the given position in data space. */
	public Point2D getPositionInDataSpace(Point p);

	/** Sets the bounds of the dragging rectangle. */
	public void setDragBounds(int x, int y, int w, int h);

	/** Sets the drawing range in data space. */
	public void setDrawRange(double x, double y, double w, double h);

	/**
	 * Returns the ID of the object under the mouse cursor if it has an id.
	 * Otherwise null is returned.
	 */
	public String getIdForPos(int x, int y);

	/** Adds a mouse motion listener to the plotter component. */
	public void addMouseMotionListener(MouseMotionListener listener);

	/** Adds a mouse listener to the plotter component. */
	public void addMouseListener(MouseListener listener);

	/** Sets a key text (legend). */
	public void setKey(String key);
	
	/** Returns true if a log scale for this column is supported. */
	public boolean isSupportingLogScale(int axis);

	/** Returns true if a log scale for this column is supported. */
	public boolean isSupportingLogScaleForPlotColumns();
	
	/** Sets if the given axis should be plotted with log scale. */
	public void setLogScale(int axis, boolean logScale);
	
	/** Sets if the plot columns should be plotted with log scale. */
	public void setLogScaleForPlotColumns(boolean logScale);
	
	/** Returns true if this plotter supports sorting for the selected plot dimension. */
	public boolean isSupportingSorting();
	
	/** Indicates if the data should be sorted according to the currently selected plot dimension. */
	public void setSorting(boolean sorting);
	
	/** Returns true if this plotter supports the usage of absolute values. */
	public boolean isSupportingAbsoluteValues();
	
	/** Indicates if absolute data values should be used instead of the actual data. */
	public void setAbsolute(boolean absolute);
	
	/** Returns the list of parameter types supported by this plotter for setting from outside. 
	 * @param inputPort TODO*/
	public List<ParameterType> getParameterTypes(InputPort inputPort);
	
	/** Sets the given parameter. */
	public void applyParameterSetting(DataTable dataTable, String key, String value);
	
	/** This method sets the coordinate transformation, used for the mapping of user space coordinates inside the plotter 
	 * itself to the screen. In arbitrary situations, it doesn't hold that the coordinate of the plotter component is 
	 * the same as on the screen. For example this might not be the case, if the component is zoomed.
	 */
	public void setCoordinateTransformation(CoordinateTransformation transformation);
	
}
