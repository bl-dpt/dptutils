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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author wpalmer
 *
 */
@SuppressWarnings("serial")
public class ChecksumDiffTest {

	/**
	 * Test two empty sets compare ok
	 */
	@Test
	public void testCompareEqualsEmpty() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		ChecksumDiff.compare(setA, setB);
		
		assertTrue(setA.isEmpty());
		assertTrue(setB.isEmpty());
		
	}

	/**
	 * Test an empty set against a set with one entry
	 */
	@Test
	public void testCompareEqualsSetAData() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test.txt";
		
		setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		
		ChecksumDiff.compare(setA, setB);
		
		assertFalse(setA.isEmpty());
		assertTrue(setB.isEmpty());
		
		assertTrue(setA.keySet().size()==1);
		assertTrue(setA.containsKey(checksumOne));
		assertTrue(setA.get(checksumOne).size()==1);
		assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));
		
	}

	/**
	 * Test an empty set against a set with one entry
	 */
	@Test
	public void testCompareEqualsSetAData2() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test.txt";
		
		setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		
		ChecksumDiff.compare(setB, setA);
		
		assertFalse(setA.isEmpty());
		assertTrue(setB.isEmpty());
		
		assertTrue(setA.keySet().size()==1);
		assertTrue(setA.containsKey(checksumOne));
		assertTrue(setA.get(checksumOne).size()==1);
		assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));
		
	}

	/**
	 * Test an empty set against a set with one entry
	 */
	@Test
	public void testCompareEqualsSetBData() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test.txt";
		
		setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		
		ChecksumDiff.compare(setA, setB);
		
		assertTrue(setA.isEmpty());
		assertFalse(setB.isEmpty());
		
		assertTrue(setB.keySet().size()==1);
		assertTrue(setB.containsKey(checksumOne));
		assertTrue(setB.get(checksumOne).size()==1);
		assertTrue(setB.get(checksumOne).get(0).equals(filenameOne));
		
	}

	/**
	 * Test an empty set against a set with one entry
	 */
	@Test
	public void testCompareEqualsSetBData2() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test.txt";
		
		setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		
		ChecksumDiff.compare(setB, setA);
		
		assertTrue(setA.isEmpty());
		assertFalse(setB.isEmpty());
		
		assertTrue(setB.keySet().size()==1);
		assertTrue(setB.containsKey(checksumOne));
		assertTrue(setB.get(checksumOne).size()==1);
		assertTrue(setB.get(checksumOne).get(0).equals(filenameOne));
		
	}

	/**
	 * Test with two sets - same checksum, but different filenames
	 */
	@Test
	public void testCompareEqualsSameChecksumDifferentName() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test1.txt";
		final String filenameTwo = "test2.txt";
		
		setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); }});
		
		ChecksumDiff.compare(setA, setB);
		
		assertFalse(setA.isEmpty());
		assertFalse(setB.isEmpty());
		
		assertTrue(setA.keySet().size()==1);
		assertTrue(setA.containsKey(checksumOne));
		assertTrue(setA.get(checksumOne).size()==1);
		assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));
		
		assertTrue(setB.keySet().size()==1);
		assertTrue(setB.containsKey(checksumOne));
		assertTrue(setB.get(checksumOne).size()==1);
		assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));
		
	}

	/**
	 * Test with two sets - same checksum, but different filenames
	 */
	@Test
	public void testCompareEqualsSameChecksumDifferentName2() {
		Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
		Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();
		
		final String checksumOne = "DEADBEEF";
		final String filenameOne = "test1.txt";
		final String filenameTwo = "test2.txt";
		
		setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
		setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); }});
		
		ChecksumDiff.compare(setB, setA);
		
		assertFalse(setA.isEmpty());
		assertFalse(setB.isEmpty());
		
		assertTrue(setA.keySet().size()==1);
		assertTrue(setA.containsKey(checksumOne));
		assertTrue(setA.get(checksumOne).size()==1);
		assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));
		
		assertTrue(setB.keySet().size()==1);
		assertTrue(setB.containsKey(checksumOne));
		assertTrue(setB.get(checksumOne).size()==1);
		assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));
		
	}

	/**
	 * Test with two sets - same checksum, but different filenames plus an entry with 
	 * the same filename
	 */
    @Test
    public void testCompareEqualsSameChecksumDifferentNameAndSame() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); add(filenameThree); }});

        ChecksumDiff.compare(setA, setB);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumOne));
        assertTrue(setB.get(checksumOne).size()==1);
        assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));

    }

	/**
	 * Test with two sets - same checksum, but different filenames plus an entry with 
	 * the same filename
	 */
    @Test
    public void testCompareEqualsSameChecksumDifferentNameAndSame2() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); add(filenameThree); }});

        ChecksumDiff.compare(setB, setA);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumOne));
        assertTrue(setB.get(checksumOne).size()==1);
        assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));

    }

    /**
     * Test two sets with multiple files, one of which is in each set
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFiles() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});

        ChecksumDiff.compare(setA, setB);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumOne));
        assertTrue(setB.get(checksumOne).size()==1);
        assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));

    }

    /**
     * Test two sets with multiple files, one of which is in each set
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFiles2() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameTwo); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});

        ChecksumDiff.compare(setB, setA);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumOne));
        assertTrue(setB.get(checksumOne).size()==1);
        assertTrue(setB.get(checksumOne).get(0).equals(filenameTwo));

    }
    
    /**
     * Test two sets with different files
     */
    @Test
    public void testCompareEqualsSameChecksumDifferentFiles() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameTwo = "test4.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameTwo); }});

        ChecksumDiff.compare(setA, setB);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumTwo));
        assertTrue(setB.get(checksumTwo).size()==1);
        assertTrue(setB.get(checksumTwo).get(0).equals(filenameTwo));

    }
    
    /**
     * Test two sets with different files
     */
    @Test
    public void testCompareEqualsSameChecksumDifferentFiles2() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameTwo = "test4.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameTwo); }});

        ChecksumDiff.compare(setB, setA);

        assertFalse(setA.isEmpty());
        assertFalse(setB.isEmpty());

        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameOne));

        assertTrue(setB.keySet().size()==1);
        assertTrue(setB.containsKey(checksumTwo));
        assertTrue(setB.get(checksumTwo).size()==1);
        assertTrue(setB.get(checksumTwo).get(0).equals(filenameTwo));

    }


    /**
     * Test two sets with larg(er) amounts of data, all of which is identical
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFilesSame() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";
        
        final String checksumThree = "CAFECAFE";
        final String filenameFive = "test5.txt";
        final String filenameSix = "test6.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setA.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});
        
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});

        ChecksumDiff.compare(setA, setB);

        assertTrue(setA.isEmpty());
        assertTrue(setB.isEmpty());

    }

    /**
     * Test two sets with larg(er) amounts of data, all of which is identical
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFilesSame2() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";
        
        final String checksumThree = "CAFECAFE";
        final String filenameFive = "test5.txt";
        final String filenameSix = "test6.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setA.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});
        
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});

        ChecksumDiff.compare(setB, setA);

        assertTrue(setA.isEmpty());
        assertTrue(setB.isEmpty());

    }

    /**
     * Test two sets with larg(er) amounts of data, all of which is identical
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFilesSameOneDiff() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";
        
        final String checksumThree = "CAFECAFE";
        final String filenameFive = "test5.txt";
        final String filenameSix = "test6.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setA.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});
        
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});

        ChecksumDiff.compare(setA, setB);

        assertFalse(setA.isEmpty());
        assertTrue(setB.isEmpty());
        
        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameTwo));

    }

    /**
     * Test two sets with larg(er) amounts of data, all of which is identical
     */
    @Test
    public void testCompareEqualsSameChecksumMultipleFilesSameOneDiff2() {
        Map<String, ArrayList<String>> setA = new HashMap<String, ArrayList<String>>();
        Map<String, ArrayList<String>> setB = new HashMap<String, ArrayList<String>>();

        final String checksumOne = "DEADBEEF";
        final String filenameOne = "test1.txt";
        final String filenameTwo = "test2.txt";
        final String filenameThree = "test3.txt";

        final String checksumTwo = "BEEFCAFE";
        final String filenameFour = "test4.txt";
        
        final String checksumThree = "CAFECAFE";
        final String filenameFive = "test5.txt";
        final String filenameSix = "test6.txt";

        setA.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameTwo); add(filenameThree); }});
        setA.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setA.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});
        
        setB.put(checksumOne, new ArrayList<String>() {{ add(filenameOne); add(filenameThree); }});
        setB.put(checksumTwo, new ArrayList<String>() {{ add(filenameFour); }});
        setB.put(checksumThree, new ArrayList<String>() {{ add(filenameFive); add(filenameSix); }});

        ChecksumDiff.compare(setB, setA);

        assertFalse(setA.isEmpty());
        assertTrue(setB.isEmpty());
        
        assertTrue(setA.keySet().size()==1);
        assertTrue(setA.containsKey(checksumOne));
        assertTrue(setA.get(checksumOne).size()==1);
        assertTrue(setA.get(checksumOne).get(0).equals(filenameTwo));

    }

}
