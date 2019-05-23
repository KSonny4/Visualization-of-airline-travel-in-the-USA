package core;

public final class Configuration {

    public static final double DEFAULT_STEP_SIZE = 0.1;
    public static final double DEFAULT_EDGE_STIFFNESS = 0.9;
    public static final double DEFAULT_COMPATIBILITY_THRESHOLD = 0.6;
    public static final int DEFAULT_ITERATIONS_COUNT = 90;
    public static final int DEFAULT_CYCLES_COUNT = 6;
    public static final int DEFAULT_SUBDIVISION_POINTS_COUNT = 1;
    public static final double DEFAULT_ITERATIONS_INCREASE_RATE = 0.666;
    public static final int DEFAULT_SUBDIVISION_POINTS_RATE = 2;

    private Configuration() throws IllegalAccessException {
        throw new IllegalAccessException("Trying to initialize configuration class...");
    }

}
