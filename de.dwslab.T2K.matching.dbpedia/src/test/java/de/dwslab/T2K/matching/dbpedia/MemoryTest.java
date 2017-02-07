package de.dwslab.T2K.matching.dbpedia;

//package de.mannheim.uni.matching.dbpedia;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.Collection;
//import java.util.LinkedList;
//
//import com.google.common.base.Predicate;
//
//import objectexplorer.MemoryMeasurer;
//import objectexplorer.ObjectGraphMeasurer;
//import objectexplorer.ObjectGraphMeasurer.Footprint;
//import de.mannheim.uni.matching.blocking.IdentityBlocking;
//import de.mannheim.uni.matching.blocking.TypeBasedBlocking;
//import de.mannheim.uni.matching.dbpedia.model.TableCell;
//import de.mannheim.uni.matching.dbpedia.model.TableRow;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableCellMatchingAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableColumnMatchingAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableRowToCellHierarchyAdapter;
//import de.mannheim.uni.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
//import de.mannheim.uni.matching.firstline.ValueBasedMatcherWithFiltering;
//import de.mannheim.uni.similarity.functions.IdentitySimilarity;
//import de.mannheim.uni.similarity.functions.string.GeneralisedStringJaccard;
//import de.mannheim.uni.similarity.functions.string.JaccardOnNGramsSimilarity;
//import de.mannheim.uni.similarity.functions.string.LevenshteinSimilarity;
//import de.mannheim.uni.similarity.matrix.SimilarityMatrix;
//import de.mannheim.uni.similarity.measures.TypeBasedSimilarityMeasure;
//import de.mannheim.uni.tableprocessor.IO.TableReader;
//import de.mannheim.uni.tableprocessor.model.Table;
//import de.mannheim.uni.tableprocessor.model.TableColumn;
//import de.mannheim.uni.tableprocessor.model.TableColumn.ColumnDataType;
//import de.mannheim.uni.utils.concurrent.Parallel;
//import junit.framework.TestCase;
//
//public class MemoryTest extends TestCase {
//
//    public void testMemoryConsumption() throws UnsupportedEncodingException, FileNotFoundException, IOException {
//
//        TableReader r = new TableReader();
//        
//        Table t = r.readLODTable("resources/dbpedia/Country.csv.gz");
//        
//        int numElements = 0;
//        
//        Object[][] columnOriented = new Object[t.getColumns().size()][];
//        String[][] columnString = new String[t.getColumns().size()][];
//        
//        int col = 0;
//        for(TableColumn c : t.getColumns()) {
//            Object[] columnData = new Object[t.getTotalNumOfRows()];
//            String[] stringData = new String[t.getTotalNumOfRows()];
//            
//            for(int idx : c.getValues().keySet()) {
//                columnData[idx] = c.getValues().get(idx);
//                stringData[idx] = c.getValues().get(idx).toString();
//                numElements++;
//            }
//            
//            columnString[col] = stringData;
//            columnOriented[col++] = columnData;
//            
//        }
//        
//        System.out.println(String.format("Table %s has %,d elements", t.getHeader(), numElements));
//        
////        measure(t, "Table");
////        measureObjectArray(columnOriented, "ColumnArray");
////        measureObjectArray(columnString, "StringArray");
//        
//        Parallel.SetDefaultNumProcessors(1);
//        
//        ValueBasedMatcherWithFiltering<TableRow, TableCell> matcher = new ValueBasedMatcherWithFiltering<>();
//        matcher.setBlocking(new IdentityBlocking<TableRow>());
//        matcher.setValueBlocking(new TypeBasedBlocking<>(new TableCellMatchingAdapter()));
//        TypeBasedSimilarityMeasure<TableCell> measure = new TypeBasedSimilarityMeasure<>();
//        //measure.getSimilarityFunctions().put(ColumnDataType.string, new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.5, 0.5));
//        measure.getSimilarityFunctions().put(ColumnDataType.string, new JaccardOnNGramsSimilarity(3));
//        //measure.getSimilarityFunctions().put(ColumnDataType.string, new IdentitySimilarity<>());
//        //measure.getSimilarityFunctions().put(ColumnDataType.numeric, new IdentitySimilarity<>());
//        //measure.getSimilarityFunctions().put(ColumnDataType.date, new IdentitySimilarity<>());
//        matcher.setSimilarityMeasure(measure);
//        TableToRowHierarchyAdapter ttr = new TableToRowHierarchyAdapter();
//        Collection<TableRow> rows = ttr.getParts(t);
//        LinkedList<TableRow> testRows = new LinkedList<>();
//        int rowCount = 0;
//        for(TableRow row : rows) {
//            if(rowCount++<5) {
//                testRows.add(row);
//            }
//        }
//        
//        SimilarityMatrix<TableCell> sim = matcher.match(testRows, testRows, new TableRowToCellHierarchyAdapter(), new TableCellMatchingAdapter());
////        SimilarityMatrix<TableCell> sim = matcher.match(rows, rows, new TableRowToCellHierarchyAdapter(), new TableCellMatchingAdapter());
//        
//        System.out.println(sim.getOutput());
//        
//        // get size of similarity matrix without adding the table size
////        measureExcludingCells(sim, "Value-based Similarity");
//    }
//
//    private static void measureExcludingCells(Object obj, String name) {
//        Predicate<Object> noCells = new Predicate<Object>() {
//            
//            @Override
//            public boolean apply(Object input) {
//                return input!=null && !input.getClass().equals(TableCell.class);
//            }
//        };
//        long memory = MemoryMeasurer.measureBytes(obj, noCells);
//
//        System.out.println(String.format("%s Memory Size: %,d", name, memory));
//        
//        Footprint footprint = ObjectGraphMeasurer.measure(obj, noCells);
//        System.out.println(String.format("%s Graph Footprint: \n\tObjects: %,d\n\tReferences %,d", name, footprint.getObjects(), footprint.getReferences()));
//    }
//    
//    private static void measure(Object obj, String name) {
//        long memory = MemoryMeasurer.measureBytes(obj);
//
//        System.out.println(String.format("%s Memory Size: %,d", name, memory));
//        
//        Footprint footprint = ObjectGraphMeasurer.measure(obj);
//        System.out.println(String.format("%s Graph Footprint: \n\tObjects: %,d\n\tReferences %,d", name, footprint.getObjects(), footprint.getReferences()));
//    }
//    
//    private static void measureObjectArray(Object[][] obj, String name) {
//        long memory = MemoryMeasurer.measureBytes(obj);
//        
//        
////        System.out.println(String.format("%s x%d", name, obj.length));
////        for(int col = 0; col < obj.length; col++) {
////            Object[] o = obj[col];
////            
////            if(o!=null) {
////                System.out.println(String.format(" [%d] x%d", col, o.length));
////            } else {
////                System.out.println(String.format(" [%d] null", col));
////            }
////        }
//        
//
//        System.out.println(String.format("%s Memory Size: %,d", name, memory));
//        
//        Footprint footprint = ObjectGraphMeasurer.measure(obj);
//        System.out.println(String.format("%s Graph Footprint: \n\tObjects: %,d\n\tReferences %,d", name, footprint.getObjects(), footprint.getReferences()));
//    }
//}
