/*
 * Copyright 2014 The British Library/SCAPE Project Consortium
 * Author: William Palmer (William.Palmer@bl.uk)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package uk.bl.dpt.utils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * Utility methods for using resources
 * @author wpalmer
 *
 */
public class ResourceUtil {

	private static Logger gLogger = Logger.getLogger(ResourceUtil.class);
	
	private static final String RESOURCES_DIR = "src/main/resources/";
	
	private ResourceUtil() {}
	
	/**
	 * Loads a specified resource from the jar
	 * @param pResource resource to load
	 * @return stream (or null)
	 */
	public static InputStream loadResource(String pResource) {
		InputStream resource = ResourceUtil.class.getClassLoader().getResourceAsStream(pResource);
		
		if(resource!=null) {
			gLogger.trace("loaded from jar: "+pResource);
			return resource;
		}
		
		File file = new File(RESOURCES_DIR+pResource);
		if(file.exists()) {
			try {
				gLogger.trace("loaded from filesystem: "+pResource);
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		gLogger.trace("can't find resource: "+pResource);
		
		return null;
		
	}
}
