package com.rapidminer.operator.io;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.nio.file.BufferedFileObject;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.parameter.conditions.OutputPortNotConnectedCondition;

/**
 * Abstract super type of stream writing operators.
 * 
 * @author Dominik Halfkann
 */
public abstract class AbstractStreamWriter extends AbstractWriter<ExampleSet> {

	private OutputPort fileOutputPort = getOutputPorts().createPort("file");

	public AbstractStreamWriter(OperatorDescription description) {
		super(description, ExampleSet.class);
		getTransformer().addGenerationRule(fileOutputPort, FileObject.class);
	}

	@Override
	public ExampleSet write(ExampleSet exampleSet) throws OperatorException {

		OutputStream outputStream = null;
		try {
			outputStream = getOutputStream();
			writeStream(exampleSet, outputStream);

		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				if (outputStream instanceof FileOutputStream) {
					throw new UserError(this, e, 322, getParameterAsFile(getFileParameterName()), "");
				} else if (outputStream instanceof ByteArrayOutputStream) {
					throw new UserError(this, e, 322, "output stream", "");
				} else {
					throw new UserError(this, e, 322, "unknown file or stream", "");
				}
			}
		}

		return exampleSet;
	}

	/**
	 * Creates (but does not add) the file parameter named by
	 * {@link #getFileParameterName()} that depends on whether or not
	 * {@link #fileOutputPort} is connected.
	 */
	protected ParameterType makeFileParameterType() {
		final ParameterTypeFile fileParam = new ParameterTypeFile(
				getFileParameterName(),
				"Name of the file to write the data in.", getFileExtension(),
				true);
		fileParam.setExpert(false);
		fileParam
				.registerDependencyCondition(new OutputPortNotConnectedCondition(
						this, new PortProvider() {
							@Override
							public Port getPort() {
								return fileOutputPort;
							}
						}, true));
		return fileParam;
	}

	/**
	 * Returns an OutputStream, depending on whether the {@link #fileOutputPort}
	 * is connected or a file name is given.
	 */
	public OutputStream getOutputStream() throws OperatorException {
		if (!fileOutputPort.isConnected()) {
			try {
				return new FileOutputStream(
						getParameterAsFile(getFileParameterName()));
			} catch (FileNotFoundException e) {
				throw new UserError(this, 301,
						getParameterAsFile(getFileParameterName()));
			}
		} else {
			return new ByteArrayOutputStream() {
				@Override
				public void close() throws IOException {
					super.close();
					fileOutputPort.deliver(new BufferedFileObject(this.toByteArray()));
				}
			};
		}
	}

	/**
	 * Writes data to an OutputStream in a format which is defined in the subclass.
	 */
	abstract void writeStream(ExampleSet exampleSet, OutputStream outputStream)
			throws OperatorException;

	/**
	 * Returns the name of the {@link ParameterTypeFile} to be added through
	 * which the user can specify the file name.
	 */
	abstract String getFileParameterName();

	/** Returns the allowed file extension. */
	abstract String getFileExtension();

}
