//package main;
//
//import javafx.application.Application;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.stage.Stage;
//
//import java.net.URL;
//import java.nio.file.Paths;
//
//public class RunGUI extends Application {
//
//    @Override
//    public void start(Stage stage) throws Exception {
//
//        URL url = Paths.get( "src/main/resources/FXMLDocument.fxml").toUri().toURL();
//        Parent root = FXMLLoader.load(url);
//        Scene scene = new Scene(root);
//        stage.setScene(scene);
//        stage.show();
//
//    }
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//}