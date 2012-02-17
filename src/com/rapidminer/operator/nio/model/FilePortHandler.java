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
package com.rapidminer.operator.nio.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.operator.ports.metadata.SimplePrecondition;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.conditions.InputPortNotConnectedCondition;


/**
 * Provides methods for creating and working with a file InputPort. Used by reading operators.
 * @author Dominik Halfkann
 *
 */
public class FilePortHandler {

	private InputPort fileInputPort;
	private String fileParameterName;
	private Operator operator;
	
	public FilePortHandler (Operator operator, InputPort fileInputPort, String fileParameterName) {
		this.fileInputPort = fileInputPort;
		this.fileParameterName = fileParameterName;
		this.operator = operator;
		
		fileInputPort.addPrecondition(new SimplePrecondition(fileInputPort, new MetaData(FileObject.class)) {
			@Override
			protected boolean isMandatory() {
				return false;
			}
		});
	}
	
	/** Returns either the selected file referenced by the value of the parameter with the name
	 *  {@link #getFileParameterName()} or the file delivered at {@link #fileInputPort}.
	 *  Which of these options is chosen is determined by the parameter {@link #PARAMETER_DESTINATION_TYPE}. 
	 *  */
	public File getSelectedFile() throws OperatorException {
		if(!fileInputPort.isConnected()){
			return operator.getParameterAsFile(fileParameterName);
		} else {
			return fileInputPort.getData(FileObject.class).getFile();
		}
	}
	
	/** Same as {@link #getSelectedFile()}, but opens the stream. 
	 *  */
	public InputStream openSelectedFile() throws OperatorException, IOException {
		if(!fileInputPort.isConnected()){
			return new FileInputStream(operator.getParameterAsFile(fileParameterName));
		} else {
			return fileInputPort.getData(FileObject.class).openStream();
		}
	}
	
	/** Same as {@link #getSelectedFile()}, but returns true if file is specified (in the respective way). 
	 *  */
	public boolean isFileSpecified() {
		if(!fileInputPort.isConnected()){
			return operator.isParameterSet(fileParameterName);
		} else {
			try {
				return (fileInputPort.getData(IOObject.class) instanceof FileObject);
			} catch (OperatorException e) {
				return false;
			}
		}

	}


	/** Creates the file parameter named by fileParameterName 
	 *  that depends on whether or not the port returned by the given PortProvider is connected. */
	public static ParameterType makeFileParameterType(
			ParameterHandler parameterHandler,
			String parameterName, String fileExtension, PortProvider portProvider) {
		final ParameterTypeFile fileParam = new ParameterTypeFile(parameterName, "Name of the file to read the data from.", fileExtension, true);
		fileParam.setExpert(false);
		fileParam.registerDependencyCondition(new InputPortNotConnectedCondition(parameterHandler, portProvider, true));
		return fileParam;
	}
}
