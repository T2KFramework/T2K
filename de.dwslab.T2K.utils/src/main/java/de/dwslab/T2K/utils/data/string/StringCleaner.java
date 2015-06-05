/**
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
package de.dwslab.T2K.utils.data.string;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

public class StringCleaner {
    
    private static final Pattern removePattern = Pattern.compile("\"|\\||,|\\{|\\}|<.*>");
    private static final Pattern whitespacePattern = Pattern.compile("\n|\\s+|&*nbsp;*");
    private static final Pattern bracketsPattern = Pattern.compile("\\(.*\\)");
    
    public static String cleanString(String value,
            boolean removeContentInBrackets) {
        try {
            value = StringEscapeUtils.unescapeJava(value);
            value = removePattern.matcher(value).replaceAll("");
            value = whitespacePattern.matcher(value).replaceAll(" ");

            if (removeContentInBrackets) {
                value = bracketsPattern.matcher(value).replaceAll("");
            }
            if (value.equals("")) {
                value = null;
            } else {
                value = value.toLowerCase();
                value = value.trim();
            }
        } catch (Exception e) {
        }
        return value;
    }
}
