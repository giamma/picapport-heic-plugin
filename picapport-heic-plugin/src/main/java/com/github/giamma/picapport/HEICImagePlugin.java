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
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
	private static final String IMAGEMAGICK_EXECUTABLE = "convert";
	public static final String PLUGIN_NAME = "PicApport HEIC Plugin";
	private IPicApportPlugInLogger logger;

	@Override
	public List<OtherFormatsDescriptor> init(File pluginDirectory, Properties props, IPicApportPlugInLogger logger) {
		this.logger = logger;

		String version = getVersion();
		logger.logDebugMessage("Version: "+version);
		
		try {
			Runtime.getRuntime().exec(IMAGEMAGICK_EXECUTABLE);
		} catch (IOException e) {
			logger.logErrorMessage("Cannot find 'convert' executable, make sure ImageMagick is installed and included in PATH");
		}
		
		return Arrays.asList(new OtherFormatsDescriptor[] {
				new OtherFormatsDescriptor(".heic", "image/heic", true, PLUGIN_NAME, "(c) 2023 Gianmaria Romanato",
						version, props),
				new OtherFormatsDescriptor(".heif", "image/heif", true, PLUGIN_NAME, "(c) 2023 Gianmaria Romanato",
						version, props) });
		}
	
		private String getVersion() {
			String version = null;
			try {
				ClassLoader cl = getClass().getClassLoader();
				URL url = cl.getResource("META-INF/MANIFEST.MF");
				Manifest manifest = new Manifest(url.openStream());
				Attributes attr = manifest.getMainAttributes();
				version = attr.getValue("Implementation-Version");
			} catch (Exception e) {
			}
			if (version == null) {
				return "1.0.0";
			} else {
				return version;
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
