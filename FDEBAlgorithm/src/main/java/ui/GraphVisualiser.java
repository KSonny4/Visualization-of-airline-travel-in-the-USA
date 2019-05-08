package ui;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
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

public class GraphVisualiser implements Initializable, Observer {

    // Ovechkin constant for positioning graph
    private int OK2 = 50;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button visualiseButton;

    @FXML
    private Canvas canvas;
    @FXML
    private TextField compatibilityTextField;
    @FXML
    private TextField stepSizeTextField;
    @FXML
    private TextField edgeStiffnessTextField;
    @FXML
    private TextField cyclesCountTextField;
    @FXML
    private TextField iterationsCountTextField;


    //TODO mozna scrolling?
    //https://stackoverflow.com/questions/31593859/zoom-levels-with-the-javafx-8-canvas

    //TODO Long,lat na x a y
    //TODO ať se vejde graf na každém monitoru
    //TODO Zoom na gui


    DoubleProperty myScale = new SimpleDoubleProperty(1.0);
    @FXML
    private void onScroll(ScrollEvent event) {

        //System.out.println("on zoom BordePane");
    }


    //https://stackoverflow.com/questions/29506156/javafx-8-zooming-relative-to-mouse-pointer
    public void addMouseScrolling(Canvas node) {



        node.setOnScroll((ScrollEvent event) -> {
            // Adjust the zoom factor as per your requirement
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            System.out.println("deltaY " + deltaY);

            if (deltaY < 0){
                zoomFactor = 4.0 - zoomFactor;
            }
            System.out.println(zoomFactor);
            node.setScaleX(node.getScaleX() * zoomFactor);
            node.setScaleY(node.getScaleY() * zoomFactor);
        });
    }




    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {

        double inputCompatibility = Double.parseDouble(compatibilityTextField.getText());
        double inputStepSize = Double.parseDouble(stepSizeTextField.getText());
        double inputEdgeStiffness = Double.parseDouble(edgeStiffnessTextField.getText());
        int inputIterationsCount = Integer.parseInt(iterationsCountTextField.getText());
        int inputCyclesCount = Integer.parseInt(cyclesCountTextField.getText());

        // set default value if input value is out of range
        inputCompatibility = (inputCompatibility < 0 || inputCompatibility > 1) ? 0.6 : inputCompatibility;
        inputStepSize =  (inputStepSize < 0 || inputStepSize > 3) ? 0.1 : inputStepSize;
        inputEdgeStiffness =  (inputEdgeStiffness < 0 || inputEdgeStiffness > 1) ? 0.9 : inputEdgeStiffness;
        inputIterationsCount =  (inputIterationsCount < 0 || inputIterationsCount > 400) ? 90 : inputIterationsCount;
        inputCyclesCount =  (inputCyclesCount < 0 || inputCyclesCount > 20) ? 6 : inputCyclesCount;

        compatibilityTextField.setText(String.valueOf(inputCompatibility));
        stepSizeTextField.setText(String.valueOf(inputStepSize));
        edgeStiffnessTextField.setText(String.valueOf(inputEdgeStiffness));
        iterationsCountTextField.setText(String.valueOf(inputIterationsCount));
        cyclesCountTextField.setText(String.valueOf(inputCyclesCount));

        IOParser IOParser = new IOParser("src/main/resources/airlines.graphml");

        Node[] airports = IOParser.getAirports();
        Edge[] flights = IOParser.getFlights();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(airports, flights, inputStepSize, inputCompatibility, inputEdgeStiffness,inputIterationsCount, inputCyclesCount);
        fdeb.registerObserver(this);
        new Thread(fdeb::run).start();

    }



    private void drawNodesAndEdges(Node[]nodes, Edge[]edges){
        GraphicsContext gc = canvas.getGraphicsContext2D();


        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

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
        readTextField(compatibilityTextField);
        readTextField(stepSizeTextField);
        readTextField(iterationsCountTextField);
        readTextField(cyclesCountTextField);
        readTextField(edgeStiffnessTextField);
        addMouseScrolling(canvas);


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