package main;

import core.ForceDirectedEdgeBundling;
import core.IOParser;
import model.Node;
import model.Edge;
import java.io.*;

public class Main {


    public static void main(String[] args) throws IOException {

        IOParser parser = new IOParser("src/main/resources/airlines.graphml");

        Node[] nodes = parser.getNodes();
        Edge[] edges = parser.getEdges();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(nodes, edges);
        fdeb.run();

//        parser.printBundledEdges(edges);
//        parser.printToJson(edges);
    }

}
