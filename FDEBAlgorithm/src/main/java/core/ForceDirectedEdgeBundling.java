package core;

import model.Coordinate;
import model.Node;
import model.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForceDirectedEdgeBundling implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ForceDirectedEdgeBundling.class.getName());

    private final double STEP_SIZE;
    private final double COMPATIBILITY;
    private final double EDGE_STIFFNESS;
    private final int CYCLES_COUNT;
    private final int ITERATIONS_COUNT;
    private final double EPS = 0.000001;
    private final int INITIAL_SUBDIVISION_POINTS_COUNT = 1;
    private final double ITERATIONS_INCREASE_RATE = 0.666;
    private final int SUBDIVISION_POINTS_RATE = 2;

    private Node[] airports;
    private Edge[] flights;

    /**
     * Constructor called from GUI with user-specified values.
     *
     * @param airports
     * @param flights
     * @param STEP_SIZE
     * @param COMPATIBILITY
     * @param EDGE_STIFFNESS
     * @param ITERATIONS_COUNT
     * @param CYCLES_COUNT
     */
    public ForceDirectedEdgeBundling(Node[] airports, Edge[] flights, double STEP_SIZE, double COMPATIBILITY, double EDGE_STIFFNESS
    , int ITERATIONS_COUNT, int CYCLES_COUNT) {
        this.STEP_SIZE = STEP_SIZE;
        this.COMPATIBILITY = COMPATIBILITY;
        this.EDGE_STIFFNESS = EDGE_STIFFNESS;
        this.ITERATIONS_COUNT = ITERATIONS_COUNT;
        this.CYCLES_COUNT = CYCLES_COUNT;
        this.airports = airports;
        this.flights = flights;
    }

    /**
     * Uses default values for algorithm.
     *
     * @param airports
     * @param flights
     */
    public ForceDirectedEdgeBundling(Node[] airports, Edge[] flights){
        this(airports, flights, 0.3, 0.6, 0.9, 90, 7);
    }


    public void run(){

        LOGGER.log(Level.INFO, String.format("Running FDEG Algorithm with configuration:" +
                " \n STEP_SIZE %f \n EDGE_STIFFNESS %f \n COMPATIBILITY %f \n INITIAL_SUBDIVISION_POINTS %d \n" +
                        " ITERATIONS %d \n CYCLES %d \n",
                STEP_SIZE, EDGE_STIFFNESS, COMPATIBILITY, INITIAL_SUBDIVISION_POINTS_COUNT, ITERATIONS_COUNT, CYCLES_COUNT));

        if(CYCLES_COUNT > 15 || ITERATIONS_COUNT > 300)
            LOGGER.log(Level.WARNING, "HIGH NUMBER OF ITERATIONS OR CYCLES, ALGORITHM MIGHT RUN TOO LONG...");

        double currentStepSize = STEP_SIZE;
        double currIterationsCount = ITERATIONS_COUNT;
        int currentSubdivisionPointsCount = INITIAL_SUBDIVISION_POINTS_COUNT;

        updateEdgeSubdivisions(currentSubdivisionPointsCount);
        calculateCompatibilities();

        for (int cycle = 0; cycle < CYCLES_COUNT; cycle++) {
            LOGGER.log(Level.INFO, String.format("Cycle: %d \n", cycle));

            for (int iter = 0; iter < currIterationsCount; iter++) {

                if(iter % 10 == 0)
                    LOGGER.log(Level.INFO, String.format("Iteration: %d \n", iter));

                List<List<Coordinate>> forces = new ArrayList<>(flights.length);
                for (int i = 0; i < flights.length ; i++) {
                    forces.add(applyForces(i, currentSubdivisionPointsCount, currentStepSize));
                }

                for (int i = 0; i < flights.length; i++) {
                    for (int j = 0; j < currentSubdivisionPointsCount + 1; j++) {
                        Coordinate force = forces.get(i).get(j);
                        flights[i].getSubdivisionPoints().get(j).alterPositionBy(force.getX(), force.getY());
                    }
                }
            }

            currentStepSize /= 2;
            currIterationsCount *= ITERATIONS_INCREASE_RATE;
            currentSubdivisionPointsCount *= SUBDIVISION_POINTS_RATE;

            updateEdgeSubdivisions(currentSubdivisionPointsCount);
        }

    }

    private Coordinate calculateSpringForce(int edgeID, int i, double kP){
        Node curr = flights[edgeID].getSubdivisionPoints().get(i);
        Node prev = flights[edgeID].getSubdivisionPoints().get(i-1);
        Node next = flights[edgeID].getSubdivisionPoints().get(i+1);

        return new Coordinate((prev.getPosition().getX() + next.getPosition().getX() - 2*curr.getPosition().getX()) * kP,
                (prev.getPosition().getY() + next.getPosition().getY() - 2*curr.getPosition().getY()) * kP);
    }

    private Coordinate calculateElectrostaticForce(int currentEdgeID, int i){
        double x = 0;
        double y = 0;

        List<Edge> compatibleEdges = flights[currentEdgeID].getCompatibleEdges();

        for(Edge compatibleEdge : compatibleEdges) {

            List<Node> currentEdgeSubdivisionPoints = flights[currentEdgeID].getSubdivisionPoints();
            List<Node> compatibleEdgeSubdivisionPoints = compatibleEdge.getSubdivisionPoints();

            double forceX = compatibleEdgeSubdivisionPoints.get(i).getPosition().getX() -
                    currentEdgeSubdivisionPoints.get(i).getPosition().getX();
            double forceY = compatibleEdgeSubdivisionPoints.get(i).getPosition().getY() -
                    currentEdgeSubdivisionPoints.get(i).getPosition().getY();

            Coordinate force = new Coordinate(forceX, forceY);


            if (Math.abs(force.getX()) > EPS || Math.abs(force.getY()) > EPS) {
                Coordinate src = compatibleEdgeSubdivisionPoints.get(i).getPosition();
                double diff = (1 / Math.pow(src.euclideanDistance(currentEdgeSubdivisionPoints.get(i).getPosition()), 1));

                x += force.getX() * diff;
                y += force.getY() * diff;
            }
        }

        return new Coordinate(x, y);
    }


    private List<Coordinate> applyForces(int currentEdgeID, final int SEGMENTS_COUNT, final double S){
        List<Coordinate> forces = new ArrayList<>();
        forces.add(new Coordinate(0,0));

        double kP = EDGE_STIFFNESS / (flights[currentEdgeID].getLength(EPS) * (SEGMENTS_COUNT + 1));

        for (int i = 1; i < (SEGMENTS_COUNT + 1); i++) {
            double x;
            double y;

            Coordinate springForce = calculateSpringForce(currentEdgeID, i, kP);
            Coordinate electroStaticForce = calculateElectrostaticForce(currentEdgeID, i);

            x = S * (springForce.getX() + electroStaticForce.getX());
            y = S * (springForce.getY() + electroStaticForce.getY());

            forces.add(new Coordinate(x, y));

        }

        forces.add(new Coordinate(0,0));
        return forces;
    }

    private void updateEdgeSubdivisions(final int SEGMENTS_COUNT){
        for (int i = 0; i < flights.length; i++) {

            List<Node> subdivisionPoints = flights[i].getSubdivisionPoints();

            if(SEGMENTS_COUNT == 1){
                subdivisionPoints.add(flights[i].getFrom());
                Coordinate midpoint = flights[i].getMidpoint();
                subdivisionPoints.add(new Node(midpoint.getX(), midpoint.getY()));
                subdivisionPoints.add(flights[i].getTo());
            }else{
                double dividedLength = flights[i].getDividedEdgeLength(subdivisionPoints);
                final double segmentLength = dividedLength / (SEGMENTS_COUNT + 1);
                double currSegmentLength = segmentLength;

                List<Node> newEdgeSubdivisions = new ArrayList<>(flights.length);
                newEdgeSubdivisions.add(flights[i].getFrom());

                for (int j = 1; j < subdivisionPoints.size(); j++) {

                    double oldSegmentLength = subdivisionPoints.get(j).getPosition().euclideanDistance(subdivisionPoints.get(j-1).getPosition());

                    while(oldSegmentLength > currSegmentLength){
                        double percentage = currSegmentLength / oldSegmentLength;
                        double x = subdivisionPoints.get(j-1).getPosition().getX();
                        double y = subdivisionPoints.get(j-1).getPosition().getY();

                        x += percentage * (subdivisionPoints.get(j).getPosition().getX() - subdivisionPoints.get(j-1).getPosition().getX());
                        y += percentage * (subdivisionPoints.get(j).getPosition().getY() - subdivisionPoints.get(j-1).getPosition().getY());

                        newEdgeSubdivisions.add(new Node(x,y));

                        oldSegmentLength -= currSegmentLength;
                        currSegmentLength = segmentLength;
                    }

                    currSegmentLength -= oldSegmentLength;
                }

                newEdgeSubdivisions.add(flights[i].getTo());

                flights[i].setSubdivisionPoints(newEdgeSubdivisions);

            }
        }
    }

    private void calculateCompatibilities(){
        for (int i = 0; i < flights.length - 1; i++) {
            for (int j = i + 1; j < flights.length; j++) {
                if(flights[i].compatible(flights[j], COMPATIBILITY)){
                    flights[i].addCompatibleEdge(flights[j]);
                    flights[j].addCompatibleEdge(flights[i]);
                }
            }
        }
    }

    /**
     * Returns the array of airports.
     * This method is called from GUI when plotting nodes.
     *
     * @return
     */
    public Node[] getAirports() {
        return airports;
    }
}
