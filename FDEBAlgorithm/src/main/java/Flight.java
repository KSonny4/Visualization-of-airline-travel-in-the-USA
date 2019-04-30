public class Flight {

    private Node from;
    private Node to;
    private final int ID;

    public Flight(Node from, Node to, final int ID) {
        this.from = from;
        this.to = to;
        this.ID = ID;
    }

    public Node getFrom() {
        return from;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public Node getTo() {
        return to;
    }

    public void setTo(Node to) {
        this.to = to;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "Flight with id " + ID +
                " from: " + from +
                ", to:" + to;
    }
}
