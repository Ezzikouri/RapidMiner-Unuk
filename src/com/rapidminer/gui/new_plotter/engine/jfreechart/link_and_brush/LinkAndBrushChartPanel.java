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

package com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelection;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelection.SelectionType;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelectionListener;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.plots.LinkAndBrushPlot;
import com.rapidminer.tools.container.Pair;

/**
 * A Swing GUI component for displaying a {@link JFreeChart} object. The chart will be buffered.
 * <P>
 * 
 * @author Nils Woehler
 * 
 */
public class LinkAndBrushChartPanel extends ChartPanel {

	private static final long serialVersionUID = 1L;
	private final boolean zoomOnLinkAndBrushSelection;

	private transient List<WeakReference<LinkAndBrushSelectionListener>> listeners = new LinkedList<WeakReference<LinkAndBrushSelectionListener>>();

	public LinkAndBrushChartPanel(JFreeChart chart, boolean zoomOnLinkAndBrushSelection) {
		super(chart, 600, 400, 200, 133, DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT, DEFAULT_BUFFER_USED, 
				false,  // copy
				false,  // properties
				false,  // save
				false,  // print
				false,  // zoom
				true);   // tooltips

		this.zoomOnLinkAndBrushSelection = zoomOnLinkAndBrushSelection;
		setInitialDelay(200);

		setMouseWheelEnabled(false);
	}

	public LinkAndBrushChartPanel(JFreeChart chart, int defaultWidth, int defaultHeigth, int minDrawWidth, int minDrawHeigth, boolean zoomOnLinkAndBrush) {
		super(chart, defaultWidth, defaultHeigth, minDrawWidth, minDrawHeigth, DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT, DEFAULT_BUFFER_USED, 
				false,  // copy
				false,  // properties
				false,  // save
				false,  // print
				false,  // zoom
				true);   // tooltips

		this.zoomOnLinkAndBrushSelection = zoomOnLinkAndBrush;

		setMouseWheelEnabled(false);
	}

	/**
	 * Restores the auto-range calculation on both axes.
	 */
	public void restoreAutoBounds() {
		Plot plot = getChart().getPlot();
		if (plot == null) {
			return;
		}
		// here we tweak the notify flag on the plot so that only
		// one notification happens even though we update multiple
		// axes...
		boolean savedNotify = plot.isNotify();
		plot.setNotify(false);

		if (plot instanceof LinkAndBrushPlot) {

			LinkAndBrushPlot LABPlot = (LinkAndBrushPlot) plot;
			
			List<Pair<Integer, Range>> zoomedDomainAxisRanges = new LinkedList<Pair<Integer, Range>>();
			List<Pair<Integer, Range>> zoomedRangeAxisRanges = new LinkedList<Pair<Integer, Range>>();

			zoomedDomainAxisRanges.addAll(LABPlot.restoreAutoDomainAxisBounds(zoomOnLinkAndBrushSelection));
			zoomedRangeAxisRanges.addAll(LABPlot.restoreAutoRangeAxisBounds(zoomOnLinkAndBrushSelection));
			
			informLinkAndBrushSelectionListeners(new LinkAndBrushSelection(SelectionType.RESTORE_AUTO_BOUNDS, zoomedDomainAxisRanges, zoomedRangeAxisRanges));

		} else {
			restoreAutoDomainBounds();
			restoreAutoRangeBounds();
		}

		plot.setNotify(savedNotify);
	}

	@Override
	public void zoom(Rectangle2D selection) {
		// get the origin of the zoom selection in the Java2D space used for
		// drawing the chart (that is, before any scaling to fit the panel)
		Point2D selectOrigin = translateScreenToJava2D(new Point((int) Math.ceil(selection.getX()), (int) Math.ceil(selection.getY())));
		PlotRenderingInfo plotInfo = getChartRenderingInfo().getPlotInfo();
		Rectangle2D scaledDataArea = getScreenDataArea((int) selection.getCenterX(), (int) selection.getCenterY());
		if ((selection.getHeight() > 0) && (selection.getWidth() > 0)) {

			double hLower = (selection.getMinX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			double hUpper = (selection.getMaxX() - scaledDataArea.getMinX()) / scaledDataArea.getWidth();
			double vLower = (scaledDataArea.getMaxY() - selection.getMaxY()) / scaledDataArea.getHeight();
			double vUpper = (scaledDataArea.getMaxY() - selection.getMinY()) / scaledDataArea.getHeight();

			Plot p = getChart().getPlot();
			if (p instanceof LinkAndBrushPlot) {

				PlotOrientation orientation = null;
				if (p instanceof XYPlot) {
					XYPlot xyPlot = (XYPlot) p;
					orientation = xyPlot.getOrientation();
				}
				if (p instanceof CategoryPlot) {
					CategoryPlot categoryPlot = (CategoryPlot) p;
					orientation = categoryPlot.getOrientation();
				}

				// here we tweak the notify flag on the plot so that only
				// one notification happens even though we update multiple
				// axes...

				boolean savedNotify = p.isNotify();
				p.setNotify(false);
				LinkAndBrushPlot LABPlot = (LinkAndBrushPlot) p;

				List<Pair<Integer, Range>> zoomedDomainAxisRanges = new LinkedList<Pair<Integer, Range>>();
				List<Pair<Integer, Range>> zoomedRangeAxisRanges = new LinkedList<Pair<Integer, Range>>();

				if (orientation == PlotOrientation.HORIZONTAL) {
					zoomedDomainAxisRanges.addAll(LABPlot.calculateDomainAxesZoom(vLower, vUpper, zoomOnLinkAndBrushSelection));
					zoomedRangeAxisRanges.addAll(LABPlot.calculateRangeAxesZoom(hLower, hUpper, plotInfo, selectOrigin, zoomOnLinkAndBrushSelection));
				} else {
					zoomedDomainAxisRanges.addAll(LABPlot.calculateDomainAxesZoom(hLower, hUpper, zoomOnLinkAndBrushSelection));
					zoomedRangeAxisRanges.addAll(LABPlot.calculateRangeAxesZoom(vLower, vUpper, plotInfo, selectOrigin, zoomOnLinkAndBrushSelection));
				}
				p.setNotify(savedNotify);

				informLinkAndBrushSelectionListeners(new LinkAndBrushSelection(SelectionType.ZOOM_IN, zoomedDomainAxisRanges, zoomedRangeAxisRanges));

			} else {
				super.zoom(selection);
			}
		}
	}

	/**
	 * The listener is saved as a {@link WeakReference}. Thus listener must not be hidden classes!
	 */
	public void addLinkAndBrushSelectionListener(LinkAndBrushSelectionListener l) {
		listeners.add(new WeakReference<LinkAndBrushSelectionListener>(l));
	}

	public void removeLinkAndBrushSelectionListener(LinkAndBrushSelectionListener l) {
		listeners.remove(l);
	}

	private void informLinkAndBrushSelectionListeners(LinkAndBrushSelection e) {
		Iterator<WeakReference<LinkAndBrushSelectionListener>> it = listeners.iterator();
		while (it.hasNext()) {
			WeakReference<LinkAndBrushSelectionListener> wrl = it.next();
			LinkAndBrushSelectionListener l = wrl.get();
			if (l != null) {
				l.selectedLinkAndBrushRectangle(e);
			} else {
				it.remove();
			}
		}
	}
}
