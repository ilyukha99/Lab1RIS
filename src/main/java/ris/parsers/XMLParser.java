package ris.parsers;

import org.openstreetmap.osm._0.Node;
import org.openstreetmap.osm._0.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ris.db.ConnectionManager;
import ris.db.DBInitializer;
import ris.db.dao.TagDAO;
import ris.db.exceptions.CustomRuntimeException;
import ris.db.info.DBMode;
import ris.db.dao.NodeDAO;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class XMLParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("MainLogger");

    private final Unmarshaller unmarshaller;

    public XMLParser() {
        try {
            unmarshaller = JAXBContext.newInstance(Node.class, Tag.class).createUnmarshaller();
        } catch (JAXBException jaxbException) {
            throw new UnmarshallerCreationException("Unable to create unmarshaller for Node and Tag classes.",
                    jaxbException);
        }
    }

    public long parseXML(InputStream in, boolean printResults, DBMode mode)
            throws XMLStreamException, JAXBException {
        XMLEventReader reader = XMLInputFactory.newDefaultFactory().createXMLEventReader(in);
        Map<String, Integer> userNodes = new HashMap<>();
        Map<String, Integer> tagNumber = new HashMap<>();

        try {
            NodeDAO nodeDAO = new NodeDAO();
            TagDAO tagDAO = new TagDAO(nodeDAO);
            tagDAO.dropTableIfExists();
            nodeDAO.dropTableIfExists();
            new DBInitializer().createBDStructure();

            Consumer<Node> saveNodeConsumer;
            BiConsumer<Tag, Long> saveTagConsumer;
            switch (mode) {
                case PREPARED_STATEMENT -> {
                    saveNodeConsumer = nodeDAO::saveNodePrepared;
                    saveTagConsumer = tagDAO::saveTagPrepared;
                }
                case BATCH -> {
                    saveNodeConsumer = nodeDAO::saveNodePreparedWithBatch;
                    saveTagConsumer = tagDAO::saveTagPreparedWithBatch;
                }
                default -> {
                    saveNodeConsumer = nodeDAO::saveNode;
                    saveTagConsumer = tagDAO::saveTag;
                }
            }

            long start = System.nanoTime();
            if (mode != DBMode.STATEMENT) {
                nodeDAO.initializePreparedStatement();
                tagDAO.initializePreparedStatement();
            }
            while (reader.hasNext()) {
                XMLEvent xmlEvent = reader.peek();
                if (xmlEvent.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    StartElement startElement = xmlEvent.asStartElement();
                    String startElementName = startElement.getName().getLocalPart();
                    if (startElementName.equals("node")) {
                        Node node = unmarshaller.unmarshal(reader, Node.class).getValue();
                        saveNodeConsumer.accept(node);

                        String userName = node.getUserName();
                        if (userNodes.containsKey(userName)) {
                            userNodes.put(userName, userNodes.get(userName) + 1);
                        } else {
                            userNodes.put(userName, 1);
                        }

                        long nodeId = node.getId();
                        for (Tag tag : node.getTags()) {
                            saveTagConsumer.accept(tag, nodeId);
                            String k = tag.getK();
                            if (tagNumber.containsKey(k)) {
                                tagNumber.put(k, tagNumber.get(k) + 1);
                            } else {
                                tagNumber.put(k, 1);
                            }
                        }
                    } else if (startElementName.equals("way")) {
                        break;
                    }
                }
                reader.nextEvent();
            }

            if (mode == DBMode.BATCH) {
                tagDAO.executeBatch();
                nodeDAO.executeBatch();
            }

            if (mode != DBMode.STATEMENT) {
                nodeDAO.closeNodePreparedStatement();
                tagDAO.closeTagPreparedStatement();
            }
            ConnectionManager.closeConnection();

            LOGGER.info(String.format("Estimated time of %s: %.10f sec",
                    mode, nodeDAO.getExecutionTime() / 1000_000_000d));
            long end = System.nanoTime();
            if (printResults) {
                printSortedByValue(userNodes, (i1, i2) -> i2 - i1);
                printSortedByValue(tagNumber, (i1, i2) -> i2 - i1);
            }

            return end - start;
        } catch (SQLException | CustomRuntimeException exception) {
            throw new DBException("parseXML", exception);
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
