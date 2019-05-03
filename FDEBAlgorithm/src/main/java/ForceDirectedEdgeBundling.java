import model.Coordinate;
import model.Node;
import model.Edge;

import java.util.ArrayList;
import java.util.List;

public class ForceDirectedEdgeBundling {

    private final double STEP_SIZE;
    private final double COMPATIBILITY;
    private final double EDGE_STIFFNESS = 0.9;
    private final int ITERATIONS_COUNT = 90;
    private final double ITERATIONS_INCREASE_RATE = 0.666;
    private final int CYCLES_COUNT = 6;
    private final int SUBDIVISION_POINTS = 1;
    private final int SUBDIVISION_POINTS_RATE = 2;
    private final double EPS = 0.000001;

    private Node[] airports;
    private Edge[] flights;
    private List<List<Edge>> adjacency;
    private List<List<Node>> edgeSubdivisions;
    private List<List<Edge>> edgeCompatibility;

    /**
     * Constructor with specified values of STEP_SIZE and COMPATIBILITY.
     *
     * @param airports
     * @param flights
     * @param adjacency
     * @param STEP_SIZE
     * @param COMPATIBILITY
     */
    public ForceDirectedEdgeBundling(Node[] airports, Edge[] flights, List<List<Edge>> adjacency, final double STEP_SIZE, double COMPATIBILITY) {
        this.STEP_SIZE = STEP_SIZE;
        this.COMPATIBILITY = COMPATIBILITY;
        this.adjacency = adjacency;
        this.airports = airports;
        this.flights = flights;
    }

    /**
     * Constructor with default values of STEP_SIZE and COMPATIBILITY.
     *
     * @param airports
     * @param flights
     * @param adjacency
     */
    public ForceDirectedEdgeBundling(Node[] airports, Edge[] flights, List<List<Edge>> adjacency){
        this(airports, flights,  adjacency, 0.1, 0.6);
    }

    public List<List<Node>> run(){
        double stepSize = STEP_SIZE;
        double iterations = ITERATIONS_COUNT;
        int subdivisionPoints = SUBDIVISION_POINTS;

        initializeEdgeSubdivision();
        initializeCompatibilityLists();
        updateEdgeSubdivisions(subdivisionPoints);
        calculateCompatibilities();

        for (int cycle = 0; cycle < CYCLES_COUNT; cycle++) {
            System.out.println("cycle: " + cycle);

            for (int iter = 0; iter < iterations; iter++) {
                System.out.println("iteration: " + iter);

                List<List<Coordinate>> forces = new ArrayList<>(flights.length);
                for (int i = 0; i < flights.length ; i++) {
                    List<Coordinate> frcs = applyForces(i, subdivisionPoints, stepSize);
                    forces.add(frcs);
                }

                for (int i = 0; i < flights.length; i++) {
                    for (int j = 0; j < subdivisionPoints + 1; j++) {
                        Coordinate force = forces.get(i).get(j);
                        edgeSubdivisions.get(i).get(j).alterPositionBy(force.getX(), force.getY());
                    }
                }
            }

            stepSize /= 2;
            iterations *= ITERATIONS_INCREASE_RATE;
            subdivisionPoints *= SUBDIVISION_POINTS_RATE;

            updateEdgeSubdivisions(subdivisionPoints);
        }

        return edgeSubdivisions;
    }



    private void initializeEdgeSubdivision(){
        edgeSubdivisions = new ArrayList<>(flights.length);
        for (int i = 0; i < flights.length; i++) {
            List<Node> points = new ArrayList<>();
            if(SUBDIVISION_POINTS > 1){
                points.add(flights[i].getFrom());
                points.add(flights[i].getTo());
            }
            edgeSubdivisions.add(points);
        }
    }

    private void initializeCompatibilityLists(){
        edgeCompatibility = new ArrayList<>(flights.length);
        for (int i = 0; i < flights.length; i++) {
            edgeCompatibility.add(new ArrayList<>());
        }
    }

    private Coordinate calculateSpringForce(int edgeID, int i, double kP){
        Node curr = edgeSubdivisions.get(edgeID).get(i);
        Node prev = edgeSubdivisions.get(edgeID).get(i-1);
        Node next = edgeSubdivisions.get(edgeID).get(i+1);

        return new Coordinate((prev.getPosition().getX() + next.getPosition().getX() - 2*curr.getPosition().getX()) * kP,
                (prev.getPosition().getY() + next.getPosition().getY() - 2*curr.getPosition().getY()) * kP);
    }

    private Coordinate calculateElectrostaticForce(int edgeID, int i){
        double x = 0;
        double y = 0;

        List<Edge> compatibleEdges = edgeCompatibility.get(edgeID);

        for (int j = 0; j < compatibleEdges.size(); j++) {

            Edge compatibleEdge = compatibleEdges.get(j);
            Coordinate force = new Coordinate(edgeSubdivisions.get(compatibleEdge.getID()).get(i).getPosition().getX() -
                    edgeSubdivisions.get(edgeID).get(i).getPosition().getX(),
                    edgeSubdivisions.get(compatibleEdge.getID()).get(i).getPosition().getY() -
                            edgeSubdivisions.get(edgeID).get(i).getPosition().getY());


            if(Math.abs(force.getX()) > EPS || Math.abs(force.getY()) > EPS){
                Coordinate src = edgeSubdivisions.get(compatibleEdge.getID()).get(i).getPosition();
                double diff = (1 / Math.pow(src.euclideanDistance(edgeSubdivisions.get(edgeID).get(i).getPosition()), 1));

                x += force.getX() * diff;
                y += force.getY() * diff;
            }
        }

        return new Coordinate(x, y);
    }


    private List<Coordinate> applyForces(int edgeID, final int SEGMENTS_COUNT, final double S){
        List<Coordinate> forces = new ArrayList<>();
        forces.add(new Coordinate(0,0));

        double kP = EDGE_STIFFNESS / (flights[edgeID].getLength(EPS) * (SEGMENTS_COUNT + 1));

        for (int i = 1; i < (SEGMENTS_COUNT + 1); i++) {
            double x;
            double y;

            Coordinate springForce = calculateSpringForce(edgeID, i, kP);
            Coordinate electroStaticForce = calculateElectrostaticForce(edgeID, i);

            x = S * (springForce.getX() + electroStaticForce.getX());
            y = S * (springForce.getY() + electroStaticForce.getY());

            forces.add(new Coordinate(x, y));

        }

        forces.add(new Coordinate(0,0));
        return forces;
    }

    private void updateEdgeSubdivisions(final int SEGMENTS_COUNT){
        for (int i = 0; i < flights.length; i++) {
            if(SEGMENTS_COUNT == 1){
                edgeSubdivisions.get(i).add(flights[i].getFrom());
                Coordinate midpoint = flights[i].getMidpoint();
                edgeSubdivisions.get(i).add(new Node(midpoint.getX(), midpoint.getY()));
                edgeSubdivisions.get(i).add(flights[i].getTo());
            }else{
                double dividedLength = flights[i].getDividedEdgeLength(edgeSubdivisions.get(i));
                final double segmentLength = dividedLength / (SEGMENTS_COUNT + 1);
                double currSegmentLength = segmentLength;

                List<Node> newEdgeSubdivisions = new ArrayList<>(flights.length);
                newEdgeSubdivisions.add(flights[i].getFrom());

                for (int j = 1; j < edgeSubdivisions.get(i).size(); j++) {
                    List<Node> points = edgeSubdivisions.get(i);
                    double oldSegmentLength = points.get(j).getPosition().euclideanDistance(points.get(j-1).getPosition());

                    while(oldSegmentLength > currSegmentLength){
                        double percentage = currSegmentLength / oldSegmentLength;
                        double x = points.get(j-1).getPosition().getX();
                        double y = points.get(j-1).getPosition().getY();

                        x += percentage * (points.get(j).getPosition().getX() - points.get(j-1).getPosition().getX());
                        y += percentage * (points.get(j).getPosition().getY() - points.get(j-1).getPosition().getY());

                        newEdgeSubdivisions.add(new Node(x,y));

                        oldSegmentLength -= currSegmentLength;
                        currSegmentLength = segmentLength;
                    }

                    currSegmentLength -= oldSegmentLength;
                }

                newEdgeSubdivisions.add(flights[i].getTo());
                edgeSubdivisions.get(i).clear();
                edgeSubdivisions.get(i).addAll(newEdgeSubdivisions);

            }
        }
    }

    private void calculateCompatibilities(){
        for (int i = 0; i < flights.length - 1; i++) {
            for (int j = i + 1; j < flights.length; j++) {
                if(flights[i].compatible(flights[j], COMPATIBILITY)){
                    edgeCompatibility.get(i).add(flights[j]);
                    edgeCompatibility.get(j).add(flights[i]);
                }
            }
        }
    }








}
