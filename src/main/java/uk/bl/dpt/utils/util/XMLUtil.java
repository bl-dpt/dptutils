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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Utility methods for working with XML
 * @author wpalmer
 */
public class XMLUtil {

	@SuppressWarnings("unused")
	private static Logger gLogger = Logger.getLogger(XMLUtil.class);
	
	private XMLUtil() {}
	
	/**
	 * Recover the value associated with a xpath expression
	 * @param pInputStream xml file
	 * @param pXPath XPath expression to evaluate
	 * @return value value associated with the xpath expression (or null if error)
	 */
	public static String getXpathVal(InputStream pInputStream, String pXPath) {
		try {
			DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docB.parse(pInputStream);
			Node root = doc.getFirstChild();		
			XPath xpath = XPathFactory.newInstance().newXPath();
			return xpath.evaluate(pXPath, root);
		} catch (ParserConfigurationException pce) {
		} catch (NumberFormatException e) {
		} catch (XPathExpressionException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * Wrap an element text in CDATA, without using an XML parser
	 * -> e.g. for text that is not valid XML (such as filenames)
	 * NOTE: data should be all on one line
	 * @param pElement
	 * @param pFile
	 * @throws IOException
	 */
	public static void wrapElementInCDATA(String pElement, String pFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(pFile));
		PrintWriter out = new PrintWriter(new FileWriter(pFile+".fixed.xml"));
	
		final String startKey = "<"+pElement+">";
		final String endKey = "</"+pElement+">";
		
		String line = "";
		while(in.ready()) {
			line = in.readLine();
			if(line.contains(startKey)) {
				line = line.substring(0, line.indexOf(startKey))+
						startKey+
						"<![CDATA["+
						line.substring(line.indexOf(startKey)+startKey.length(), line.indexOf(endKey))+
						"]]>"+
						endKey;
			} 
			out.println(line);
		}
		
		in.close();
		out.close();
	}
}
