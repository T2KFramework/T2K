/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.tableprocessor.model.TableColumn;

/**
 *
 * @author domi
 */
public class DBpediaPropertyAdapter extends EvaluationAdapter<TableColumn>{

    private Canoniser canon;
    
    public DBpediaPropertyAdapter(Canoniser canoniser) {
        canon = canoniser;
    }
    
    @Override
    public Object getUniqueIdentifier(TableColumn instance) {
        return canon.canoniseResource(instance.getURI());
    }
    
}
