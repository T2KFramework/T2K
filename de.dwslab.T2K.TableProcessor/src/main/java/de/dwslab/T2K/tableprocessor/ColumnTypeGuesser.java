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
package de.dwslab.T2K.tableprocessor;

import java.util.Date;

import de.dwslab.T2K.tableprocessor.IO.parsers.DateUtil;
import de.dwslab.T2K.tableprocessor.IO.parsers.GeoCoordinateParser;
import de.dwslab.T2K.tableprocessor.IO.parsers.NumericParser;
import de.dwslab.T2K.tableprocessor.IO.parsers.URLParser;
import de.dwslab.T2K.tableprocessor.model.TableColumn.ColumnDataType;
import de.dwslab.T2K.units.UnitParserDomi;
import de.dwslab.T2K.units.Unit_domi;
import de.dwslab.T2K.util.Variables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author petar
 *
 */
public class ColumnTypeGuesser {
    
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
    public ColumnType guessTypeForValue(String columnValue, Unit_domi headerUnit) {
        if (checkIfList(columnValue)) {
            List<String> columnValues;
//            columnValue = columnValue.replace("{", "");
//            columnValue = columnValue.replace("}", "");
            columnValue = listCharactersPattern.matcher(columnValue).replaceAll("");
            columnValues = Arrays.asList(columnValue.split("\\|"));
            Map<ColumnDataType, Integer> countTypes = new HashMap<>();
            Map<Unit_domi, Integer> countUnits = new HashMap<>();
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
            ColumnDataType finalType = null;
            for(ColumnDataType type : countTypes.keySet()) {
                if(countTypes.get(type)>max) {
                    max = countTypes.get(type);
                    finalType = type;
                }
            }
            max = 0;
            Unit_domi finalUnit = null;
            for(Unit_domi type : countUnits.keySet()) {
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
        if (listPattern.matcher(columnValue).matches()) {
            return true;
        }
        return false;
    }
    
    private ColumnType guessTypeForSingleValue(String columnValue, Unit_domi headerUnit) {
        // check the length
        boolean validLenght = true;
        if (columnValue.length() > 50) {
            validLenght = false;
        }
        if (validLenght && Boolean.parseBoolean(columnValue)) {
            return new ColumnType(ColumnDataType.bool,null);
        }
        if (URLParser.parseURL(columnValue)) {
            return new ColumnType(ColumnDataType.link,null);
        }
        if (validLenght && GeoCoordinateParser.parseGeoCoordinate(columnValue)) {
            return new ColumnType(ColumnDataType.coordinate,null);
        }
        if (validLenght) {
            try {
                Date date = DateUtil.parse(columnValue);
                if (date != null) {
                    return new ColumnType(ColumnDataType.date,null);
                }
            } catch (Exception e) {
            }
        }
        if (validLenght && NumericParser.parseNumeric(columnValue)) {        
            if(Variables.useUnitDetection) {
                Unit_domi unit = headerUnit;
                if(headerUnit==null) {
                    unit = UnitParserDomi.checkUnit(columnValue);
                }
                return new ColumnType(ColumnDataType.unit,unit);
            }
            else {
                return new ColumnType(ColumnDataType.numeric,null);
            }
        }        
        return new ColumnType(ColumnDataType.string, null);
    }
}
