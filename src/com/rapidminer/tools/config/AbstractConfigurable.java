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
