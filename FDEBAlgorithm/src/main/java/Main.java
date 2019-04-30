import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static List<List<Flight>> edges;
    private static Node[] nodes;
    private int numVertices;
    private int numEdges;

    private Node parseNodeData(final int ID, String data){
        String[] splitted = data.split("\\(");
        String name = splitted[0];

        String[] locSplit = splitted[1].split(",");
        String lngxPart = locSplit[0];
        String latyPart = locSplit[1].replace(")", "");

        double lngx = Double.valueOf(lngxPart.split("lngx=")[1]);
        double laty = Double.valueOf(latyPart.split("laty=")[1]);

        return new Node(lngx, laty, ID, name);
    }

    public void parseInputData() throws IOException {

        Graph graph = new TinkerGraph();
        GraphMLReader reader = new GraphMLReader(graph);

        InputStream is = new BufferedInputStream(new FileInputStream("src/main/resources/airlines.graphml"));
        reader.inputGraph(is);

        numVertices = (int)graph.getVertices().spliterator().getExactSizeIfKnown();
        numEdges = (int)graph.getEdges().spliterator().getExactSizeIfKnown();

//        System.out.println(numVertices);
//        System.out.println(numEdges);

        edges = new ArrayList<>(numVertices);
        for (int i = 0; i < numVertices; i++) {
            edges.add(new ArrayList<>());
        }

        nodes = new Node[numVertices];

        for(Vertex v : graph.getVertices()){
            int nodeID = Integer.valueOf((String) v.getId());
            String nodeData = v.getProperty("tooltip").toString();
            nodes[nodeID] = parseNodeData(nodeID, nodeData);
        }

//        printNodes();

        for(Edge e : graph.getEdges()){
            int nodeFromID = Integer.valueOf((String)e.getVertex(Direction.OUT).getId());
            int nodeToID = Integer.valueOf((String)e.getVertex(Direction.IN).getId());
            edges.get(nodeFromID).add(new Flight(nodes[nodeFromID], nodes[nodeToID], Integer.valueOf((String) e.getId())));
        }

        printEdges();


    }

    public void printNodes(){
        Arrays.stream(nodes).forEach(System.out::println);
    }

    public void printEdges(){
        for (int i = 0; i < numVertices; i++) {
            System.out.println("Edges from node " + i + ":");
            for(Flight flight : edges.get(i)){
                System.out.println(flight);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Main m  = new Main();
        m.parseInputData();

//        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(nodes, edges);


//        Graph graph = new TinkerGraph();
//        GraphMLReader reader = new GraphMLReader(graph);
//
//        InputStream is = new BufferedInputStream(new FileInputStream("./res/graph.xml"));
//        reader.inputGraph(is);
//
//        Iterable<Vertex> vertices = graph.getVertices();
//        Iterator<Vertex> verticesIterator = vertices.iterator();
//
//        while (verticesIterator.hasNext()) {
//
//            Vertex vertex = verticesIterator.next();
//            Iterable<Flight> edges
//            Iterator<Flight> edgesIterator = edges.iterator();
//
//            while (edgesIterator.hasNext()) {
//
//                Flight edge = edgesIterator.next();
//                Vertex outVertex = edge.getOutVertex();
//                Vertex inVertex = edge.getInVertex();
//
//                String person = (String) outVertex.getProperty("name");
//                String knownPerson = (String) inVertex.getProperty("name");
//                int since = (Integer) edge.getProperty("since");
//
//                String sentence = person + " " + edge.getLabel() + " " + knownPerson
//                        + " since " + since + ".";
//                System.out.println(sentence);
//            }
//        }
    }

}
