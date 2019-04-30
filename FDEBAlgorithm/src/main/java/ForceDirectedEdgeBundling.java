public class ForceDirectedEdgeBundling {

    private final double stepSize;
    private final double compatibility;
    private final double bundlingStiffness = 0.1;
    private final int  iterations = 90;
    private final double iterations_rate = 0.666;
    private final int cycles = 6;
    private final int subdivision_points_seed = 1;
    private final int subdivision_rate = 2;

    private Flight[][] flights;
    private Node[] nodes;

    public ForceDirectedEdgeBundling(Node[] nodes, Flight[][] flights){
        this.stepSize = 0.1;
        this.compatibility = 0.6;
        this.flights = flights;
        this.nodes = nodes;
    }

    public ForceDirectedEdgeBundling(Node[] nodes, Flight[][] flights, double stepSize, double compatibility) {
        this.stepSize = stepSize;
        this.compatibility = compatibility;
        this.flights = flights;
        this.nodes = nodes;
    }


}
