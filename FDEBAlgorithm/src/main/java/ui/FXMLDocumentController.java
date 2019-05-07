package ui;

import core.IOParser;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import main.RunGUI;
import model.Edge;
import model.Node;
import core.ForceDirectedEdgeBundling;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {
    //Ovechkin constant for positioning graph
    private int OK2 = 50;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button visualiseButton;
    @FXML
    private Label updateMeLabel;
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
    //TODO Visualise button zobrazit processing
    //TODO ať se vejde graf na každém monitoru
    //TODO Zmáčknout Button a zakázat ho než doběhne alg
    //TODO Zoom na gui


    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {


        Stage stage = RunGUI.getMainStage();

        String c = compability.getText();
        String s = step_size.getText();
        String e = edge_stiffness.getText();
        String i = iterations_count.getText();
        String cy = cycles_count.getText();

        double COMPABILITY = Double.parseDouble(c);
        double STEP_SIZE = Double.parseDouble(s);
        double EDGE_STIFFNESS = Double.parseDouble(e);
        int ITERATIONS_COUNT = Integer.parseInt(i);
        int CYCLES_COUNT = Integer.parseInt(cy);

        COMPABILITY = (COMPABILITY < 0 || COMPABILITY > 1) ? 0.6 : COMPABILITY;
        STEP_SIZE =  (STEP_SIZE < 0 || STEP_SIZE > 3) ? 0.1 : STEP_SIZE;
        EDGE_STIFFNESS =  (EDGE_STIFFNESS < 0 || EDGE_STIFFNESS > 1) ? 0.9 : EDGE_STIFFNESS;
        ITERATIONS_COUNT =  (ITERATIONS_COUNT < 0 || ITERATIONS_COUNT > 400) ? 90 : ITERATIONS_COUNT;
        CYCLES_COUNT =  (CYCLES_COUNT < 0 || CYCLES_COUNT > 20) ? 6 : CYCLES_COUNT;


        compability.setText(String.valueOf(COMPABILITY));
        step_size.setText(String.valueOf(STEP_SIZE));
        edge_stiffness.setText(String.valueOf(EDGE_STIFFNESS));
        iterations_count.setText(String.valueOf(ITERATIONS_COUNT));
        cycles_count.setText(String.valueOf(CYCLES_COUNT));


        //TODO: ted to nejak aktualizovat at se tohle vykresli hned omg
        //zkousel jsem to proste vsema metodama ale proste to nejde :DDDDDDDDDDDDD
        updateMeLabel.setText("Ahoj");
        visualiseButton.setText("asdsada");


        //this.refresh();
        //stage.refresh();
        //borderPane.refresh();





        IOParser IOParser = new IOParser("src/main/resources/airlines.graphml");

        Node[] airports = IOParser.getAirports();
        Edge[] flights = IOParser.getFlights();


        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(airports, flights, STEP_SIZE, COMPABILITY, EDGE_STIFFNESS,ITERATIONS_COUNT, CYCLES_COUNT);
        Thread t = new Thread(fdeb);
        t.start();
        fdeb.run();

        //https://stackoverflow.com/questions/52793431/problem-with-observer-pattern-and-bar-chart-using-javafx

        //https://stackoverflow.com/questions/43095744/observer-wont-run-update-in-javafx-gui

        //zkousim tohle zbrzdit, at se stihne setText
        //proste ne
//        ForceDirectedEdgeBundling f;
//        new Thread(() -> {
//            f = new ForceDirectedEdgeBundling(airports, flights, STEP_SIZE, COMPABILITY, EDGE_STIFFNESS,ITERATIONS_COUNT, CYCLES_COUNT);
//            f.run();
//        }).start();







        GraphicsContext gc = canvasID.getGraphicsContext2D();

        gc.clearRect(0, 0, canvasID.getWidth(), canvasID.getHeight());

//        biggest x = 1000
//        smallest x = 50
//        biggest y = 560
//        smallest y = 50

        for (Node airport : fdeb.getAirports()) {
            double x = airport.getPosition().getX();
            double y = airport.getPosition().getY();

            gc.setFill(Color.BLUE);
            //Ovechkin constant for positioning graph
            int OK = 3;
            gc.fillOval(x-OK+OK2, y-OK, 7, 7);
        }


        for (Edge flight : flights) {


            ArrayList<Double> points = new ArrayList<>();
            for (Node n : flight.getSubdivisionPoints()) {
                points.add(n.getPosition().getX() + OK2);
                points.add(n.getPosition().getY());
            }
            drawSomething(gc, createPath(points));

        }

    }

    private void drawSomething(GraphicsContext gc, Path pathList) {

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
        read(compability);
        read(step_size);
        read(iterations_count);
        read(cycles_count);
        read(edge_stiffness);
    }



    private void read(TextField field){
        DecimalFormat format = new DecimalFormat( "#.0" );


        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if ( c.getControlNewText().isEmpty() )
            {
                return c;
            }

            ParsePosition parsePosition = new ParsePosition( 0 );
            Object object = format.parse( c.getControlNewText(), parsePosition );

            if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() )
            {
                return null;
            }
            else
            {
                return c;
            }
        }));
    }
}