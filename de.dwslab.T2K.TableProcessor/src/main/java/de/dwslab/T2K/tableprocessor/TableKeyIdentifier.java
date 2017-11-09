package de.dwslab.T2K.tableprocessor;

import java.util.ArrayList;
import java.util.List;

import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;
import de.dwslab.T2K.util.Variables;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Detects the key column of the table.
 *
 * @author petar
 *
 */
public class TableKeyIdentifier {

    public void indenfityLODKeys(Table table) throws IOException {

        identifyKeys(table);
    }
    
    private static final Pattern prefLabelPattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?prefLabel$");
    private static final Pattern namePattern =Pattern.compile("([^#]*#)?name$");
    private static final Pattern labelPattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?label$");
    private static final Pattern titlePattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?title$");
    private static final Pattern labelPattern2 =Pattern.compile("([^#]*#)?.*Label$");
    private static final Pattern namePattern2 = Pattern.compile("([^#]*#)?.*Name$");
    private static final Pattern titlePattern2 = Pattern.compile("([^#]*#)?.*Title$");
    private static final Pattern alternateNamePattern = Pattern.compile("([^#]*#)?([a-z]{1,9})?alternateName$");
    
    public void identifyKeys(Table table) throws IOException {
        
        File fileEndings = new File("fileEndings.csv");
        List<String> endings = new ArrayList<>();
        if (fileEndings.exists()) {
            BufferedReader read = new BufferedReader(new FileReader(fileEndings));
            String line = read.readLine();
            while (line != null) {
                endings.add(line.toLowerCase().trim());
                line = read.readLine();
            }
        }
        else {
            System.out.println("no endings file provided!");
        }
        
        TableColumn key = null;
        int keyColumnIndex = -1;
        List<Double> columnUniqueness = new ArrayList<>(table.getColumns().size());

        for (int i=table.getColumns().size()-1; i>=0; i--) {
            TableColumn column = table.getColumns().get(i);
//            System.out.println("header: " + column.getHeader());
//            System.out.println("dt: " + column.getDataType());
            if (column.getDataType() != TableColumn.ColumnDataType.string) {
                continue;
            }
            if (prefLabelPattern.matcher(column.getHeader().toString()).matches()) {
                key = column;
                break;
            }
            if (namePattern.matcher(column.getHeader().toString()).matches()) {
                key = column;
                break;
            }
            if (labelPattern.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }

            if (titlePattern.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }
            if (labelPattern2.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }

            if (namePattern2.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }

            if (titlePattern2.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }
            if (alternateNamePattern.matcher(column.getHeader().toString()).matches()) {
                key = column;
            }

        }
        if (key != null) {
            keyColumnIndex = table.getColumns().indexOf(key);
            
            int countAlphabetic = 0, countFiles = 0;
            for (Object o : table.getColumns().get(keyColumnIndex).getValues().values()) {
                if (o.toString().matches(".*[a-zA-Z].*")) {
                    countAlphabetic++;
                }
                for (String s : endings) {
                    if (o.toString().toLowerCase().trim().endsWith(s)) {                        
                        countFiles++;
                    }
                    else {
//                        System.out.println(s + " - " + o.toString().toLowerCase());
                    }
                }
            }
            boolean isOk = true;
            if (countAlphabetic < 2) {
                isOk = false;
            }
            
            System.out.println((double) countFiles / (double) table.getColumns().get(keyColumnIndex).getValues().size());
            if ((double) countFiles / (double) table.getColumns().get(keyColumnIndex).getValues().size() > 0.5) {                
                isOk = false;
            }
            
            if (isOk && table.getColumns().get(keyColumnIndex).getColumnUniqnessRank() >= Variables.keyUniqueness
                    && table.getColumns().get(keyColumnIndex).getColumnStatistic().getAverageValueLength() > 3.5
                    && table.getColumns().get(keyColumnIndex).getColumnStatistic().getAverageValueLength() <= 200) {
                table.getColumns().get(keyColumnIndex).setKey(true);
                table.setHasKey(true);
                return;
            }
            //the found key does not fit the requirements, see if another column does
            key = null;
        }

        for (TableColumn column : table.getColumns()) {
            //System.out.println("uniqueness: " +column + " - "+ table.getColumns().indexOf(column) + " - "+column.getColumnUniqnessRank());
            columnUniqueness.add(column.getColumnUniqnessRank());
        }

        if (columnUniqueness.isEmpty()) {
            return;
        }
        double maxCount = -1;
        int maxColumn = -1;

        for (int i = 0; i < columnUniqueness.size(); i++) {
            
            int countAlphabetic = 0, countFiles = 0;
            for (Integer o : table.getColumns().get(i).getValues().keySet()) {
                if (table.getColumns().get(i).getValues().get(o).toString().matches(".*[a-zA-Z].*")) {
                    countAlphabetic++;
                }
                for (String s : endings) {
                    if (table.getColumns().get(i).getValues().get(o).toString().toLowerCase().trim().endsWith(s)) {
                        countFiles++;
                    }
                }
            }
            if (countAlphabetic < 2) {
                continue;
            }
            if ((double) countFiles / (double) table.getColumns().get(i).getValues().size() > 0.5) {
                continue;
            }
            
            if (columnUniqueness.get(i) > maxCount && table.getColumns().get(i).getDataType() == TableColumn.ColumnDataType.string
                    && table.getColumns().get(i).getColumnStatistic().getAverageValueLength() > 3.5
                    && table.getColumns().get(i).getColumnStatistic().getAverageValueLength() <= 200) {
                maxCount = (Double) columnUniqueness.get(i);
                maxColumn = i;
            }
        }

        if (key == null) {
            if (maxColumn == -1) {
                table.setHasKey(false);
                return;
            }
            key = table.getColumns().get(maxColumn);
        }
        keyColumnIndex = table.getColumns().indexOf(key);

        if (columnUniqueness.get(keyColumnIndex) < Variables.keyUniqueness) {
            table.setHasKey(false);
            return;
        }

        table.getColumns().get(keyColumnIndex).setKey(true);
        table.setHasKey(true);
    }
}
