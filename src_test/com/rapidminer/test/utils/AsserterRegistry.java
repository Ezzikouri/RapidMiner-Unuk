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

import com.rapidminer.tools.container.Pair;

/**
 * @author Marius Helf
 *
 */
public class AsserterRegistry {
	private List<Pair<Class, Asserter>> registeredAsserters = new LinkedList<Pair<Class,Asserter>>();
	
	public void registerAsserter(Class clazz, Asserter asserter) {
		registeredAsserters.add(new Pair<Class, Asserter>(clazz, asserter));
	}
	
	public <T> Asserter<? super T> getAsserterForObject(T object) {
		for(Pair<Class, Asserter> classAndAsserter : registeredAsserters) {
			if (classAndAsserter.getFirst().isInstance(object) ) {
				return (Asserter<? super T>)classAndAsserter.getSecond();
			}
		}
		return null; 
	}
	
	public <T> Asserter<? super T> getAsserterForObjects(T o1, T o2) {
		for(Pair<Class, Asserter> classAndAsserter : registeredAsserters) {
			Class clazz = classAndAsserter.getFirst();
			if (clazz.isInstance(o1) && clazz.isInstance(o2)) {
				return (Asserter<? super T>)classAndAsserter.getSecond();
			}
		}
		return null; 
	}
	
	public <T> Asserter<? super T> getAsserterForClass(Class<T> clazz) {
		for(Pair<Class, Asserter> classAndAsserter : registeredAsserters) {
			if (classAndAsserter.getFirst().isAssignableFrom(clazz) ) {
				return (Asserter<? super T>)classAndAsserter.getSecond();
			}
		}
		return null; 
	}
}
