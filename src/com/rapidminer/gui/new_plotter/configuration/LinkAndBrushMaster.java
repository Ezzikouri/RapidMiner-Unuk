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
package com.rapidminer.gui.new_plotter.configuration;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.Range;

import com.rapidminer.gui.new_plotter.PlotConfigurationError;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelection;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelection.SelectionType;
import com.rapidminer.gui.new_plotter.engine.jfreechart.link_and_brush.listener.LinkAndBrushSelectionListener;
import com.rapidminer.tools.container.Pair;

/**
 * @author Nils Woehler
 * 
 */
public class LinkAndBrushMaster implements LinkAndBrushSelectionListener {

	private final PlotConfiguration plotConfig;

	private boolean zoomedIn = false;

	private Map<Integer, Range> rangeAxisIndexToZoomMap = new HashMap<Integer, Range>();
	private Range domainAxisZoom;

	public LinkAndBrushMaster(PlotConfiguration plotConfig) {
		this.plotConfig = plotConfig;
	}

	public List<PlotConfigurationError> getErrors() {
		List<PlotConfigurationError> errors = new LinkedList<PlotConfigurationError>();

		return errors;
	}

	public List<PlotConfigurationError> getWarnings() {
		List<PlotConfigurationError> warnings = new LinkedList<PlotConfigurationError>();

		if (zoomedIn) {
			warnings.add(new PlotConfigurationError("zoomed_in"));
		}

		return warnings;
	}

	public boolean isZoomedIn() {
		return zoomedIn;
	}

	public void clearZooming() {
		zoomedIn = false;
		domainAxisZoom = null;
		rangeAxisIndexToZoomMap.clear();
	}

	public void clearRangeAxisZooming() {
		rangeAxisIndexToZoomMap.clear();
		zoomedIn = (domainAxisZoom != null);
	}

	public void clearDomainAxisZooming() {
		domainAxisZoom = null;
		zoomedIn = (rangeAxisIndexToZoomMap.keySet().size() > 0);
	}

	/**
	 * Returns <code>null</code> if isZoomedIn() returns <code>false</code>.
	 */
	public Range getDomainZoom() {
		return domainAxisZoom;
	}

	/**
	 * Returns <code>null</code> if isZommedIn() returns <code>false</code> or if checking for a at
	 * zoom time unknown {@link RangeAxisConfig}.
	 */
	public Range getRangeAxisZoom(RangeAxisConfig rangeAxisConfig, PlotConfiguration plotConfig) {
		int indexOf = plotConfig.getIndexOfRangeAxisConfigById(rangeAxisConfig.getId());
		return rangeAxisIndexToZoomMap.get(indexOf);
	}

	@Override
	public void selectedLinkAndBrushRectangle(LinkAndBrushSelection e) {
		if (e.getType() == SelectionType.ZOOM_IN) {
			zoomedIn = true;

			// fetch domain axis range
			Pair<Integer, Range> domainAxisRange = e.getDomainAxisRange();
			if (domainAxisRange != null) {
				domainAxisZoom = domainAxisRange.getSecond();
			}

			// fetch range axis config ranges
			List<RangeAxisConfig> rangeAxisConfigs = plotConfig.getRangeAxisConfigs();
			List<Pair<Integer, Range>> valueAxisRanges = e.getValueAxisRanges();
			if (valueAxisRanges.size() > 0) {
				for (Pair<Integer, Range> newRangeAxisRangePair : valueAxisRanges) {
					RangeAxisConfig rangeAxisConfig = rangeAxisConfigs.get(newRangeAxisRangePair.getFirst());
					int indexOf = plotConfig.getIndexOfRangeAxisConfigById(rangeAxisConfig.getId());
					rangeAxisIndexToZoomMap.put(indexOf, newRangeAxisRangePair.getSecond());
				}
			}

		}

		if (e.getType() == SelectionType.RESTORE_AUTO_BOUNDS) {
			clearZooming();
		}
	}
	
	protected LinkAndBrushMaster clone(PlotConfiguration plotConfig) {
		LinkAndBrushMaster clone = new LinkAndBrushMaster(plotConfig);
		
		clone.domainAxisZoom = this.domainAxisZoom;
		
		Map<Integer, Range> clonedRangeAxisIndexToZoomMap = new HashMap<Integer, Range>();
		
		for(Integer key : rangeAxisIndexToZoomMap.keySet()) {
			Range value = rangeAxisIndexToZoomMap.get(key);
			clonedRangeAxisIndexToZoomMap.put(key, new Range(value.getLowerBound(), value.getUpperBound()));
		}
		clone.rangeAxisIndexToZoomMap = clonedRangeAxisIndexToZoomMap; 
		
		clone.zoomedIn = this.zoomedIn;
		
		return clone;
	}

}
