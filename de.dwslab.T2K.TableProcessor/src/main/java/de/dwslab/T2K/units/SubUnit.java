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
package de.dwslab.T2K.units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author petar
 * 
 */
public class SubUnit
	implements java.io.Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6545299925932595153L;

	private String name;

	boolean isConvertible;

	private double rateToConvert;

	private List<String> abbrevations;
	private HashMap<String, Pattern> abbrPatterns1;
	private HashMap<String, Pattern> abbrPatterns2;
	
	private Unit baseUnit;

	private String newValue;

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Unit getBaseUnit() {
		return baseUnit;
	}

	public void setBaseUnit(Unit baseUnit) {
		this.baseUnit = baseUnit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isConvertible() {
		return isConvertible;
	}

	public void setConvertible(boolean isConvertible) {
		this.isConvertible = isConvertible;
	}

	public double getRateToConvert() {
		return rateToConvert;
	}

	public void setRateToConvert(double rateToConvert) {
		this.rateToConvert = rateToConvert;
	}

	public List<String> getAbbrevations() {
		return abbrevations;
	}

	public void setAbbrevations(List<String> abbrevations) {
		this.abbrevations = abbrevations;
		
		for(String s : abbrevations)
		{
			abbrPatterns1.put(s, Pattern.compile("\\d{1,20}.*" + Pattern.quote(s.toLowerCase())));
			abbrPatterns2.put(s, Pattern.compile(Pattern.quote(s.toLowerCase()) + ".*\\d{1,20}"));
			
		}		
		
	}

	public void setAbbrevationsFromStringField(String[] abbrs) {

		for (String str : abbrs) {
			abbrevations.add(str.replace("\"", ""));
		}
	}

	public SubUnit() {
		abbrevations = new ArrayList<String>();
		abbrPatterns1 = new HashMap<String, Pattern>();
		abbrPatterns2 = new HashMap<String, Pattern>();
	}
	
	public String getMatchingAbbreviation(String text)
	{
		for (String unitName : getAbbrevations()) {
			//if(abbrPatterns1.get(unitName).matcher(text).matches())
		    //TODO check whether this works as good as the regex before!
		    if(text.contains(unitName))
				return unitName;
		}
		return null;
	}
	
	public String getMatchingAbbreviation2(String text)
	{
		for (String unitName : getAbbrevations()) {
			//if(abbrPatterns2.get(unitName).matcher(text).matches())
	        //TODO check whether this works as good as the regex before!
            if(text.contains(unitName)) {
				return unitName;
            }
		}
		return null;
	}
}
