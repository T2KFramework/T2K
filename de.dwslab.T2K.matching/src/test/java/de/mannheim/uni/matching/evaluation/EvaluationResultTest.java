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
package de.mannheim.uni.matching.evaluation;

import static junit.framework.Assert.assertEquals;
import de.dwslab.T2K.matching.evaluation.EvaluationResult;
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
