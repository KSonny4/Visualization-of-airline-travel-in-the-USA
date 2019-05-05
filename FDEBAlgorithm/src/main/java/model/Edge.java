package model;

import java.util.List;

public class Edge {

    private Node from;
    private Node to;
    private final int ID;

    public Edge(Node from, Node to, final int ID) {
        this.from = from;
        this.to = to;
        this.ID = ID;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public int getID() {
        return ID;
    }

    public Coordinate asVector(){
        return new Coordinate(to.getPosition().getX() - from.getPosition().getX(), to.getPosition().getY() - from.getPosition().getY());
    }

    public double getLength(final double EPS){
        if(Math.abs(from.getPosition().getX() - to.getPosition().getX()) <= EPS ||
                Math.abs(from.getPosition().getY() - to.getPosition().getY()) <= EPS){
            return EPS;
        }

        return from.getPosition().euclideanDistance(to.getPosition());
    }

    public double getLength(){
        return getLength(0.000001);
    }

    public Coordinate getMidpoint(){
        return new Coordinate((from.getPosition().getX() + to.getPosition().getX()) / 2.0,
                (from.getPosition().getY() + to.getPosition().getY()) / 2.0);
    }

    public double getDividedEdgeLength(List<Node> subdivisionPoints){
        double length = 0;

        for (int i = 1; i < subdivisionPoints.size(); i++) {
            length += subdivisionPoints.get(i).getPosition().euclideanDistance(subdivisionPoints.get(i-1).getPosition());
        }
        return length;
    }

    public double angleCompatibility(Edge other){

        return Math.abs(this.asVector().dotProduct(other.asVector()) / (this.getLength() * other.getLength()));
    }

    public double scaleCompatibility(Edge other){
        double lavg = (this.getLength() + other.getLength()) / 2;

        return 2 / (lavg / Math.min(this.getLength(), other.getLength()) + Math.max(this.getLength(), other.getLength()) / lavg);
    }

    public double positionCompatibility(Edge other){
        double lavg = (this.getLength() + other.getLength()) / 2;

        return lavg / (lavg + this.getMidpoint().euclideanDistance(other.getMidpoint()));
    }

    public double edgeVisibility(Edge other){
        Node i0 = other.getFrom().projectPointOnLine(this);
        Node i1 = other.getTo().projectPointOnLine(this);

        Coordinate midI = new Edge(i0, i1, -1).getMidpoint();
        Coordinate midP = this.getMidpoint();

        return Math.max(0, 1 - 2 * midI.euclideanDistance(midP) / i0.getPosition().euclideanDistance(i1.getPosition()));
    }

    public double visibilityCompatibilitu(Edge other){
        return Math.min(this.edgeVisibility(other), other.edgeVisibility(this));
    }

    public double compatibilityScore(Edge other){

        return this.angleCompatibility(other) * this.scaleCompatibility(other) *  this.positionCompatibility(other)*  this.visibilityCompatibilitu(other);
    }

    public boolean compatible(Edge other, double threshold){
        return compatibilityScore(other) >= threshold;
    }


    @Override
    public String toString() {
        return "Edge with id " + ID +
                " from: " + from +
                ", to: " + to;
    }
}
