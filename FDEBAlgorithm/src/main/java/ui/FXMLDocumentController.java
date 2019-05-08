package ui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import model.Edge;
import model.Node;
import core.IOParser;
import core.Observer;
import core.ForceDirectedEdgeBundling;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable, Observer {

    // Ovechkin constant for positioning graph
    private int OK2 = 50;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button visualiseButton;

    @FXML
    private Canvas canvasID;
    @FXML
    private TextField compability;
    @FXML
    private TextField step_size;
    @FXML
    private TextField edge_stiffness;
    @FXML
    private TextField cycles_count;
    @FXML
    private TextField iterations_count;


    //TODO mozna scrolling?
    //https://stackoverflow.com/questions/31593859/zoom-levels-with-the-javafx-8-canvas

    //TODO Long,lat na x a y
    //TODO ať se vejde graf na každém monitoru
    //TODO Zoom na gui


    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {

        double inputCompatibility = Double.parseDouble(compability.getText());
        double inputStepSize = Double.parseDouble(step_size.getText());
        double inputEdgeStiffness = Double.parseDouble(edge_stiffness.getText());
        int inputIterationsCount = Integer.parseInt(iterations_count.getText());
        int inputCyclesCount = Integer.parseInt(cycles_count.getText());

        // set default value if input value is out of range
        inputCompatibility = (inputCompatibility < 0 || inputCompatibility > 1) ? 0.6 : inputCompatibility;
        inputStepSize =  (inputStepSize < 0 || inputStepSize > 3) ? 0.1 : inputStepSize;
        inputEdgeStiffness =  (inputEdgeStiffness < 0 || inputEdgeStiffness > 1) ? 0.9 : inputEdgeStiffness;
        inputIterationsCount =  (inputIterationsCount < 0 || inputIterationsCount > 400) ? 90 : inputIterationsCount;
        inputCyclesCount =  (inputCyclesCount < 0 || inputCyclesCount > 20) ? 6 : inputCyclesCount;


        IOParser IOParser = new IOParser("src/main/resources/airlines.graphml");

        Node[] airports = IOParser.getAirports();
        Edge[] flights = IOParser.getFlights();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(airports, flights, inputStepSize, inputCompatibility, inputEdgeStiffness,inputIterationsCount, inputCyclesCount);
        fdeb.registerObserver(this);
        new Thread(fdeb::run).start();

    }

    private void drawNodesAndEdges(Node[]nodes, Edge[]edges){
        GraphicsContext gc = canvasID.getGraphicsContext2D();

        gc.clearRect(0, 0, canvasID.getWidth(), canvasID.getHeight());

//        biggest x = 1000
//        smallest x = 50
//        biggest y = 560
//        smallest y = 50

        for (Node airport : nodes) {
            double x = airport.getPosition().getX();
            double y = airport.getPosition().getY();

            gc.setFill(Color.BLUE);

            // Ovechkin constant for positioning graph
            int OK = 3;
            gc.fillOval(x-OK+OK2, y-OK, 7, 7);
        }


        for (Edge flight : edges) {
            ArrayList<Double> points = new ArrayList<>();

            for (Node n : flight.getSubdivisionPoints()) {
                points.add(n.getPosition().getX() + OK2);
                points.add(n.getPosition().getY());
            }

            drawEdge(gc, createPath(points));
        }
    }

    private void drawEdge(GraphicsContext gc, Path pathList) {

        gc.setStroke(Color.rgb(0, 0, 230, 0.1));
        gc.setFill(Color.rgb(0, 0, 230, 0.1));
        gc.setLineWidth(0.7);

        ObservableList<PathElement> l = pathList.getElements();

        gc.beginPath();
        for (PathElement pe : l) {
            if (pe.getClass() == MoveTo.class) {
                gc.moveTo(((MoveTo) pe).getX(), ((MoveTo) pe).getY());
            } else if (pe.getClass() == LineTo.class) {
                gc.lineTo(((LineTo) pe).getX(), ((LineTo) pe).getY());
            }
        }
        gc.stroke();
        gc.closePath();
    }

    private Path createPath(ArrayList<Double> points) {
        Path path = new Path();
        for (int i = 0; i < points.size() - 4; i += 4) {
            double startX = points.get(i);
            double startY = points.get(i + 1);
            double endX = points.get(i + 2);
            double endY = points.get(i + 3);

            if (i == 0) {
                MoveTo moveTo = new MoveTo(startX, startY);
                moveTo.setAbsolute(true);
                path.getElements().add(moveTo);
            } else {

                double lastStartX = points.get(i - 4);
                double lastStartY = points.get(i - 3);
                double lastEndX = points.get(i - 2);
                double lastEndY = points.get(i - 1);

                double lastLength = Math.sqrt((lastEndX - lastStartX) * (lastEndX - lastStartX)
                        + (lastEndY - lastStartY) * (lastEndY - lastStartY));
                double length = Math.sqrt((endX - startX) * (endX - startX)
                        + (endY - startY) * (endY - startY));
                double aveLength = (lastLength + length) / 2;

                double control1X = lastEndX + (lastEndX - lastStartX) * aveLength / lastLength;
                double control1Y = lastEndY + (lastEndY - lastStartY) * aveLength / lastLength;

                double control2X = startX - (endX - startX) * aveLength / length;
                double control2Y = startY - (endY - startY) * aveLength / length;

                CubicCurveTo cct = new CubicCurveTo(control1X, control1Y, control2X, control2Y, startX, startY);
                cct.setAbsolute(true);
                path.getElements().add(cct);

            }
            LineTo lineTo = new LineTo(endX, endY);
            lineTo.setAbsolute(true);
            path.getElements().add(lineTo);

        }

        return path;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        readTextField(compability);
        readTextField(step_size);
        readTextField(iterations_count);
        readTextField(cycles_count);
        readTextField(edge_stiffness);
    }


    private void readTextField(TextField field){
        DecimalFormat format = new DecimalFormat( "#.0" );

        field.setTextFormatter( new TextFormatter<>(c -> {
            if(c.getControlNewText().isEmpty()){
                return c;
            }

            ParsePosition parsePosition = new ParsePosition( 0 );
            Object object = format.parse(c.getControlNewText(), parsePosition);

            if(object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                return null;
            }
            else {
                return c;
            }
        }));
    }

    @Override
    public void updateProcessInfo(int iteration, int cycle) {
        Platform.runLater(() -> visualiseButton.setText(String.format("Processing...\nCycle: %d\nIteration: %d", cycle, iteration)));
    }

    @Override
    public void finished(Node[]nodes, Edge[]edges) {
        Platform.runLater(() -> {
            visualiseButton.setText("Visualise");
            visualiseButton.setDisable(false);
            drawNodesAndEdges(nodes, edges);
        });

    }
}