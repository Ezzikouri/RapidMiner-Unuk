package com.rapidminer.tools.config;

import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.rapidminer.parameter.ParameterType;

/** Can be used to configure {@link Configurable}s. The {@link ConfigurationManager} will take
 *  care of saving the configuration to configuration files or to a database and to provide access
 *  to dialogs which can be used to edit these configurables.   
 * 
 * @author Simon Fischer
 */
public abstract class Configurator<T extends Configurable> {

	/** Parameter types used to configure this configurator. Values will be passed to {@link #configure(Map)}. */
	public abstract List<ParameterType> getParameterTypes();
	
	public abstract Class<T> getConfigurableClass();
	
	/** Creates a new {@link Configurable} based on parameters. The parameters passed to this method match the
	 *  ones specified by {@link #getParameterTypes()}. 
	 * @throws ConfigurationException 
	 *  @name a unique (user defined) name identifying this {@link Configurable}. */
	public T create(String name, Map<String,Object> parameters) throws ConfigurationException {
		T instance;
		try {
			instance = getConfigurableClass().newInstance();
			instance.setName(name);
			instance.configure(parameters);
		} catch (InstantiationException e) {
			throw new ConfigurationException("Cannot instantiate "+getConfigurableClass(), e);
		} catch (IllegalAccessException e) {
			throw new ConfigurationException("Cannot instantiate "+getConfigurableClass(), e);
		}
		return instance;
	}
	
	/** The ID used for identifying this Configurator. Must be a valid XML tag identifier and
	 *  file name. Should include the plugin namespace. 
	 *  Example: "olap_connection". */
	public abstract String getTypeId();

	/** The display name used in UI components. */
	public abstract String getName();
	
	/** A short help text to be used in dialogs. */
	public abstract String getDescription();
	
	/** Creates a new panel which is used to configure a {@link Configurable}. Initializes
	 *  all components with the current parameters of the configurable. */
	public JComponent createConfigurationPanel(T configurable) {
		return new JPanel();
	}
}
