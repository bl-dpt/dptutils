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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Calculate differences between two file manifests
 * Created by wpalmer on 28/05/2014.
 */
public class ChecksumDiff {

	private static Map<String, ArrayList<String>> checksumSetA;
	private static Map<String, ArrayList<String>> checksumSetB;
	private static PrintWriter printWriter;

	private ChecksumDiff() { }

	/**
	 * Print String to stdout and to a report file
	 * @param s String to print
	 */
	private static void out(String s) {
		System.out.println(s);
		if(printWriter!=null) {
			printWriter.println(s);
		}
	}

	/**
	 * Load checksums from an OS CRC file
	 * @param pFile OS CRC file
	 * @param pChecksumSet Map in which to load CRC values
	 * @return true if loaded ok, false if not
	 */
	private static boolean loadOSChecksums(File pFile, Map<String, ArrayList<String>> pChecksumSet) {

		try {
			BufferedReader buf = new BufferedReader(new FileReader(pFile));
			while(buf.ready()) {
				String[] line = buf.readLine().split(",");
				//pChecksumSet.put(line[1], line[0]);
				addChecksum(line[1], line[0], pChecksumSet);
			}
			buf.close();
			return true;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Load checksums from a "ThreadingSpider" XML file
	 * @param pFile "ThreadingSpider" XML file
	 * @param pChecksumSet Map in which to load checksum values (note hardcoded checksum type in method)
	 * @return true if loaded ok, false if not
	 */
	private static boolean loadXMLChecksums(File pFile, Map<String, ArrayList<String>> pChecksumSet) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			final String CHECKSUM_TYPE = "cksum";
			xmlReader.setContentHandler(new ChecksumSaxLoader(pChecksumSet, CHECKSUM_TYPE));
			xmlReader.parse(new InputSource(new FileInputStream(pFile)));
			return true;
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Add checksum to map - this is done in one place here as we need to check that
	 * there are no valid duplicate checksums within the data
	 * @param pChecksum checksum to add
	 * @param pFile filename relating to checksum
	 * @param pChecksumSet Map in which to load checksum values
	 */
	@SuppressWarnings("serial")
	static void addChecksum(String pChecksum, final String pFile, Map<String, ArrayList<String>> pChecksumSet) {

		if(pChecksumSet.containsKey(pChecksum)) {
			ArrayList<String> values = pChecksumSet.get(pChecksum);
			values.add(pFile);
			pChecksumSet.put(pChecksum, values);
		} else {
			// Just add the new checksum
			pChecksumSet.put(pChecksum, new ArrayList<String>() {{ add(pFile); }});
		}

	}

	/**
	 * Entry point for loading checksums from files of different formats
	 * @param pFile file to load data from
	 * @param pChecksumSet Map in which to load checksum values
	 * @return true if loaded ok, false if not
	 */
	private static boolean loadChecksums(File pFile, Map<String, ArrayList<String>> pChecksumSet) {
		if(pFile.getName().toLowerCase().endsWith(".xml")) {
			return loadXMLChecksums(pFile, pChecksumSet);
		} else {
			return loadOSChecksums(pFile, pChecksumSet);
		}

	}

	/**
	 * Remove the duplicate values (checksum & filename pairs) from two Maps
	 * @param pChecksumSetA Master set
	 * @param pChecksumSetB Secondary set
	 */
	private static void removeDuplicates(Map<String, ArrayList<String>> pChecksumSetA, Map<String, ArrayList<String>> pChecksumSetB) {
		Iterator<String> it = pChecksumSetA.keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			if(pChecksumSetB.containsKey(key)) {
				// At this point we know we have a matching checksum
				// Therefore - check to see if we have any matching filenames, with the same checksum
				Iterator<String> valuesA = pChecksumSetA.get(key).iterator();
				while(valuesA.hasNext()) {
					String a = valuesA.next();
					Iterator<String> valuesB = pChecksumSetB.get(key).iterator();
					while(valuesB.hasNext()) {
						String b = valuesB.next();
						if(a==null||b==null) {
							if(a==null&&b==null) {
								// we have a match - remove filenames from both lists and continue
								valuesA.remove();
								valuesB.remove();		
							} else {
								// names don't match - one is null, continue
							}
						} else {
							if(new File(a).getName().equalsIgnoreCase(new File(b).getName())) {
								// we have a match - remove filenames from both lists and continue
								valuesA.remove();
								valuesB.remove();
							}
						}
					}
				}
				// If no files are listed against the checksum then remove the checksum from the sets
				if(pChecksumSetA.get(key).size()==0) {
					it.remove();
				}
				if(pChecksumSetB.get(key).size()==0) {
					pChecksumSetB.remove(key);
				}

			}
		}
	}

	/**
	 * Print the entries in a dataset
	 * @param pFile Data file for data set
	 * @param pChecksumSet Map containing remaining checksums for dataset
	 */
	private static void printEntries(File pFile, Map<String, ArrayList<String>> pChecksumSet) {

		out("Unique files in "+pFile.getName()+": "+pChecksumSet.keySet().size());

		for(String k:pChecksumSet.keySet()) {
			//System.out.println(k+": "+pChecksumSet.get(k));
			// just to the report file for these
			if(printWriter!=null) {
				printWriter.println(k+": "+pChecksumSet.get(k));
			}
		}

	}

	private static void cleanup() {
		if(printWriter!=null) {
			printWriter.close();
		}
		checksumSetA = null;
		checksumSetB = null;

	}

	private static void diff(File pFileA, File pFileB) {
		out("Removing duplicates (1)");
		removeDuplicates(checksumSetA, checksumSetB);
		// Is this step strictly necessary?
		// -> Unit testing says it's not (yay for unit testing!)
		//		out("Removing duplicates (2)");
		//		removeDuplicates(checksumSetB, checksumSetA);

		out("Finished");
		printEntries(pFileA, checksumSetA);
		printEntries(pFileB, checksumSetB);
	}

	/**
	 * Compare two sets of prepared checksum data
	 * @param pChecksumSetA
	 * @param pChecksumSetB
	 */
	public static void compare(Map<String, ArrayList<String>> pChecksumSetA,  Map<String, ArrayList<String>> pChecksumSetB) {
		try {
			compare(pChecksumSetA, pChecksumSetB, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Compare two sets of prepared checksum data and output a report to file
	 * @param pChecksumSetA
	 * @param pChecksumSetB
	 * @param pReportFile
	 * @throws IOException
	 */
	public static void compare(Map<String, ArrayList<String>> pChecksumSetA,  Map<String, ArrayList<String>> pChecksumSetB, File pReportFile) throws IOException {
		checksumSetA = pChecksumSetA;
		checksumSetB = pChecksumSetB;

		if(pReportFile!=null) {
			printWriter = new PrintWriter(new FileWriter(pReportFile));
		} else {
			printWriter = null;
		}

		out("Entries in Set 1: "+checksumSetA.keySet().size());
		out("Entries in Set 2: "+checksumSetB.keySet().size());

		// FIXME: Hack
		diff(new File("Set 1"), new File("Set 1"));

		cleanup();
	}

	/**
	 * Compare checksum values from two files
	 * @param pFileA First file to check
	 * @param pFileB Second file to check
	 */
	public static void compare(File pFileA, File pFileB) {
		try {
			compare(pFileA, pFileB, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Compare checksum values from two files
	 * @param pFileA First file to check
	 * @param pFileB Second file to check
	 * @param pReportFile 
	 * @throws IOException 
	 */
	public static void compare(File pFileA, File pFileB, File pReportFile) throws IOException {
		checksumSetA = new TreeMap<String, ArrayList<String>>();
		checksumSetB = new TreeMap<String, ArrayList<String>>();

		if(pReportFile!=null) {
			printWriter = new PrintWriter(new FileWriter(pReportFile));
		} else {
			printWriter = null;
		}

		out("Loading checksum set 1");
		if(!loadChecksums(pFileA, checksumSetA)) {
			System.out.println("Can't load data: "+pFileA);
		}
		out("Entries: "+checksumSetA.keySet().size());

		out("Loading checksum set 2");
		if(!loadChecksums(pFileB, checksumSetB)) {
			System.out.println("Can't load data: "+pFileA);
		}
		out("Entries: " + checksumSetB.keySet().size());

		diff(pFileA, pFileB);

		cleanup();

	}

	@SuppressWarnings("javadoc")
	public static void main(String[] pArgs) throws IOException {

		final String year = "2007";

		ChecksumDiff.compare(new File("f:/geospatial/crc values/LL_" + year + ".txt"),
				new File("f:/geospatial/geospatial_diff/" + year + "blcopy.xml"),
				new File("report-" + year + ".txt"));

	}

}
