package de.dwslab.T2K.tableprocessor.model.json;

import de.dwslab.T2K.tableprocessor.model.json.TableData;
import junit.framework.TestCase;

public class TableDataTest extends TestCase {

    public void testTransposeRelation() {
        TableData data = new TableData();
        
        String[][] rel = new String[][] {
                new String[] { "c1r1", "c1r2", "c1r3" },
                new String[] { "c2r1" },
                new String[] { "c3r1", "c3r2", "c3r3", "c3r4" }
            };
        
        data.setRelation(rel);
        
        for(int row = 0; row < 4; row++) {
            for(int col = 0; col < data.getRelation().length; col++) {
                System.out.print(" | ");
                if(data.getRelation().length > col && data.getRelation()[col].length > row) {
                    System.out.print(data.getRelation()[col][row]);
                } else {
                    System.out.print("    ");
                }
            }
            System.out.println();
        }
    
        
        System.out.println("-------------------------");
        System.out.println("-------------------------");
        System.out.println("-------------------------");
        data.transposeRelation();
        
        for(int row = 0; row < 4; row++) {
            for(int col = 0; col < data.getRelation().length; col++) {
                System.out.print(" | ");
                if(data.getRelation().length > col && data.getRelation()[col].length > row) {
                    System.out.print(data.getRelation()[col][row]);
                } else {
                    System.out.print("    ");
                }
            }
            System.out.println();
        }
    }

}
