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

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Coordinate dotProduct(Coordinate other){
        return new Coordinate(this.x * other.x, this.y * other.y);
    }

    @Override
    public String toString() {
        return "[" + Math.round(x * 100.0) / 100.0 + "," + Math.round(y * 100.0) / 100.0 + "]";

    }
}
