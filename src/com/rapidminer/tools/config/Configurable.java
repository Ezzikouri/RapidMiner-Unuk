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

import java.util.Map;

import com.rapidminer.repository.remote.RemoteRepository;

/**
 * Interface, describing objects which can be listed and configured through a {@link ConfigurationDialog}.
 * @author Simon Fischer, Dominik Halfkann
 *
 */
public interface Configurable {

	/** Sets the user defined unique name. */
	public void setName(String name);
	
	/** Gets the user defined unique name. */
	public String getName();
	
	/** Sets the given parameters.
	 * @see #getParameters() */
	public void configure(Map<String, String> parameterValues);
	
	/** The parameter values representing this Configurable. 
	 * @see #configure(Map) */
	public Map<String,String> getParameters();
	
	/** If this configurable was loaded from a RapidAnalytics instance, this is the connection
	 *  it was loaded from. May be null for local entries. */
	public RemoteRepository getSource();

	/** Set when this configurable was loaded from a RapidAnalytics instance. */
	public void setSource(RemoteRepository source);
	
	/** Gets the user defined short info which will be shown in the list on the left */
	public String getShortInfo();
	
	/** Sets the parameter value for the given key **/
	public void setParameter(String key, String value);
	
	/** Gets the parameter value for the given key **/
	public String getParameter(String key);
	
	/** Compares the name and the parameter values of this Configurable with a given Configurable **/
	public boolean hasSameValues(Configurable comparedConfigurable);

	/** Checks if the Configurable is empty (has no values/only empty values) **/
	public boolean isEmpty();
	
	/** Returns the type id of the corresponding {@link Configurator}. */
	public String getTypeId();
	
}
