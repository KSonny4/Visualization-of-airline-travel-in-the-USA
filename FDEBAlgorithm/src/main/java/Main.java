import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static List<List<Flight>> adjacency;
    private static Flight[]flights;
    private static Airport[] airports;
    private int numVertices;
    private int numEdges;

    private Airport parseAirportData(final int ID, String data){
        String[] splitted = data.split("\\(");
        String name = splitted[0];

        String[] locSplit = splitted[1].split(",");
        String lngxPart = locSplit[0];
        String latyPart = locSplit[1].replace(")", "");

        double lngx = Double.valueOf(lngxPart.split("lngx=")[1]);
        double laty = Double.valueOf(latyPart.split("laty=")[1]);

        return new Airport(lngx, laty, ID, name);
    }

    private void parseInputData() throws IOException {

        Graph graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);

        InputStream is = new BufferedInputStream(new FileInputStream("src/main/resources/airlines.graphml"));
        reader.inputGraph(is);

        numVertices = (int)graph.getVertices().spliterator().getExactSizeIfKnown();
        numEdges = (int)graph.getEdges().spliterator().getExactSizeIfKnown();

//        System.out.println(numVertices);
//        System.out.println(numEdges);


        flights = new Flight[numEdges];
        airports = new Airport[numVertices];

        for(Vertex v : graph.getVertices()){
            int nodeID = Integer.valueOf((String) v.getId());
            String nodeData = v.getProperty("tooltip").toString();
            airports[nodeID] = parseAirportData(nodeID, nodeData);
        }

//        printAirports();

        adjacency = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            adjacency.add(new ArrayList<>());
        }

        for(Edge e : graph.getEdges()){
            int nodeFromID = Integer.valueOf((String)e.getVertex(Direction.OUT).getId());
            int nodeToID = Integer.valueOf((String)e.getVertex(Direction.IN).getId());
            int flightID = Integer.valueOf((String) e.getId());
            Flight flight = new Flight(airports[nodeFromID], airports[nodeToID], flightID);
            adjacency.get(nodeFromID).add(flight);
            flights[flightID] = flight;
        }

        printAdjacency();

        printFlights();

    }

    private void printAirports(){
        Arrays.stream(airports).forEach(System.out::println);
    }

    private void printAdjacency(){
        for (int i = 0; i < numVertices; i++) {
            System.out.println("Edges from node " + i + ":");
            for(Flight flight : adjacency.get(i)){
                System.out.println(flight);
            }
        }
    }

    private void printFlights(){
        Arrays.stream(flights).forEach(System.out::println);
    }

    public static void main(String[] args) throws IOException {
        Main m  = new Main();
        m.parseInputData();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(airports, adjacency);

    }

}
