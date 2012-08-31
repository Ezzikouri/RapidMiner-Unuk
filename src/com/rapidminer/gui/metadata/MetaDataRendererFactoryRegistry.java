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
