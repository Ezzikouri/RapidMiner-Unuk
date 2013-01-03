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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.nio.file.FileObject;
import com.rapidminer.tools.Tools;

/**
 * @author Marius Helf
 * 
 */
public class ZipFileObject extends FileObject {
	public enum BufferType {
		MEMORY, FILE
	};

	private static final long serialVersionUID = 1L;

	private ZipOutputStream zipOutputStream;
	private OutputStream compressedData;
	private int compressionLevel = Deflater.DEFAULT_COMPRESSION;
	private final BufferType bufferType;

	private File tmpFile;

	public ZipFileObject() throws OperatorException {
		bufferType = BufferType.FILE;
		init();
	}

	public ZipFileObject(BufferType bufferType) throws OperatorException {
		super();
		this.bufferType = bufferType;
		init();
	}

	private void init() throws OperatorException {
		switch (bufferType) {
		case FILE:
			try {
				tmpFile = File.createTempFile("rm_zipfile_", ".dump");
				tmpFile.deleteOnExit();
				compressedData = new FileOutputStream(tmpFile);
			} catch (IOException e) {
				throw new OperatorException("303", e, tmpFile, e.getMessage());
			}
			break;
		case MEMORY:
			compressedData = new ByteArrayOutputStream();
			break;

		}
		zipOutputStream = new ZipOutputStream(compressedData);
	}

	@Override
	public InputStream openStream() throws OperatorException {

		try {
			zipOutputStream.flush();
			zipOutputStream.finish();
			compressedData.flush();
		} catch (IOException e) {
			throw new OperatorException("zipfile.stream_error", e, new Object[0]);
		}

		switch (bufferType) {
		case FILE:
			try {
				return new FileInputStream(tmpFile);
			} catch (FileNotFoundException e) {
				throw new OperatorException("301", e, tmpFile);
			}
		case MEMORY:
			return new ByteArrayInputStream(((ByteArrayOutputStream) compressedData).toByteArray());
		default:
			throw new RuntimeException("bufferType should never be null");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rapidminer.operator.nio.file.FileObject#getFile()
	 */
	@Override
	public File getFile() throws OperatorException {
		if (tmpFile == null) {
			try {
				tmpFile = File.createTempFile("rm_zipfile_", ".dump");
				FileOutputStream fos = new FileOutputStream(tmpFile);
				InputStream in = openStream();
				Tools.copyStreamSynchronously(in, fos, true);
				tmpFile.deleteOnExit();
			} catch (IOException e) {
				throw new OperatorException("303", e, tmpFile, e.getMessage());
			}
		}
		return tmpFile;
	}

	public void addEntry(FileObject fileObject, String directory) throws OperatorException {
		addEntry(fileObject, directory, compressionLevel);
	}

	/**
	 * @param fileObject
	 * @param directory 
	 * @throws IOException
	 * @throws OperatorException
	 */
	public void addEntry(FileObject fileObject, String directory, int compressionLevel) throws OperatorException {
		if (directory == null) {
			directory = "";
		}
		// remove trailing slashes
		directory = directory.replaceAll("\\\\", "/");
		directory = directory.replaceAll("[\\\\|/]+$", "");
		
		String source = fileObject.getAnnotations().getAnnotation(Annotations.KEY_SOURCE);
		String filename = source.replaceAll(".*[/\\\\]([^/\\\\\\?]*).*", "$1");
		
		
		if (!directory.isEmpty()) {
			filename = directory + "/" + filename;
		}

		zipOutputStream.setLevel(compressionLevel);

		InputStream fileStream = fileObject.openStream();
		try {
			try {
				zipOutputStream.putNextEntry(new ZipEntry(filename));
				Tools.copyStreamSynchronously(fileStream, zipOutputStream, false);
				zipOutputStream.closeEntry();
			} catch (IOException e) {
				throw new OperatorException("zipfile.stream_error", e, new Object[0]);
			}
		} finally {
			try {
				fileStream.close();
			} catch (IOException e) {
				throw new OperatorException("zipfile.stream_error", e, new Object[0]);
			}
		}
	}

	@Override
	public String getName() {
		return "Zip File";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		switch (bufferType) {
		case FILE:
			builder.append("File");
			break;
		case MEMORY:
			builder.append("Memory");
			break;
		}
		builder.append(" buffered zip file");

		return builder.toString();
	}

	@Override
	protected void finalize() throws Throwable {
		if (tmpFile != null) {
			tmpFile.delete();
		}
		super.finalize();
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}

	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}
}
