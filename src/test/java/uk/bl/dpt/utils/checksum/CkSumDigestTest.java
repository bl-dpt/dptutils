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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.bl.dpt.utils.checksum.cksum.CkSumProvider;

/**
 * Test the CkSumDigest class
 * @author wpalmer
 *
 */
@SuppressWarnings("javadoc")
public class CkSumDigestTest {

	private final static String res = "src/test/resources/";
	
	/**
	 * Register the security provider
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		CkSumProvider.register();
	}

	@Test
	public void emptyFileTest() throws FileNotFoundException {
		
		File test = new File(res+"cksum/empty.txt");
		String expectedChecksum = "FFFFFFFF";

		Map<String, String> pChecksums = new HashMap<String, String>();
		ChecksumUtil.calcChecksums(test, pChecksums);
		assertTrue(pChecksums.get("cksum").equals(expectedChecksum));
	}

	@Test
	public void testFileTest() throws FileNotFoundException {

		File test = new File(res+"cksum/test.txt");
		String expectedChecksum = "B75D6A42";

		Map<String, String> pChecksums = new HashMap<String, String>();
		ChecksumUtil.calcChecksums(test, pChecksums);
		assertTrue(pChecksums.get("cksum").equals(expectedChecksum));
		
	}

}
