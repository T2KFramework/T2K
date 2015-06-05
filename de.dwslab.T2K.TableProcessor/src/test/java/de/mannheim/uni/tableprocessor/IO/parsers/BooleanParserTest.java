/**
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of
							Mannheim (t2k@dwslab.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
