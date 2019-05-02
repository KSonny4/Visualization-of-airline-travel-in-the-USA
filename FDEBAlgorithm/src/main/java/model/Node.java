package model;

public class Node {

    private Coordinate position;
    private final int ID;
    private final String name;

    public Node(double x, double y, int ID, String name){
        this.position = new Coordinate(x, y);
        this.ID = ID;
        this.name = name;
    }

    public Node(double x, double y){
        this.position = new Coordinate(x,y);
        this.ID = -1;
        this.name = "custom";
    }

    public void alterPositionBy(double x, double y){
        this.position.alterBy(x, y);
    }

    public Coordinate getPosition() {
        return position;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Node projectPointOnLine(Edge other){
        double L = Math.sqrt((other.getTo().getPosition().getX() - other.getFrom().getPosition().getX()) *
                (other.getTo().getPosition().getX() - other.getFrom().getPosition().getX()) +
                (other.getTo().getPosition().getY() - other.getFrom().getPosition().getY()) *
                        (other.getTo().getPosition().getY() - other.getFrom().getPosition().getY()));
        double r = ((other.getFrom().getPosition().getY() - this.position.getY()) *
                (other.getFrom().getPosition().getY() - other.getTo().getPosition().getY()) +
                (other.getTo().getPosition().getX() - this.position.getX()) *
                        (other.getTo().getPosition().getX() - other.getFrom().getPosition().getX()));

        return new Node(other.getFrom().getPosition().getX() + r * (other.getTo().getPosition().getX() - other.getFrom().getPosition().getX()),
                other.getFrom().getPosition().getY() + r * (other.getTo().getPosition().getY() - other.getFrom().getPosition().getY()));
    }


    @Override
    public String toString() {
        return "Node{" +
                "position=" + position +
                ", ID=" + ID +
                ", name='" + name + '\'' +
                '}';
    }
}
