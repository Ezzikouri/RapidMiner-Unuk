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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.gui.new_plotter.event.AxisParallelLineConfigurationChangeEvent;
import com.rapidminer.gui.new_plotter.listener.AxisParallelLineConfigurationListener;
import com.rapidminer.gui.new_plotter.listener.events.LineFormatChangeEvent;


/**
 * A class which configures a line which is parallel to one of the
 * plot axes.
 * 
 * @author Marius Helf
 *
 */
public class AxisParallelLineConfiguration implements LineFormatListener, Cloneable {
	LineFormat format = new LineFormat();
	private boolean labelVisible = true;
	private double value;
	
	List<WeakReference<AxisParallelLineConfigurationListener>> listeners;
	
	
	/**
	 * Creates a new {@link AxisParallelLineConfiguration}.
	 */
	public AxisParallelLineConfiguration(double value, boolean labelVisible) {
		if (this.value != value) {
			this.value = value;
//			fireAxisParallelLineConfigurationChanged(new AxisParallelLineConfigurationChangeEvent(this, value));
		}
	}


	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public boolean isLabelVisible() {
		return labelVisible;
	}


	public LineFormat getFormat() {
		return format;
	}


	public void setLabelVisible(boolean labelVisible) {
		if (labelVisible != this.labelVisible) {
			this.labelVisible = labelVisible;
			fireAxisParallelLineConfigurationChanged(new AxisParallelLineConfigurationChangeEvent(this, labelVisible));
		}
	}


	@Override
	public void lineFormatChanged(LineFormatChangeEvent e) {
		fireAxisParallelLineConfigurationChanged(new AxisParallelLineConfigurationChangeEvent(this, e));
	}


	private void fireAxisParallelLineConfigurationChanged(AxisParallelLineConfigurationChangeEvent e) {
		Iterator<WeakReference<AxisParallelLineConfigurationListener>> it = listeners.iterator();
		while (it.hasNext()) {
			AxisParallelLineConfigurationListener l = it.next().get();
			if (l != null) {
				l.axisParallelLineConfigurationChanged(e);
			} else {
				it.remove();
			}
		}
	}
	
	@Override
	public AxisParallelLineConfiguration clone() {
		AxisParallelLineConfiguration clone = new AxisParallelLineConfiguration(this.value, this.labelVisible);
		clone.format = format.clone();
		return clone;
	}
}
