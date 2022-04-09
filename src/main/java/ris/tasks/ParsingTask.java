package ris.tasks;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import ris.parsers.XMLParser;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.*;

import static ris.db.info.DBMode.*;

public class ParsingTask {
    public void execute(String[] args) {
        Options options = new Options();
        options.addOption("f", "file", true, "xml file with data");
        options.addOption("l", "limit", true, "limit number of xml element");
        options.addOption("o", "output", false, "output file name");
        DefaultParser parser = new DefaultParser();

        try {
            CommandLine line = parser.parse(options, args);
            String limitStr = line.getOptionValue("l", "all");
            String filePath = line.getOptionValue("f");
            String outputFilePath = line.getOptionValue("o");
            int limit = "all".equals(limitStr) ? Integer.MAX_VALUE : Integer.parseInt(limitStr);
            System.out.println("Limit = " + limitStr + "; " + " file: " + filePath);
            int bufferSize = 2048;

            if (outputFilePath != null) {
                try (InputStream decompressedInputStream = new BZip2CompressorInputStream(new BufferedInputStream(
                        new FileInputStream(filePath), bufferSize));
                     OutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
                    IOUtils.copyRange(decompressedInputStream, limit, fileOutputStream);
                }
            }
            else {
                try (InputStream decompressedInputStream = new BZip2CompressorInputStream(new BufferedInputStream(
                        new FileInputStream(filePath), bufferSize))) {

                    long nanos = new XMLParser().parseXML(decompressedInputStream, true, STATEMENT);

                    System.out.println("Approximate time of execution: " +
                            nanos / 1_000_000_000d + " sec, size of buffer = " + bufferSize + "\n");
                }
            }
        } catch (XMLStreamException | ParseException | IOException |  JAXBException exception) {
            exception.printStackTrace();
        }
    }
}
