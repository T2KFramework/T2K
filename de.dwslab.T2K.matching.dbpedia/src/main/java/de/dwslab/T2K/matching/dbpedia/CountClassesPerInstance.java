package de.dwslab.T2K.matching.dbpedia;

import java.util.HashSet;
import java.util.Set;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableToRowHierarchyAdapter;
import de.dwslab.T2K.matching.secondline.SecondLineMatcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;

/**
 * determines which classes are in the candidates for each instances and aggregates this on class level 
 * @author Oliver
 *
 */
public class CountClassesPerInstance extends SecondLineMatcher {

    public SimilarityMatrix<Table> run(SimilarityMatrix<TableRow> instances, SimilarityMatrix<Table> classSim) {

        SimilarityMatrix<TableRow> sim = getSimilarityMatrixFactory().createSimilarityMatrix(instances.getFirstDimension().size(), instances.getSecondDimension().size());
        
        for(TableRow instance : instances.getFirstDimension()) {
            
            Set<Table> countedClasses = new HashSet<Table>();
            
            for(TableRow candidate : instances.getMatchesAboveThreshold(instance, 0.0)) {
                
                if(!countedClasses.contains(candidate.getTable())) {
                    // we count each class only once per instance
                    sim.set(instance, candidate, 1.0);
                    countedClasses.add(candidate.getTable());
                }
                
            }
            
        }
        
        return Matcher.countChildrenSimilarity(sim, classSim, new TableToRowHierarchyAdapter());
        
    }
    
}
