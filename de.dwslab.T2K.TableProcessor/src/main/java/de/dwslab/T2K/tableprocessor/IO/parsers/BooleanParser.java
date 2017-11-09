package de.dwslab.T2K.tableprocessor.IO.parsers;

import java.util.regex.Pattern;

public class BooleanParser {

	public static final Pattern booleanRegex = Pattern.compile("(yes|true|1|no|false|0)", Pattern.CASE_INSENSITIVE);

	public static boolean parseBoolean(String text) {

	    if(booleanRegex.matcher(text).matches()) {
			return true;
		}
		return false;
	}
}
