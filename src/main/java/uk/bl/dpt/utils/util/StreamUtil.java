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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for working with streams
 * @author wpalmer
 *
 */
public class StreamUtil {

	@SuppressWarnings("unused")
	private static Logger gLogger = LoggerFactory.getLogger(StreamUtil.class);
	
	private StreamUtil() {}
	
	/**
	 * Gets an InputStream for the contents of a file - if the file is .gz it will return
	 * a stream of the decompressed contents
	 * @param pFile file to open
	 * @return stream (or null)
	 */
	public static InputStream getInputStreamFromFile(File pFile) {
		
		if(!pFile.exists()) return null;
		
		try {
			return new GZIPInputStream(new FileInputStream(pFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				return new FileInputStream(pFile);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return null;
		
	}
	
	/**
	 * Copy an InputStream to a File
	 * @param pInputStream InputStream to copy
	 * @param pFile desktination file
	 * @return true if successful
	 */
	public static boolean copyStreamToFile(InputStream pInputStream, File pFile) {
//		if(!pFile.exists()) {
//			try {
//				pFile.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(pFile);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] buffer = new byte[32768];
		int read = 0;
		try {
			while(pInputStream.available()>0) {
				read = pInputStream.read(buffer, 0, buffer.length);
				fos.write(buffer, 0, read);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(pInputStream!=null) {
					pInputStream.close();
				}
				if(fos!=null) {
					fos.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

}
