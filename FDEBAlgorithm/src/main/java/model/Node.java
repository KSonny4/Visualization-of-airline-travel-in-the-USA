package model;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

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

    public static class FXMLDocumentController implements Initializable {

        @FXML
        private Canvas canvas;


        @FXML
        private Label label;

        @FXML
        private void visualiseButtonAction(ActionEvent event) {
            System.out.println("You clicked me!");
            label.setText("Hello World!");
        }


        @Override
        public void initialize(URL url, ResourceBundle rb) {
            // TODO
        }

    }
}
