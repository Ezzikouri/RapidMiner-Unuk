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
package com.rapidminer.operator.nio.file.compression;

import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.GenerateNewMDRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;

/**
 * @author Marius Helf
 *
 */
public class CreateZipFileOperator extends Operator {
	public static final String[] BUFFER_TYPES = { "memory", "file" };
	public static final int BUFFER_TYPE_MEMORY = 0;
	public static final int BUFFER_TYPE_FILE = 1;


	private static final String PARAMETER_BUFFER_TYPE = "buffer_type";
	private static final String PARAMETER_USE_DEFAULT_COMPRESSION_LEVEL = "use_default_compression_level";
	private static final String PARAMETER_COMPRESSION_LEVEL = "compression_level";	
	
	OutputPort zipFileOuputPort = getOutputPorts().createPort("zip file");
	
	/**
	 * @param description
	 */
	public CreateZipFileOperator(OperatorDescription description) {
		super(description);
		getTransformer().addRule(new GenerateNewMDRule(zipFileOuputPort, ZipFileObject.class));
	}

	
	@Override
	public void doWork() throws OperatorException {
		ZipFileObject zipFileObject = null;
		switch (getParameterAsInt(PARAMETER_BUFFER_TYPE)) {
		case BUFFER_TYPE_FILE:
			zipFileObject = new ZipFileObject(ZipFileObject.BufferType.FILE);
			break;
		case BUFFER_TYPE_MEMORY:
			zipFileObject = new ZipFileObject(ZipFileObject.BufferType.MEMORY);
			break;
		default:
			throw new RuntimeException("illegal parameter value for " + PARAMETER_BUFFER_TYPE);
		}
		if (!getParameterAsBoolean(PARAMETER_USE_DEFAULT_COMPRESSION_LEVEL)) {
			zipFileObject.setCompressionLevel(getParameterAsInt(PARAMETER_COMPRESSION_LEVEL));
		}
		zipFileOuputPort.deliver(zipFileObject);
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeCategory(PARAMETER_BUFFER_TYPE, "Defines where the buffer for the zip file will be created. A memory buffered zip file will usually perform faster in terms of execution time, but the complete zip file must be kept in memory, which can lead to problems if large files or a large amount of files is added to the zip archive.", BUFFER_TYPES, BUFFER_TYPE_FILE, true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DEFAULT_COMPRESSION_LEVEL, "Allows to override the default compression level.", true, true));
		ParameterTypeInt type = new ParameterTypeInt(PARAMETER_COMPRESSION_LEVEL, "Defines the compression level. A compression level of 0 stands for no compression, whereas the highest level 9 means best compression. In general, higher compression levels result also in higher runtime.", 0, 9, 9, true);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_USE_DEFAULT_COMPRESSION_LEVEL, true, false));
		types.add(type);
		
		return types;
	}
}
