package de.dwslab.T2K.matching.dbpedia.model;

import de.dwslab.T2K.matching.dbpedia.properties.Canoniser;
import java.util.Collection;
import java.util.List;

/**
 * Parameters of the evaluation process
 *
 * @author Oliver
 *
 */
public class EvaluationParameters {

    private String propertyGoldStandardLocation;

    /**
     * gets the directory of the property gold standard
     *
     * @return
     */
    public String getPropertyGoldStandardLocation() {
        return propertyGoldStandardLocation;
    }

    /**
     * Sets the directory of the property gold standard
     *
     * @param propertyGoldStandardLocation
     */
    public void setPropertyGoldStandardLocation(
            String propertyGoldStandardLocation) {
        this.propertyGoldStandardLocation = propertyGoldStandardLocation;
    }
    private String equivalentPropertiesLocation;

    /**
     * gets the path of the equivalent properties file
     *
     * @return
     */
    public String getEquivalentPropertiesLocation() {
        return equivalentPropertiesLocation;
    }
    private Canoniser equivPropertyCanoniser;
    private Canoniser equivInstanceCanoniser;

    public void loadCanoniser() {
        if (getEquivPropertyCanoniser() == null) {

            equivInstanceCanoniser = new Canoniser();
            getEquivInstanceCanoniser().loadEquivalentResources(correctedInstancesLocation);

            equivPropertyCanoniser = new Canoniser();
            getEquivPropertyCanoniser().loadEquivalentResources(getEquivalentPropertiesLocation());
        }
    }

    /**
     * sets the path of the equivalent properties file
     *
     * @param equivalentPropertiesLocation
     */
    public void setEquivalentPropertiesLocation(
            String equivalentPropertiesLocation) {
        this.equivalentPropertiesLocation = equivalentPropertiesLocation;
    }
    private String propertyRangeGoldstandardLocation;
    private String instanceGoldStandardLocation;

    /**
     * gets the directory of the instance gold standard
     *
     * @return
     */
    public String getInstanceGoldStandardLocation() {
        return instanceGoldStandardLocation;
    }

    /**
     * sets the directory of the instance gold standard
     *
     * @param instanceGoldStandardLocation
     */
    public void setInstanceGoldStandardLocation(
            String instanceGoldStandardLocation) {
        this.instanceGoldStandardLocation = instanceGoldStandardLocation;
    }
    private String correctedInstancesLocation;

    /**
     * gets the path of the corrected instances file
     *
     * @return
     */
    public String getCorrectedInstancesLocation() {
        return correctedInstancesLocation;
    }

    /**
     * sets the path of the corrected instances file
     *
     * @param correctedInstancesLocation
     */
    public void setCorrectedInstancesLocation(String correctedInstancesLocation) {
        this.correctedInstancesLocation = correctedInstancesLocation;
    }
    private String classGoldStandardLocation;

    /**
     * gets the path of the class gold standard file
     *
     * @return
     */
    public String getClassGoldStandardLocation() {
        return classGoldStandardLocation;
    }

    /**
     * sets the path of the class gold standard file
     *
     * @param classGoldStandardLocation
     */
    public void setClassGoldStandardLocation(String classGoldStandardLocation) {
        this.classGoldStandardLocation = classGoldStandardLocation;
    }
    private String classHierarchyLocation;

    /**
     * sets the path of the class hierarchy file
     *
     * @param classHierarchyLocation
     */
    public void setClassHierarchyLocation(String classHierarchyLocation) {
        this.classHierarchyLocation = classHierarchyLocation;
    }

    /**
     * gets the path of the class hierarchy file
     *
     * @return
     */
    public String getClassHierarchyLocation() {
        return classHierarchyLocation;
    }

    /**
     * @return the propertyRangeGoldstandardLocation
     */
    public String getPropertyRangeGoldstandardLocation() {
        return propertyRangeGoldstandardLocation;
    }

    /**
     * @param propertyRangeGoldstandardLocation the
     * propertyRangeGoldstandardLocation to set
     */
    public void setPropertyRangeGoldstandardLocation(String propertyRangeGoldstandardLocation) {
        this.propertyRangeGoldstandardLocation = propertyRangeGoldstandardLocation;
    }
    private String propertyRangesLocation;

    /**
     * @return the propertyRangesLocation
     */
    public String getPropertyRangesLocation() {
        return propertyRangesLocation;
    }

    /**
     * @param propertyRangesLocation the propertyRangesLocation to set
     */
    public void setPropertyRangesLocation(String propertyRangesLocation) {
        this.propertyRangesLocation = propertyRangesLocation;
    }

    /**
     * @return the equivPropertyCanoniser
     */
    public Canoniser getEquivPropertyCanoniser() {
        return equivPropertyCanoniser;
    }

    /**
     * @return the equivInstanceCanoniser
     */
    public Canoniser getEquivInstanceCanoniser() {
        return equivInstanceCanoniser;
    }


}
