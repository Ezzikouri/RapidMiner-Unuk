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

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.repository.RepositoryException;
import com.rapidminer.repository.RepositoryManager;

/**
 * An Operator to move repository entries to another repository location. If the destination folder does not exists yet it is created recursively.
 * The user can select the entry that should be moved, a new location for the entry and whether overwriting should be allowed or not.
 * If overwriting is not allowed (default case) a user error is thrown if there already exists an element with the same name at the desired new location. 
 * 
 * @author Nils Woehler
 *
 */
public class RepositoryEntryMoveOperator extends AbstractRepositoryEntryRelocationOperator {

	public RepositoryEntryMoveOperator(OperatorDescription description) {
		super(description);
	}

	@Override
	public void doWork() throws OperatorException {

		super.doWork();
		
		RepositoryManager repoMan = RepositoryManager.getInstance(null);

		try {
			// move repository element to new destination
			repoMan.move(getFromRepositoryLocation(), getDestinationFolder(), getDestinationName(), null);
		} catch (RepositoryException e) {
			throw new UserError(this, e, "repository_management.move_repository_entry", getFromRepositoryLocation(), e.getMessage());
		}
	}
}
