import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import model.ForceDirectedEdgeBundling;
import model.Node;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {

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



    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {


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
        STEP_SIZE =  (STEP_SIZE < 0 || STEP_SIZE > 1) ? 0.1 : STEP_SIZE;
        EDGE_STIFFNESS =  (EDGE_STIFFNESS < 0 || EDGE_STIFFNESS > 1) ? 0.9 : EDGE_STIFFNESS;
        ITERATIONS_COUNT =  (ITERATIONS_COUNT < 0 || ITERATIONS_COUNT > 90) ? 90 : ITERATIONS_COUNT;
        CYCLES_COUNT =  (CYCLES_COUNT < 0 || CYCLES_COUNT > 6) ? 6 : CYCLES_COUNT;


        compability.setText(String.valueOf(COMPABILITY));
        step_size.setText(String.valueOf(STEP_SIZE));
        edge_stiffness.setText(String.valueOf(EDGE_STIFFNESS));
        iterations_count.setText(String.valueOf(ITERATIONS_COUNT));
        cycles_count.setText(String.valueOf(CYCLES_COUNT));



        Main m = new Main();
        m.loadInputData();

        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(m.airports, m.flights,m.adjacency, STEP_SIZE, COMPABILITY, EDGE_STIFFNESS,ITERATIONS_COUNT, CYCLES_COUNT);
        //ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(m.airports, m.flights, m.adjacency,STEP_SIZE, COMPABILITY);
        List<List<Node>> edges = fdeb.run();





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
            gc.fillOval(x-3, y-3, 7, 7);
        }


        for(List<Node> nodes : edges){
            ArrayList<Double> points = new ArrayList<>();
            for (Node n : nodes){
                points.add(n.getPosition().getX());
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