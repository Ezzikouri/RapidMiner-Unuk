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

package com.rapidminer.operator.repository;

import java.util.List;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeRepositoryLocation;
import com.rapidminer.repository.Entry;
import com.rapidminer.repository.Folder;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryLocation;
import com.rapidminer.repository.RepositoryManager;

/**
 * An Operator to copy repository entries to another repository location. If the destination folder does not exists yet it is created recursively.
 * The user can select the entry that should be copied, a new location for the entry and whether overwriting should be allowed or not.
 * If overwriting is not allowed (default case) a user error is thrown if there already exists an element with the same name at the desired new location.
 * 
 * @author Nils Woehler
 *
 */
public class RepositoryEntryCopyOperator extends RepositoryManagerOperator {

	public static final String ELEMENT_TO_COPY = "file_to_copy";
	public static final String DESTINATION = "destination";
	public static final String OVERWRITE = "overwrite";

	public RepositoryEntryCopyOperator(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {
		RepositoryLocation repoLoc = getParameterAsRepositoryLocation(ELEMENT_TO_COPY);

		RepositoryManager repoMan = RepositoryManager.getInstance(repoLoc.getAccessor());

		boolean overwrite = getParameterAsBoolean(OVERWRITE);

		// fetch destination folder
		Folder destination;
		RepositoryLocation destinationRepoLoc = getParameterAsRepositoryLocation(DESTINATION);
		try {
			Entry destEntry = destinationRepoLoc.locateEntry();
			if(destEntry != null && !(destEntry instanceof Folder)) {
				throw new UserError(this, "311", destinationRepoLoc);
			}
			destination = (Folder) destEntry;
		} catch (RepositoryException e1) {
			throw new UserError(this, e1, "302", destinationRepoLoc, e1.getMessage());
		}

		// if destination folder does not exists, create a new one recursively
		if (destination == null) {
			try {
				destinationRepoLoc.createFoldersRecursively();
			} catch (RepositoryException e1) {
				throw new UserError(this, e1, "311", destinationRepoLoc);
			}
			try {
				destination = (Folder) destinationRepoLoc.locateEntry();
			} catch (RepositoryException e1) {
				throw new UserError(this, e1, "302", destinationRepoLoc, e1.getMessage());
			}
		}
		
		// finally try to copy the repository element to the new destination
		try {
			repoMan.copy(repoLoc, destination, null, overwrite);
		} catch (RepositoryException e) {
			throw new UserError(this, e, "repository_management.copy_repository_entry", repoLoc, e.getMessage());
		}

		super.doWork();
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeRepositoryLocation(ELEMENT_TO_COPY, "Entry that should be copied", true, true, false));
		types.add(new ParameterTypeRepositoryLocation(DESTINATION, "Copy destination folder", false, true, false));
		types.add(new ParameterTypeBoolean(OVERWRITE, "Overwrite entry at copy destination?", false, false));

		return types;
	}

}
