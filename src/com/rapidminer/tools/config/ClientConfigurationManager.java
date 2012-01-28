package com.rapidminer.tools.config;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Document;

import com.rapidminer.io.process.XMLTools;
import com.rapidminer.tools.FileSystemService;
import com.rapidminer.tools.LogService;

/** Stores configurations in files (one file per {@link Configurator}.
 * 
 * @author Simon Fischer
 *
 */
public class ClientConfigurationManager extends ConfigurationManager {
	
	@Override
	protected Map<String, Map<String, String>> loadAllParameters(Configurator configurator) throws ConfigurationException {
		final File file = getConfigFile(configurator);
		if (!file.exists()) {
			LogService.getRoot().info("No configuration file found for "+configurator.getName());
			return Collections.emptyMap();
		}
		try {			
			return fromXML(XMLTools.parse(file), configurator);
		} catch (Exception e) {
			throw new ConfigurationException("Failed to read configuration file '"+file.getAbsolutePath()+"' for "+configurator.getName()+": "+e, e);
		}
	}

	@Override
	public void saveConfiguration() {
		for (String typeId : getAllTypeIds()) {
			Configurator configurator = getConfigurator(typeId);
			try {
				Document xml = getConfigurablesAsXML(configurator);
				File file = getConfigFile(configurator);
				XMLTools.stream(xml, file, null);
			} catch (Exception e) {
				LogService.getRoot().log(Level.WARNING, "Failed to save configurations for "+configurator.getName()+": "+e, e);
			}
		}		
	}

	/** Returns the config file in which this data will be saved. */
	private File getConfigFile(Configurator configurator) {
		return FileSystemService.getUserConfigFile("configurable-"+configurator.getTypeId()+".xml");
	}
}
