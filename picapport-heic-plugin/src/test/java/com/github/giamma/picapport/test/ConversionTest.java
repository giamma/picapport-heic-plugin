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
package com.github.giamma.picapport.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adobe.internal.xmp.XMPException;
import com.github.giamma.picapport.HEICImagePlugin;

import de.contecon.imageutils.CcXMPMetaData;
import de.contecon.picapport.plugins.PluginLogger;
import net.essc.util.GenLog;

public class ConversionTest {

	@Test
	public void convertHEICSample() throws Exception {
		doConvert("/sample1.heic");

	}

	@Test
	public void convertHEIFSample() throws Exception {
		doConvert("/sample1.heif");

	}

	private void doConvert(String fileName)
			throws URISyntaxException, IOException, Exception, XMPException, MalformedURLException {
		GenLog.setTraceLevel("DEBUG");
		PluginLogger logger = new PluginLogger(HEICImagePlugin.PLUGIN_NAME +" - ");

		HEICImagePlugin plg = new HEICImagePlugin();
		plg.init(new File(""), new Properties(), logger);

		URL sample = getClass().getResource(fileName);

		File inputFile = new File(sample.toURI());
		Path outputPath = Files.createTempFile(inputFile.getName(), "jpg");
		File outputFile = new File(outputPath.toString() + ".jpg");

		CcXMPMetaData newData = plg.createJpegFile(inputFile, outputFile, new CcXMPMetaData());

		Assertions.assertNotNull(newData, "metadata should not be null");
		Assertions.assertEquals(HEICImagePlugin.PLUGIN_NAME, newData.getCreator(), "metadata should not be empty");

		URLConnection connection = outputFile.toURL().openConnection();
		String mimeType = connection.getContentType();
		outputFile.deleteOnExit();
		Assertions.assertEquals("image/jpeg", mimeType, "converted type should be JPG");
	}

}
