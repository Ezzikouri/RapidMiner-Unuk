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
package com.rapidminer.tools.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rapidminer.io.process.XMLTools;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.repository.RepositoryAccessor;
import com.rapidminer.tools.I18N;
import com.rapidminer.tools.LogService;

/** Singleton to access configurable items and to provide means to configure them by the user.
 *
 * @author Simon Fischer
 *
 */
public abstract class ConfigurationManager {

	//private static Class<? extends ConfigurationManager> implementationClass = ClientConfigurationManager.class;
	private static ConfigurationManager theInstance;
		
	/** Map from {@link Configurator#getTypeId()} to {@link Configurator}. */
	private Map<String,Configurator<? extends Configurable>> configurators = new TreeMap<String,Configurator<? extends Configurable>>();
	
	/** Private singleton constructor. */
	protected ConfigurationManager() { 
	}

	private Map<String,Map<String,Configurable>> configurables = new HashMap<String, Map<String,Configurable>>();

	private boolean initialized = false;
	
	public static synchronized void setInstance(ConfigurationManager manager) {
		if (theInstance != null) {
			throw new RuntimeException("Configuration manager already set.");
		}
		ConfigurationManager.theInstance = manager;
	}
	
	public static synchronized ConfigurationManager getInstance() {
		if (theInstance == null) {			
			theInstance = new ClientConfigurationManager();			
		}
		return theInstance;
	}
	
	/** Loads all parameters from a configuation file or database. 
	 * @throws ConfigurationException */
	protected abstract Map<String, Map<String, String>> loadAllParameters(Configurator<?> configurator) throws ConfigurationException;
	
	/** Registers a new {@link Configurator}. Will create GUI actions and JSF pages to configure it. */
	public synchronized void register(Configurator<? extends Configurable> configurator) {
		if (configurator == null) {
			throw new NullPointerException("Registered configurator is null.");
		}
		LogService.getRoot().log(Level.INFO, "com.rapidminer.tools.config.ConfigurationManager.registered", configurator.getName());
		final String typeId = configurator.getTypeId();
		if (typeId == null) {
			throw new RuntimeException("typeID must not be null for "+configurator.getClass()+"!");
		}
		configurators.put(typeId, configurator);
		configurables.put(typeId, new TreeMap<String, Configurable>());
	}

	/** Returns the {@link Configurator} with the given {@link Configurator#getTypeId()}. */
	public Configurator<? extends Configurable> getConfigurator(String typeId) {
		return configurators.get(typeId);
	}
	
	/** Returns all registered {@link Configurator#getTypeId()}s. */
	public List<String> getAllTypeIds() {
		List<String> result = new LinkedList<String>();
		result.addAll(configurators.keySet());
		return result ;
	}
	
	public List<String> getAllConfigurableNames(String typeId) {
		Map<String, Configurable> configurablesForType = configurables.get(typeId);
		if (configurablesForType == null) {
			throw new IllegalArgumentException("Unknown configurable type: "+typeId);
		}
		return new LinkedList<String>(configurablesForType.keySet());
	}
	
	/** Looks up a {@link Configurable} of the given type. 
	 * @param typeId must be one of {@link #getAllTypeIds()} 
	 * @param name must be a {@link Configurable#getName()} where {@link Configurable} is registered under the given type.
	 * @param accessor represents the user accessing the repository. Can and should be taken from {@link com.rapidminer.Process#getRepositoryAccessor()}. 
	 * @throws ConfigurationException */
	public Configurable lookup(String typeId, String name, RepositoryAccessor accessor) throws ConfigurationException {
		checkAccess(typeId, name, accessor);
		Map<String, Configurable> nameToConfigurable = configurables.get(typeId);
		if (nameToConfigurable == null) {
			throw new ConfigurationException("No such configuration type: "+typeId);
		}
		Configurable result = nameToConfigurable.get(name);
		if (result == null) {
			Configurator configurator = configurators.get(typeId);
			throw new ConfigurationException("No such configured object of name "+name+" of "+configurator.getName());
		}
		return result;
	}
	
	/** Checks access to the {@link Configurable} with the given type and name. 
	 *  If access is permitted, throws. The default implementation does nothing (everyone can access everything).
	 */
	protected void checkAccess(String typeId, String name, RepositoryAccessor accessor) throws ConfigurationException {
	}

	/** Adds the configurable to internal maps. Once they are added, they can be obtained via
	 *  {@link #lookup(String, String, RepositoryAccessor)}. */
	public void registerConfigurable(String typeId, Configurable configurable) throws ConfigurationException {
		Map<String,Configurable> configurablesForType = configurables.get(typeId);
		if (configurablesForType == null) {
			throw new ConfigurationException("No such configuration type: "+typeId);
		}
		configurablesForType.put(configurable.getName(), configurable);
	}
	
	public void initialize() {
		if (initialized) {
			return;
		}
		//TODO: Observe refreshed connections to RA. Fetch configurables from there.
//		RepositoryManager.getInstance(null).addObserver(new Observer<Repository>() {			
//			@Override
//			public void update(Observable<Repository> observable, Repository repos) {
//				observeRepos
//			}
//		}, false);
		loadConfiguration();
		initialized = true;
	}
	/** Loads all configurations from the configuration database or file. */
	private void loadConfiguration()  {		
		for (Configurator<? extends Configurable> configurator : configurators.values()) {
			LogService.getRoot().log(Level.INFO, "com.rapidminer.tools.config.ConfigurationManager.loading_configuration", configurator.getName());
			Map<String, Map<String, String>> parameters;
			try {
				parameters = loadAllParameters(configurator);
			} catch (ConfigurationException e1) {
				//LogService.getRoot().log(Level.WARNING, "Failed to load configuration for "+configurator.getName()+": "+e1, e1);
				LogService.getRoot().log(Level.WARNING,
						I18N.getMessage(LogService.getRoot().getResourceBundle(), 
								"com.rapidminer.tools.config.ConfigurationManager.loading_configuration_error", 
								configurator.getName(), e1),
						e1);
				continue;
			}
			for (Entry<String, Map<String, String>> entry : parameters.entrySet()) {
				try {
					Map<String,String> translated = new HashMap<String, String>();
					Map<String,ParameterType> types = parameterListToMap(configurator.getParameterTypes());
					for (Entry<String, String> parameter : entry.getValue().entrySet()) {
						String paramKey = parameter.getKey();
						String paramValue = parameter.getValue();
						ParameterType type = types.get(paramKey);
						if ((paramValue == null) && (type != null)) {
							paramValue = type.getDefaultValueAsString();							
						}
						translated.put(paramKey, paramValue);
					}
					Configurable configurable = configurator.create(entry.getKey(), translated);
					registerConfigurable(configurator.getTypeId(), configurable);
				} catch (ConfigurationException e) {
					//LogService.getRoot().log(Level.WARNING, "Failed to configure configurable of type: "+configurator.getName()+": "+e, e);		
					LogService.getRoot().log(Level.WARNING,
							I18N.getMessage(LogService.getRoot().getResourceBundle(), 
									"com.rapidminer.tools.config.ConfigurationManager.configuring_configurable_error", 
									configurator.getName(), e),
							e);
				}
			}
			//LogService.getRoot().info("Loaded configurations for "+configurables.get(configurator.getTypeId()).size()+" objetcs of type "+configurator.getName()+".");
			LogService.getRoot().log(Level.INFO, "com.rapidminer.tools.config.ConfigurationManager.loaded_configurations", 
					new Object[] {configurables.get(configurator.getTypeId()).size(), configurator.getName()});
		}
	}
	
	public Configurable create(String typeId, String name) throws ConfigurationException {
		Configurator<? extends Configurable> configurator = configurators.get(typeId);
		if (configurator == null) {
			throw new ConfigurationException("Unknown configurable type: "+typeId);
		}
		final Configurable configurable = configurator.create(name, Collections.<String,String>emptyMap());
		registerConfigurable(typeId, configurable);
		return configurable;
	}
	
	/** Saves the configuration, e.g. when RapidMiner exits. 
	 * @throws ConfigurationException */
	public void saveConfiguration() throws ConfigurationException {
		for (String typeId : getAllTypeIds()) {
			//Configurator configurator = getConfigurator(typeId);
			saveConfiguration(typeId);
		}
	}
	
	/** Saves one configuration with the given typeID 
	 * @throws ConfigurationException */
	public abstract void saveConfiguration(String typeId) throws ConfigurationException;

	private Map<String, ParameterType> parameterListToMap(List<ParameterType> parameterTypes) {
		Map<String, ParameterType> result = new HashMap<String, ParameterType>();
		for (ParameterType type : parameterTypes) {
			result.put(type.getKey(), type);
		}
		return result;
	}	
	
	public void removeConfigurable(String typeId, String identifier) {
		configurables.get(typeId).remove(identifier);
	}
	
	public Document getConfigurablesAsXML(Configurator configurator) {
		Document doc = XMLTools.createDocument();
		Element root = doc.createElement("configuration");
		doc.appendChild(root);
		for (Configurable configurable : configurables.get(configurator.getTypeId()).values()) {
			root.appendChild(toXML(doc, configurator, configurable));
		}
		return doc;
	}
	
	public static Element toXML(Document doc, Configurator configurator, Configurable configurable) {
		Element element = doc.createElement(configurator.getTypeId());
		element.setAttribute("name", configurable.getName());
		for (Entry<String, String> param : configurable.getParameters().entrySet()) {			
			Element paramElement = doc.createElement(param.getKey());
			paramElement.appendChild(doc.createTextNode(param.getValue().toString()));
			element.appendChild(paramElement);
		}		
		return element;
	}
	
	public Map<String, Map<String, String>> fromXML(Document doc, Configurator configurator) throws ConfigurationException {		
		Map<String, Map<String, String>> result = new TreeMap<String, Map<String,String>>();
		Element root = doc.getDocumentElement();
		if (!"configuration".equals(root.getTagName())) {
			throw new ConfigurationException("XML root tag must be <configuration>");
		}
		
		for (Element element : XMLTools.getChildElements(root, configurator.getTypeId())) {
			String name = element.getAttribute("name");
			HashMap<String,String> parameters = new HashMap<String, String>();
			for (Element paramElem : XMLTools.getChildElements(element)) {
				String key = paramElem.getTagName();
				String value = paramElem.getTextContent();
				parameters.put(key, value);
			}
			result.put(name, parameters);
		}		
		return result;
	}
	
}
