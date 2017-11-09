/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.dbpedia.model.adapters;

import java.util.List;

import de.dwslab.T2K.matching.dbpedia.model.TableRow;
import de.dwslab.T2K.matching.evaluation.EvaluationAdapter;
import de.dwslab.T2K.utils.StringUtils;

/**
 *
 * @author domi
 */
public class CandidateAdapter extends EvaluationAdapter<TableRow> {

        @Override
        public Object getUniqueIdentifier(TableRow instance) {
            if(instance!=null) {
                return Integer.toString(instance.getRowIndexInFile());
//                
//                if(instance.getKey() instanceof List) {
//                    return "{" + StringUtils.join((List)instance.getKey(), "|") + "}";
//                } else {
//                    return instance.getKey().toString().replaceAll("\\s", " ");
//                }
            } else {
                return null;
            }
        }
    }
