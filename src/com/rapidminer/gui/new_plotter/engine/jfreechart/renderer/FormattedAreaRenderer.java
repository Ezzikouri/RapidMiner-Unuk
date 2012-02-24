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
package com.rapidminer.gui.new_plotter.engine.jfreechart.renderer;

import java.awt.Paint;
import java.awt.Shape;

import org.jfree.chart.renderer.category.AreaRenderer;

import com.rapidminer.gui.new_plotter.engine.jfreechart.RenderFormatDelegate;

/**
 * @author Marius Helf
 */
public class FormattedAreaRenderer extends AreaRenderer implements FormattedRenderer {
	private static final long serialVersionUID = 1L;
	
	private RenderFormatDelegate formatDelegate = new RenderFormatDelegate();
	
	public FormattedAreaRenderer() {
		super();
	}

	@Override
	public RenderFormatDelegate getFormatDelegate() {
		return formatDelegate;
	}

	@Override
	public Paint getItemPaint(int seriesIdx, int valueIdx) {
		Paint paintFromDelegate = getFormatDelegate().getItemPaint(seriesIdx, valueIdx);
		if (paintFromDelegate == null) {
			return super.getItemPaint(seriesIdx, valueIdx);
		} else {
			return paintFromDelegate;
		}
	}

	@Override 
	public Shape getItemShape(int seriesIdx, int valueIdx) {
		Shape shapeFromDelegate = getFormatDelegate().getItemShape(seriesIdx, valueIdx);
		if (shapeFromDelegate == null) {
			return super.getItemShape(seriesIdx, valueIdx);
		} else {
			return shapeFromDelegate;
		}
	}


}
