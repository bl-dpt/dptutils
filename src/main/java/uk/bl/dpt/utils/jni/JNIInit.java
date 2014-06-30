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
package uk.bl.dpt.utils.jni;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.utils.util.ResourceUtil;
import uk.bl.dpt.utils.util.StreamUtil;

/**
 * Initialise and sort out dll dependencies from the jar
 * @author wpalmer
 */
public class JNIInit {

	private JNIInit() {}
	
	private static Logger gLogger = LoggerFactory.getLogger(JNIInit.class);
	
	//////////////////////////////////////////////////////////////
	
	private static final String SEP = "/";
	// default to 64 bits
	private static String BITS = "64";
	private static final String ROOT = "jni_lib"+SEP;
	private static String LOCAL_LIB_DIR = null;

	////////////////////////////////////////////////////////////
	
	/**
	 * Initialise some native libraries (contained in the jar) - OS/CPU arch is detected
	 * Shared objects (dll/so) should be in <resources/jar>/packagename/version/ostype/
	 * @param pPackage package name
	 * @param pVersion version of the package
	 * @param pLibs library names to load (note: on Linux will be prefixed with lib)
	 */
	public static void init(String pPackage, String pVersion, String[] pLibs) {
		gLogger.trace("JNIInit initialising library...");
		BITS = System.getProperty("sun.arch.data.model");
		String os = System.getProperty("os.name").toLowerCase();
		String ext = "";
		String prefix = "";

		if(os.contains("linux")) {
			prefix="lib";
			os="linux-x86_";
			ext=".so";
		}
		if(os.contains("windows")) {
			prefix="";
			os="win";
			ext=".dll";
		}
		
		String path = ROOT+pPackage+SEP+pVersion+SEP+os+BITS+SEP+prefix;
		
		try {
			File libDir = File.createTempFile("dptutils_", ".dir");
			libDir.delete();
			libDir = new File(libDir.getAbsolutePath()+SEP);
			libDir.mkdirs();
			LOCAL_LIB_DIR = libDir.getAbsolutePath();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Add LOCAL_LIB_DIR to java.library.path to prevent UnsatisfiedLinkErrors
		// SEE: http://fahdshariff.blogspot.jp/2011/08/changing-java-library-path-at-runtime.html
		final String JLP = "java.library.path";
		System.setProperty(JLP, System.getProperty(JLP)+";"+LOCAL_LIB_DIR+";");
		//System.out.println("path: "+System.getProperty(JLP));
		
		try {
			// http://fahdshariff.blogspot.co.uk/2011/08/changing-java-library-path-at-runtime.html
			final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
			sysPathsField.setAccessible(true);
			sysPathsField.set(null, null);
		} catch(Exception e) {
			gLogger.error("Failed to add temp lib dir to "+JLP);
		}
		
		for(String libname:pLibs) {
			String lib = path+libname+ext;
			loadLib(lib);
		}
		
	}
	
	private static File loadLib(String pResource) {
		gLogger.trace("Attempting to load native library: "+pResource);
		File lib = null;
		try {
			lib = new File(LOCAL_LIB_DIR+SEP+new File(pResource).getName());
			gLogger.trace("Creating file: "+lib);
			lib.createNewFile();
			lib.deleteOnExit();
		} catch (IOException e) {
			e.printStackTrace();
			gLogger.error("Creating temp file failed: "+pResource);
			return null;
		}
		InputStream libInputStream = ResourceUtil.loadResource(pResource);
		if(libInputStream==null) {
			gLogger.trace("Stream is null: "+pResource);
		}
		StreamUtil.copyStreamToFile(libInputStream, lib);
		System.load(lib.getAbsolutePath());
		gLogger.trace("Loaded native library: "+pResource+" -> "+lib.getAbsolutePath());
		return lib;
	}
	
}
