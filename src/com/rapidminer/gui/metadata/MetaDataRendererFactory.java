package com.rapidminer.gui.metadata;

import java.awt.Component;

import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.repository.gui.ToolTipProviderHelper;

/** Provides a custom renderer added by the {@link ToolTipProviderHelper}
 *  for subclasses of {@link MetaData}.
 * 
 * @author Simon Fischer, Gabor Makrai
 *
 */
public interface MetaDataRendererFactory {

	public Class<? extends MetaData> getSupportedClass();
	
	public Component createRenderer(MetaData metaData);
}
