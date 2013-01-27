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
package com.rapidminer.operator.nio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.rapidminer.operator.nio.model.ColumnMetaData;
import com.rapidminer.operator.nio.model.DataResultSetTranslationConfiguration;
import com.rapidminer.operator.nio.model.ParsingError;

/**
 * Validates the MetaData set by the user in the MetaDataDeclarationWizardStep which is a part of importing e.g. an Excel file.
 * @author Dominik Halfkann
 */
public class MetaDataValidator {

	private DataResultSetTranslationConfiguration configuration;
	private MetaDataDeclarationWizardStep wizardStep;
	private List<ParsingError> errorList = new ArrayList<ParsingError>();
	
	public MetaDataValidator(MetaDataDeclarationWizardStep wizardStep,DataResultSetTranslationConfiguration configuration) {
		this.configuration = configuration;
		this.wizardStep = wizardStep;
	}

	public void validate() {
		errorList = new ArrayList<ParsingError>();
		ColumnMetaData[] metaData = configuration.getColumnMetaData();
		Map<String, List<Integer>> columnRoles = new HashMap<String, List<Integer>>();
		Map<String, List<Integer>> columnNames = new HashMap<String, List<Integer>>();
		
		int counter = 1;
		for (ColumnMetaData columnMetaData : metaData) {
			if (columnRoles.containsKey(columnMetaData.getRole())) {
				columnRoles.get(columnMetaData.getRole()).add(counter);
			} else {
				columnRoles.put(columnMetaData.getRole(), new ArrayList<Integer>(Arrays.asList((counter))));
			}
			if (columnNames.containsKey(columnMetaData.getUserDefinedAttributeName())) {
				columnNames.get(columnMetaData.getUserDefinedAttributeName()).add(counter);
			} else {
				columnNames.put(columnMetaData.getUserDefinedAttributeName(), new ArrayList<Integer>(Arrays.asList((counter))));
			}
			counter++;
		}
		
		for (Entry<String, List<Integer>> roleEntry : columnRoles.entrySet()) {
			if (roleEntry.getValue().size() > 1 && !roleEntry.getKey().equals("attribute")) {
				errorList.add(new ParsingError(roleEntry.getValue(), ParsingError.ErrorCode.SAME_ROLE_FOR_MULTIPLE_COLUMNS, roleEntry.getKey()));
			}
		}
		for (Entry<String, List<Integer>> nameEntry : columnNames.entrySet()) {
			if (nameEntry.getValue().size() > 1) {
				errorList.add(new ParsingError(nameEntry.getValue(), ParsingError.ErrorCode.SAME_NAME_FOR_MULTIPLE_COLUMNS, nameEntry.getKey()));
			}
		}
		
		wizardStep.updateErrors();
	}
	
	public List<ParsingError> getErrors() {
		return errorList;
	}
}
