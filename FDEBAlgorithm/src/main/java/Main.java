import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import model.Node;
import model.Edge;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main {

    private static List<List<Edge>> adjacency;
    private static Edge[] flights;
    private static Node[] airports;
    private int numNodes;
    private int numEdges;

    private Node parseAirportData(final int ID, String data){
        String[] splitted = data.split("\\(");
        String name = splitted[0];

        String[] locSplit = splitted[1].split(",");
        String lngxPart = locSplit[0];
        String latyPart = locSplit[1].replace(")", "");

        double lngx = Double.valueOf(lngxPart.split("lngx=")[1]);
        double laty = Double.valueOf(latyPart.split("laty=")[1]);

        return new Node(lngx, laty, ID, name);
    }

    private void parseInputData() throws IOException {

        Graph graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);

        InputStream is = new BufferedInputStream(new FileInputStream("src/main/resources/airlines.graphml"));
        reader.inputGraph(is);

        numNodes = (int)graph.getVertices().spliterator().getExactSizeIfKnown();
        numEdges = (int)graph.getEdges().spliterator().getExactSizeIfKnown();

        flights = new Edge[numEdges];
        airports = new Node[numNodes];

        for(Vertex v : graph.getVertices()){
            int nodeID = Integer.valueOf((String) v.getId());
            String nodeData = v.getProperty("tooltip").toString();
            airports[nodeID] = parseAirportData(nodeID, nodeData);
        }

//        printAirports();

        adjacency = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            adjacency.add(new ArrayList<>());
        }

        for(com.tinkerpop.blueprints.Edge e : graph.getEdges()){
            int nodeFromID = Integer.valueOf((String)e.getVertex(Direction.OUT).getId());
            int nodeToID = Integer.valueOf((String)e.getVertex(Direction.IN).getId());
            int flightID = Integer.valueOf((String) e.getId());
            Edge edge = new Edge(airports[nodeFromID], airports[nodeToID], flightID);
            adjacency.get(nodeFromID).add(edge);
            flights[flightID] = edge;
        }

//        printAdjacency();

//        printFlights();

    }

    private void printAirports(){
        Arrays.stream(airports).forEach(System.out::println);
    }

    private void printAdjacency(){
        for (int i = 0; i < numNodes; i++) {
            System.out.println("Edges from node " + i + ":");
            for(Edge edge : adjacency.get(i)){
                System.out.println(edge);
            }
        }
    }

    private void printFlights(){
        Arrays.stream(flights).forEach(System.out::println);
    }

    private void printEdgeSubdivisions(List<List<Node>> edgeSubdivisions){
        for (int i = 0; i < edgeSubdivisions.size(); i++) {
            System.out.println(flights[i]);
            for(Node point : edgeSubdivisions.get(i)){
                System.out.println("\t " + point.getPosition());
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Main m  = new Main();
        m.parseInputData();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(airports, flights, adjacency);
        List<List<Node>> edges = fdeb.run();
        m.printEdgeSubdivisions(edges);

    }

}
