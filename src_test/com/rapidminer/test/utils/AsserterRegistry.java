/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2011 by Rapid-I and the contributors
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

package com.rapidminer.test.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Marius Helf
 *
 */
public class AsserterRegistry {
	private List<Asserter> registeredAsserters = new LinkedList<Asserter>();
	
	public void registerAsserter(Asserter asserter) {
		registeredAsserters.add(asserter);
	}
	
	public Asserter getAsserterForObject(Object object) {
		for(Asserter asserter : registeredAsserters) {
			if (asserter.getAssertable().isInstance(object) ) {
				return asserter;
			}
		}
		return null; 
	}
	
	public Asserter getAsserterForObjects(Object o1, Object o2) {
		for(Asserter asserter: registeredAsserters) {
			Class clazz = asserter.getAssertable();
			if (clazz.isInstance(o1) && clazz.isInstance(o2)) {
				return asserter;
			}
		}
		return null; 
	}
	
	public Asserter getAsserterForClass(Class clazz) {
		for(Asserter asserter : registeredAsserters) {
			if (asserter.getAssertable().isAssignableFrom(clazz) ) {
				return asserter;
			}
		}
		return null; 
	}
}
