package ris.parsers;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class XMLParser {
    public long parseXML(InputStream in, boolean printResults) throws XMLStreamException {
        XMLEventReader reader = XMLInputFactory.newDefaultFactory().createXMLEventReader(in);
        QName userAttr = new QName("user");
        QName idAttr = new QName("k");
        Map<String, Integer> userNodes = new HashMap<>();
        Map<String, Integer> tagNumber = new HashMap<>();

        long start = System.nanoTime();
        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                StartElement startElement = xmlEvent.asStartElement();
                String startElementName = startElement.getName().getLocalPart();
                if (startElementName.equals("node")) {
                    addElement(userAttr, userNodes, startElement);
                }
                else if (startElementName.equals("tag")) {
                    addElement(idAttr, tagNumber, startElement);
                }
            }
        }
        long end = System.nanoTime();

        if (printResults) {
            printSortedByValue(userNodes, (i1, i2) -> i2 - i1);
            printSortedByValue(tagNumber, (i1, i2) -> i2 - i1);
        }

        return end - start;
    }

    private static void addElement(QName idAttr, Map<String, Integer> tagNumber, StartElement startElement) {
        Attribute attr = startElement.getAttributeByName(idAttr);
        if (attr != null) {
            String attrValue = attr.getValue();
            if (tagNumber.containsKey(attrValue)) {
                tagNumber.put(attrValue, tagNumber.get(attrValue) + 1);
            } else {
                tagNumber.put(attrValue, 1);
            }
        }
    }

    private static <K, V> void printSortedByValue(Map<K, V> map, Comparator<V> comparator) {
        System.out.println(map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .map(Map.Entry<K, V>::toString)
                .collect(Collectors.joining("], [", "[", "]")));
    }
}
