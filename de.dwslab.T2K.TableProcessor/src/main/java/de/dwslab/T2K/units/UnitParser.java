/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.units;

import de.dwslab.T2K.util.Variables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author domi
 */
public class UnitParser {

    public static Double parseUnit(String value, String unitInformation) {
        for (Unit unit : Variables.units) {
            if (!unitInformation.isEmpty()) {
                if (unitInformation.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(unitInformation.toLowerCase())) {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation * unit.getFactor();
                }
            } else {
                String nonNumberPart = value.replaceAll("[0-9\\,\\.\\-Ee\\+]", "");
                if (nonNumberPart.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(nonNumberPart.toLowerCase())) {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation * unit.getFactor();
                } else {
                    value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
                    Double valueBeforeTransformation = Double.parseDouble(value);
                    return valueBeforeTransformation;
                }
            }
        }
        return null;
    }

    public static Double transformUnit(String value, Unit unit) throws ParseException {
        value = value.replaceAll("[^0-9\\,\\.\\-Ee\\+]", "");
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(value);
        Double valueBeforeTransformation = number.doubleValue();
        return valueBeforeTransformation * unit.getFactor();
    }

    public static Unit checkUnit(String value) {
        for (Unit unit : Variables.units) {
//            if (!header.isEmpty()) {
//                if (header.toLowerCase().equals(unit.getName())
//                        || unit.getAbbreviations().contains(header.toLowerCase())
//                        || extractUnitAbbrFromHeader(header).toLowerCase().equals(unit.getName())
//                        || unit.getAbbreviations().contains(extractUnitAbbrFromHeader(header).toLowerCase())) {
//                    return unit;
//                }
//            }
            String nonNumberPart = value.replaceAll("[0-9\\,\\.\\-Ee\\+]", "");
            if (nonNumberPart.toLowerCase().equals(unit.getName())
                    || unit.getAbbreviations().contains(nonNumberPart.toLowerCase())) {
                return unit;
            }
        }
        return null;
    }

    private static Pattern unitInHeaderPattern = Pattern.compile(".*\\((.*)\\).*");
    private static Pattern dotPattern = Pattern.compile("\\.");

    public static Unit parseUnitFromHeader(String header) {
        String unitName = extractUnitAbbrFromHeader(header).toLowerCase();

        for (Unit unit : Variables.units) {
            if (!header.isEmpty()) {
                if (header.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(header.toLowerCase())
                        || unitName.equals(unit.getName())
                        || header.toLowerCase().equals(unit.getName())
                        || unit.getAbbreviations().contains(unitName)) {
                    return unit;
                }
            }
        }

        return null;
    }

    private static String extractUnitAbbrFromHeader(String header) {
        try {
            //if (header.matches(".*\\(.*\\).*")) {
            Matcher m = unitInHeaderPattern.matcher(header);
            if (m.matches()) {
                String unit = m.group(1);

                return dotPattern.matcher(unit).replaceAll("");
                //return header.substring(header.indexOf("(") + 1, header.indexOf(")")).replaceAll("\\.", "");
            }
        } catch (Exception e) {
        }

        return header;
    }

    //Threads possible?
    public static void readInUnits() {
        synchronized (Variables.units) {
            if (Variables.units.isEmpty()) {
                //Variables.units = new ArrayList<>();
                File folder = new File("Units/Convertible");
                for (File fileEntry : folder.listFiles()) {
                    Variables.units.addAll(readConvertibleUnit(fileEntry));
                }
            }
        }
    }

    private static Set<Unit> readConvertibleUnit(File unitPath) {
        Set<Unit> unitsOfFile = new HashSet<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(unitPath), "UTF8"));
            String fileLine = in.readLine();
            while (fileLine != null) {
                Unit currentUnit = new Unit();
                String[] parts = fileLine.split("\\|");
                currentUnit.setName(parts[0].replace("\"", ""));
                //List<String> abbs = new ArrayList();
                HashSet<String> abbs = new HashSet<>();
                String[] subUnitsStrs = parts[1].split(",");
                for (String s : subUnitsStrs) {
                    abbs.add(s.replace("\"", ""));
                }
                currentUnit.setAbbreviations(abbs);
                if (parts.length < 3) {
                    currentUnit.setFactor(1.0);
                } else {
                    currentUnit.setFactor(Double.parseDouble(parts[2]));
                }
                unitsOfFile.add(currentUnit);
                fileLine = in.readLine();
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return unitsOfFile;
    }
}
