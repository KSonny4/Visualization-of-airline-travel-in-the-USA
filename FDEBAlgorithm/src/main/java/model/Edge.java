package model;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    private Node from;
    private Node to;
    private final int ID;
    private List<Node> subdivisionPoints;
    private List<Edge> compatibleEdges;

    /**
     * Creates new Edge instance with endpoints in {@code from} and {@code to}, with given ID.
     *
     * @param from
     * @param to
     * @param ID
     */
    public Edge(Node from, Node to, final int ID) {
        this.from = from;
        this.to = to;
        this.ID = ID;
        this.subdivisionPoints = new ArrayList<>();
        this.compatibleEdges = new ArrayList<>();
    }

    public List<Node> getSubdivisionPoints() {
        return subdivisionPoints;
    }

    public List<Edge> getCompatibleEdges() {
        return compatibleEdges;
    }

    public void setSubdivisionPoints(List<Node> subdivisionPoints) {
        this.subdivisionPoints = subdivisionPoints;
    }

    public void addCompatibleEdge(Edge edge) {
        this.compatibleEdges.add(edge);
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public Coordinate vector(){
        return new Coordinate(to.getPosition().getX() - from.getPosition().getX(), to.getPosition().getY() - from.getPosition().getY());
    }

    /**
     * Returns the length of this edge as euclidean distance between start-point and end-point
     * @return
     */
    public double getLength(){
        return from.getPosition().euclideanDistance(to.getPosition());
    }


    /**
     * Returns the middle point of this edge
     * @return
     */
    public Coordinate getMidpoint(){
        return new Coordinate((from.getPosition().getX() + to.getPosition().getX()) / 2.0,
                (from.getPosition().getY() + to.getPosition().getY()) / 2.0);
    }

    /**
     * Returns the length of this edge which is already curved based on its subdivision points
     * @return
     */
    public double getCurvedLength(){
        double length = 0;

        for (int i = 1; i < subdivisionPoints.size(); i++) {
            length += subdivisionPoints.get(i).getPosition().euclideanDistance(subdivisionPoints.get(i-1).getPosition());
        }
        return length;
    }

    /**
     * Returns angle compatibility value of this edge and {@param other} edge
     * C_a(P,Q) = |cos(arccos(dot(P,Q) / length(P)*length(Q))|
     *
     * @param other
     * @return
     */
    private double angleCompatibility(Edge other){
        return Math.abs(this.vector().dotProduct(other.vector()) / (this.getLength() * other.getLength()));
    }

    /**
     * Returns scale compatibility value of this edge and {@param other} edge
     * C_s(P,Q) = 2 / (l_avg / min(length(P), length(Q) + max(length(P), length(Q)) / l_avg)
     *
     * @param other
     * @return
     */
    private double scaleCompatibility(Edge other){
        double l_avg = (this.getLength() + other.getLength()) / 2;

        return 2 / (l_avg / Math.min(this.getLength(), other.getLength()) + Math.max(this.getLength(), other.getLength()) / l_avg);
    }

    /**
     * Returns position compatibility value of this edge and {@param other} edge
     * C_p(P,Q) = l_avg / (l_avg + ||P_m = Q_m||), where P_m and Q_m are midpoints of edges P and Q
     *      * and l_avg is average length of edges P and Q
     *
     * @param other
     * @return
     */
    private double positionCompatibility(Edge other){
        double l_avg = (this.getLength() + other.getLength()) / 2;

        return l_avg / (l_avg + this.getMidpoint().euclideanDistance(other.getMidpoint()));
    }


    /**
     * Return the visibility value of this edge and {@param other} edge
     * visibility(P,Q) = max(1 - (2 * || P_m - I_m||) / ||Io_ - I1||, 0)
     *
     * @param other
     * @return
     */
    private double edgeVisibility(Edge other){
        Node i0 = other.getFrom().projectPointOnLine(this);
        Node i1 = other.getTo().projectPointOnLine(this);

        Coordinate P_m = this.getMidpoint();
        Coordinate I_m = new Edge(i0, i1, -1).getMidpoint();

        return Math.max(1 - 2 * I_m.euclideanDistance(P_m) / i0.getPosition().euclideanDistance(i1.getPosition()),0);
    }

    /**
     * Returns visibility compatibility value of this edge and {@param other} edge
     * C_v(P,Q) = min(visibility(P,Q), visibility(Q,P))
     *
     * @param other
     * @return
     */
    private double visibilityCompatibility(Edge other){
        return Math.min(this.edgeVisibility(other), other.edgeVisibility(this));
    }

    /**
     * Returns total compatibility score of this edge and {@param other} edge based on all four compatibility measures
     * @param other
     * @return
     */
    private double edgeCompatibilityScore(Edge other){
        return this.angleCompatibility(other) * this.scaleCompatibility(other) *  this.positionCompatibility(other)*  this.visibilityCompatibility(other);
    }

    /**
     * Returns true if this edge and {@param other} edge are compatible within given threshold
     * @param other
     * @param threshold
     * @return
     */
    public boolean compatible(Edge other, double threshold){
        return edgeCompatibilityScore(other) >= threshold;
    }


    @Override
    public String toString() {
        return "Edge with id " + ID +
                " from: " + from +
                ", to: " + to;
    }
}
