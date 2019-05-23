package core;

import model.Coordinate;
import model.Node;
import model.Edge;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class performing Force-Directed Edge Bundling algorithm based on this paper:
 * http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.212.7989&rep=rep1&type=pdf
 *
 * In addition to parameters listed in the constructor, instance takes additional parameters:
 * DEFAULT_SUBDIVISION_POINTS_COUNT - initial number of subdivision points of each edge, 1 by default
 * DEFAULT_ITERATIONS_INCREASE_RATE - multiplier of number of iterations (used after each cycle), 0.66 by default
 * DEFAULT_SUBDIVISION_POINTS_RATE - divisor of number of subdivision points (used after each cycle), 2 by default
 *
 */
public class ForceDirectedEdgeBundling implements Observable {

    private static final Logger LOGGER = Logger.getLogger(ForceDirectedEdgeBundling.class.getName());

    private final double STEP_SIZE;
    private final double COMPATIBILITY;
    private final double K;
    private final int CYCLES_COUNT;
    private final int ITERATIONS_COUNT;


    private Node[] nodes;
    private Edge[] edges;

    private List<Observer> observers;

    /**
     * Constructor called from GUI with user-specified values.
     *
     * @param edges array of edges representing edges of graph indexed by its ID
     * @param edges array of edges representing edges of graph indexed by its ID
     * @param STEP_SIZE constant determining the size of step each subdivision point performs in the direction of
     *                  force
     * @param COMPATIBILITY compatibility threshold determining which pairs of edges will interact with each other
     * @param K flexibility of edges
     * @param ITERATIONS_COUNT number of iterations to perform
     * @param CYCLES_COUNT number of cycles to perform
     */
    public ForceDirectedEdgeBundling(Node[] nodes, Edge[] edges, double STEP_SIZE, double COMPATIBILITY, double K
    , int ITERATIONS_COUNT, int CYCLES_COUNT) {
        this.STEP_SIZE = STEP_SIZE;
        this.COMPATIBILITY = COMPATIBILITY;
        this.K = K;
        this.ITERATIONS_COUNT = ITERATIONS_COUNT;
        this.CYCLES_COUNT = CYCLES_COUNT;
        this.nodes = nodes;
        this.edges = edges;
        this.observers = new ArrayList<>();
    }

    /**
     * Uses default values for algorithm.
     *
     * @param nodes array of nodes representing nodes of graph indexed by its ID
     * @param edges array of edges representing edges of graph indexed by its ID
     */
    public ForceDirectedEdgeBundling(Node[] nodes, Edge[] edges){
        this(nodes, edges,
                Configuration.DEFAULT_STEP_SIZE,
                Configuration.DEFAULT_COMPATIBILITY_THRESHOLD,
                Configuration.DEFAULT_EDGE_STIFFNESS,
                Configuration.DEFAULT_ITERATIONS_COUNT,
                Configuration.DEFAULT_CYCLES_COUNT);
    }

    /**
     * Runs FDEB Algorithm on selected dataset.
     */
    public void run(){

        LOGGER.log(Level.INFO, String.format("Running FDEG Algorithm with configuration:" +
                " \n STEP_SIZE %f \n K %f \n COMPATIBILITY %f \n INITIAL_SUBDIVISION_POINTS %d \n" +
                        " ITERATIONS %d \n CYCLES %d \n",
                STEP_SIZE, K, COMPATIBILITY, Configuration.DEFAULT_SUBDIVISION_POINTS_COUNT, ITERATIONS_COUNT, CYCLES_COUNT));

        if(CYCLES_COUNT > 15 || ITERATIONS_COUNT > 300)
            LOGGER.log(Level.WARNING, "HIGH NUMBER OF ITERATIONS OR CYCLES, ALGORITHM MIGHT RUN TOO LONG...");

        double currentStepSize = STEP_SIZE;
        double currIterationsCount = ITERATIONS_COUNT;
        int currentSubdivisionPointsCount = Configuration.DEFAULT_SUBDIVISION_POINTS_COUNT;

        updateEdgeSubdivisions(currentSubdivisionPointsCount);
        calculateCompatibilities();

        for (int cycle = 0; cycle < CYCLES_COUNT; cycle++) {
            LOGGER.log(Level.INFO, String.format("Cycle: %d \n", cycle));

            for (int iter = 0; iter < currIterationsCount; iter++) {

                if(iter % 10 == 0)
                    LOGGER.log(Level.INFO, String.format("Iteration: %d \n", iter));

                notifyObservers(iter, cycle, false);

                List<List<Coordinate>> forces = new ArrayList<>(edges.length);
                for (int i = 0; i < edges.length ; i++) {
                    forces.add(calculateTotalForce(i, currentSubdivisionPointsCount, currentStepSize));
                }

                for (int i = 0; i < edges.length; i++) {
                    for (int j = 0; j < currentSubdivisionPointsCount + 1; j++) {
                        Coordinate force = forces.get(i).get(j);
                        edges[i].getSubdivisionPoints().get(j).alterPositionBy(force.getX(), force.getY());
                    }
                }
            }

            currentStepSize /= 2;
            currIterationsCount *= Configuration.DEFAULT_ITERATIONS_INCREASE_RATE;
            currentSubdivisionPointsCount *= Configuration.DEFAULT_SUBDIVISION_POINTS_RATE;

            updateEdgeSubdivisions(currentSubdivisionPointsCount);
        }

        // notify GUI that algorithm has finished, to draw the result
        notifyObservers(0, 0, true);

    }

    /**
     * Calculates spring force applied on given subdivision point of given edge
     * F_s = k_p * ||p_1 - p_2||  + k_p * ||p_2 - p_3|| where p_1,2,3 are adjacent subdivision points
     *
     * @param edgeID
     * @param i
     * @param kP
     * @return
     */
    private Coordinate calculateSpringForce(int edgeID, int i, double kP){
        Node prev = edges[edgeID].getSubdivisionPoints().get(i-1);
        Node curr = edges[edgeID].getSubdivisionPoints().get(i);
        Node next = edges[edgeID].getSubdivisionPoints().get(i+1);

        return new Coordinate((prev.getPosition().getX() + next.getPosition().getX() - 2*curr.getPosition().getX()) * kP,
                (prev.getPosition().getY() + next.getPosition().getY() - 2*curr.getPosition().getY()) * kP);
    }


    /**
     * Calculates electrostatic force applied on given edge
     * F_e = 1 / ||p - q|| where p and q are corresponding subdivision points
     *
     * @param currentEdgeID
     * @param i
     * @return
     */
    private Coordinate calculateElectrostaticForce(int currentEdgeID, int i){
        double x = 0;
        double y = 0;
        // constant to ignore forces if they are too small
        final double EPS = 0.0001;

        List<Edge> compatibleEdges = edges[currentEdgeID].getCompatibleEdges();

        for(Edge compatibleEdge : compatibleEdges) {

            // list of subdivision points for current edge and its compatible edge
            List<Node> currentEdgeSubdivisionPoints = edges[currentEdgeID].getSubdivisionPoints();
            List<Node> compatibleEdgeSubdivisionPoints = compatibleEdge.getSubdivisionPoints();

            double forceX = compatibleEdgeSubdivisionPoints.get(i).getPosition().getX() -
                    currentEdgeSubdivisionPoints.get(i).getPosition().getX();
            double forceY = compatibleEdgeSubdivisionPoints.get(i).getPosition().getY() -
                    currentEdgeSubdivisionPoints.get(i).getPosition().getY();

            // ignore force between current edge and its compatible edge of its too small
            if(Math.abs(forceX) < EPS || Math.abs(forceY) < EPS)
                continue;

            Coordinate src = compatibleEdgeSubdivisionPoints.get(i).getPosition();
            double divisor = src.euclideanDistance(currentEdgeSubdivisionPoints.get(i).getPosition());

            x += forceX / divisor;
            y += forceY / divisor;

        }

        return new Coordinate(x, y);
    }


    /**
     * Calculates total resulting force on given edge
     *
     * @param currentEdgeID
     * @param subdivisionPointsCount
     * @param stepSize
     * @return
     */
    private List<Coordinate> calculateTotalForce(int currentEdgeID, int subdivisionPointsCount, double stepSize){

        List<Coordinate> forces = new ArrayList<>();
        // zero force of start-point of edge
        forces.add(new Coordinate(0,0));

        double kP = K / (edges[currentEdgeID].getLength() * (subdivisionPointsCount + 1));

        for (int currentSubdivisionPoint = 1; currentSubdivisionPoint < (subdivisionPointsCount + 1); currentSubdivisionPoint++) {

            Coordinate springForce = calculateSpringForce(currentEdgeID, currentSubdivisionPoint, kP);
            Coordinate electroStaticForce = calculateElectrostaticForce(currentEdgeID, currentSubdivisionPoint);

            double totalForceX = stepSize * (springForce.getX() + electroStaticForce.getX());
            double totalForceY = stepSize * (springForce.getY() + electroStaticForce.getY());

            forces.add(new Coordinate(totalForceX, totalForceY));

        }

        // zero force of end-point of edge
        forces.add(new Coordinate(0,0));
        return forces;
    }


    /**
     * Calculates new list of subdivision points for each edge, based on its current list of subdivision points
     *
     * @param newSubdivisionPointsCount
     */
    private void updateEdgeSubdivisions(int newSubdivisionPointsCount){

        for (Edge edge : edges) {

            List<Node> subdivisionPoints = edge.getSubdivisionPoints();

            // if edge has not yet been divided, add only its midpoint
            if (newSubdivisionPointsCount == 1) {
                subdivisionPoints.add(edge.getFrom());
                Coordinate midpoint = edge.getMidpoint();
                subdivisionPoints.add(new Node(midpoint.getX(), midpoint.getY()));
                subdivisionPoints.add(edge.getTo());
            } else {


                List<Node> newEdgeSubdivisions = new ArrayList<>();
                newEdgeSubdivisions.add(edge.getFrom());

                // get length of segment in current iteration
                final double segmentLength = edge.getCurvedLength() / (newSubdivisionPointsCount + 1);
                double currSegmentLength = segmentLength;

                for (int j = 1; j < subdivisionPoints.size(); j++) {

                    // get length of segment in previous iteration
                    double oldSegmentLength = subdivisionPoints.get(j).getPosition().euclideanDistance(subdivisionPoints.get(j - 1).getPosition());

                    while (oldSegmentLength > currSegmentLength) {

                        // calculate fraction of segment lengths between current and previous iteration
                        double percentage = currSegmentLength / oldSegmentLength;
                        double x = subdivisionPoints.get(j - 1).getPosition().getX();
                        double y = subdivisionPoints.get(j - 1).getPosition().getY();

                        x += percentage * (subdivisionPoints.get(j).getPosition().getX() - x);
                        y += percentage * (subdivisionPoints.get(j).getPosition().getY() - y);

                        newEdgeSubdivisions.add(new Node(x, y));

                        oldSegmentLength -= currSegmentLength;
                        currSegmentLength = segmentLength;
                    }

                    currSegmentLength -= oldSegmentLength;
                }

                newEdgeSubdivisions.add(edge.getTo());

                edge.setSubdivisionPoints(newEdgeSubdivisions);

            }
        }
    }

    /**
     * Determines the list of compatible edges for each edge
     */
    private void calculateCompatibilities(){
        for (int i = 0; i < edges.length - 1; i++) {
            for (int j = i + 1; j < edges.length; j++) {
                if(edges[i].compatible(edges[j], COMPATIBILITY)){
                    edges[i].addCompatibleEdge(edges[j]);
                    edges[j].addCompatibleEdge(edges[i]);
                }
            }
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void notifyObservers(int iteration, int cycle, boolean finished) {
        for(Observer observer : observers){
            if(finished)
                observer.finished(nodes, edges);
            else
                observer.updateProcessInfo(iteration, cycle);
        }
    }
}
