/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package com.github.giamma.picapport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import de.contecon.imageutils.CcXMPMetaData;
import de.contecon.picapport.plugins.IPicApportPlugInLogger;
import de.contecon.picapport.plugins.otherformats.IOtherFileFormat;
import de.contecon.picapport.plugins.otherformats.OtherFormatsDescriptor;

/**
 * This is a PicApport plug-in for HEIC/HEIF images.
 * 
 * This plug-in was tested against PicApport 10.4.00 but should work with
 * previous versions as well.
 * 
 * For HEIC/HEIF to JPEG conversion this plug-in requires that ImageMagick is
 * installed on the server and is in the system path.
 * 
 * @author giamma
 */
public class HEICImagePlugin implements IOtherFileFormat {
	private static final String VERSION_PROPERTY = "version";
	private static final String COPYRIGHT = "(c) 2023 Gianmaria Romanato";
	private static final String HEIF_MIME_TYPE = "image/heif";
	private static final String HEIC_MIME_TYPE = "image/heic";
	private static final String HEIF_FILE_EXTENSION = ".heif";
	private static final String HEIC_FILE_EXTENSION = ".heic";
	private static final String INTERNAL_PROPERTIES = "/version.properties";
	private static final String IMAGEMAGICK_EXECUTABLE = "convert";
	public static final String PLUGIN_NAME = "picapport-heic-plugin";
	private IPicApportPlugInLogger logger;

	@Override
	public List<OtherFormatsDescriptor> init(File pluginDirectory, Properties props, IPicApportPlugInLogger logger) {
		this.logger = logger;

		String version = getVersion();
		if (version != null) {
			logger.logDebugMessage("version: " + version);
		}

		try {
			Runtime.getRuntime().exec(IMAGEMAGICK_EXECUTABLE);
		} catch (IOException e) {
			logger.logErrorMessage(
					"Cannot find 'convert' executable, make sure ImageMagick is installed and included in PATH");
		}

		return Arrays.asList(new OtherFormatsDescriptor[] {
				new OtherFormatsDescriptor(HEIC_FILE_EXTENSION, HEIC_MIME_TYPE, true, PLUGIN_NAME, COPYRIGHT,
						version, props),
				new OtherFormatsDescriptor(HEIF_FILE_EXTENSION, HEIF_MIME_TYPE, true, PLUGIN_NAME, COPYRIGHT,
						version, props) });
	}

	private String getVersion() {
		try (InputStream is = getClass().getResourceAsStream(INTERNAL_PROPERTIES)) {
			Properties p = new Properties();
			p.load(is);
			return p.getProperty(VERSION_PROPERTY);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public CcXMPMetaData createJpegFile(File otherFormatFile, File jpegFileToCreate, CcXMPMetaData metaDataIn)
			throws Exception {

		logger.logDebugMessage("Converting " + otherFormatFile + " to " + jpegFileToCreate);

		long start;
		try {
			start = System.currentTimeMillis();
			ConvertCmd cmd = new ConvertCmd();
			IMOperation op = new IMOperation();
			op.addImage(otherFormatFile.getCanonicalPath());
			op.addImage(jpegFileToCreate.getCanonicalPath());
			cmd.run(op);
			logger.logDebugMessage("Conversion completed in (ms): " + (System.currentTimeMillis() - start));
		} catch (IM4JavaException e) {
			logger.logErrorMessage("Conversion failed: " + e.getMessage());
			throw e;
		}

		Calendar dateCreated = Calendar.getInstance();
		dateCreated.setTime(new Date(otherFormatFile.lastModified()));
		metaDataIn.setCreationDate(dateCreated);
		metaDataIn.setTitle(otherFormatFile.getName());
		metaDataIn.setCreator(PLUGIN_NAME);

		return metaDataIn;
	}
}
