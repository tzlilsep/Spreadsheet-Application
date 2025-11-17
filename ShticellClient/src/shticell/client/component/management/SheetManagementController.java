package shticell.client.component.management;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import shticell.client.component.main.ShticellAppMainController;
import shticell.client.component.viewSheet.ViewSheet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SheetManagementController {


    private String userName;

    private int requestCounter = 1; // אתחול המונה

    @FXML
    public Label usernameLable;

    @FXML
    private TableView<SheetData> sheetsTable; // Table for displaying sheets
    @FXML
    private TableColumn<SheetData, String> usernameColumn; // Username column in the sheets table
    @FXML
    private TableColumn<SheetData, String> sheetNameColumn; // Sheet name column in the sheets table
    @FXML
    private TableColumn<SheetData, String> sizeColumn; // Size column in the sheets table
    @FXML
    private TableColumn<SheetData, String> permissionType; // Authorization type column in the sheets table


    @FXML
    private TableView<PermissionDto> permissionTable; // Table for displaying sheets
    @FXML
    private TableColumn<PermissionDto, String> permissionUsernameColumn; // Username column in the permissions table
    @FXML
    private TableColumn<PermissionDto, String> permissionAuthTypeColumn; // Authorization type column in the permissions table
    @FXML
    private TableColumn<PermissionDto, String> permissionStatusColumn; // Status column in the permissions table



    @FXML
    private Button viewSheetButton; // Button to view the selected sheet

    @FXML
    private Button requestPermissionButton; // Button to request permission for a sheet

    @FXML
    private Button acknowledgeDenyPermissionButton; // Button to acknowledge or deny permissions

    @FXML
    private Button loadXmlButton; // Button to load an XML file for sheets

    @FXML
    private Label commandsLabel; // Label for the "Commands" section

    @FXML
    private Label chooseSheetLabel; // Label prompting the user to select a sheet

    private ShticellAppMainController shticellAppMainController;


    //timer for refreshing
    private ScheduledExecutorService scheduler;



    @FXML
    public void initialize() {
        // הפעלת הטיימר לבדוק שינוי בגרסה כל 3 שניות
        startVersionCheckTimer();

        // Set up the table columns with corresponding SheetData properties
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("uploader"));
        sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        permissionType.setCellValueFactory(new PropertyValueFactory<>("permission"));

        // Set up the permission table columns with corresponding PermissionDto properties
        permissionUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        permissionAuthTypeColumn.setCellValueFactory(new PropertyValueFactory<>("permissionType"));
        permissionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("statusMessage"));

        // Listener for selecting a sheet to view its permissions
        sheetsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                handleViewPermissions(); // Load and display permissions for the selected sheet

            }
        });

        checkVersion();
    }

    private void startVersionCheckTimer() {
        scheduler = Executors.newScheduledThreadPool(1);

        // הפעלת משימה חוזרת לבדיקה כל 3 שניות
        scheduler.scheduleAtFixedRate(this::checkVersion, 0, 3, TimeUnit.SECONDS);
    }

    private void checkVersion() {
        OkHttpClient client = new OkHttpClient();

        // Create GET request
        Request request = new Request.Builder()
                .url("http://localhost:8080/ShticellWebApp_Web_exploded/version")
                .get()
                .addHeader("Cookie", "JSESSIONID=911143E2964D8721C71C0E068CC2D43E")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> System.out.println("Failed to check version: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    Gson gson = new Gson();
                    try {
                        // Parse JSON as a Map to extract the "version" key
                        Map<String, Object> map = gson.fromJson(responseData, Map.class);

                        // Confirm if "version" exists in the Map and is correctly parsed
                        if (map.containsKey("version")) {
                            double serverVersionDouble = (double) map.get("version"); // Gson parses JSON numbers as Double by default
                            int serverVersion = (int) serverVersionDouble;


                            if (serverVersion != currentVersion) {
                                currentVersion = serverVersion;
                                Platform.runLater(() -> onVersionChange());
                            }
                        } else {
                            Platform.runLater(() -> System.out.println("No 'version' key found in the Map."));
                        }
                    } catch (Exception e) {
                        System.out.println("Error while parsing JSON as Map: " + e.getMessage());
                    }

                } else {
                    System.out.println("Response not successful: " + response.message());
                }
            }
        });
    }

    // פעולה שתופעל כאשר הגרסה בשרת משתנה
    private void onVersionChange() {
        //System.out.println("Version changed! Refreshing data...");
        refreshSheetList(); // לדוגמה, רענון רשימת הגיליונות
        refreshPermissionTable();
    }

    @FXML
    private void handleViewSheet() {
        // קבלת השורה הנבחרת
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();

        // בדיקה אם נבחר גיליון
        if (selectedSheet == null) {
            showErrorMessage("No sheet selected. Please select a sheet to view.");
            return;
        }


        // שם המשתמש המחובר
        String username = userName;

        // שליחת בקשה לשרת כדי לקבל את המידע של הגיליון
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ShticellWebApp_Web_exploded/viewSheet").newBuilder();
        urlBuilder.addQueryParameter("sheetName", selectedSheet.getSheetName());
        urlBuilder.addQueryParameter("userName", username); // הוספת שם המשתמש כדי לבדוק הרשאות
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    showErrorMessage("Failed to retrieve sheet: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    Gson gson = new Gson();
                    SheetDataResponse sheetDataResponse = gson.fromJson(responseData, SheetDataResponse.class);


                    // הצגת הגיליון והעברת סוג ההרשאה לפונקציה
                    Platform.runLater(() -> displaySingleSheet(sheetDataResponse, sheetDataResponse.getPermissionType()));
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error message provided";
                    Platform.runLater(() -> showErrorMessage("Failed to retrieve sheet: " + errorBody));
                }
            }


        });
    }

    private void displaySingleSheet(SheetDataResponse sheetDataResponse,PermissionType permissionType) {
        try {
            // Load the FXML for ViewSheet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shticell/client/component/viewSheet/viewSheet.fxml"));
            BorderPane viewSheetRoot = loader.load();

            // Get the controller associated with the FXML and set the sheet data
            ViewSheet viewSheetController = loader.getController();
            viewSheetController.setSheetData(sheetDataResponse, permissionType); // Pass the SheetDataResponse object
            viewSheetController.setUsername(userName);

            // Add a back button to return to the management room
            Button backButton = new Button("Back to Home Screen");
            Label sheetname = new Label("User Name: "+userName+" | Sheet Name: " + sheetDataResponse.getSheetName() + " | Permission Type: " + permissionType);
            sheetname.setStyle("-fx-font-size: 14.5px; -fx-font-weight: bold;"); // Set font size and bold style

         // Set up the back button
            backButton.setPrefSize(160, 35);  // Adjust width and height as needed
            backButton.setStyle("-fx-font-size: 14px;");  // Set a larger font size for the button text
            backButton.setOnAction(event -> shticellAppMainController.switchToManagementRoom());

// Assuming you already have some content at the top
            Node existingTopContent = viewSheetRoot.getTop();  // Get the existing top content
            Node existingTopContent1 = viewSheetRoot.getBottom();  // Get the existing top content


            // Create an HBox to hold both the existing top content and the back button
            VBox topContainer = new VBox();
            topContainer.setSpacing(10);  // Add some space between the button and the existing content
            topContainer.setAlignment(Pos.CENTER_LEFT);  // Align all items to the right

// Create an HBox to hold both the existing top content and the back button
            VBox topContainer1 = new VBox();
            topContainer1.setSpacing(10);  // Add some space between the button and the existing content
            topContainer1.setAlignment(Pos.CENTER_RIGHT);  // Align all items to the right

// Add the existing content and the back button to the HBox
            topContainer1.getChildren().add(backButton);  // Add the button to the HBox
            topContainer.getChildren().add(sheetname);  // Add the button to the HBox

            if (existingTopContent != null) {
                topContainer.getChildren().add(existingTopContent);
            }

            if (existingTopContent1 != null) {
                topContainer.getChildren().add(existingTopContent1);
            }
// Set the HBox as the new top content of the BorderPane
            viewSheetRoot.setTop(topContainer);
            viewSheetRoot.setBottom(topContainer1);


// Switch to the ViewSheet in the same window
            shticellAppMainController.setMainPanelTo(viewSheetRoot);

            backButton.requestFocus();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // This method is triggered when the "Load XML" button is clicked
    // This method is triggered when the "Load XML" button is clicked
    @FXML
    private void handleLoadXmlAction() {
        Stage stage = (Stage) loadXmlButton.getScene().getWindow(); // Get the current stage
        String filePath = loadXML(stage); // Load the XML file using the file chooser

        if (filePath != null) {
            File file = new File(filePath);

            try(BufferedReader reader =  new BufferedReader(new FileReader(file))) {

                String text = reader.lines().collect(Collectors.joining("\n"));
                // Now send the file to the server using OkHttpClient
                OkHttpClient client = new OkHttpClient().newBuilder().build();

                // Extract the owner name and remove "Hello " from the text
                String ownerName = usernameLable.getText().replace("Hello ", "").trim();

                // Log the cleaned owner name for debugging
                System.out.println("Owner Name (cleaned): " + ownerName);
                RequestBody body = RequestBody.create(MediaType.parse("application/xml"), text);


                // Construct the URL with the cleaned ownerName as a query parameter
                String url = "http://localhost:8080/ShticellWebApp_Web_exploded/sheets?ownerName=" + ownerName;

                Request request = new Request.Builder()
                        .url(url)
                        .method("POST", body)
                        .addHeader("Cookie", "JSESSIONID=F3AA39546EB94650EE1B3FF472DC1101")
                        .build();

                // Make the asynchronous request
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        // Handle failure
                        Platform.runLater(() -> {
                            System.out.println("File upload failed: " + e.getMessage());
                        });
                    }

                    // Once the file is successfully uploaded, the server will return the updated list of sheet names. we need to update the TableView.
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // File uploaded successfully
                            System.out.println("File upload succeeded.");
                            Platform.runLater(() -> {
                                refreshSheetList();
                            });
                        } else {
                            String errorBody = response.body() != null ? response.body().string() : "Unknown error occurred";
                            Platform.runLater(() -> {
                                showErrorMessage(errorBody);
                            });
                        }
                    }

                });
            }catch (Exception e) {

                showErrorMessage(e.getMessage());
            }

        } else {
            System.out.println("No file selected.");
        }
    }




    public String loadXML(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            return selectedFile.getAbsolutePath();
        }
        return null;
    }




    public void setActive(String username) {
        System.out.println("Sheet Management Screen is now active.");

        usernameLable.setText(" Hello " +username);
        this.userName = username;
        //usersListComponentController.startListRefresher();
    }



    // Method to link this controller to the main controller
    public void setChatAppMainController(ShticellAppMainController shticellAppMainController) {
        this.shticellAppMainController = shticellAppMainController;
        // You can use this method to communicate with the main controller if needed.
    }

    // Close any open resources or handle cleanup when this screen is closed
    public void close() {
        System.out.println("Sheet Management Screen closed.");
        // Implement any necessary cleanup actions here.

    }
    public void loadSheetsData(String data) {
        // להמיר את המידע לנתונים רלוונטיים, כגון רשימת גיליונות
        System.out.println("Received sheets data: " + data);
        // אפשר להוסיף קוד כאן כדי לעדכן את טבלת הגיליונות לפי הנתונים שהתקבלו
    }



    @FXML
    private void handleAcknowledgeDenyPermission(ActionEvent event) {
        // Get the selected permission from the permission table
        PermissionDto selectedPermission = permissionTable.getSelectionModel().getSelectedItem();
        if (selectedPermission == null) {
            showErrorMessage("No permission selected. Please select a permission to ack/deny");
            return;
        }



        // Show confirmation dialog to approve or deny
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Acknowledge/Deny Permission");
        confirmationAlert.setHeaderText("Approve or Deny Permission for: " + selectedPermission.getUserName());
        confirmationAlert.setContentText("Choose your action:");

        ButtonType approveButton = new ButtonType("Approve");
        ButtonType denyButton = new ButtonType("Deny");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationAlert.getButtonTypes().setAll(approveButton, denyButton, cancelButton);

        // Wait for user response
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == approveButton) {
                sendAcknowledgeOrDenyRequest(selectedPermission, userName, true); // Approve with loggedInUser
            } else if (response == denyButton) {
                sendAcknowledgeOrDenyRequest(selectedPermission, userName, false); // Deny with loggedInUser
            }
        });
    }

    private void sendAcknowledgeOrDenyRequest(PermissionDto permission, String loggedInUser, boolean approve) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ShticellWebApp_Web_exploded/ackOrDenyPermission").newBuilder();
        urlBuilder.addQueryParameter("sheetName", permission.getSheetName());
        urlBuilder.addQueryParameter("userName", permission.getUserName());
        urlBuilder.addQueryParameter("approve", String.valueOf(approve));
        urlBuilder.addQueryParameter("loggedInUser", loggedInUser); // העברת שם המשתמש כפרמטר
        urlBuilder.addQueryParameter("perID", String.valueOf(permission.getPermissionID())); // העברת id כפרמטר


        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0])) // גוף ריק
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showErrorMessage("Failed to send acknowledge/deny request: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        System.out.println("Permission " + (approve ? "approved" : "denied") + " successfully.");
                        onVersionChange();

                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    Platform.runLater(() -> showErrorMessage("Failed to acknowledge/deny permission: " + errorBody));
                }
            }
        });
    }



    // משתנה לאחסון הגרסה הנוכחית שהלקוח יודע עליה
    private int currentVersion = -1;

    private void refreshSheetList() {
        // שמירת הבחירה הנוכחית
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();
        String selectedSheetName = selectedSheet != null ? selectedSheet.getSheetName() : null;

        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // יצירת בקשה לרשימת הגיליונות המעודכנת
        Request request = new Request.Builder()
                .url("http://localhost:8080/ShticellWebApp_Web_exploded/sheets/list?username=" + userName)
                .get()
                .addHeader("Cookie", "JSESSIONID=ECD8FD51A2BC3D4EC35E44ADBF236350")
                .build();

        // ביצוע הבקשה
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    System.out.println("Failed to retrieve sheet list: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // ניתוח תגובת JSON
                    Gson gson = new Gson();
                    SheetData[] sheets = gson.fromJson(responseData, SheetData[].class);

                    List<SheetData> sheetList = Arrays.asList(sheets);

                    // עדכון טבלת ה-UI ב-thread המתאים
                    Platform.runLater(() -> {
                        sheetsTable.getItems().clear();
                        sheetsTable.getItems().addAll(sheetList);

                        // החזרת הבחירה אם אפשר
                        if (selectedSheetName != null) {
                            for (SheetData sheet : sheetList) {
                                if (sheet.getSheetName().equals(selectedSheetName)) {
                                    sheetsTable.getSelectionModel().select(sheet);
                                    break;
                                }
                            }
                        }

                    });
                }
            }
        });
    }



    @FXML
    private void handleRequestPermission() {
        // Get the selected sheet
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();

        if(selectedSheet==null) {
            showErrorMessage("No sheet selected.");
        }

        else if(selectedSheet.getUploader().equals(userName)) {
            showErrorMessage("The owner of a sheet cannot request permission for their own sheet.");
        }
        else {
            // Create a dialog for choosing permission type
            Stage dialog = new Stage();

            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.setSpacing(10);

            // Create a label for the title
            Label titleLabel = new Label("Request Permission for sheet:");
            titleLabel.setStyle("-fx-font-size: 14px;");
            Label titleLabel1 = new Label(selectedSheet.getSheetName());
            titleLabel1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            // Create radio buttons for permission types
            RadioButton readRadioButton = new RadioButton("READ");
            RadioButton writeRadioButton = new RadioButton("WRITE");
            ToggleGroup group = new ToggleGroup();
            readRadioButton.setToggleGroup(group);
            writeRadioButton.setToggleGroup(group);
            readRadioButton.setSelected(true); // Default to READ

            // Create submit button
            Button submitButton = new Button("Submit");
            submitButton.setOnAction(event -> {
                PermissionType permissionType = readRadioButton.isSelected() ? PermissionType.READ : PermissionType.WRITE;
                dialog.close();  // Close the dialog

                // Send the selected permission (using the PermissionType enum) to the server
                sendPermissionRequestToServer(selectedSheet.getSheetName(), permissionType);
            });

            // Add all components to the VBox
            vbox.getChildren().addAll(titleLabel, readRadioButton, writeRadioButton, submitButton);

            // Create a scene and set it in the dialog
            Scene dialogScene = new Scene(vbox, 200, 180);
            dialog.setScene(dialogScene);
            dialog.show();
        }
    }



    @FXML
    private void sendPermissionRequestToServer(String sheetName, PermissionType permissionType) {
        OkHttpClient client = new OkHttpClient();

        // Append username to the URL as a query parameter
        String url = "http://localhost:8080/ShticellWebApp_Web_exploded/permissions?username=" + userName;

        // Create the DTO object with the enum as an integer for permissionType
        PermissionRequestDto requestDto = new PermissionRequestDto(sheetName, userName, permissionType,requestCounter);
        Gson gson = new Gson();
        String json = gson.toJson(requestDto);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

        Request request = new Request.Builder()
                .url(url) // Use the modified URL with username as query parameter
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> {
                    System.out.println("Failed to request permission: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Platform.runLater(() -> {
                        System.out.println("Permission requested successfully.");
                        refreshPermissionTable();
                        requestCounter++;
                    });
                } else {
                    String errorBody = response.body().string();  // Read error response body
                    Platform.runLater(() -> {
                        System.out.println("Failed to request permission: " + errorBody);
                    });
                }

            }

        });
    }

    @FXML
    private void handleViewPermissions() {
        // קבלת השורה הנבחרת (הגיליון הנבחר)
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();

        if (selectedSheet != null) {
            // יצירת בקשה לשרת לקבלת הרשאות של הגיליון
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ShticellWebApp_Web_exploded/permissions").newBuilder();
            urlBuilder.addQueryParameter("sheetName", selectedSheet.getSheetName());
            urlBuilder.addQueryParameter("username", userName); // הוספת שם המשתמש ל-URL
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Platform.runLater(() -> {
                        System.out.println("Failed to retrieve permissions: " + e.getMessage());
                    });
                }
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();

                        // המרת ה-JSON לאובייקטים של PermissionDto
                        Gson gson = new Gson();
                        PermissionDto[] permissions = gson.fromJson(responseData, PermissionDto[].class);
                        List<PermissionDto> permissionList = Arrays.asList(permissions);

                        // עדכון טבלת ההרשאות ב-UI
                        Platform.runLater(() -> {
                            permissionTable.getItems().clear();
                            permissionTable.getItems().addAll(permissionList);


                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No response body";
                        Platform.runLater(() -> {
                            System.out.println("Failed to retrieve permissions. Status code: " + response.code() + ", Error: " + errorBody);
                        });
                    }
                }

            });
        } else {
            System.out.println("No sheet selected.");
        }
    }

    private void refreshPermissionTable() {
        SheetData selectedSheet = sheetsTable.getSelectionModel().getSelectedItem();


        if (selectedSheet != null) {
            handleViewPermissions(); // Refresh permissions for the selected sheet
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Loading Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
