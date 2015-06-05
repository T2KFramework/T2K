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
package de.dwslab.T2K.matching.dbpedia.model.settings;

import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

/**
 * contains all similarity matrices used by the matching algorithm
 * @author Oliver
 *
 */
public class Similarities {

    /*
     *  Candidate Similarities
     */
    private SimilarityMatrix<TableRow> initialCandidateSimilarity = null;
    public SimilarityMatrix<TableRow> getInitialCandidateSimilarity() {
        return initialCandidateSimilarity;
    }
    public void setInitialCandidateSimilarity(
            SimilarityMatrix<TableRow> initialCandidateSimilarity) {
        this.initialCandidateSimilarity = initialCandidateSimilarity;
    }
    
    private SimilarityMatrix<TableRow> candidateSimilarity = null;
    public void setCandidateSimilarity(
            SimilarityMatrix<TableRow> candidateSimilarity) {
        this.candidateSimilarity = candidateSimilarity;
    }
    public SimilarityMatrix<TableRow> getCandidateSimilarity() {
        if (candidateSimilarity == null) {
            return getInitialCandidateSimilarity();
        } else {
            return candidateSimilarity;
        }
    }

    /*
     * Value Similarities
     */
    
    private SimilarityMatrix<TableCell> valueSimilarity = null;
    public SimilarityMatrix<TableCell> getValueSimilarity() {
        return valueSimilarity;
    }
    public void setValueSimilarity(
            SimilarityMatrix<TableCell> valueSimilarity) {
        this.valueSimilarity = valueSimilarity;
    }

    /*
     * Property Similarities
     */
    
    private SimilarityMatrix<TableColumn> labelSimilarity = null;
    public SimilarityMatrix<TableColumn> getLabelSimilarity() {
        return labelSimilarity;
    }
    public void setLabelSimilarity(SimilarityMatrix<TableColumn> labelSimilarity) {
        this.labelSimilarity = labelSimilarity;
    }

    private SimilarityMatrix<TableColumn> propertySimilarity = null;
    public SimilarityMatrix<TableColumn> getPropertySimilarity() {
        if(propertySimilarity==null) {
            return getLabelSimilarity();
        } else {
            return propertySimilarity;
        }
    }
    public void setPropertySimilarity(
            SimilarityMatrix<TableColumn> propertySimilarity) {
        this.propertySimilarity = propertySimilarity;
    }
    
    /*
     * Class Similarities
     */
    
    private SimilarityMatrix<Table> initialClassSimilarity = null;
    public SimilarityMatrix<Table> getInitialClassSimilarity() {
        return initialClassSimilarity;
    }
    public void setInitialClassSimilarity(
            SimilarityMatrix<Table> initialClassSimilarity) {
        this.initialClassSimilarity = initialClassSimilarity;
    }

    private SimilarityMatrix<Table> classSimilarity = null;
    public SimilarityMatrix<Table> getClassSimilarity() {
        if(classSimilarity==null) {
            return getInitialClassSimilarity();
        } else {
            return classSimilarity;
        }
    }
    public void setClassSimilarity(SimilarityMatrix<Table> classSimilarity) {
        this.classSimilarity = classSimilarity;
    }

    private Table finalClass;
    public Table getFinalClass() {
        return finalClass;
    }
    public void setFinalClass(Table finalClass) {
        this.finalClass = finalClass;
    }
}
