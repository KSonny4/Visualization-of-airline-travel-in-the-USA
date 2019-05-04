import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class RunGUI extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        //TODO tohle mi v Ubuntu proste nejde dat primo do slozky :DDDDDDDDDDDDDDDDdd
        URL url = Paths.get( "/home/ksonny/Projects/Visualization-of-airline-travel-in-the-USA/FDEBAlgorithm/src/main/java/FXMLDocument.fxml").toUri().toURL();
        //URL url = Paths.get( "file:FXMLDocument.fxml").toUri().toURL();
        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}