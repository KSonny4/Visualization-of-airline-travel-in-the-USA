public class Airport {

    private final Coordinate position;
    private final int ID;
    private final String name;

    public Airport(Coordinate position, int ID, String name) {
        this.position = position;
        this.ID = ID;
        this.name = name;
    }

    public Airport(double x, double y, int ID, String name){
        this.position = new Coordinate(x, y);
        this.ID = ID;
        this.name = name;
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


    @Override
    public String toString() {
        return "Airport{" +
                "position=" + position +
                ", ID=" + ID +
                ", name='" + name + '\'' +
                '}';
    }
}
