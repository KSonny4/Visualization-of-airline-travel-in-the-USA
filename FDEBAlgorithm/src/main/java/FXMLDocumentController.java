import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import model.ForceDirectedEdgeBundling;
import model.Node;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {

    @FXML
    private Button visualiseButton;
    @FXML
    private Canvas canvasID;

    @FXML
    private void handleVisButtonAction(ActionEvent event) throws IOException {
        Main m  = new Main();
        m.loadInputData();
        ForceDirectedEdgeBundling fdeb = new ForceDirectedEdgeBundling(m.airports, m.flights, m.adjacency);
        //List<List<Node>> edges = fdeb.run();

        GraphicsContext gc = canvasID.getGraphicsContext2D();

//        biggest x = 1000
//        smallest x = 50
//        biggest y = 560
//        smallest y = 50

        for( Node airport : fdeb.getAirports()){
            double x = airport.getPosition().getX();
            double y = airport.getPosition().getY();

            gc.setFill(Color.BLUE);
            gc.fillOval(x,y, 7, 7);
        }





    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}