/*
 * Copyright 2014 The British Library / The SCAPE Project Consortium
 * Authors: Alecs Geuder (alecs.geuder@bl.uk),
 *          William Palmer (William.Palmer@bl.uk)
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

package uk.bl.dpt.utils.schematron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.bl.dpt.utils.util.InvertedDict;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Validate the contents of an inputFile with an already created schematron XSLT.
 * (right side of http://www.xml.com/2003/11/12/graphics/Processor.jpg)
 */
public class Validator {

    private static final Logger logger = LoggerFactory.getLogger(Validator.class);

    /**
     * The namespace for Schematron Validation Report Language
     */
    private static final String namespace = "http://purl.oclc.org/dsdl/svrl";

    private byte[] xslt;
    LinkedHashMap<String, String> assertPatternMap;

    /**
     * A collection of Strings that represents the assertion failures of interest.
     * On how to this is used see also the tests
     */
    private Set<String> failureFilter;
    private LinkedHashMap<String, InvertedDict> report;

    public Validator(byte[] aXslt, LinkedHashMap<String, String> aPMap) {
        this.xslt = aXslt;
        this.assertPatternMap = aPMap;
        report = new LinkedHashMap<String, InvertedDict>();
        // initialise the report with all patterns we want to report on
        for (String pattern : this.assertPatternMap.values()) {
            report.put(pattern, new InvertedDict());
            logger.debug("added pattern {} to default report", pattern);
        }
        logger.trace("Initialising Validator with xslt: {}", new String(xslt));
    }

    /**
     * Takes a Source and returns the validation result as a ByteArrayOutputStream
     *
     * @param source The source to validate
     * @return ByteArrayOutputStream
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     */
    public ByteArrayOutputStream validate(Source source) throws TransformerException, ParserConfigurationException, SAXException, IOException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer validateInput = factory.newTransformer(new StreamSource(new ByteArrayInputStream(xslt)));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        logger.debug("validating inputStream");
        validateInput.transform(source, new StreamResult(output));
        logger.trace("validation result: {}", output);
        createReport(output);
        return output;
    }

    /**
     * Takes a source and an outputFile where it writes the validation result to, returns the overall
     * information whether the result is valid.
     *
     * @param source The source to validate
     * @return boolean
     * @throws javax.xml.transform.TransformerException
     */
    public boolean validate(Source source, File outputFile) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        ByteArrayOutputStream output = validate(source);
        output.writeTo(new FileOutputStream(outputFile));
        createReport(output);
        return resultIsValid();
    }

    /**
     * Returns true if no assertion failures of interest are found,
     * e.g. if the generated * report is empty, otherwise returns false.
     *
     * NOTE that this, if {@link #failureFilter} is set, skips all failures
     * that subsequently aren't mentioned in the report.
     *
     * @return boolean
     */
    public boolean resultIsValid() {
        for (String pattern : report.keySet()) {
            if (!report.get(pattern).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an inverted map containing all failures of interest as keys
     * and their count as values.
     *
     * NOTE that this, if {@link #failureFilter} is set, only writes failures
     * to the report that are in {@link #failureFilter}.
     *
     * @param output the ByteArrayOutputStream to base the report creation on.
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     */
    private void createReport(ByteArrayOutputStream output) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // need this in order to call doc.getElementsByTagNameNS later
        DocumentBuilder docB = dbf.newDocumentBuilder();
        Document doc = docB.parse(new ByteArrayInputStream(output.toByteArray()));
        NodeList failed = doc.getElementsByTagNameNS(namespace, "failed-assert");
        for (int i=0;i<failed.getLength();i++) {
            String fText = failed.item(i).getTextContent().replaceAll("(\r\n|\n)", "").trim();
            String p = this.assertPatternMap.get(fText);
            if (this.failureFilter == null || this.failureFilter.contains(fText)) {
                report.get(p).update(fText);
            }
            logger.debug("added results for pattern {} to report: {}", p, report.get(p));
        }
    }

    // Getters&Setters
    @SuppressWarnings("unused")
    public Set<String> getFailureFilter() {
        return failureFilter;
    }
    public void setFailureFilter(Set<String> failureFilter) {
        this.failureFilter = failureFilter;
    }

    public LinkedHashMap<String, InvertedDict> getReport() {
        return report;
    }
}
