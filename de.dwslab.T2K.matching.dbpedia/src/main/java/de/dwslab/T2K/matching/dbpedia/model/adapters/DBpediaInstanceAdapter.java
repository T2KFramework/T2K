/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;

/**
 *
 * @author domi
 */
public class DBpediaInstanceAdapter extends EvaluationAdapter<TableRow>{
    
    private Canoniser canon;
    
    public DBpediaInstanceAdapter(Canoniser canoniser) {
        canon = canoniser;
    }
    
    public DBpediaInstanceAdapter() {}
    
    @Override
    public Object getUniqueIdentifier(TableRow instance) {
       if(canon == null) {
           return instance.getURI();
       }
       else {
        return canon.canoniseResource(instance.getURI().toString());
       }
    }
    
}
