package de.mannheim.uni.tableprocessor.IO.parsers;

import de.dwslab.T2K.tableprocessor.IO.parsers.BooleanParser;
import junit.framework.TestCase;

public class BooleanParserTest extends TestCase {

    public void testParse() {
        assertEquals(true, BooleanParser.parseBoolean("true"));
        assertEquals(true, BooleanParser.parseBoolean("false"));
        
        assertEquals(true, BooleanParser.parseBoolean("yes"));
        assertEquals(true, BooleanParser.parseBoolean("no"));
        
        assertEquals(true, BooleanParser.parseBoolean("1"));
        assertEquals(true, BooleanParser.parseBoolean("0"));
        
        assertEquals(false, BooleanParser.parseBoolean("right"));
        assertEquals(false, BooleanParser.parseBoolean("wrong"));
    }
    
}
