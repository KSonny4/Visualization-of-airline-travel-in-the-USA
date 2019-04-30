import java.util.List;

public class ForceDirectedEdgeBundling {

    private final double stepSize;
    private final double compatibility;
    private final double bundlingStiffness = 0.1;
    private final int  iterations = 90;
    private final double iterations_rate = 0.666;
    private final int cycles = 6;
    private final int subdivision_points_seed = 1;
    private final int subdivision_rate = 2;

    private List<List<Flight>> flights;
    private Airport[] airports;

    public ForceDirectedEdgeBundling(Airport[] airports, List<List<Flight>> flights, double stepSize, double compatibility) {
        this.stepSize = stepSize;
        this.compatibility = compatibility;
        this.flights = flights;
        this.airports = airports;
    }

    public ForceDirectedEdgeBundling(Airport[] airports, List<List<Flight>> flights){
        this(airports, flights, 0.1, 0.7);
    }



}
