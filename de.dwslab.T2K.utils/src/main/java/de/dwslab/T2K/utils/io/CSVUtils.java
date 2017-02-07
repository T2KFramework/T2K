/*
 * Copyright (C) 2015 T2K-Team, Data and Web Science Group, University of Mannheim (t2k@dwslab.de)
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
package de.dwslab.T2K.utils.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;

import au.com.bytecode.opencsv.CSVReader;

public class CSVUtils {

    protected static boolean verbose = false;
    public static void setVerbose(boolean verbose) {
        CSVUtils.verbose = verbose;
    }
    public static boolean isVerbose() {
        return verbose;
    }
    
    public static Collection<String[]> readCSV(String path) {
        return readCSV(path, null);
    }
    
	public static Collection<String[]> readCSV(String path, String delimiter)
	{
		Collection<String[]> lst = null;
		
		try {
		    if(isVerbose()) {
		        System.out.println("measuring size of " + path);
		    }
			BufferedReader r = new BufferedReader(new FileReader(path));
			int lines=0;
			while(r.readLine()!=null)
			{
				lines++;
			}
			r.close();
			
			lst = new ArrayList<String[]>(lines);
			
			if(isVerbose()) {
			    System.out.println("reading " + lines + " lines from " + path);
			}
			CSVReader reader = null;
			
			if(delimiter==null) {
			    reader = new CSVReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			} else {
			    reader = new CSVReader(new InputStreamReader(new FileInputStream(path), "UTF-8"), delimiter.charAt(0));
			}
			
			String[] values = null;
			
			while((values = reader.readNext()) != null)
			{
				lst.add(values);
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return lst;
	}
	
}
