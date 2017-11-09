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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.joda.time.DateTime;

import de.dwslab.T2K.units.Unit;
import de.dwslab.T2K.units.UnitParser;

public class TypeConverter {

	private boolean verbose = false;
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * Converts a String into the given type
	 * @param Value
	 * @param type
	 * @param unit
	 * @return
	 * @throws ParseException
	 */
    public Object typeValue(String value, DataType type, Unit unit) {
        Object typedValue = null;
        
        if(value!=null) {
	        try {
		        switch (type) {
		            case string:
		                typedValue = value;
		                break;
		            case date:
		                typedValue = new DateTime(DateUtil.parse(value));
		                break;
		            case numeric:
		                //TODO: how to handle numbers with commas (German style)
		                if (unit != null) {
		                    typedValue = UnitParser.transformUnit(value, unit);
		
		                } else {
		                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
		                    NumberFormat format = NumberFormat.getInstance(Locale.US);
		                    Number number = format.parse(value);
		                    typedValue = number.doubleValue();
		                }
		                break;
		            case bool:
		                typedValue = Boolean.parseBoolean(value);
		                break;
		            case coordinate:
		                typedValue = value;
		                break;
		            case link:
		                typedValue = value;
		            default:
		                break;
		        }
	        } catch(ParseException e) {
	        	if(isVerbose()) {
	        		e.printStackTrace();
	        	}
	        }
        }
        
        return typedValue;
    }
	
}
