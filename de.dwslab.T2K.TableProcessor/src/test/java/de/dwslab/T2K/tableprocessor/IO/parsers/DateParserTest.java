/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.tableprocessor.IO.parsers;

import de.dwslab.T2K.tableprocessor.IO.parsers.DateUtil;
import java.util.regex.Pattern;
import junit.framework.TestCase;

/**
 *
 * @author domi
 */
public class DateParserTest extends TestCase {
    
    public void testParse() {
        try {
            
        System.out.println(DateUtil.parse("jul 29, 1953"));
        System.out.println(DateUtil.parse("jul 1953"));
        System.out.println(DateUtil.parse("2011-05-17may 17, 2011"));
        
        } catch(Exception e) {
            System.out.println(e);
        }
    }
    
}
