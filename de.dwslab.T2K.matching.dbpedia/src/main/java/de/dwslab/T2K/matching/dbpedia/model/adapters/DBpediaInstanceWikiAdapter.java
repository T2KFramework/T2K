/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.utils.data.link.DBpediaURIEncoder;

/**
 *
 * @author domi
 */
public class DBpediaInstanceWikiAdapter extends EvaluationAdapter<TableRow> {

    @Override
    public Object getUniqueIdentifier(TableRow instance) {
        if (instance.getTable().getWikiIDCol() != -1) {
            if (instance.getWikiID() != null) {
                try {
                    String uncodedWiki = DBpediaURIEncoder.encodeURIForDBpedia("http://dbpedia.org/resource/"+instance.getWikiID().toString());
                    return  uncodedWiki;
                } catch (Exception e) {
                    return instance.getURI().toString();
                }
            }
        }
        return instance.getURI().toString();
    }
}
