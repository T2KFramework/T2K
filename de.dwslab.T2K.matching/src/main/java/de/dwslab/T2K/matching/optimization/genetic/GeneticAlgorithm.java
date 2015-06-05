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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dwslab.T2K.matching.optimization.genetic;

import de.dwslab.T2K.matching.optimization.OptimizationAlgorithm;
import de.dwslab.T2K.matching.process.Configuration;
import de.dwslab.T2K.matching.process.MatchingComponent;
import de.dwslab.T2K.matching.process.Parameter;
import de.dwslab.T2K.matching.process.ParameterRange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author domi
 */
public class GeneticAlgorithm implements OptimizationAlgorithm {

    private int populationSize;
    private int startPopulationSize;
    private double rejectionRate = 0.6;
    private int numberOfCrossovers = (int) Math.round(populationSize * rejectionRate);
    private double mutationRate = 0.1;
    private List<Individual> currentPopulation;
    private double terminationCriterion = 0.001;
    private int individualCounter = 0;
    private ParameterRange ranges;
    private MatchingComponent m;
    private double lastBest;
    private double currentBest;

    @Override
    public Configuration run(ParameterRange ranges, MatchingComponent m) {
        this.ranges = ranges;
        this.m=m;
        
        individualCounter = 0;
        currentPopulation = new ArrayList<>();
        //http://annals-csis.org/proceedings/2013/pliks/167.pdf best values
        //for 3 variables with a population of 100 -> factor 33
        //populationSize = m.getParameters().size()*33;
        startPopulationSize = ranges.getRanges().size()*4;
        populationSize = ranges.getRanges().size()*2;
        
        boolean hasValues = false;
        for (Parameter p : ranges.getRanges().keySet()) {
            if (m.getParameters().contains(p)) {
                hasValues = true;
            }
        }
//        
//        //if the number of possible combinations is smaller than the
//        //population size, just try out all configurations
//        int counter = 1;
//        for(Parameter p : ranges.getRanges().keySet()) {
//            if (m.getParameters().contains(p)) {
//                counter *= ranges.getRanges().get(p).size();
//            }
//        }
//        if(counter<populationSize) {
//            AllPossibilities a = new AllPossibilities();
//            return a.run(ranges, m);
//        }
        
        if(hasValues) {
            createFirstPopulation();
            Collections.sort(currentPopulation);
            deleteWeakestStart();
            lastBest = -1.0;
            currentBest = currentPopulation.get(currentPopulation.size()-1).getScore();
            while (true) {
            //while (Math.abs(lastBest-currentBest)>terminationCriterion && individualCounter<300 && lastBest!=currentBest) {
                lastBest = currentBest;
                deleteWeakest();
                crossover();
                mutate();
                Collections.sort(currentPopulation);
                currentBest = currentPopulation.get(currentPopulation.size()-1).getScore();                
                System.out.println(String.format("Current best: %f --- last best: %f", currentBest, lastBest));                
            }
            //Collections.sort(currentPopulation);
            //return currentPopulation.get(currentPopulation.size() - 1).getConfig();
        } else {
            return new Configuration(new HashMap<Parameter, Object>(), ranges.getAliases());
        }
    }

    /**
     * Delete the weakest individuals of a population.
     *
     */
    private void deleteWeakest() {
        Collections.sort(currentPopulation);
        for (int i = 0; i < numberOfCrossovers; i++) {
            currentPopulation.remove(0);
        }
    }
    
    private void deleteWeakestStart() {
        Collections.sort(currentPopulation);
        for (int i = 0; i < populationSize; i++) {
            currentPopulation.remove(0);
        }
    }

    /**
     * Performs the crossover step. A certain amount of new individuals are
     * created according to the number of crossovers. Therefore, the mother as
     * well as the father is randomly chosen as well as the breaking point. Out
     * of these parents, the new individual is generated and added to the
     * current population.
     *
     */
    private void crossover() {
        Random generator = new Random();
        for (int i = 0; i < numberOfCrossovers; i++) {
            //select parents
            Individual father = currentPopulation.get(generator.nextInt(currentPopulation.size()));
            Individual mother = currentPopulation.get(generator.nextInt(currentPopulation.size()));
            //selct breaking point
            int breakingPoint = generator.nextInt(father.getConfig().getConfig().size());
            Map<Parameter, Object> parameterAssignments = new HashMap<>();
            List<Parameter> keys = new ArrayList<>();
            Object[] fatherArray = father.getConfig().getConfig().keySet().toArray();
            for (int j = 0; j < breakingPoint; j++) {
                keys.add((Parameter) fatherArray[j]);
            }
            //assign the according values
            for (Parameter key : father.getConfig().getConfig().keySet()) {
                if (keys.contains(key)) {
                    parameterAssignments.put(key, father.getConfig().getConfig().get(key));
                } else {
                    parameterAssignments.put(key, mother.getConfig().getConfig().get(key));
                }
            }
            Individual child = new Individual(new Configuration(parameterAssignments, ranges.getAliases()),m);
            individualCounter++;
            currentPopulation.add(child);
        }
    }

    /**
     * Performs the mutation step where several values of all individuals are
     * selected and changed to a certain degree.
     *
     */
    private void mutate() {
        Random generator = new Random();
        List<Individual> delete = new ArrayList<>();
        List<Individual> add = new ArrayList<>();
        for (Individual currentIndividuum : currentPopulation) {
            Map<Parameter, Object> configValues = new HashMap<>();
            boolean mutated = false;
            for (Parameter key : currentIndividuum.getConfig().getConfig().keySet()) {
                //determine the values
                if (new Double(generator.nextInt(101)) / 100.00 < mutationRate) {
                    int index = (int) Math.round(Math.random() * (ranges.getRanges().get(key).size()-1));
                    configValues.put(key, ranges.getRanges().get(key).get(index));
                    mutated = true;
                } else {
                    configValues.put(key, currentIndividuum.getConfig().getConfig().get(key));
                }
            }
            if (mutated) {
                Individual mutatedIndividual = new Individual(new Configuration(configValues, ranges.getAliases()),m);
                add.add(mutatedIndividual);
                individualCounter++;
            }
        }
        for (Individual i : delete) {
            currentPopulation.remove(i);
        }
        for (Individual i : add) {
            currentPopulation.add(i);
        }
    }

    /**
     * Create the initial population.
     *
     */
    private void createFirstPopulation() {
        currentPopulation = new ArrayList<>();
        for (int i = 0; i < startPopulationSize; i++) {
            individualCounter++;
            Individual newIndividuum = new Individual(ranges,m);
            currentPopulation.add(newIndividuum);
        }
    }
}
