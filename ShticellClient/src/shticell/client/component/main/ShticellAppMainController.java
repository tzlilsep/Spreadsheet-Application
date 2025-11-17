package shticell.client.component.main;

import javafx.scene.Scene;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import shticell.client.component.api.ChatCommands;
import shticell.client.component.api.HttpStatusUpdate;
import shticell.client.component.login.LoginController;
import shticell.client.component.management.SheetManagementController;
import shticell.client.component.status.StatusController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import shticell.client.util.http.HttpClientUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static shticell.client.util.Constants.*;

public class ShticellAppMainController implements Closeable, HttpStatusUpdate {

   // @FXML private Parent httpStatusComponent;
  //  @FXML private StatusController httpStatusComponentController;

    private GridPane loginComponent;
    private LoginController logicController;

    private Parent managementComponent;
    private SheetManagementController sheetManagementController;


    @FXML private Label userGreetingLabel;
    @FXML private AnchorPane mainPanel;

    private final StringProperty currentUserName;

    public ShticellAppMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
    }

    @FXML
    public void initialize() {

        // prepare components
        loadLoginPage();
        loadManagementRoomPage();

    //    switchToManagementRoom();  // Force a switch to management room to test

    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }

    public void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @Override
    public void close() throws IOException {
        sheetManagementController.close();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            logicController = fxmlLoader.getController();
            logicController.shticellAppMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadManagementRoomPage() {
        URL loginPageUrl = getClass().getResource(MANAGE_ROOM_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            managementComponent = fxmlLoader.load();
            sheetManagementController = fxmlLoader.getController();
            sheetManagementController.setChatAppMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHttpLine(String line) {
      //  httpStatusComponentController.addHttpStatusLine(line);
    }

    public void switchToManagementRoom(String username) {
        setMainPanelTo(managementComponent);
        sheetManagementController.setActive(username);
    }



    public void switchToManagementRoom() {
        setMainPanelTo(managementComponent);
        sheetManagementController.setActive(currentUserName.get());
    }

}
