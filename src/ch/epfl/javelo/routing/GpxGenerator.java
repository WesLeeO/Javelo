package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;


/**
 * GpxGenerator class
 * <p>
 * This class offers methods which enable to create
 * a GPX document using an elevation profile and a route.
 *
 * @author Wesley Nana Davies(344592)
 * @author David Farah (341017)
 */

public class GpxGenerator {
    private GpxGenerator() {
    }

    /**
     * This method returns a GPX document of all information about an itinerary (profile, route)
     *
     * @param profile : ElevationProfile of the itinerary
     * @param route   : Route of the itinerary
     * @return a GPX document
     */

    public static Document createGPX(ElevationProfile profile, Route route) {
        Document doc = newDocument();

        Element root = doc.createElementNS(
                "http://www.topografix.com/GPX/1/1",
                "gpx");

        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 " + "http://www.topografix.com/GPX/1/1/gpx.xsd");

        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement("rte");
        root.appendChild(rte);

        double distance = 0;
        Iterator<Edge> edgesIterator = route.edges().iterator();

        for (PointCh currentPoint : route.points()) {
            Element rtept = doc.createElement("rtept");
            Element ele = doc.createElement("ele");

            rtept.setAttribute("lat", String.format("%.5f", (Math.toDegrees(currentPoint.lat()))));
            rtept.setAttribute("lon", String.format("%.5f", (Math.toDegrees(currentPoint.lon()))));
            ele.setTextContent(String.format("%.2f", profile.elevationAt(distance)));
            rte.appendChild(rtept);
            rtept.appendChild(ele);

            if (edgesIterator.hasNext()) {
                distance += edgesIterator.next().length();
            }

        }

        return doc;
    }


    /**
     * This private method simply returns a new document
     *
     * @return a document
     */
    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e); // Should never happen
        }
    }

    /**
     * This method writes a GPX file with a given name using an elevation profile and a route.
     *
     * @param fileName         : name of the file
     * @param route            : route of the itinerary
     * @param elevationProfile : elevation profile of the route.
     * @throws IOException :  in the event of an input/output error
     */
    public static void writeGPX(String fileName, Route route, ElevationProfile elevationProfile) throws IOException {
        Document document = createGPX(elevationProfile, route);
        try (Writer w = new FileWriter(fileName)) {
            Transformer transformer = TransformerFactory
                    .newDefaultInstance()
                    .newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(w));
        } catch (TransformerException e) {
            throw new Error(e);
        }

    }

}
