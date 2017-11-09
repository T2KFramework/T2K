package de.mannheim.uni.matching.evaluation;

import de.dwslab.T2K.matching.evaluation.EvaluationResult;
import static junit.framework.Assert.assertEquals;
import junit.framework.TestCase;

public class EvaluationResultTest extends TestCase {
    
    public void testGetPrecision() {
        assertEquals(1.0, new EvaluationResult(1,1,1,0).getPrecision());
        assertEquals(1.0, new EvaluationResult(1,1,2,0).getPrecision());
        assertEquals(0.5, new EvaluationResult(1,2,1,0).getPrecision());
        assertEquals(0.5, new EvaluationResult(1,2,2,0).getPrecision());
        assertEquals(0.0, new EvaluationResult(0,1,1,0).getPrecision());
    }

    public void testGetRecall() {
        assertEquals(1.0, new EvaluationResult(1, 0, 1,0).getRecall());
        assertEquals(1.0, new EvaluationResult(1, 1, 1,0).getRecall());
        assertEquals(0.5, new EvaluationResult(1, 1, 2,0).getRecall());
        assertEquals(0.5, new EvaluationResult(1, 2, 2,0).getRecall());
        assertEquals(0.0, new EvaluationResult(0, 1, 1,0).getRecall());
    }

    public void testGetF1Score() {
        assertEquals(1.0, new EvaluationResult(1, 1, 1,0).getF1Score());
        assertEquals(0.5, new EvaluationResult(1, 2, 2,0).getF1Score());
    }

    public void testGetAccuracyScore() {
        assertEquals(0.0, new EvaluationResult(0,0,1,1).getAccuracy());
        assertEquals(0.5, new EvaluationResult(1,1,2,2).getAccuracy());
        assertEquals(1.0, new EvaluationResult(1,1,1,2).getAccuracy());
    }
}
