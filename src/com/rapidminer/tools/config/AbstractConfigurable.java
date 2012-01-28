package com.rapidminer.tools.config;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.repository.remote.RemoteRepository;

/**
 * 
 * @author Simon Fischer
 *
 */
public abstract class AbstractConfigurable implements Configurable {

	private String name;
	private Map<String,Object> parameters = new HashMap<String, Object>();
	private RemoteRepository source;
	
	public Object getParameter(String key) {
		return parameters.get(key);
	}
	
	public void setParameter(String key, Object value) {
		parameters.put(key, value);
	}
	@Override
	public void configure(Map<String,Object> parameters) {
		this.parameters.clear();		
		this.parameters.putAll(parameters);
	}
	@Override
	public Map<String,Object> getParameters() {
		return parameters;
	}
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void setSource(RemoteRepository source) {
		this.source = source;
	}
	
	@Override
	public RemoteRepository getSource() {
		return source;
	}
}
