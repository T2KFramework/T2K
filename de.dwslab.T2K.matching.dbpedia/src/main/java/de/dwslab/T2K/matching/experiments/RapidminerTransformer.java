package de.dwslab.T2K.matching.experiments;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author domi
 */
public class RapidminerTransformer {

    public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException {
        File dir = new File("C:\\Users\\domi\\WebTables\\Improvements\\FrequentItemsets");
        Map<String, Double> itemsets = new HashMap<>();
        for (File f : dir.listFiles()) {

            System.out.println(f.getName());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);

            NodeList numer = doc.getElementsByTagName("numberOfTransactions");
            double numberOfTrans = 0.0;
            for (int temp = 0; temp < numer.getLength(); temp++) {
                Element e1Element = (Element) numer.item(temp);
                numberOfTrans = Double.parseDouble(e1Element.getTextContent());
                //System.out.println(f.getName() + " - " + Double.parseDouble(e1Element.getTextContent()));
            }

            NodeList nameList = doc.getElementsByTagName("com.rapidminer.operator.learner.associations.FrequentItemSet");

            for (int temp = 0; temp < nameList.getLength(); temp++) {
                Element e1Element = (Element) nameList.item(temp);

//               System.out.println(e1Element
//                  .getElementsByTagName("name")
//                  .item(0)
//                  .getTextContent());
//               
//               System.out.println(e1Element
//                  .getElementsByTagName("frequency")
//                  .item(0)
//                  .getTextContent());
                double freq = Double.parseDouble(e1Element.getElementsByTagName("frequency").item(0).getTextContent());
                System.out.println(f.getName() + "\t" + e1Element.getElementsByTagName("name").item(0).getTextContent() +"\t"+ freq / numberOfTrans);

                if (itemsets.containsKey(e1Element.getElementsByTagName("name").item(0).getTextContent())) {
                    double sum = itemsets.get(e1Element.getElementsByTagName("name").item(0).getTextContent());
                    sum++;
                    itemsets.put(e1Element.getElementsByTagName("name").item(0).getTextContent(), sum);
                } else {
                    itemsets.put(e1Element.getElementsByTagName("name").item(0).getTextContent(), 1.0);
                }
            }

        }
        
        for(String p : itemsets.keySet()) {
            System.out.println(p + "\t"+itemsets.get(p));
        }

    }

}
