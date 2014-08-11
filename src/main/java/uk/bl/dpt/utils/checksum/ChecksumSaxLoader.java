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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by wpalmer on 28/05/2014.
 */
public class ChecksumSaxLoader extends DefaultHandler {

    private Map<String, ArrayList<String>> gChecksumMap;
    private String gFile = null;
    private String gText = null;
    private String gChecksum = "SHA-256";
    private final String NAMEKEY = "file";

    /**
     * @param pChecksumMap
     */
    public ChecksumSaxLoader(Map<String, ArrayList<String>> pChecksumMap) {
        gChecksumMap = pChecksumMap;
    }

    /**
     * @param pChecksumMap
     * @param pChecksum
     */
    public ChecksumSaxLoader(Map<String, ArrayList<String>> pChecksumMap, String pChecksum) {
        this(pChecksumMap);
        gChecksum = pChecksum;
    }

    /**
     * Get the checksums that have been loaded from the XML file
     * @return map of checksums loaded from the XML file
     */
    public Map<String, ArrayList<String>> getChecksumMap() {
        return gChecksumMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        if(qName.equals(NAMEKEY)) {
            gText = "";
            return;
        }
        if(qName.equals("checksum")) {
            if(attributes.getValue("digest").startsWith(gChecksum)) {
                gText = "";
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        if(gText!=null) {
            gText += new String(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if(gText!=null) {
        	if(qName.equals(NAMEKEY)) {
        		// set filename key and continue
        		gFile = gText.trim();
        		gText = null;
        		return;
        	} else {
        		if(qName.equals("checksum")) {
        			// we should have a filename and checksum at this point
        			if(gFile==null) {
        				System.err.println("filename null");
        			}
        			ChecksumDiff.addChecksum(gText.trim(), gFile, gChecksumMap);
        			gText = null;
        			gFile = null;
        		}
        	}
        }
    }

}
