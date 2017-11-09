package de.dwslab.T2K.matching.dbpedia.model;

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
    
    private SimilarityMatrix<TableRow> beforeFiltering = null;

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
    public void clean() {
        initialCandidateSimilarity = null;
        initialClassSimilarity = null;
        finalClass = null;
        classSimilarity = null;
        propertySimilarity = null;
        labelSimilarity = null;
        candidateSimilarity = null;
        valueSimilarity = null;
    }

    /**
     * @return the beforeFiltering
     */
    public SimilarityMatrix<TableRow> getBeforeFiltering() {
        return beforeFiltering;
    }

    /**
     * @param beforeFiltering the beforeFiltering to set
     */
    public void setBeforeFiltering(SimilarityMatrix<TableRow> beforeFiltering) {
        this.beforeFiltering = beforeFiltering;
    }
}
