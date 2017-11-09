package de.mannheim.uni.similarity.matrix;

import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import java.util.Random;

import junit.framework.TestCase;

public class MemoryUsageTest extends TestCase {

    private final double fillRatio = 0.05;
    private final int firstDimension = 1000;
    private final int secondDimension = 1000;
    private final int runs = 10;
    private final int numIterations = 10;
    
    private void fillMatrix(SimilarityMatrix<Object> matrix) {
        
        Random r = new Random();
        
        for(int i1 = 0; i1 < firstDimension; i1++) {
            for(int i2 = 0; i2 < secondDimension; i2++) {
                
                if(r.nextInt(100) < fillRatio*100) {
                    matrix.set(i1, i2, r.nextDouble());
                }
                
            }
        }
        
    }
    
    private void copyMatrix(SimilarityMatrix<Object> from, SimilarityMatrix<Object> to) {
        for(Object o1 : from.getFirstDimension()) {
            
            for(Object o2 : from.getMatches(o1)) {
                
                to.set(o1, o2, from.get(o1, o2));
                
            }
            
        }
    }
    
    static long waitGetFreeMemory() {
        // waits for free memory measurement to stabilize
      long init = Runtime.getRuntime().freeMemory(), init2;
      int count = 0;
      do {
          //System.out.println("waiting..." + init);
          System.gc();
          try { Thread.sleep(250); } catch (Exception x) { }
          init2 = init;
          init = Runtime.getRuntime().freeMemory();
          if (init == init2) ++ count; else count = 0;
      } while (count < 5);
      //System.out.println("ok..." + init);
      return init;
    }
    
    private long measureIterationTime(SimilarityMatrix<Object> m) {
        long start = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++){
            for(Object o1 : m.getFirstDimension()) {
                for(Object o2 : m.getMatches(o1)) {
                    m.get(o1, o2);
                }
            }
        }
        return System.currentTimeMillis() - start;
    }
    
    private long measureUpdateTime(SimilarityMatrix<Object> m) {
        long start = System.currentTimeMillis();
        for(int i = 0; i < numIterations; i++){
            for(Object o1 : m.getFirstDimension()) {
                for(Object o2 : m.getMatches(o1)) {
                    m.set(o1, o2, 0.5);
                }
            }
        }
        return System.currentTimeMillis() - start;
    }
    
    static long getFreeMemory () {
        return Runtime.getRuntime().freeMemory();
    }
    
    public void testSparseMatrix() {
        
//        for(int i = 0; i < runs; i++) {
//            
//            System.gc();
//            long memBefore = waitGetFreeMemory();
//            
//            SimilarityMatrix<Object> m1 = new ArrayBasedSimilarityMatrix<>(firstDimension, secondDimension);
//            fillMatrix(m1);
//            System.gc();
//            long mem1 = memBefore - waitGetFreeMemory();
//            
//            memBefore = getFreeMemory();
//            SimilarityMatrix<Object> m2 = new SparseSimilarityMatrix<>(1000,1000);
//            copyMatrix(m1, m2);
//            System.gc();
//            long mem2 = memBefore - waitGetFreeMemory();
//            
//            memBefore = getFreeMemory();
//            SimilarityMatrix<Object> m3 = new FastSparseSimilarityMatrix<>(1000,1000);
//            copyMatrix(m1, m3);
//            System.gc();
//            long mem3 = memBefore - waitGetFreeMemory();
//            
//            long time1 = measureIterationTime(m1);
//            long time2 = measureIterationTime(m2);
//            long time3 = measureIterationTime(m3);
//            
//            long upd1 = measureUpdateTime(m1);
//            long upd2 = measureUpdateTime(m2);
//            long upd3 = measureUpdateTime(m3);
//            
//            m1.printStatistics("Run "+ i);
//            System.out.println( m1.getClass().getName() + ": " + String.format("\t%.2f MB\tread: %d\tupdate: %d", mem1 / 1024.0 / 1024.0, time1, upd1));
//            System.out.println( m2.getClass().getName() + ": " + String.format("\t%.2f MB\tread: %d\tupdate: %d", mem2 / 1024.0 / 1024.0, time2, upd2));
//            System.out.println( m3.getClass().getName() + ": " + String.format("\t%.2f MB\tread: %d\tupdate: %d", mem3 / 1024.0 / 1024.0, time3, upd3));
//        }
        
    }
    
}
