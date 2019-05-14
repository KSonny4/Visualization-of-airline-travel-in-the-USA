package model;

public class Node {

    private Coordinate position;
    private final int ID;
    private final String name;

    /**
     * Creates new Node instance with given x,y coordinated and specified ID and name.
     *
     * @param x
     * @param y
     * @param ID
     * @param name
     */
    public Node(double x, double y, int ID, String name){
        this.position = new Coordinate(x, y);
        this.ID = ID;
        this.name = name;
    }

    /**
     * Creates new Node instance with given x,y coordinates.
     * ID and name will be set to default values, -1 and 'custom' respectively.
     *
     * @param x
     * @param y
     */
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

    public void setPosition(double x, double y) {
        this.position = new Coordinate(x,y);
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
                (other.getFrom().getPosition().getY() - other.getTo().getPosition().getY()) -
                (other.getFrom().getPosition().getX() - this.position.getX()) *
                        (other.getTo().getPosition().getX() - other.getFrom().getPosition().getX())) / (L*L);

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
