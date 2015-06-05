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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author petar
 * 
 */
public class UnitManager {

	private Pattern unitPattern;
	
	private List<Unit> units;

	public List<Unit> getUnits() {
		return units;
	}

	public void setUnits(List<Unit> units) {
		this.units = units;
	}

	public UnitManager() {
		// TODO Auto-generated constructor stub
		if (units == null)
			try {
				loadUnits();
			} catch (Exception e) {
				// TODO: handle exception
			}
		
		unitPattern = Pattern.compile("(?!=\\d.\\d.})([\\d.,]+)");
	}

	public void loadUnits() {
		units = new ArrayList<Unit>();

		units.add(readCurrencies());
		units.addAll(readAllConvertibleUnits());
		// System.out.println("Units are loaded");
	}

	private static List<Unit> readAllConvertibleUnits() {
		List<Unit> units = new ArrayList<Unit>();
		File folder = new File("Units/Convertible");

		for (File fileEntry : folder.listFiles()) {

			Unit unit = readConvertibleUnit(fileEntry.getPath());
			units.add(unit);
		}
		return units;
	}

	private static Unit readCurrencies() {
		Unit unit = new Unit();
		unit.setName("Currency");
		try {
			File fileDir = new File("Units/Currency.txt");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));

			List<SubUnit> subUnits = new ArrayList<SubUnit>();

			String fileLine = "";
			while ((fileLine = in.readLine()) != null) {
				String[] parts = fileLine.split("\\|");
				SubUnit subUnit = new SubUnit();
				subUnit.setName(parts[0].replace("\"", ""));
				subUnit.setConvertible(false);
				String[] subUnitsStrs = parts[1].split(",");
				List<String> abbrevations = new ArrayList<String>();
				for (String str : subUnitsStrs) {
					abbrevations.add(str.replace("\"", ""));
				}
				subUnit.setAbbrevations(abbrevations);
				subUnit.setBaseUnit(unit);
				subUnits.add(subUnit);
			}
			unit.setSubunits(subUnits);
			in.close();
		} catch (Exception e) {
			System.err.println("Could not load currencies (Units/Currency.txt missing?)!");
		}

		return unit;
	}

	private static Unit readConvertibleUnit(String unitPath) {
		Unit unit = new Unit();
		String unitName = unitPath.replace(".txt", "");
		if (unitName.contains("\\"))
			unitName = unitName.substring(unitName.lastIndexOf("\\") + 1,
					unitName.length());
		if (unitName.contains("/"))
			unitName = unitName.substring(unitName.lastIndexOf("/") + 1,
					unitName.length());
		unit.setName(unitName);
		try {
			File fileDir = new File(unitPath);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileDir), "UTF8"));

			List<SubUnit> subUnits = new ArrayList<SubUnit>();

			String fileLine = in.readLine();
			// set main unit
			SubUnit mainUnit = new SubUnit();
			String[] parts = fileLine.split("\\|");
			mainUnit.setName(parts[0].replace("\"", ""));
			mainUnit.setBaseUnit(unit);
			String[] subUnitsStrs = parts[1].split(",");
			List<String> abbrevations = new ArrayList<String>();
			for (String str : subUnitsStrs) {
				abbrevations.add(str.replace("\"", ""));
			}
			mainUnit.setAbbrevations(abbrevations);
			mainUnit.setConvertible(false);
			unit.setMainUnit(mainUnit);

			while ((fileLine = in.readLine()) != null) {
				parts = fileLine.split("\\|");
				SubUnit subUnit = new SubUnit();
				subUnit.setName(parts[0].replace("\"", ""));
				subUnit.setConvertible(true);
				subUnitsStrs = parts[1].split(",");
				abbrevations = new ArrayList<String>();
				for (String str : subUnitsStrs) {
					abbrevations.add(str.replace("\"", ""));
				}
				abbrevations.add(subUnit.getName());
				subUnit.setAbbrevations(abbrevations);
				subUnit.setRateToConvert(Double.parseDouble(parts[2]));
				subUnit.setBaseUnit(unit);
				subUnits.add(subUnit);
			}
			unit.setSubunits(subUnits);
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return unit;

	}
	public SubUnit parseUnit(String text) {
		// if it is negative value, remove it and add it later
		boolean isNegative = false;
		if (text.contains("-")) {
			text = text.replaceFirst("-", " ");
			isNegative = true;
		}

		Map<String, SubUnit> matchedUnits = new HashMap<String, SubUnit>();
		for (Unit unit : units) {
			if (unit.getMainUnit() != null)
			{
				//for (String unitName : unit.getMainUnit().getAbbrevations()) {
					/*String regexp = "\\d{1,20}.*"
							+ Pattern.quote(unitName.toLowerCase());
					if (text.toLowerCase().matches(regexp)) {
						matchedUnits.put(unitName, unit.getMainUnit());
					}*/
					String abbr = unit.getMainUnit().getMatchingAbbreviation(text);
					if(abbr!=null)
						matchedUnits.put(abbr, unit.getMainUnit());
					
					/*regexp = Pattern.quote(unitName.toLowerCase()) + ".*\\d{1,20}";
					if (text.toLowerCase().matches(regexp)) {
						matchedUnits.put(unitName, unit.getMainUnit());
					}*/
					
					abbr = unit.getMainUnit().getMatchingAbbreviation(text);
					if(abbr!=null)
						matchedUnits.put(abbr, unit.getMainUnit());
			}
			
			for (SubUnit subunit : unit.getSubunits()) {
				//for (String unitName : subunit.getAbbrevations()) {
					/*String regexp = "\\d{1,20}.*"
							+ Pattern.quote(unitName.toLowerCase());
					if (text.toLowerCase().matches(regexp)) {
						matchedUnits.put(unitName, subunit);
					}*/
					String abbr = subunit.getMatchingAbbreviation(text);
					if(abbr!=null)
						matchedUnits.put(abbr, subunit);
					
					/*regexp = Pattern.quote(unitName.toLowerCase()) + ".*\\d{1,20}";
					if (text.toLowerCase().matches(regexp)) {
						matchedUnits.put(unitName, subunit);
					}*/
					abbr = subunit.getMatchingAbbreviation2(text);
					if(abbr!=null)
						matchedUnits.put(abbr, subunit);
				//}
			}
		}
		
		if (matchedUnits.size() > 0) {

			String subunitAbb = matchedUnits.keySet().iterator().next();
			int maxLenght = subunitAbb.length();
			for (Entry<String, SubUnit> entry : matchedUnits.entrySet()) {
				int length = entry.getKey().length();
				if (length > maxLenght) {
					maxLenght = length;
					subunitAbb = entry.getKey();
				}
			}
			SubUnit subUnit = matchedUnits.get(subunitAbb);
			//Pattern p = Pattern.compile("(?!=\\d.\\d.})([\\d.,]+)");
			Pattern p = unitPattern;
			Matcher m = p.matcher(text);
			double d = 0;
			// check if the complete unitName matches
			String unitNameFromText = "";
			while (m.find()) {
				d = Double.valueOf(m.group(1).replaceAll(",", "."));
				unitNameFromText = text.replace(m.group(1), "");
				break;
			}
			if (!subUnit.getAbbrevations().contains(unitNameFromText.trim())
					&& !unitNameFromText.trim().equals(subUnit.getName()))
				return null;
			if (isNegative)
				d = 0 - d;
			
			//
			//TODO: the following line is the pure horror
			// units cannot be used unless this is fixed
			// we cannot set a value from a specific cell in this unit object, as it will most probably be re-used for the next cell of the same column
			// i.e. the values will be incorrect!!!
			//
			subUnit.setNewValue(Double.toString(d * subUnit.getRateToConvert()));
			// + subUnit.getBaseUnit().getMainUnit().getAbbrevations()
			// .get(0));
			return matchedUnits.get(subunitAbb);
		}
		return null;
	}

	public static void main(String[] args) {

		UnitManager mgr = new UnitManager();
		SubUnit sub = mgr.parseUnit("15 squarekilometre");
		System.out.println(sub.getNewValue());
		System.out.println(sub.getName() + " "
				+ sub.getBaseUnit().getMainUnit().getName());
	}
}
