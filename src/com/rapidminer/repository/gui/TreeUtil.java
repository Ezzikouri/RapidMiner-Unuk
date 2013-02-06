/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2013 by Rapid-I and the contributors
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

package com.rapidminer.repository.gui;

import java.util.HashSet;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *  A utility class to save and restore expansion states and selection paths of the repository tree.
 * 
 * @author Nils Woehler
 *
 */
public class TreeUtil {

	private static TreePath selectedPath;
	private static HashSet<String> expandedNodes;

	public static void saveSelectionPath(TreePath path) {
		selectedPath = path;
	}

	public static void restoreSelectionPath(JTree parentTree) {
		if (selectedPath != null) {
			parentTree.setSelectionPath(selectedPath);
			parentTree.scrollPathToVisible(parentTree.getSelectionPath());
		}
	}
	
	public static void saveExpansionState(JTree tree) {

		expandedNodes = new HashSet<String>();
		
		TreeUtil.saveSelectionPath(tree.getSelectionPath());

		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath path = tree.getPathForRow(i);
			if (tree.isExpanded(path)) {
				expandedNodes.add(path.toString());
			}
		}
	}

	public static void restoreExpansionState(JTree tree) {

		for (int i = 0; i < tree.getRowCount(); i++) {
			TreePath path = tree.getPathForRow(i);
			if (expandedNodes.contains(path.toString())) {
				tree.expandPath(path);
			}
		}
		restoreSelectionPath(tree);
	}
}
