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

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.*;

/**
 * Creates XSLT Processors from schematron files that can validate xml documents.
 * (left side of http://www.xml.com/2003/11/12/graphics/Processor.jpg)
 */
public class ValidatorFactory {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorFactory.class);

    private byte[] schXSLT;
    private File schXSLTFile;

    DOMSource doc;

    private Map<String, String> assertPatternMap = new HashMap<String,String>();

    /**
     * The namespace for Schematron schemas
     */
    private static final String namespace = "http://purl.oclc.org/dsdl/schematron";

    /**
     * Use this constructor if the XSLT resulting from the merge of the
     * generic skeleton files and a domain-specific .sch schematron file
     * shall be calculated on the fly and not saved to a file.
     */
    public ValidatorFactory() {}

    /**
     * Constructor for the case where a file-path to the XSLT file from the merge of the
     * generic skeleton files and a domain-specific .sch schematron file is
     * provided.
     *
     * In case the file content already exists it is directly used.
     * In case of an empty file the translated XSLT is written to the file.
     *
     * @param schematronSXLTFilePath The path to the XSLT file.
     * @throws java.io.IOException
     */
    @SuppressWarnings("unused")
    public ValidatorFactory(String schematronSXLTFilePath) throws IOException {
        this.schXSLTFile = new File(schematronSXLTFilePath);
        if (schXSLTFile.exists()) {
            this.schXSLT = Files.toByteArray(schXSLTFile);
        }
    }

    /**
     * The collection of iso-schematron files used to create the XSLT processor.
     * (http://www.schematron.com/tmp/iso-schematron-xslt1.zip)
     * TODO: make this configurable?
     */
    private final static class ISOFiles {
        // (1) ISO_DSDL:
        // "This is a macro processor to assemble the schema from various parts.
        // If your schema is not in separate parts, you can skip this stage.
        // This stage also generates error messages for some common XPath syntax problems."
        final static String ISO_DSDL="/iso-schematron/iso_dsdl_include.xsl";
        // (2) ISO_ABSTRACT:
        // "This is a macro processor to convert abstract patterns to real patterns.
        // If your schema does not use abstract patterns, you can skip this
        // stage."
        final static String ISO_ABSTRACT="/iso-schematron/iso_abstract_expand.xsl";
        // (3) ISO_SVRL:
        // compiles the Schematron schema into an XSLT script
        final static String ISO_SVRL="/iso-schematron/iso_svrl_for_xslt1.xsl";
    }

    //see here: http://stackoverflow.com/a/12453881
    /**
     * Implement a URIResolver so that XSL files can be found in the jar resources
     * @author wpalmer
     */
    public static class ResourceResolver implements URIResolver {
        public StreamSource resolve(String pRef, String pBase) {
            return new StreamSource(ResourceResolver.class.getClassLoader().getResourceAsStream("iso-schematron/"+pRef));
        }
    }

    /**
    * Returns a XSLT OutputStream converted from a Schematron .sch
    *
    * If the internal byte[] buffer is already populated it just returns that one.
    * If a file-path was specified in the constructor, the results are also written there.
    *
    * @param schSource The source of the schematron file.
    * @return a byte[] representation of the XSLT OutputStream
    * @throws javax.xml.transform.TransformerException
    * @throws java.io.IOException
    * @throws javax.xml.parsers.ParserConfigurationException
    * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
    */
    private byte[] schToXSLT(Source schSource) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        if (schXSLT == null) {
            ByteArrayOutputStream streamedSchematronSXLT = new ByteArrayOutputStream();
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(new ResourceResolver());

            logger.debug("Building schematronSXLT: assembling the schema from possibly various parts..");
            logger.trace("..using as input: {})", schSource);
            Transformer assemble = factory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(ISOFiles.ISO_DSDL)));
            ByteArrayOutputStream assembleOutput = new ByteArrayOutputStream();
            assemble.transform(schSource, new StreamResult(assembleOutput));

            logger.debug("Building schematronSXLT: converting abstract patterns to real patterns..");
            Transformer abstract2real = factory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(ISOFiles.ISO_ABSTRACT)));
            ByteArrayOutputStream abstract2realOutput = new ByteArrayOutputStream();
            logger.trace("..using as input: {}", abstract2realOutput);
            abstract2real.transform(new StreamSource(new ByteArrayInputStream(assembleOutput.toByteArray())),
                    new StreamResult(abstract2realOutput));

            logger.debug("Building schematronSXLT: compiling the .sch into an XSLT script..");
            Transformer toXSLT = factory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(ISOFiles.ISO_SVRL)));
            toXSLT.setParameter("terminate", "false"); //don't halt on errors
            logger.trace("..using as input: {}", abstract2realOutput);
            toXSLT.transform(new StreamSource(new ByteArrayInputStream(abstract2realOutput.toByteArray())),
                    new StreamResult(streamedSchematronSXLT));

            // in case the constructor was provided with a path to an empty (not yet existing) file,
            // write this now.
            if (this.schXSLTFile != null && !this.schXSLTFile.exists()) {
                streamedSchematronSXLT.writeTo(new FileOutputStream(this.schXSLTFile));
            }
            schXSLT = streamedSchematronSXLT.toByteArray();
        }
        return schXSLT;
    }

    /**
     * Removes all pattern elements from a DOM whose names are not specified in the given patternFilter.
     *
     * (for an example see also the tests.
     *
     * @param doc the original schema as Document
     * @param patternFilter a set of strings representing the names of pattern elements in a schematron schema file
     * @return the filtered DOM as {@link javax.xml.transform.dom.DOMSource}
     * @throws org.xml.sax.SAXException
     * @throws java.io.IOException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    private DOMSource filterPatterns(Document doc, Set<String> patternFilter) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        if (this.doc != null) {
            return this.doc;
        }
        NodeList patterns = doc.getElementsByTagNameNS(namespace, "pattern");
        for (int p=0;p<patterns.getLength();p++) {
            Element pattern = (Element) patterns.item(p);
            if (patternFilter != null && !patternFilter.contains(pattern.getAttribute("name").trim())) {
                pattern.getParentNode().removeChild(pattern);
                p--; // prevent off-by-one error
            } else {
                NodeList rules = pattern.getElementsByTagNameNS(namespace, "rule");
                for (int r=0;r<rules.getLength();r++) {
                    Element rule = (Element) rules.item(r);
                    NodeList tests = rule.getElementsByTagNameNS(namespace, "assert");
                    // add pattern to each assert it contains
                    for (int i=0;i<tests.getLength();i++) {
                        Element test = (Element) tests.item(i);
                        logger.debug("Adding (grand-)parent pattern {} to assert {}", pattern.getAttribute("name"), test.getTextContent());
                        assertPatternMap.put(test.getTextContent(), pattern.getAttribute("name"));
                    }
                }
            }
        }
        this.doc = new DOMSource(doc);
        return this.doc;
    }

    private Document toDoc(StreamSource original) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // need this in order to call doc.getElementsByTagNameNS later
        DocumentBuilder dB = dbf.newDocumentBuilder();
        return dB.parse(original.getInputStream());
    }

    public Validator newValidator(String schemaPath) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return newValidator(new StreamSource(new FileInputStream(schemaPath)));
    }

    public Validator newValidator(StreamSource schemaInput) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return newValidator(schemaInput, null);
    }

    public Validator newValidator(String schemaPath, Set<String> patternFilter) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return newValidator(new StreamSource(new FileInputStream(schemaPath)), patternFilter);
    }

    /**
     * Create a new {@link Validator} providing a schematron schema input and a
     * pattern filter.
     *
     * @param schemaInput the input containing the schematron schema (the
     * 'policy')
     * @param patternFilter contains a set of strings, each representing a
     *        pattern element in the schematron schema; all patterns of which the
     *        name is not mentioned will be ignored. <br>
     *        NOTE: patternFilter can be null, in this case all patterns will be
     *              taken into account
     * @return a new Validator instance
     * @throws javax.xml.transform.TransformerException
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    public Validator newValidator(StreamSource schemaInput, Set<String> patternFilter) throws TransformerException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        return new Validator(schToXSLT(filterPatterns(toDoc(schemaInput), patternFilter)), assertPatternMap);
    }

    @SuppressWarnings("unused")
    public Collection<String> getPatternNames(StreamSource schemaInput, Set<String> patternFilter) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        filterPatterns(toDoc(schemaInput), patternFilter);
        return new LinkedHashSet<String>(assertPatternMap.values());
    }

    @SuppressWarnings("unused")
    public Map<String, String> getAssertPatternMap(StreamSource schemaInput, Set<String> patternFilter) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, TransformerException {
        if (assertPatternMap.size() == 0) {
            schToXSLT(filterPatterns(toDoc(schemaInput), patternFilter));
        }
        return assertPatternMap;
    }
}
