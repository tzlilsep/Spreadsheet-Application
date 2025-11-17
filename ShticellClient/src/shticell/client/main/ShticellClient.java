package shticell.client.main;

import shticell.client.component.main.ShticellAppMainController;
import shticell.client.util.http.HttpClientUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

import static shticell.client.util.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class ShticellClient extends Application {

    private ShticellAppMainController shticellAppMainController;

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setMinHeight(660);
        primaryStage.setMinWidth(750);
        primaryStage.setTitle("Shticell App Client");

        URL loginPage = getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            shticellAppMainController = fxmlLoader.getController();

            Scene scene = new Scene(root, 700, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        shticellAppMainController.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
