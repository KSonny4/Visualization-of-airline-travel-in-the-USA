package model;

public class Coordinate {

    private double x;
    private double y;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void alterBy(double x, double y) {
        this.x += x;
        this.y += y;
    }

    public double getY() {
        return y;
    }

    /**
     * Returns value of dot product of this edge and {@param other} edge
     *
     * @param other the other edge
     * @return
     */
    public double dotProduct(Coordinate other){
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Returns value of euclidean distance between this edge and {@param other} edge
     *
     * @param other the other edge
     * @return
     */
    public double euclideanDistance(Coordinate other){
        return Math.sqrt(Math.pow(this.x - other.getX(), 2) + Math.pow(this.y- other.getY(), 2));
    }

    @Override
    public String toString() {
        return "[" + Math.round(x * 100.0) / 100.0 + "," + Math.round(y * 100.0) / 100.0 + "]";

    }
}
