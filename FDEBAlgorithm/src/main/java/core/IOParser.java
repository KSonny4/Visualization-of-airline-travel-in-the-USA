package core;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import model.Edge;
import model.Node;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class IOParser {

    class LatLongConverter{
        private double [] topLeft;
        private double [] bottomRight;
        private final double radius = 6.371;
        private double [] topLeftXY;
        private double [] bottomRightXY;

        public LatLongConverter() {
            topLeft = new double[4];
            bottomRight = new double[4];
            topLeftXY = new double[2];
            bottomRightXY = new double[2];
        }

        public LatLongConverter setTopLeftCanvasXY(double x, double y) {
            this.topLeft[0] = x;
            this.topLeft[1] = y;
            return this;
        }
        public LatLongConverter setBottomRightCanvasXY(double x, double y) {
            this.bottomRight[0] = x;
            this.bottomRight[1] = y;
            return this;
        }
        public LatLongConverter setTopLeftLatLong(double lat, double lng) {
            this.topLeft[2] = lat;
            this.topLeft[3] = lng;
            return this;
        }
        public LatLongConverter setBottomRightLatLong(double lat, double lng) {
            this.bottomRight[2] = lat;
            this.bottomRight[3] = lng;
            return this;
        }

        private double[] convertToGlobalXY(double lat, double lng){
            double globalX = radius * lng * Math.cos((topLeft[2] + bottomRight[2]) / 2.0);
            double globalY = radius * lat;
            return new double[]{globalX, globalY};
        }

        public LatLongConverter init(){
            this.topLeftXY = convertToGlobalXY(topLeft[2], topLeft[3]);
            this.bottomRightXY = convertToGlobalXY(bottomRight[2], bottomRight[3]);
            return this;
        }

        public double[] convertLatLngToXY(double lat, double lng){

            double[] global = convertToGlobalXY(lat, lng);

            double percentX = (global[0] - topLeftXY[0]) / (bottomRightXY[0] - topLeftXY[0]);
            double percentY = (global[1] - topLeftXY[1]) / (bottomRightXY[1] - topLeftXY[1]);

            return new double[]{topLeft[0] + (bottomRight[0] - topLeft[0]) * percentX,
                                topLeft[1] + (bottomRight[1] - topLeft[1]) * percentY};
        }
    }

    private static Edge[] flights;
    private static Node[] airports;

    /**
     * Initializes IOParser with specified {@code path}.
     * Loads data from file on specified path in expected GraphML format.
     *
     * @param pathToFile
     * @throws IOException
     */
    public IOParser(String pathToFile) throws IOException {
        loadInputData(pathToFile);
    }

    /**
     * Defines nodes by their latitude/longitude coordinates rather by their x/y mappings.
     *
     * @param ID
     * @param data
     * @return
     */
    @SuppressWarnings("unused")
    private Node parseAirportData(final int ID, String data){
        String[] splitted = data.split("\\(");
        String name = splitted[0];

        String[] locSplit = splitted[1].split(",");
        String lngxPart = locSplit[0];
        String latyPart = locSplit[1].replace(")", "");

        double laty = Double.valueOf(latyPart.split("laty=")[1]);
        double lngx = Double.valueOf(lngxPart.split("lngx=")[1]);

        return new Node(laty, lngx, ID, name);
    }


    /**
     * Loads and parses input data. Stores data into {@code flights} and {@code airports} arrays.
     *
     * @throws IOException
     */
    private void loadInputData(String pathToFile) throws IOException {

        Graph graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);

        InputStream is = new BufferedInputStream(new FileInputStream(pathToFile));
        reader.inputGraph(is);

        final int numNodes = (int)graph.getVertices().spliterator().getExactSizeIfKnown();
        final int numEdges = (int)graph.getEdges().spliterator().getExactSizeIfKnown();

        flights = new Edge[numEdges];
        airports = new Node[numNodes];

        for(Vertex v : graph.getVertices()){
            int nodeID = Integer.valueOf((String) v.getId());
            String name = v.getProperty("tooltip").toString();
            airports[nodeID] = parseAirportData(nodeID, name);
        }

        // need to compute minimal and maximal latitude and longitude
        double minLat = Arrays.stream(airports).mapToDouble(e-> e.getPosition().getX()).min().orElse(0);
        double maxLat = Arrays.stream(airports).mapToDouble(e-> e.getPosition().getX()).max().orElse(0);
        double minLng = Arrays.stream(airports).mapToDouble(e-> e.getPosition().getY()).min().orElse(0);
        double maxLng = Arrays.stream(airports).mapToDouble(e-> e.getPosition().getY()).max().orElse(0);

        // initialize latLong <-> canvasXY converter
        LatLongConverter converter = new LatLongConverter()
                .setTopLeftLatLong(maxLat, minLng)
                .setBottomRightLatLong(minLat, maxLng)
                .setTopLeftCanvasXY(100, 50)
                .setBottomRightCanvasXY(1050, 610)
                .init();

        // convert location of nodes to XY canvas
        for(Node airport : airports){
            double[] xy = converter.convertLatLngToXY(airport.getPosition().getX(), airport.getPosition().getY());
            airport.setPosition(xy[0], xy[1]);
        }

        for(com.tinkerpop.blueprints.Edge e : graph.getEdges()){
            int nodeFromID = Integer.valueOf((String)e.getVertex(Direction.OUT).getId());
            int nodeToID = Integer.valueOf((String)e.getVertex(Direction.IN).getId());
            int flightID = Integer.valueOf((String) e.getId());
            Edge edge = new Edge(airports[nodeFromID], airports[nodeToID], flightID);
            flights[flightID] = edge;
        }


    }

    @SuppressWarnings("unused")
    public void printAirports(){
        Arrays.stream(airports).forEach(System.out::println);
    }

    @SuppressWarnings("unused")
    public void printFlights(){
        Arrays.stream(flights).forEach(System.out::println);
    }

    @SuppressWarnings("unused")
    public void printBundledEdges(List<List<Node>> edgeSubdivisions){
        for (int i = 0; i < edgeSubdivisions.size(); i++) {
            System.out.println(flights[i]);
            for(Node point : edgeSubdivisions.get(i)){
                System.out.println("\t " + point.getPosition());
            }
        }
    }

    @SuppressWarnings("unused")
    public void printToJson(Edge[]edges) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter("src/main/resources/results.js", "UTF-8");
        writer.println("var results = [");

        for (int j = 0; j < edges.length; j++) {
            writer.print("[");
            int idx = 0;
            for(Node point : edges[j].getSubdivisionPoints()){
                writer.print("{\"x\":" + point.getPosition().getX() + ", \"y\":" + point.getPosition().getY() + "}");
                if(++idx < edges[j].getSubdivisionPoints().size()){
                    writer.print(",");
                }
            }
            writer.print("]");
            if(j < edges.length -1){
                writer.print(",");
            }
        }

        writer.println("]");
        writer.close();
    }

    /**
     * Retrieve parsed airports
     *
     * @return
     */
    public Node[] getAirports(){
        return airports;
    }

    /**
     * Retrieve parsed flights
     *
     * @return
     */
    public Edge[] getFlights(){
        return flights;
    }

}
