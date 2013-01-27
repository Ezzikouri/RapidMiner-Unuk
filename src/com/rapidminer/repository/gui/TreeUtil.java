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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.rapidminer.repository.Folder;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.tools.LogService;

/**
 *  A utility class to save and restore expansion states and selection paths of the repository tree.
 * 
 * @author Nils Woehler
 *
 */
public class TreeUtil {

	private static Map<String, Boolean> pathToExpansionMap = new HashMap<String, Boolean>();
	private static TreePath selectedPath;
	private static TreePath expansionRoot;

	public static void saveSelectionPath(TreePath path) {
		selectedPath = path;
	}

	public static void restoreSelectionPath(JTree parentTree) {
		if (selectedPath != null) {
			parentTree.setSelectionPath(selectedPath);
		}
	}

	public static void saveExpansionState(JTree parentTree) {

		pathToExpansionMap.clear();

		selectedPath = parentTree.getSelectionPath();
		expansionRoot = parentTree.getSelectionPath();

		if (selectedPath != null) {
			Object lastPathComponent = selectedPath.getLastPathComponent();

//			if (lastPathComponent instanceof Repository) {
//				// special handling for repository because they are top level elements in the tree
//				// and a tree structure change TreeModelEvent on a top level element will collapse the whole JTree
//				// and therefore only storing the current selection is not sufficient
//				Object root = parentTree.getModel().getRoot();
//				int rootChildCount = parentTree.getModel().getChildCount(root);
//				// iterate over all elements in RepositoryTree
//				for (int i = 0; i < rootChildCount; i++) {
//					Object childRepo = parentTree.getModel().getChild(root, i);
//					// only interested in Repository
//					if (childRepo instanceof Repository) {
//						
//						if(childRepo instanceof RemoteRepository) {
//							RemoteRepository remote = (RemoteRepository) childRepo;
//							if(!remote.isConnected()) {
//								continue; // skip not connected remote repositories
//							}
//						}
//						
//						int repoChildCount = parentTree.getModel().getChildCount(childRepo);
//						// iterate over all elements in the repository
//						for (int j = 0; j < repoChildCount; j++) {
//							Object childFolder = parentTree.getModel().getChild(childRepo, j);
//							// only interested in Folder
//							if (childFolder instanceof Folder) {
//								try {
//									saveExpansionState(parentTree, expansionRoot, (Folder) childFolder);
//								} catch (RepositoryException e) {
//									// could not save expansion state. Do nothing here. It just can't be restored afterwards
//								}
//							}
//						}
//					}
//				}
//			} else
			if (lastPathComponent instanceof Folder) {
				Folder folder = (Folder) lastPathComponent; // Get the selected folder

				//check if parent of folder is still a folder
				Folder containingFolder = folder.getContainingFolder();
				if (containingFolder != null) { // start saving from parent folder
					expansionRoot = expansionRoot.getParentPath();
					folder = containingFolder;
				}

				try {
					saveExpansionState(parentTree, expansionRoot, folder);
				} catch (RepositoryException e) {
					// could not save expansion state. Do nothing here. It just can't be restored afterwards
				}
			}
		}

	}

	private static void saveExpansionState(JTree parentTree, TreePath path, Folder folder) throws RepositoryException {

		// save expansion state
		boolean expanded = parentTree.isExpanded(parentTree.getRowForPath(path));
		pathToExpansionMap.put(path.toString(), expanded);

		if (folder == null || !expanded) {
			return;
		}

		List<Folder> subfolders = folder.getSubfolders();
		if (subfolders.size() != 0) {
			for (Folder subfolder : subfolders) {
				TreePath nextPath = path.pathByAddingChild(subfolder);
				saveExpansionState(parentTree, nextPath, (Folder) nextPath.getLastPathComponent());
			}
		}
	}

	public static void restoreExpansionState(JTree parentTree) {
		if (selectedPath != null && expansionRoot != null) {
			try {
				Object lastPathComponent = expansionRoot.getLastPathComponent();
				if (lastPathComponent instanceof Folder) {
					restoreExpansionState(parentTree, expansionRoot, (Folder) lastPathComponent);
				}
			} catch (RepositoryException e) {
				LogService.getRoot().log(Level.INFO, "com.rapidminer.repository.gui.TreeUtil.could_not_restore_state");
			}
			parentTree.setSelectionPath(selectedPath);
		}
	}

	private static void restoreExpansionState(JTree parentTree, TreePath path, Folder folder) throws RepositoryException {
		Boolean wasExpanded = pathToExpansionMap.get(path.toString());
		if (wasExpanded != null && wasExpanded) {
			parentTree.expandPath(path);
		} else {
			return;
		}
		if (folder != null) {
			List<Folder> subfolders = folder.getSubfolders();
			if (subfolders.size() != 0) {
				for (Folder subfolder : subfolders) {
					TreePath nextPath = path.pathByAddingChild(subfolder);
					restoreExpansionState(parentTree, nextPath, (Folder) nextPath.getLastPathComponent());
				}
			}
		}
	}
}
