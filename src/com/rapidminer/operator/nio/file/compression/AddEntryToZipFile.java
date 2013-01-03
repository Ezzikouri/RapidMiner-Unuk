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
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.InputPortExtender;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.MetaData;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.BooleanParameterCondition;

/**
 * @author Marius Helf
 *
 */
public class AddEntryToZipFile extends Operator {
	private static final String PARAMETER_OVERRIDE_COMPRESSION_LEVEL = "override_compression_level";
	private static final String PARAMETER_COMPRESSION_LEVEL = "compression_level";
	private static final String PARAMETER_DIRECTORY = "directory";
	private InputPort zipFileInput = getInputPorts().createPort("zip file");
	private InputPortExtender fileInput = new InputPortExtender("file input", getInputPorts(), new MetaData(FileObject.class), true);
	private OutputPort zipFileOutput = getOutputPorts().createPort("zip file");
	
	
	public AddEntryToZipFile(OperatorDescription description) {
		super(description);
		getTransformer().addPassThroughRule(zipFileInput, zipFileOutput);
		fileInput.start();
	}
	
	@Override
	public void doWork() throws OperatorException {
		ZipFileObject zipfile = zipFileInput.getData(ZipFileObject.class);
		List<FileObject> files = fileInput.getData(FileObject.class, true);
		boolean overrideCompressionLevel = getParameterAsBoolean(PARAMETER_OVERRIDE_COMPRESSION_LEVEL);
		int compressionLevel = getParameterAsInt(PARAMETER_COMPRESSION_LEVEL);
		String directory = getParameterAsString(PARAMETER_DIRECTORY);
		for (FileObject file : files) {
			if (overrideCompressionLevel) {
				zipfile.addEntry(file, directory, compressionLevel);
			} else {
				zipfile.addEntry(file, directory);
			}
			checkForStop();
		}
		zipFileOutput.deliver(zipfile);
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		types.add(new ParameterTypeString(PARAMETER_DIRECTORY, "By default, new entries are added to the root directory of the zip file. With this parameter, you can specify a custom directory. Do not prepend a slash to the directory. To place the file into 'my/subdirectory', enter that exact string into this parameter.", "", false));
		types.add(new ParameterTypeBoolean(PARAMETER_OVERRIDE_COMPRESSION_LEVEL, "Each zip file in RapidMiner has a default compression level which is used for new entries. If you disable this parameter, you can override that level for the entries which are added by this operator. This is useful, if you are adding pre-compressed files to the zip archive, like other zip files, jar files etc.: these files can't be further compressed, so you can save some execution time by setting the compression level for new entries of this kind to a low value (a compression level of 0 disables compression completely).", false, true));
		ParameterTypeInt type = new ParameterTypeInt(PARAMETER_COMPRESSION_LEVEL, "Defines the compression level. A compression level of 0 stands for no compression, whereas the highest level 9 means best compression. In general, higher compression levels result also in higher runtime.", 0, 9, 0, true);
		type.registerDependencyCondition(new BooleanParameterCondition(this, PARAMETER_OVERRIDE_COMPRESSION_LEVEL, true, true));
		types.add(type);
		
		return types;
	}
}
