package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.nio.file.Paths;



public class RunGUI extends Application {
    private static Stage mainStage;

    public static Stage getMainStage(){
        return mainStage;
    }


    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        URL url = Paths.get( "src/main/resources/FXMLDocument.fxml").toUri().toURL();
        Parent root = FXMLLoader.load(url);
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}