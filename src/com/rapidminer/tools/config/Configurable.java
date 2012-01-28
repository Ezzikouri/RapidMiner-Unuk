package com.rapidminer.tools.config;

import java.util.Map;

import com.rapidminer.repository.remote.RemoteRepository;

/**
 * 
 * @author Simon Fischer
 *
 */
public interface Configurable {

	/** Sets the user defined unique name. */
	public void setName(String name);
	
	/** Gets the user defined unique name. */
	public String getName();
	
	/** Sets the given parameters.
	 * @see #getParameters() */
	public void configure(Map<String,Object> parameters);
	
	/** The parameter values representing this Configurable. 
	 * @see #configure(Map) */
	public Map<String,Object> getParameters();
	
	/** If this configurable was loaded from a RapidAnalytics instance, this is the connection
	 *  it was loaded from. May be null for local entries. */
	public RemoteRepository getSource();

	/** Set when this configurable was loaded from a RapidAnalytics instance. */
	public void setSource(RemoteRepository source);
	
}
