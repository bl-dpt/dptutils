/*
 * Copyright 2013 The British Library / The SCAPE Project Consortium
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

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * This basic test suite explains the functioning of the schematron
 * validation API following the examples of the great introduction at
 * http://www.xml.com/pub/a/2003/11/12/schematron.html .
 *
 * The context is about the validation of an XML document that in some
 * form contains a `Person` element that is expected to have a `Title`
 * attribute and the two child elements `Name` and `Gender`.
 *
 * @author ageuder
 *
 */
public class ValidatorTest {

    File schemaFile;

    /**
     * Creates a schema file containing the following rules:
     * - The context element (Person) should have an attribute Title
     * - The context element should contain two child elements, Name and Gender
     * - The child element Name should appear before the child element Gender
     * - If attribute Title has the value 'Mr' the element Gender must have the value 'Male'
     *
     * @throws java.io.IOException
     */
    @Before
    public void setUpSchema() throws IOException {
        schemaFile = File.createTempFile("schemaFile", null);
        PrintWriter pw = new PrintWriter(schemaFile);
        try {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<sch:schema xmlns:sch=\"http://purl.oclc.org/dsdl/schematron\">");
            pw.println("    <sch:pattern name=\"Check structure\">");
            pw.println("        <sch:rule context=\"Person\">");
            pw.println("            <sch:assert test=\"@Title\">The element Person must have a Title attribute</sch:assert>");
            pw.println("            <sch:assert test=\"count(*) = 2 and count(Name) = 1 and count(Gender) = 1\">The element Person should have the child elements Name and Gender.</sch:assert>");
            pw.println("            <sch:assert test=\"*[1] = Name\">The element Name must appear before element Gender.</sch:assert>");
            pw.println("        </sch:rule>");
            pw.println("    </sch:pattern>");
            pw.println("    <sch:pattern name=\"Check co-occurrence constraints\">");
            pw.println("        <sch:rule context=\"Person\">");
            pw.println("            <sch:assert test=\"(@Title = 'Mr' and Gender = 'Male') or @Title != 'Mr'\">If the Title is \"Mr\" then the Gender of the person must be \"Male\".</sch:assert>");
            pw.println("        </sch:rule>");
            pw.println("    </sch:pattern>");
            pw.println("</sch:schema>");
        } finally {
            pw.close();
        }
    }

    /**
     * Tests an XML document that is expected to pass the validation.
     *
     * @return
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testCorrectDoc() throws IOException, TransformerException, SAXException, ParserConfigurationException, XPathExpressionException {
        File correctDoc = File.createTempFile("correctDoc", null);
        PrintWriter pw = new PrintWriter(correctDoc);
        try {
            pw.println("<Person Title=\"Mr\">");
            pw.println("    <Name>Eddie</Name>");
            pw.println("    <Gender>Male</Gender>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }
        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath());
        ByteArrayOutputStream output = validator.validate(new StreamSource(correctDoc));

        DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docB.parse(new ByteArrayInputStream(output.toByteArray()));

        NodeList failed = doc.getElementsByTagName("svrl:failed-assert");
        assertThat(failed.getLength(), equalTo(0));

        // check whether the Validator's isValid method agrees
        assertTrue(validator.resultIsValid());
    }

    /**
     * Creates a file in which the order of `Gender` and `Name` is opposite of
     * how expected.
     *
     * @return
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testGenderBeforeName() throws IOException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
        File genderBeforeName = File.createTempFile("genderBeforeName", null);
        PrintWriter pw = new PrintWriter(genderBeforeName);
        try {
            pw.println("<Person Title=\"Mr\">");
            pw.println("    <Gender>Male</Gender>");
            pw.println("    <Name>Eddie</Name>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }

        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath());
        ByteArrayOutputStream output = validator.validate(new StreamSource(genderBeforeName));

        DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docB.parse(new ByteArrayInputStream(output.toByteArray()));

        NodeList failed = doc.getElementsByTagName("svrl:failed-assert");
        assertThat(failed.getLength(), equalTo(1));
        assertThat(failed.item(0).getTextContent(), containsString("The element Name must appear before element Gender."));

        // check whether the Validator's isValid method agrees
        assertFalse(validator.resultIsValid());
    }


    /**
     * Creates a file where a `Person` is called "Mr" but has their `Gender` set as Male,
     * which according to the very un-queer condition in the schema file is expected to fail.
     *
     * @return
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testMrButFemale() throws IOException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
        File mrButFemale = File.createTempFile("mrButFemale", null);
        PrintWriter pw = new PrintWriter(mrButFemale);
        try {
            pw.println("<Person Title=\"Mr\">");
            pw.println("    <Name>Eddie</Name>");
            pw.println("    <Gender>Female</Gender>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }

        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath());
        ByteArrayOutputStream output = validator.validate(new StreamSource(mrButFemale));

        DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docB.parse(new ByteArrayInputStream(output.toByteArray()));

        NodeList failed = doc.getElementsByTagName("svrl:failed-assert");
        assertThat(failed.getLength(), equalTo(1));
        assertThat(failed.item(0).getTextContent(), containsString("If the Title is \"Mr\" then the Gender of the person must be \"Male\"."));

        // check whether the Validator's isValid method agrees
        assertFalse(validator.resultIsValid());
    }

    /**
     * Creates an XML document without a Title attribute and without a Name element.
     *
     * This is expected to fail the validation for four reasons:
     * (1) no Title attribute for the Person element provided
     * (2) no Title means no 'Mr', that violates that a 'male' Gender has to have a 'Mr' title
     * (3) no Name element violates 'must have Name and Gender'
     * (4) no Name element violates the order constraint 'Name before Gender'
     *
     * @return
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testNoTitleNoName() throws IOException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
        File noTitleNoName = File.createTempFile("noTitleNoName", null);
        PrintWriter pw = new PrintWriter(noTitleNoName);
        try {
            pw.println("<Person>");
            pw.println("    <Gender>Male</Gender>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }

        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath());
        ByteArrayOutputStream output = validator.validate(new StreamSource(noTitleNoName));

        DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docB.parse(new ByteArrayInputStream(output.toByteArray()));

        NodeList failed = doc.getElementsByTagName("svrl:failed-assert");
        assertThat(failed.getLength(), equalTo(4));
        assertThat(failed.item(0).getTextContent(), containsString("The element Person must have a Title attribute"));
        assertThat(failed.item(1).getTextContent(), containsString("The element Person should have the child elements Name and Gender."));
        assertThat(failed.item(2).getTextContent(), containsString("The element Name must appear before element Gender."));
        assertThat(failed.item(3).getTextContent(), containsString("If the Title is \"Mr\" then the Gender of the person must be \"Male\"."));

        // check whether the Validator's isValid method agrees
        assertFalse(validator.resultIsValid());
    }

    /**
     * This test contains an example on how {@link Validator#setFailureFilter(java.util.Set)} can be used.
     *
     * The input data is exactly the same as for {@link #testNoTitleNoName()}; if then a set of
     * strings representing the 'failures of interest' (the text description of `failed-assert`
     * elements in the schematron policy file) is provided we expect the validation report to
     * include only these. Also the validity is checked only on them.
     *
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testNoTitleNoNameReportWithFailureFilter() throws IOException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
        File noTitleNoName = File.createTempFile("noTitleNoName", null);
        PrintWriter pw = new PrintWriter(noTitleNoName);
        try {
            pw.println("<Person>");
            pw.println("    <Gender>Male</Gender>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }

        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath());
        @SuppressWarnings("serial")
        // say we are interested only in one assert per pattern, the name and gender related one and the more complicated title-gender relation:
        Set<String> failureFilter = new HashSet<String>() {{
            add("The element Person should have the child elements Name and Gender.");
            add("If the Title is \"Mr\" then the Gender of the person must be \"Male\".");
        }};
        validator.setFailureFilter(failureFilter);
        validator.validate(new StreamSource(noTitleNoName));

        Map <String, ? extends Map<String, Integer>> report = validator.getReport();

        Map <String, Integer> structureAsserts = report.get("Check structure");
        assertThat(structureAsserts.size(), equalTo(1));
        assertThat(structureAsserts.get("The element Person should have the child elements Name and Gender."), equalTo(1));

        Map <String, Integer> coOccurrenceConstraints = report.get("Check co-occurrence constraints");
        assertThat(coOccurrenceConstraints.size(), equalTo(1));
        assertThat(coOccurrenceConstraints.get("If the Title is \"Mr\" then the Gender of the person must be \"Male\"."), equalTo(1));

        // check whether the Validator's isValid method agrees
        assertFalse(validator.resultIsValid());
    }

    /**
     * Same test setup as {@link #testNoTitleNoName()} but with a patternFilter, that removes
     * all but the specified pattern nodes from the schema prior to execution.
     *
     * This obviously removes all asserts of other patterns as well in the report and hence
     * the validity only refers to the asserts of the remaining patterns.
     *
     *
     * @throws java.io.IOException
     * @throws javax.xml.transform.TransformerException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.xpath.XPathExpressionException
     */
    @Test
    public void testPatternFilter() throws IOException, TransformerException, ParserConfigurationException, SAXException, XPathExpressionException {
        File noTitleNoName = File.createTempFile("noTitleNoName", null);
        PrintWriter pw = new PrintWriter(noTitleNoName);
        try {
            pw.println("<Person>");
            pw.println("    <Gender>Male</Gender>");
            pw.println("</Person>");
        } finally {
            pw.close();
        }

        @SuppressWarnings("serial")
        Set<String> patternFilter = new HashSet<String>() {{
            add("Check structure");
        }};
        Validator validator = new ValidatorFactory().newValidator(schemaFile.getPath(), patternFilter);
        validator.validate(new StreamSource(noTitleNoName));

        Map <String, ? extends Map<String, Integer>> report = validator.getReport();

        Map <String, Integer> structureAsserts = report.get("Check structure");
        assertThat(structureAsserts.size(), equalTo(3));
        assertThat(structureAsserts.get("The element Person must have a Title attribute"), equalTo(1));
        assertThat(structureAsserts.get("The element Person should have the child elements Name and Gender."), equalTo(1));
        assertThat(structureAsserts.get("The element Name must appear before element Gender."), equalTo(1));

        Map <String, Integer> coOccurrenceConstraints = report.get("Check co-occurrence constraints");
        assertThat(coOccurrenceConstraints, equalTo(null));
        // check whether the Validator's isValid method agrees
        assertFalse(validator.resultIsValid());
    }

}
