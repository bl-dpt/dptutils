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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;


/**
 * File utilitie methods
 * @author wpalmer
 *
 */
public class FileUtil {

	@SuppressWarnings("unused")
	private static Logger gLogger = LoggerFactory.getLogger(FileUtil.class);
	
	private FileUtil() {}
	
	/**
	 * Recurse the content of a directory and populate a List with files
	 * @param dir
	 * @param files
	 */
	public static void traverse(File dir, List<File> files) {
		if(dir.isDirectory()) {
			for(File f:dir.listFiles()) {
				if(f.isDirectory()) {
					traverse(f, files);
				} else {
					files.add(f);
				}
			}
		} else {
			//we might just pass a single file
			files.add(dir);
		}
	}
	
	/**
	 * Creates a new temporary directory 
	 * @return File object for new directory
	 * @throws IOException file access error
	 */
//	public static File newTempDir() throws IOException {
//		//create a temporary local output file name for use with the local tool in TMPDIR
//		new File(Settings.TMP_DIR).mkdirs();
//		File localOutputTempDir = File.createTempFile(Settings.JOB_NAME,"",
//				new File(Settings.TMP_DIR));
//		//change this to a directory and put all the files in there
//		localOutputTempDir.delete();
//		localOutputTempDir.mkdirs();
//		localOutputTempDir.setReadable(true, false);
//		localOutputTempDir.setWritable(true, false);//need this so output can be saved
//		return localOutputTempDir;
//	}
	

	/**
	 * Recursively delete a local directory
	 * @param pDir directory to delete
	 * @return success
	 */
	public static boolean deleteDirectory(File pDir) {
		boolean ret = true;
		for(File f:pDir.listFiles()) {
			if(f.isDirectory()) ret&=deleteDirectory(f);
			ret&=f.delete();
		}
		ret&=pDir.delete();
		return ret;
	}
	
	/**
	 * Copies an inputstream to a file
	 * @param pBuffer buffer to read
	 * @param pFile local file to write to
	 * @throws IOException if an error occurred
	 */
	public static void writeReaderToFile(Reader pBuffer, File pFile) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(pFile));
		char[] readBuffer = new char[32768];
		int bytesRead = 0;
		while(pBuffer.ready()) {
			bytesRead = pBuffer.read(readBuffer);
			out.write(readBuffer, 0, bytesRead);
		}
		out.close();
	}
	
}
