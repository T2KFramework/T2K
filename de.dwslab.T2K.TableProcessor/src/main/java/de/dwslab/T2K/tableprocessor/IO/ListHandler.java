package de.dwslab.T2K.tableprocessor.IO;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides methods to detect and split lists which are in the format {list item 1|list item 2}
 * @author Oliver
 *
 */
public class ListHandler {

    private static Pattern listPattern = Pattern.compile("^\\{.+\\|.+\\}$");
    
    public static boolean checkIfList(String columnValue) {
        if(listPattern.matcher(columnValue).matches()) {
            return true;
        }
        return false;
    }
    
    public static String[] splitList(String columnValue) {
//        String data = columnValue.replace("{", "").replace("}", "");
//        return data.split("\\|");
        String data = columnValue.substring(1, columnValue.length() - 1);
        return data.split("\\|");
    }
    
    public static String formatList(List<String> values) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("{");
        
        for(int i = 0; i < values.size(); i++) {
            
            if(i!=0) {
                sb.append("|");
            }
            
            sb.append(values.get(i).replace("|", ""));
        }
        
        sb.append("}");
        
        return sb.toString();
    }
}
