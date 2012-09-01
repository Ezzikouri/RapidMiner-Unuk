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
package com.rapidminer.gui.metadata;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import com.rapidminer.gui.flow.ExampleSetMetaDataTableModel;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.operator.ports.metadata.MetaData;

/** Subclasses of {@link MetaDataRendererFactory} can register themselves here.
 * 
 * @author Simon Fischer, Gabor Makrai
 *
 */
public class MetaDataRendererFactoryRegistry {
	
	private Map<Class<? extends MetaData>,MetaDataRendererFactory> factories = new HashMap<Class<? extends MetaData>, MetaDataRendererFactory>();

	private static final MetaDataRendererFactoryRegistry INSTANCE = new MetaDataRendererFactoryRegistry();
	static {
		getInstance().register(new MetaDataRendererFactory() {
			@Override
			public Class<? extends MetaData> getSupportedClass() {
				return ExampleSetMetaData.class;
			}
			
			@Override
			public Component createRenderer(MetaData metaData) {
				return ExampleSetMetaDataTableModel.makeTableForToolTip((ExampleSetMetaData) metaData);
			}
		});
	}
	
	/** Gets the singleton instance. */
	public static MetaDataRendererFactoryRegistry getInstance() {
		return INSTANCE;
	}
	
	/** Registers a new factory. */
	public void register(MetaDataRendererFactory factory) {
		factories.put(factory.getSupportedClass(), factory);
	}
	
	/** Creates a renderer for this meta data object or null if there is no suitable renderer
	 *  or if the meta data is null. */
	public Component createRenderer(MetaData metaData) {
		if (metaData == null) {
			return null;
		}
		MetaDataRendererFactory f = factories.get(metaData.getClass());
		if (f != null) {
			return f.createRenderer(metaData);
		} else {
			return null;
		}
	}
}
