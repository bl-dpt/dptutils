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

package uk.bl.dpt.utils.checksum;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.zip.CRC32;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.bl.dpt.utils.checksum.cksum.CkSumProvider;

/**
 * Go through a file contents to generate multiple checksums
 * @author wpalmer
 *
 */
public class ChecksumUtil {

	@SuppressWarnings("unused")
	private static Logger gLogger = LoggerFactory.getLogger(ChecksumUtil.class);
	
	private final static int BUFSIZE = 32768;
	
	private ChecksumUtil() {}
	
	/**
	 * Calculate a set of checksums for a file just using JDK libraries (and some classes in this package)
	 * This method only needs to read through the file once to generate all the checksums.
	 * Currently generates: "cksum CRC", CRC32, MD5, SHA-1 and SHA-256
	 * @param pFile File to check
	 * @param pChecksums List that calculated checksums will be stored in
	 * @throws FileNotFoundException 
	 */
	public static void calcChecksums(File pFile, Map<String, String> pChecksums) throws FileNotFoundException {
			calcChecksums(new FileInputStream(pFile), pChecksums);
	}
	
	/**
	 * Calculate a set of checksums for a file just using JDK libraries (and some classes in this package)
	 * This method only needs to read through the file once to generate all the checksums.
	 * Currently generates: "cksum CRC", CRC32, MD5, SHA-1 and SHA-256
	 * @param pInputStream File to check
	 * @param pChecksums List that calculated checksums will be stored in
	 */
	public static void calcChecksums(InputStream pInputStream, Map<String, String> pChecksums) {
		
		CkSumProvider.register();
		
		CRC32 crc32 = new CRC32();
		
		MessageDigest[] digests = null;
		try {
			// Add new digest types here and the rest of the method should just work
			// NOTE: MD5, SHA1 and SHA256 have to be implemented by each JRE/JDK.
			//       other digest types may not be available at runtime.
			// See note: http://docs.oracle.com/javase/7/docs/api/java/security/MessageDigest.html
			
			digests = new MessageDigest[] { //MessageDigest.getInstance("CRC"),
											MessageDigest.getInstance("cksum"),
											MessageDigest.getInstance("MD5"), 
											MessageDigest.getInstance("SHA-1"),
											MessageDigest.getInstance("SHA-256") };
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		
		// Open an InputStream for pFile
		boolean readError = false;
		InputStream original = null;
		original = new BufferedInputStream(pInputStream);
		
		// Create a chain of DigestInputStreams that consume the original InputStream.
		// Ensure a reference is kept to each DigestInputStream so the digests can be
		// recovered.
		DigestInputStream[] streams = null;
		streams = new DigestInputStream[digests.length];
		// initialise input stream digests
		for(int i=0;i<digests.length;i++) {
			if(0==i) {
				streams[0] = new DigestInputStream(original, digests[i]);
			} else {
				streams[i] = new DigestInputStream(streams[i-1], digests[i]);
			}
		}

		// drain the input stream
		try {
			byte[] buf = new byte[BUFSIZE];
			InputStream top = streams[digests.length-1];
			while(top.available()>0) {
				// update the CRC - we can do this because only one non-MessageDigest
				// digest needs to be calculated
				int read = top.read(buf);
				crc32.update(buf, 0, read);
			}
		} catch (IOException e) {
			readError = true;
			e.printStackTrace();
		} 
		
		// Recover the digests and add them to a List
		if(!readError) {
			String hash = "";
			
			pChecksums.put("CRC32",Long.toHexString(crc32.getValue()).toUpperCase());
			
			//recover the digests
			for(DigestInputStream stream:streams) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				hash = "";
				for(byte b : stream.getMessageDigest().digest()) hash+=String.format("%02x", b);
				pChecksums.put(stream.getMessageDigest().getAlgorithm(),hash.toUpperCase());
			}
		}

		// Clean up, close original inputstream if it is still open
		if(original!=null) {
			try {
				original.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Print out the checksums from a List
	 * @param pFile file that the checksums relate to
	 * @param checksums checksum list
	 */
	public static void printChecksums(File pFile, Map<String, String> checksums) {
		System.out.println("File: "+pFile.getAbsolutePath());
		for(String s:checksums.keySet()) {
			System.out.println(s+": "+checksums.get(s));
		}
	}
	
	/**
	 * Generates a checksum for a file 
	 * @param pType type of checksum to generate (MD5/SHA1 etc)
	 * @param pInFile file to checksum
	 * @return A String with the format MD5:XXXXXX or SHA1:XXXXXX
	 * @throws IOException file access error
	 */
	public static String generateChecksum(String pType, String pInFile) throws IOException {

		if(!new File(pInFile).exists()) throw new IOException("File not found: "+pInFile);
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(pType.toUpperCase());
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		
		FileInputStream input;
		try {
			input = new FileInputStream(pInFile);
			byte[] readBuffer = new byte[BUFSIZE];
			int bytesRead = 0;
			while(input.available()>0) {
				bytesRead = input.read(readBuffer);
				md.update(readBuffer, 0, bytesRead);
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String hash = "";
		for(byte b : md.digest()) hash+=String.format("%02x", b);
		return hash;
	}	
	
	/**
	 * Generates a checksum for a file 
	 * @param pInFile file to checksum
	 * @return A String with the format MD5:XXXXXX or SHA1:XXXXXX
	 * @throws IOException file access error
	 */
	public static String generateChecksum(String pInFile) throws IOException {

		String type = "MD5";
		return type+":"+generateChecksum(type, pInFile);
		
	}
	
	/**
	 * Generates a checksum for a file 
	 * @param pInFile file to checksum
	 * @return A String containing only the checksum
	 * @throws IOException file access error
	 */
	public static String generateChecksumOnly(String pInFile) throws IOException {

		String type = "MD5";
		return generateChecksum(type, pInFile);
		
	}
	
}
