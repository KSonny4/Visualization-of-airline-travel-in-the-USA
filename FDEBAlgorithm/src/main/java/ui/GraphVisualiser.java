package ui;

import core.Configuration;
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
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import javafx.scene.transform.Scale;
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
import java.util.List;
import java.util.ResourceBundle;

import static org.apache.commons.lang.math.NumberUtils.isNumber;

public class GraphVisualiser implements Initializable, Observer {

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

    // Ovechkin constant for positioning graph
    private final int OK2 = 50;

    private final ButtonType continueAnyway = new ButtonType("Continue anyway", ButtonBar.ButtonData.OK_DONE);

    private List<TextField> textFields;

    // current value of canvas scale X, used to prevent zooming too far
    private double canvasScaleX;
    // current value of canvas scale Y, used to prevent zooming too far
    private double canvasScaleY;

    //TODO Long,lat na x a y


    private void handleMouseScrolling(Canvas node) {
        final double SCALE_DELTA = 1.1;

        node.setOnScroll((ScrollEvent event) -> {
            event.consume();
            if(event.getDeltaY() == 0)
                return;

            // prevent zooming too far away
            if((canvasScaleX < 0.5 || canvasScaleY < 0.5) && event.getDeltaY() < 0){
                return;
            }

            double scaleFactor = event.getDeltaY() > 0 ? SCALE_DELTA : 1/SCALE_DELTA;

            Scale scale = new Scale();
            scale.setPivotX(event.getX());
            scale.setPivotY(event.getY());
            scale.setX(canvas.getScaleX() * scaleFactor);
            scale.setY(canvas.getScaleY() * scaleFactor);
            canvasScaleX = canvasScaleX * scaleFactor;
            canvasScaleY = canvasScaleX * scaleFactor;

            node.getTransforms().add(scale);
            event.consume();

        });
    }

    private boolean handleIntegerInput(String textFieldValue, int rangeFrom, int rangeTo){
        if(textFieldValue.isEmpty()) return false;

        if(!textFieldValue.matches("-?\\d+")) return false;

        int value = Integer.parseInt(textFieldValue);

        if(value >= rangeFrom && value <= rangeTo)
            return true;
        else
            return false;
    }

    private boolean handleDoubleInput(String textFieldValue, double rangeFrom, double rangeTo){
        if(textFieldValue.isEmpty()) return false;

        if(!isNumber(textFieldValue)) return false;

        double value = Double.parseDouble(textFieldValue);

        if(value >= rangeFrom && value <= rangeTo)
            return true;
        else
            return false;
    }

    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {

        double inputCompatibility = handleDoubleInput(compatibilityTextField.getText(), 0.0, 1.0) ?
                Double.parseDouble(compatibilityTextField.getText()) :
                Configuration.DEFAULT_COMPATIBILITY_THRESHOLD;
        double inputStepSize = handleDoubleInput(stepSizeTextField.getText(), 0.0, 3.0) ?
                Double.parseDouble(stepSizeTextField.getText()) :
                Configuration.DEFAULT_STEP_SIZE;
        double inputEdgeStiffness = handleDoubleInput(edgeStiffnessTextField.getText(), 0.0, 1.0) ?
                Double.parseDouble(edgeStiffnessTextField.getText()) :
                Configuration.DEFAULT_EDGE_STIFFNESS;
        int inputIterationsCount = handleIntegerInput(iterationsCountTextField.getText(), 0, 400) ?
                Integer.parseInt(iterationsCountTextField.getText()) :
                Configuration.DEFAULT_ITERATIONS_COUNT;
        int inputCyclesCount = handleIntegerInput(cyclesCountTextField.getText(), 0, 20) ?
                Integer.parseInt(cyclesCountTextField.getText()) :
                Configuration.DEFAULT_CYCLES_COUNT;

        // handle input values and raise alerts if selected values might cause long computation times
        handleInputValues(inputCompatibility, inputIterationsCount, inputCyclesCount);

        compatibilityTextField.setText(String.valueOf(inputCompatibility));
        stepSizeTextField.setText(String.valueOf(inputStepSize));
        edgeStiffnessTextField.setText(String.valueOf(inputEdgeStiffness));
        iterationsCountTextField.setText(String.valueOf(inputIterationsCount));
        cyclesCountTextField.setText(String.valueOf(inputCyclesCount));

        IOParser IOParser = new IOParser("src/main/resources/airlines.graphml");
//        IOParser IOParser = new IOParser("src/main/resources/migrations.xml");

        Node[] nodes = IOParser.getNodes();
        Edge[] edges = IOParser.getEdges();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(nodes, edges, inputStepSize, inputCompatibility, inputEdgeStiffness,inputIterationsCount, inputCyclesCount);
        fdeb.registerObserver(this);

        new Thread(fdeb::run).start();
        visualiseButton.setDisable(true);

        for(TextField field : textFields){
            field.setDisable(true);
        }
    }

    private void handleInputValues(double inputCompatibility, int inputIterationsCount, int inputCyclesCount) {
        List<Alert> alerts = new ArrayList<>();
        if(inputCompatibility < 0.3){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    String.format("Compatibility threshold value (%.2f) is set too low, computation might take some time...", inputCompatibility), continueAnyway
                    , ButtonType.CANCEL);
            alert.setResizable(true);
            alerts.add(alert);
        }
        if(inputCyclesCount > 10){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    String.format("Number of cycles (%d) is high, computation might take some time...", inputCyclesCount), continueAnyway
                    , ButtonType.CANCEL);
            alert.setResizable(true);
            alerts.add(alert);
        }
        if(inputIterationsCount > 250){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    String.format("Number of iterations (%d) is high, computation might take some time...", inputIterationsCount), continueAnyway
                    , ButtonType.CANCEL);
            alert.setResizable(true);
            alerts.add(alert);
        }

        for(Alert alert : alerts){
            if(alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.CANCEL)
                return;
        }
    }

    private void drawNodesAndEdges(Node[]nodes, Edge[]edges){
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Node node : nodes) {
            double x = node.getPosition().getX();
            double y = node.getPosition().getY();

            gc.setFill(Color.BLUE);

            // Ovechkin constant for positioning graph
            int OK = 3;
            gc.fillOval(x-OK+OK2, y-OK, 7, 7);
        }


        for (Edge edge : edges) {
            ArrayList<Double> points = new ArrayList<>();

            for (Node n : edge.getSubdivisionPoints()) {
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
        textFields = new ArrayList<>();
        textFields.add(compatibilityTextField);
        textFields.add(stepSizeTextField);
        textFields.add(iterationsCountTextField);
        textFields.add(cyclesCountTextField);
        textFields.add(edgeStiffnessTextField);
        readTextField(compatibilityTextField);
        readTextField(stepSizeTextField);
        readTextField(iterationsCountTextField);
        readTextField(cyclesCountTextField);
        readTextField(edgeStiffnessTextField);
        handleMouseScrolling(canvas);
        canvasScaleX = 1.0;
        canvasScaleY = 1.0;
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
        Platform.runLater(() -> {
            visualiseButton.setText(String.format("Processing...\nCycle: %d\nIteration: %d", cycle, iteration));
        });
    }

    @Override
    public void finished(Node[]nodes, Edge[]edges) {
        Platform.runLater(() -> {
            visualiseButton.setText("Visualise");
            visualiseButton.setDisable(false);
            for(TextField field : textFields){
                field.setDisable(false);
            }
            drawNodesAndEdges(nodes, edges);
        });

    }
}