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
package com.rapidminer.gui.new_plotter.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

import com.rapidminer.Process;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.XMLException;

/**
 * This is a utility class which can transform {@link ExampleSet}s for various needs.
 * 
 * @author Marco Boeck
 *
 */
public class DataTransformation {
	
	/**
	 * Creates a de-pivotized meta data {@link ExampleSet} from a given {@link ExampleSet}.
	 * This set de-pivots the given numerical attributes.
	 * @param exampleSet the original {@link ExampleSet}
	 * @param listOfNumericalAttributes list with the names of the numerical attributes to de-pivot
	 * @return the meta data {@link ExampleSet} or {@code null} if there was an error/empty attribute list
	 * @throws IOException thrown if the transformation process cannot be read
	 */
	public static ExampleSet createDePivotizedExampleSet(ExampleSet exampleSet, List<String> listOfNumericalAttributes) {
		if (exampleSet == null) {
			throw new IllegalArgumentException("exampleSet must not be null!");
		}
		if (listOfNumericalAttributes == null) {
			throw new IllegalArgumentException("listOfNumericalAttributes must not be null!");
		}
		if (listOfNumericalAttributes.size() == 0) {
			return null;
		}
		try {
			InputStream is = DataTransformation.class.getResourceAsStream("/com/rapidminer/resources/processes/TransformationDepivot.rmp");
			String transformProcessXML = Tools.readTextFile(is);
			
			// modify de-pivot to only de-pivot given list of numerical attributes
			StringBuffer defaultValueBuffer = new StringBuffer();
			for (String attName : listOfNumericalAttributes) {
				defaultValueBuffer.append(attName);
				defaultValueBuffer.append("|");
			}
			// remove last '|' so length -1
			String numericalValuesString = defaultValueBuffer.substring(0, defaultValueBuffer.length()-1);
			transformProcessXML = transformProcessXML.replace("TO_REPLACE_WITH_ATTRIBUTE_LIST", numericalValuesString);
			Process transformProcess = new Process(transformProcessXML);
			
			// disable logging messages
			ParameterTypeCategory loggingParameterType = (ParameterTypeCategory) transformProcess.getOperator("Process").getParameterType(ProcessRootOperator.PARAMETER_LOGVERBOSITY);
			loggingParameterType.setDefaultValue(loggingParameterType.getIndex("off"));
			
			// disable ID generation if ID already exists
			if (exampleSet.getAttributes().getId() != null) {
				transformProcess.getOperator("Generate ID").setEnabled(false);
			}
			
			IOContainer inputContainer = new IOContainer(exampleSet);
			IOContainer resultContainer = transformProcess.run(inputContainer);
			if (resultContainer.getElementAt(0) instanceof ExampleSet) {
				return (ExampleSet) resultContainer.getElementAt(0);
			} else {
				throw new OperatorException("First element returned was not ExampleSet, but " + resultContainer.getElementAt(0).getClass());
			}
		} catch (XMLException e) {
			LogService.getRoot().log(Level.SEVERE, "Failed to create MetaInformationDePivotized transformation process!", e);
		} catch (IOException e) {
			LogService.getRoot().log(Level.SEVERE, "Failed to read MetaInformationDePivotized transformation process!", e);
		} catch (OperatorException e) {
			LogService.getRoot().log(Level.SEVERE, "Failed to execute MetaInformationDePivotized transformation process!", e);
		}
		
		// we only arrive here in case of error, return null
		return null;
	}

}
