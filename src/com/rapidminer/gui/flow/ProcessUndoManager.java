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
package com.rapidminer.gui.flow;

import java.util.LinkedList;

import com.rapidminer.Process;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.tools.container.Pair;

/**
 * Handles the undo system for the current {@link MainFrame} {@link Process}. Operations are <i>not</i> synchronized.
 * 
 * @author Marco Boeck
 */
public class ProcessUndoManager {

	private final LinkedList<Pair<String, OperatorChain>> undoList;
	
	
	/**
	 * Standard constructor.
	 */
	public ProcessUndoManager() {
		undoList = new LinkedList<Pair<String, OperatorChain>>();
	}
	
	/**
	 * Resets the undo list and discards all stored entries.
	 */
	public void reset() {
		undoList.clear();
	}
	
	/**
	 * Gets the number of undos currently stored.
	 * @return
	 */
	public int getNumberOfUndos() {
		return undoList.size();
	}
	
	/**
	 * Gets the undo step with the given index or <code>null</code>.
	 * @param index
	 * @return
	 */
	public String getXml(int index) {
		try {
			return undoList.get(index).getFirst();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Gets the {@link OperatorChain} associated with the undo step with the given index or <code>null</code>.
	 * @param index
	 * @return
	 */
	public OperatorChain getOperatorChain(int index) {
		try {
			return undoList.get(index).getSecond();
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	/**
	 * Removes the last undo step. If there is none, does nothing.
	 */
	public void removeLast() {
		if (undoList.size() > 0) {
			undoList.removeLast();
		}
	}
	
	/**
	 * Removes the first undo step. If there is none, does nothing.
	 */
	public void removeFirst() {
		if (undoList.size() > 0) {
			undoList.removeFirst();
		}
	}
	
	/**
	 * Adds an undo step.
	 * @param processXml
	 * @param currentlyShownOperatorChain
	 */
	public void add(String processXml, OperatorChain currentlyShownOperatorChain) {
		if (processXml == null) {
			throw new IllegalArgumentException("processXml must not be null!");
		}
		undoList.add(new Pair<String, OperatorChain>(processXml, currentlyShownOperatorChain));
	}
	
//	/**
//	 * Overwrites the {@link OperatorChain} stored for the given index.
//	 * If the given index is invalid, does nothing.
//	 * @param index
//	 * @param currentlyShownOperatorChain
//	 */
//	public void overwriteUndoOperatorChain(int index, OperatorChain currentlyShownOperatorChain) {
//		try {
//			undoList.get(index).setSecond(currentlyShownOperatorChain);
//		} catch (IndexOutOfBoundsException e) {}
//	}
}
