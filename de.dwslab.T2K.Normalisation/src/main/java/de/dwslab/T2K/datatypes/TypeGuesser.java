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
package de.dwslab.T2K.datatypes;

import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.units.UnitParser;

/**
 * @author petar
 *
 */
public class TypeGuesser {

    private static Pattern listCharactersPattern = Pattern.compile("\\{|\\}");
    
    /**
     * use for rough type guesssing
     *
     * @param columnValue is the value of the column
     * @param columnHeader is the header of the column, often contains units
     * abbreviations
     * @param useUnit the typeGuesser will try to find units
     * @param unit the returning unit (if found)
     * @return
     */
    public ColumnType guessTypeForValue(String columnValue, Unit headerUnit) {
        if (checkIfList(columnValue)) {
            List<String> columnValues;
//            columnValue = columnValue.replace("{", "");
//            columnValue = columnValue.replace("}", "");
            columnValue = listCharactersPattern.matcher(columnValue).replaceAll("");
            columnValues = Arrays.asList(columnValue.split("\\|"));
            Map<DataType, Integer> countTypes = new HashMap<>();
            Map<Unit, Integer> countUnits = new HashMap<>();
            for(String singleValue : columnValues) {
                ColumnType guessedSingleType = guessTypeForSingleValue(singleValue, headerUnit);
                
                Integer cnt = countTypes.get(guessedSingleType.getType());
                if(cnt==null) {
                    cnt = 0;
                }
                countTypes.put(guessedSingleType.getType(), cnt+1);
//                if(countTypes.containsKey(guessedSingleType.getType())) {
//                    countTypes.put(guessedSingleType.getType(), countTypes.get(guessedSingleType.getType())+1);
//                }
//                else {
//                    countTypes.put(guessedSingleType.getType(), 1);
//                }
                
                cnt = countUnits.get(guessedSingleType.getUnit());
                if(cnt==null) {
                    cnt = 0;
                }
                countUnits.put(guessedSingleType.getUnit(), cnt+1);
//                if(countUnits.containsKey(guessedSingleType.getUnit())) {
//                    countUnits.put(guessedSingleType.getUnit(), countUnits.get(guessedSingleType.getUnit())+1);
//                }
//                else {
//                    countUnits.put(guessedSingleType.getUnit(), 1);
//                }
            }
            int max = 0;
            DataType finalType = null;
            for(DataType type : countTypes.keySet()) {
                if(countTypes.get(type)>max) {
                    max = countTypes.get(type);
                    finalType = type;
                }
            }
            max = 0;
            Unit finalUnit = null;
            for(Unit type : countUnits.keySet()) {
                if(countUnits.get(type)>max) {
                    max = countUnits.get(type);
                    finalUnit = type;
                }
            }
            return new ColumnType(finalType, finalUnit);
        }
        else {
            return guessTypeForSingleValue(columnValue, headerUnit);
        }
    }
    
    private static Pattern listPattern = Pattern.compile("^\\{.+\\|.+\\}$");
    
    private boolean checkIfList(String columnValue) {
//        if (columnValue.matches("^\\{.+\\|.+\\}$")) {
        if (columnValue!=null && listPattern.matcher(columnValue).matches()) {
            return true;
        }
        return false;
    }
    
    private ColumnType guessTypeForSingleValue(String columnValue, Unit headerUnit) {
    	if(columnValue!=null) {
	        // check the length
	        boolean validLenght = true;
	        if (columnValue.length() > 50) {
	            validLenght = false;
	        }
	        if (validLenght && Boolean.parseBoolean(columnValue)) {
	            return new ColumnType(DataType.bool,null);
	        }
	        if (URLParser.parseURL(columnValue)) {
	            return new ColumnType(DataType.link,null);
	        }
	        if (validLenght && GeoCoordinateParser.parseGeoCoordinate(columnValue)) {
	            return new ColumnType(DataType.coordinate,null);
	        }        
	        if (validLenght) {
	            try {
	                Date date = DateUtil.parse(columnValue);
	                if (date != null) {
	                    return new ColumnType(DataType.date,null);
	                }
	            } catch (Exception e) {
	            }
	        }
	        if (validLenght && NumericParser.parseNumeric(columnValue)) {        
	//            if(Variables.useUnitDetection) {
	                Unit unit = headerUnit;
	                if(headerUnit==null) {
	                    unit = UnitParser.checkUnit(columnValue);
	                }
	                return new ColumnType(DataType.unit,unit);
	//            }
	//            else {
	//                return new ColumnType(DataType.numeric,null);
	//            }
	        }      
    	}
        return new ColumnType(DataType.string, null);
    }
}
