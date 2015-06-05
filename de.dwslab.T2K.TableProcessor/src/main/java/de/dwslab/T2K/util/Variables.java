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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.util;

import de.dwslab.T2K.units.Unit_domi;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author domi
 */
public class Variables {
    
    //determines which string/character corresponds to the null value
    public static String nullValue = "NULL";
    //states which uniqueness rank a column must at least have to be the key column
    public static double keyUniqueness = 0.5;
    //the cell delimiter
    public static String delimiter = "\",\"";
    //determines whether he cell values are normalized or not
    public static Boolean normalizeValues = true;
    //remove customized stopwords
    public static List<String> stopWords = new ArrayList<>();
    //use unit detection
    public static boolean useUnitDetection = true;
    //Units
    public static List<Unit_domi> units = new ArrayList<>();
    
}
