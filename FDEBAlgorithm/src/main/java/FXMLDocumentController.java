import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import model.ForceDirectedEdgeBundling;
import model.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {

    @FXML
    private Button visualiseButton;
    @FXML
    private Canvas canvasID;

    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {
        Main m = new Main();
        m.loadInputData();
        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(m.airports, m.flights, m.adjacency);
        List<List<Node>> edges = fdeb.run();



        GraphicsContext gc = canvasID.getGraphicsContext2D();

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

        //drawLine(gc, 1000, 560, 50, 50);
        //drawCurve(gc, 1000, 560, 300, 300, 50, 50);




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

    }
}