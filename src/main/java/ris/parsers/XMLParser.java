package ris.parsers;

import org.openstreetmap.osm._0.Node;
import org.openstreetmap.osm._0.Tag;
import ris.db.DBWorker;
import ris.db.exceptions.DBException;
import ris.parsers.exceptions.UnmarshallerCreationException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class XMLParser {

    private final Unmarshaller unmarshaller;

    public XMLParser() {
        try {
            unmarshaller = JAXBContext.newInstance(Node.class, Tag.class).createUnmarshaller();
        } catch (JAXBException jaxbException) {
            throw new UnmarshallerCreationException("Unable to create unmarshaller for Node and Tag classes.",
                    jaxbException);
        }
    }

    public long parseXML(InputStream in, boolean printResults) throws XMLStreamException, JAXBException {
        XMLEventReader reader = XMLInputFactory.newDefaultFactory().createXMLEventReader(in);
        Map<String, Integer> userNodes = new HashMap<>();
        Map<String, Integer> tagNumber = new HashMap<>();
        long lastNodeId = -1;

        try {
            DBWorker dbWorker = new DBWorker();
            long start = System.nanoTime();
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.peek();
                if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    StartElement startElement = xmlEvent.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();
                    if (startElementName.equals("node")) {
                        Node node = (Node) unmarshaller.unmarshal(reader);
                        lastNodeId = node.getId();
                        String userName = node.getUserName();
                        if (userNodes.containsKey(userName)) {
                            userNodes.put(userName, userNodes.get(userName) + 1);
                        } else {
                            userNodes.put(userName, 1);
                        }
                    } else if (startElementName.equals("tag")) {
                        Tag tag = (Tag) unmarshaller.unmarshal(reader);
                        String k = tag.getK();
                        // lastNodeId
                        if (tagNumber.containsKey(k)) {
                            tagNumber.put(k, tagNumber.get(k) + 1);
                        } else {
                            tagNumber.put(k, 1);
                        }
                    }
                }
                reader.nextEvent();
            }
            long end = System.nanoTime();
            if (printResults) {
                printSortedByValue(userNodes, (i1, i2) -> i2 - i1);
                printSortedByValue(tagNumber, (i1, i2) -> i2 - i1);
            }

            return end - start;
        } catch (SQLException sqlException) {
            throw new DBException("parseXML", sqlException);
        }
    }

    private static <K, V> void printSortedByValue(Map<K, V> map, Comparator<V> comparator) {
        System.out.println(map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(comparator))
                .map(Map.Entry<K, V>::toString)
                .collect(Collectors.joining("], [", "[", "]\n")));
    }
}
