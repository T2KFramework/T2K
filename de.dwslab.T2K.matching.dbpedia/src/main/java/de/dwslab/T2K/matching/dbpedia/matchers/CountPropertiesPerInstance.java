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
package de.dwslab.T2K.matching.dbpedia.matchers;

import de.dwslab.T2K.matching.Matcher;
import de.dwslab.T2K.matching.dbpedia.model.TableCell;
import de.dwslab.T2K.matching.dbpedia.model.TableCellCache;
import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.model.adapters.TableColumnToCellHierarchyAdapter;
import de.dwslab.T2K.matching.secondline.SecondLineMatcher;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import de.dwslab.T2K.tableprocessor.model.Table;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

/**
 * determines which properties are in the candidates for each instances  
 * @author Oliver
 *
 */
public class CountPropertiesPerInstance extends SecondLineMatcher {

    public SimilarityMatrix<TableColumn> run(SimilarityMatrix<TableRow> instances, SimilarityMatrix<TableCell> values, SimilarityMatrix<TableColumn> properties, SimilarityMatrix<Table> classSim) {

        SimilarityMatrix<TableCell> sim = getSimilarityMatrixFactory().createSimilarityMatrix(values.getFirstDimension().size(), values.getSecondDimension().size());
        
        // we do this for every instance
        for(TableRow instance : instances.getFirstDimension()) {
            
            // we want to count which properties appear for this instance

            // so we iterate all properties that we know (from the candidates)
            for(TableColumn prop : properties.getSecondDimension()) {
            
                boolean found = false;
                
                // then we iterate all candidates until we found one that matches 
                for(TableRow candidate : instances.getMatchesAboveThreshold(instance, 0.0)) {
                        
                    
                    // as we are looking at actual values, we also have to iterate all cells of the rows we're currently looking at
                    for(TableCell instanceValue : instance.getCells()) {

                        TableCell candidateCell = TableCellCache.get().get(candidate.getTable(), candidate.getRowIndex(), prop.getTable().getColumns().indexOf(prop));
                        
                        if(values.get(instanceValue, candidateCell) > 0.0) {
                            // we have found a matching value (at least more similarity than 0.0)
                            sim.set(instanceValue, candidateCell, 1.0);
                            found = true;
                            break;
                        }
                        
                        
                    }
                    
                    if(found) {
                        // we can stop once we found a matching value for this property (we are only counting)
                        break;
                    }
                    
                }
            }
        }
        
        // finally, aggregate the cells to columns (will contain the number of matching candidates)
        return Matcher.countChildrenSimilarity(sim, properties, new TableColumnToCellHierarchyAdapter());
    }
}
