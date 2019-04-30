public class Flight {

    private Airport from;
    private Airport to;
    private final int ID;

    public Flight(Airport from, Airport to, final int ID) {
        this.from = from;
        this.to = to;
        this.ID = ID;
    }

    public Airport getFrom() {
        return from;
    }

    public void setFrom(Airport from) {
        this.from = from;
    }

    public Airport getTo() {
        return to;
    }

    public void setTo(Airport to) {
        this.to = to;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "Flight with id " + ID +
                " from: " + from +
                ", to: " + to;
    }
}
