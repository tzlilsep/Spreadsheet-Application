package shticell.client.component.viewSheet;

import com.google.gson.Gson;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import shticell.client.component.management.PermissionType;
import shticell.client.component.management.SheetDataResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ViewSheet {

    private String username;
    private final Map<Integer, String> columnAlignment = new HashMap<>();  // Store column alignment
    private final Map<Integer, Double> columnWidths = new HashMap<>();  // Store column widths
    private final Map<Integer, Double> rowHeights = new HashMap<>();    // Store row heights
    public Button sortButton;
    public Button filterButton;
    public Label moreCommandsLable;
    public Button AddRangeButton;
    public Button DeleteRangeButton;
    public Button viewRangeButton;
    public Button resetStyleButton;
    public Label LableUpdateColor;
    public Label LabletextColor;
    public Label LableBackgroundColor;
    public Label rowNumberText;
    public Label CloumnLetterText;
    public Button updateColumnAlignmentButton;
    public Label Lable1;
    //public Label sheetVersionText;
    private PermissionType permissionType; // Store permission type
    @FXML
    //גירסאות
    private ChoiceBox<Integer> versionChoiceBox; // רכיב לבחירת גרסה

    //עדכון צבע
    @FXML
    private ColorPicker textColorPicker;  // Color picker for text color
    @FXML
    private ColorPicker backgroundColorPicker;  // Color picker for background color

    //נתונים של תא בודד
    private Label selectedCell; // Track the currently selected cell
    private int selectedRow; // Track selected row
    private int selectedCol; // Track selected column
    @FXML
    private HBox cellUpdateControls; // The HBox containing the fields for cell updates
    @FXML
    private TextField selectedCellIdField;
    @FXML
    private TextField originalCellValueField;
    @FXML
    private TextField cellVersionField;
    @FXML
    private Button updateValueButton;

    private SheetDataResponse sheet; // The spreadsheet object

    //תצוגה
    @FXML
    private GridPane gridPane;
    @FXML
    private BorderPane leftPane;     // If the entire left section needs to be enabled

    //רנג'ים
    @FXML
    private VBox rangeControlPanel;  // The VBox containing the range controls
    @FXML
    private TextField rangeNameField;
    @FXML
    private TextField fromCellField;
    @FXML
    private TextField toCellField;


    //עמודות
    @FXML
    private TextField columnLetterField; // For entering the column letter (e.g., A, B, etc.)
    @FXML
    private TextField columnWidthField; // For entering the new column width
    @FXML
    private TextField rowHeightField;   // For entering the new row height
    @FXML
    private Button updateColumnWidthButton;
    @FXML
    private Button updateRowHeightButton;
    @FXML
    private ComboBox<String> alignmentComboBox;
    public TextField rowNumberField;
    private int[] topLeft;


    // Called when the FXML file is loaded
    @FXML
    private void initialize() {
        // Disable controls until a file is loaded

        // מאזין לבחירת צבע טקסט
        textColorPicker.setOnAction(event -> handleTextColorChange());
        // מאזין לבחירת צבע רקע
        backgroundColorPicker.setOnAction(event -> handleBackgroundColorChange());
        // Disable the update button initially, until a cell is selected
        //updateValueButton.setDisable(true);
        // Set default colors for color pickers
        textColorPicker.setValue(Color.BLACK);  // ברירת מחדל לצבע הטקסט - שחור
        backgroundColorPicker.setValue(Color.WHITE);  // ברירת מחדל לצבע הרקע - לבן

        // Initialize alignment options for ComboBox
        alignmentComboBox.getItems().addAll("Left", "Center", "Right");
        alignmentComboBox.setPromptText("Center");  // Default prompt text

        // מאזין לבחירת גרסה מתוך ה-ChoiceBox
        //versionChoiceBox.setOnAction(event -> handleVersionChange());
    }


    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File Loading Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }


    public CellDto getCellInfo(String sheetName, int row, int col) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ShticellWebApp_Web_exploded/cellInfo").newBuilder();
        urlBuilder.addQueryParameter("sheetName", sheetName);
        urlBuilder.addQueryParameter("row", String.valueOf(row));
        urlBuilder.addQueryParameter("col", String.valueOf(col));

        String url = urlBuilder.build().toString();
        Request request = new Request.Builder().url(url).get().build();

        try (Response response = client.newCall(request).execute()) { // Synchronous call
            if (response.isSuccessful() && response.body() != null) {
                String responseData = response.body().string();
                Gson gson = new Gson();
                return gson.fromJson(responseData, CellDto.class);  // Directly return the CellDto
            } else {
                throw new IOException("Failed to retrieve cell info: " + response.message());
            }
        } catch (IOException e) {
            //showErrorMessage("Error retrieving cell information: " + e.getMessage());
            return null;
        }
    }


    public void displaySheetInGridPane() {


        if (sheet != null) {
            cellUpdateControls.setDisable(false);
            leftPane.setDisable(false);
            rangeControlPanel.setDisable(false);
            versionChoiceBox.setValue(sheet.getVersion());  // הגרסה האחרונה היא הנוכחית

            gridPane.getChildren().clear(); // Clear existing content
            gridPane.setGridLinesVisible(true); // Enable grid lines

            // Set column headers (A, B, C, ...) on row 0
            for (int col = 0; col < sheet.getNumColumns(); col++) {
                String columnLetter = String.valueOf((char) ('A' + col)); // Convert to A, B, C, etc.
                Label headerLabel = new Label(columnLetter);

                double columnWidth = columnWidths.getOrDefault(col, sheet.getColumnsWidth() * 2);  // Default width if not set


                headerLabel.setMinWidth(columnWidth);
                headerLabel.setPrefWidth(columnWidth);

                headerLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px; -fx-background-color: #c5c2c2");
                gridPane.add(headerLabel, col + 1, 0);
            }

            // Set row headers (1, 2, 3, ...) in column 0
            for (int row = 0; row < sheet.getNumRows(); row++) {
                Label rowLabel = new Label(Integer.toString(row + 1));

                double rowHeight =  rowHeights.getOrDefault(row, sheet.getRowsHeight() * 2);  // Default height of 30.0 if not set


                // Apply height for each row based on user-specified value
                rowLabel.setMinHeight(rowHeight);
                rowLabel.setPrefHeight(rowHeight);
                rowLabel.setMinWidth(30);
                rowLabel.setPrefWidth(30);

                rowLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;-fx-background-color: #c5c2c2");
                gridPane.add(rowLabel, 0, row + 1);
            }


            // Display all cells, including empty cells
            for (int row = 0; row < sheet.getNumRows(); row++) {
                for (int col = 0; col < sheet.getNumColumns(); col++) {

                    double rowHeight =  rowHeights.getOrDefault(row, sheet.getRowsHeight() * 2);  // Default height of 30.0 if not set
                    double columnWidth = columnWidths.getOrDefault(col, sheet.getColumnsWidth() * 2);  // Default width if not set


//                    Cell cell = sheet.getCell(row, col);
//                    if (cell == null) {
//                        sheet.setCell(row, col, "", false);  // Create empty cell if needed
//                        cell = sheet.getCell(row, col);
//                        //cell.setVersion(1);
//                    }

                    // Retrieve the cell value from the data list in SheetDataResponse
                    String cellVal = sheet.getData().get(row).get(col);  // Access the value from the row and column

                    // If the cell value is null or empty, treat it as an empty string
                    String effectiveValue = (cellVal != null && !cellVal.isEmpty()) ? cellVal : "";

                    // Create a label for the cell value
                    Label cellLabel = new Label(effectiveValue);

                    cellLabel.setMinWidth(columnWidth);
                    cellLabel.setPrefWidth(columnWidth);
                    cellLabel.setMinHeight(rowHeight);
                    cellLabel.setPrefHeight(rowHeight);
                    cellLabel.setStyle("-fx-alignment: center;");

//                    // Apply alignment for the specific column from columnAlignment map
                    String alignmentStyle = getColumnAlignment(col);
                    if(alignmentStyle==null)
                        alignmentStyle= "-fx-alignment: center;";

                    CellDto cell = getCellInfo(sheet.getSheetName(),row,col);
                    if (cell != null) {
                        cellLabel.setStyle("-fx-border-color: black; -fx-border-width: 0.5px;" +
                                " -fx-text-fill: " + cell.getTextColor() + "; -fx-background-color: " + cell.getBackgroundColor() + ";" +
                                alignmentStyle);  // Apply alignment style
                    }
                    else
                    {  // Define default styles for cell label
                        String defaultTextColor = "#000000"; // Black text
                        String defaultBackgroundColor = "#FFFFFF"; // White background
                        String defaultBorderStyle = "-fx-border-color: black; -fx-border-width: 0.5px;";
                         // Center alignment for default

                        // Apply the default style to the cell label
                        cellLabel.setStyle(defaultBorderStyle +
                                " -fx-text-fill: " + defaultTextColor + ";" +
                                " -fx-background-color: " + defaultBackgroundColor + ";" +
                                alignmentStyle);


                    }

                    // Attach click event only if the user has WRITE permission


                    gridPane.add(cellLabel, col + 1, row + 1);

                    int finalRow = row;
                    int finalCol = col;
                    cellLabel.setOnMouseClicked(event -> handleCellClick(finalRow, finalCol, cellLabel));


                }
            }
        }
    }



    // Add this method to set the sheet data from SheetDataResponse
    public void setSheetData(SheetDataResponse sheetDataResponse, PermissionType permissionType) {
        this.sheet = sheetDataResponse;
        this.permissionType = permissionType;

        configurePermissions(); // Configure based on permission

        displaySheetInGridPane();
    }

    // Configure the UI based on the permission type
    private void configurePermissions() {
        if (permissionType == PermissionType.READ)
            disableEditingControls();
    }



    private void disableEditingControls() {

        updateValueButton.setDisable(true);
        textColorPicker.setDisable(true);
        backgroundColorPicker.setDisable(true);
        updateColumnWidthButton.setDisable(true);
        updateRowHeightButton.setDisable(true);
        alignmentComboBox.setDisable(true);
        AddRangeButton.setDisable(true);
        DeleteRangeButton.setDisable(true);
        rangeControlPanel.setDisable(true);
        rowNumberField.setDisable(true);
        columnLetterField.setDisable(true);
        rowHeightField.setDisable(true);
        columnWidthField.setDisable(true);
        resetStyleButton.setDisable(true);
        LableUpdateColor.setDisable(true);
        LabletextColor.setDisable(true);
        LableBackgroundColor.setDisable(true);
        rowNumberText.setDisable(true);
        CloumnLetterText.setDisable(true);
        updateColumnAlignmentButton.setDisable(true);
        AddRangeButton.setDisable(true);
        DeleteRangeButton.setDisable(true);
        Lable1.setDisable(true);


    }
    private Map<CellDto, String> originalBackgroundColors = new HashMap<>();

    private void handleCellClick(int row, int col, Label cellLabel) {

        // Update the selected cell reference
        selectedCell = cellLabel;
        selectedRow = row;
        selectedCol = col;

        // Update the selected cell information
        updateSelectedCellInfo(row, col);

    }

    // Updates the selected cell information in the text fields
    private void updateSelectedCellInfo(int row, int col) {
        CellDto currCell = getCellInfo(sheet.getSheetName(), row, col);
        selectedCellIdField.setText((char) ('A' + col) + Integer.toString(row + 1)); // Update Cell ID

        if (currCell != null) {
            //System.out.printf("cell info: row-" + row + " ,col-" + col + " ,curCell original value-" + currCell.getOriginalValue() + " ,effective value-" + currCell.getEffectiveValue());
            originalCellValueField.setText(currCell.getOriginalValue()); // Update original value
            highlightRelatedCells(currCell);

            textColorPicker.setValue(Color.web(currCell.getTextColor())); // Update text color
            backgroundColorPicker.setValue(Color.web(currCell.getBackgroundColor())); // Update background color

        } else {
            originalCellValueField.setText("");
            resetCellBackgrounds();

            textColorPicker.setValue(Color.web("#000000")); // Update text color
            backgroundColorPicker.setValue( Color.web("#FFFFFF")); // Update background color

        }
        cellVersionField.setText(String.valueOf(currCell != null ? currCell.getVersion() : "1")); // Update version

    }

////
////    private void saveOriginalCellColors() {
////        for (int row = 0; row < sheet.getNumRows(); row++) {
////            for (int col = 0; col < sheet.getNumCols(); col++) {
////                Cell cell = sheet.getCell(row, col);
////                if (cell != null) {
////                    Label cellLabel = (Label) getNodeFromGridPane(gridPane, row + 1, col + 1);
////                    if (cellLabel != null) {
////                        originalBackgroundColors.put(cell, cell.getBackgroundColor());
////                    }
////                }
////            }
////        }
////    }


    private void resetCellBackgrounds() {
        for (int row = 0; row < sheet.getNumRows(); row++) {
            for (int col = 0; col < sheet.getNumColumns(); col++) {

                CellDto cell = getCellInfo(sheet.getSheetName(),row, col);

                if (cell != null) {

                    Label cellLabel = (Label) getNodeFromGridPane(gridPane, row + 1, col + 1);
                    if (cellLabel != null) {
                        String originalColor = originalBackgroundColors.getOrDefault(cell, cell.getBackgroundColor());
                        cellLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;" +
                                " -fx-text-fill: " + cell.getTextColor() + "; -fx-background-color: " + originalColor + ";" + getColumnAlignment(col));
                    }
                }
            }
        }
    }


    @FXML
    private void handleUpdateCellValue() {
        // Refresh the display to show the updated value

        int selectedRowTemp = selectedRow;
        int selectedColTemp = selectedCol;
        Label selectedCellLabelTemp = selectedCell;


        String newValue = originalCellValueField.getText();
        String pos = selectedCellIdField.getText().toUpperCase();  // Get the position from the input (e.g., "A1")

        try {
            // Convert the position (e.g., "A1") into row and column
            if (pos.length() < 2) {
                System.out.println("Error: Invalid cell identifier.");
                return;
            }

            // Extract the column (letter) and row (number)
            char columnLetter = pos.charAt(0);  // First character is the column (A, B, C, ...)
            int row = Integer.parseInt(pos.substring(1)) - 1;  // Convert row (e.g., "1" -> 0 for 0-based indexing)

            int column = columnLetter - 'A';  // Convert column letter to index (A = 0, B = 1, etc.)

            // Check if the row and column are within the sheet's limits
            if (row < 0 || row >= sheet.getNumRows() || column < 0 || column >= sheet.getNumColumns()) {
                System.out.println("Error: Cell identifier is out of bounds. Please enter a valid cell within the sheet's limits.");
                return;
            }

            try {

                // Try to update the cell in the sheet
                updateCellValue(sheet.getSheetName(), row, column, newValue);

                sheet.setVersion(sheet.getVersion()+1);

                Platform.runLater(() -> {
                    // Refresh display and reselect cell
                    getSheetInfo();

                    displaySheetInGridPane();

// Set a brief delay to ensure UI updates are complete before reselecting
                    PauseTransition pause = new PauseTransition(Duration.millis(1));
                    pause.setOnFinished(event -> {
                        handleCellClick(selectedRowTemp, selectedColTemp, selectedCellLabelTemp);
                        versionChoiceBox.setValue(sheet.getVersion()+1);  // Update the version choice box
                    });
                    pause.play();

                });


                System.out.println("Cell updated successfully.");

            } catch (Exception e) {
                // In case of an error in setCell, show error and reset the field
                showErrorMessage("Error updating cell: " + e.getMessage());
            }
        } catch (Exception e) {
            // General error handling
            showErrorMessage("Error processing cell update: " + e.getMessage());
            originalCellValueField.clear();
        }
    }

    private void getSheetInfo() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("http://localhost:8080/ShticellWebApp_Web_exploded/viewSheet").newBuilder();
        urlBuilder.addQueryParameter("sheetName", sheet.getSheetName());
        urlBuilder.addQueryParameter("userName", username); // Add username to check permissions
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() -> showErrorMessage("Failed to retrieve sheet: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response) { // Ensure response is closed
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();

                        Gson gson = new Gson();
                        sheet = gson.fromJson(responseData, SheetDataResponse.class);

                        Platform.runLater(() -> displaySheetInGridPane()); // Update UI on JavaFX thread
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No error message provided";
                        Platform.runLater(() -> showErrorMessage("Failed to retrieve sheet: " + errorBody));
                    }
                }
            }
        });
    }

////
////    // Additional methods for ranges and column/row updates
////    @FXML
////    private void handleAddRange() {
////        String rangeName = rangeNameField.getText();
////        String fromCell = fromCellField.getText();
////        String toCell = toCellField.getText();
////
////        // Check if any of the fields are empty
////        if (rangeName.isEmpty() || fromCell.isEmpty() || toCell.isEmpty()) {
////            showErrorMessage("Error: Range details not entered");
////            return;
////        }
////
////
////        // איפוס הסימון של ה-View Range הקודם
////        resetCellBackgrounds();  // איפוס כל התאים לצבעי הרקע המקוריים
////
////
////        try {
////            sheet.defineRange(rangeName, fromCell, toCell);
////            System.out.println("Range " + rangeName + " added successfully.");
////        } catch (IllegalArgumentException e) {
////            showErrorMessage("Error adding range: " + e.getMessage());
////        }
////    }
////
////    @FXML
////    private void handleDeleteRange() {
////        String rangeName = rangeNameField.getText();  // קבלת שם הטווח למחיקה
////
////
////        // איפוס הסימון של ה-view range הקודם
////        resetCellBackgrounds();  // איפוס כל התאים לצבעי הרקע המקוריים
////
////        // בדיקה אם שם הטווח קיים במפה של הטווחים
////        if (sheet.getRanges().containsKey(rangeName)) {
////            sheet.removeRange(rangeName);  // מחיקת הטווח
////            System.out.println("Range " + rangeName + " deleted successfully.");
////        } else {
////            showErrorMessage("Range " + rangeName + " does not exist.");
////        }
////    }
////

    @FXML
    private void handleTextColorChange() {
        if (selectedCell != null) {
            // Get the new text color from the ColorPicker
            Color textColor = textColorPicker.getValue();
            String textColorString = String.format("#%02X%02X%02X",
                    (int) (textColor.getRed() * 255),
                    (int) (textColor.getGreen() * 255),
                    (int) (textColor.getBlue() * 255));

            // Update the cell's text color in the model (CellImpl)
            CellDto cell = getCellInfo(sheet.getSheetName(),selectedRow, selectedCol);
            if (cell != null) {
                cell.setTextColor(textColorString);
                updateCellStyle(selectedRow,selectedCol,cell.getBackgroundColor(),textColorString);

                // Update the selected cell's style immediately in the UI
                selectedCell.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;" +
                        " -fx-text-fill: " + cell.getTextColor() + "; -fx-background-color: " + cell.getBackgroundColor() + ";" + getColumnAlignment(selectedCol));
            }
        }
    }

    @FXML
    private void handleBackgroundColorChange() {
        if (selectedCell != null) {
            // Get the new background color from the ColorPicker
            Color backgroundColor = backgroundColorPicker.getValue();
            String backgroundColorString = String.format("#%02X%02X%02X",
                    (int) (backgroundColor.getRed() * 255),
                    (int) (backgroundColor.getGreen() * 255),
                    (int) (backgroundColor.getBlue() * 255));

            // Update the cell's background color in the model (CellImpl)
            CellDto cell = getCellInfo(sheet.getSheetName(),selectedRow, selectedCol);
            if (cell != null) {
                cell.setBackgroundColor(backgroundColorString);
                updateCellStyle(selectedRow,selectedCol,backgroundColorString,cell.getTextColor());

                // Update the selected cell's style immediately in the UI
                selectedCell.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;" +
                        " -fx-text-fill: " + cell.getTextColor() + "; -fx-background-color: " + cell.getBackgroundColor() + ";" + getColumnAlignment(selectedCol));
            }



        }
    }


    public void updateCellStyle(int row, int col, String backgroundColor, String textColor) {
        OkHttpClient client = new OkHttpClient();

        // Set up the URL and query parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse( "http://localhost:8080/ShticellWebApp_Web_exploded/updateCellStyle").newBuilder();
        urlBuilder.addQueryParameter("sheetName", sheet.getSheetName());
        urlBuilder.addQueryParameter("row", String.valueOf(row));
        urlBuilder.addQueryParameter("col", String.valueOf(col));
        urlBuilder.addQueryParameter("textColor", textColor);
        urlBuilder.addQueryParameter("backgroundColor", backgroundColor);

        // Create the request
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, IOException e) {
                System.err.println("Failed to update cell style: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Cell style updated successfully.");
                } else {
                    System.err.println("Failed to update cell style: " + response.body().string());
                }
            }
        });
    }


    public void updateCellValue(String sheetName, int row, int col, String newOriginalValue) {
        OkHttpClient client = new OkHttpClient();

        // Set up the URL and query parameters
        HttpUrl.Builder urlBuilder = HttpUrl.parse( "http://localhost:8080/ShticellWebApp_Web_exploded/updateCellValue").newBuilder();
        urlBuilder.addQueryParameter("sheetName", sheet.getSheetName());
        urlBuilder.addQueryParameter("row", String.valueOf(row));
        urlBuilder.addQueryParameter("col", String.valueOf(col));
        urlBuilder.addQueryParameter("newOriginalValue", newOriginalValue);

        // Create the request
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create(null, new byte[0])) // Empty body for POST request
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, IOException e) {
                System.err.println("Failed to update cell style: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    System.out.println("Cell style updated successfully.");
                } else {
                    System.err.println("Failed to update cell style: " + response.body().string());
                }
            }
        });
    }

    @FXML
    private void handleResetStyle() {
        if (selectedCell != null) {
            // Reset the cell's colors to default in the model (CellImpl)
            CellDto cell = getCellInfo(sheet.getSheetName(),selectedRow, selectedCol);
            if (cell != null) {
                cell.setBackgroundColor("#FFFFFF");  // Reset to white background
                cell.setTextColor("#000000");        // Reset to black text

                // Update the selected cell's style immediately in the UI
                selectedCell.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;" +
                        " -fx-text-fill: " + cell.getTextColor() + "; -fx-background-color: " + cell.getBackgroundColor() + ";" + getColumnAlignment(selectedCol));

                updateCellStyle(selectedRow,selectedCol,"#FFFFFF","#000000");
                textColorPicker.setValue(Color.web(cell.getTextColor())); // Update text color
                backgroundColorPicker.setValue(Color.web(cell.getBackgroundColor())); // Update background color
            }
        }


    }

    @FXML
    private void handleUpdateColumnWidth() {
        String columnLetter = columnLetterField.getText().toUpperCase();
        String widthText = columnWidthField.getText();

        try {

            if (!isColumnValid(columnLetter, sheet.getNumColumns())) {
                showErrorMessage("Error: Invalid column letter. Please enter a valid column (A to " + (char) ('A' + sheet.getNumColumns() - 1) + ").");
                return;
            }

            int columnIndex = columnLetter.charAt(0) - 'A';  // Convert column letter to index
            double newWidth = Double.parseDouble(widthText); // Parse width input

            // Store the new width for the column
            columnWidths.put(columnIndex, newWidth);
            // Store the currently selected cell coordinates
            int selectedRowTemp = selectedRow;
            int selectedColTemp = selectedCol;

            // Refresh the display to show the updated column width
            displaySheetInGridPane();
            // Reselect the cell using handleCellClick if a valid cell was previously selected
            if (selectedRowTemp >= 0 && selectedColTemp >= 0) {
                Label cellLabel = getCellLabelAt(selectedRowTemp, selectedColTemp);
                if (cellLabel != null) {
                    handleCellClick(selectedRowTemp, selectedColTemp, cellLabel);
                }
            }

            System.out.println("Updated width of column " + columnLetter + " to " + newWidth);
        } catch (NumberFormatException e) {
            showErrorMessage("Invalid width input.");
        }
    }


    @FXML
    private void handleUpdateRowHeight() {
        String heightText = rowHeightField.getText();

        try {
            // Get the row index from the rowNumberField
            int rowIndex = Integer.parseInt(rowNumberField.getText()) - 1; // Convert 1-based to 0-based

            if (!isRowValid(rowIndex, sheet.getNumRows())) {
                showErrorMessage("Error: Invalid row number. Please enter a valid row (1 to " + sheet.getNumRows() + ").");
                return;
            }

            double newHeight = Double.parseDouble(heightText); // Parse height input

            // Store the new height
            rowHeights.put(rowIndex, newHeight);

            // Refresh the display

            // Store the currently selected cell coordinates
            int selectedRowTemp = selectedRow;
            int selectedColTemp = selectedCol;

            displaySheetInGridPane();


            // Reselect the cell using handleCellClick if a valid cell was previously selected
            if (selectedRowTemp >= 0 && selectedColTemp >= 0) {
                Label cellLabel = getCellLabelAt(selectedRowTemp, selectedColTemp);
                if (cellLabel != null) {
                    handleCellClick(selectedRowTemp, selectedColTemp, cellLabel);
                }
            }

            System.out.println("Updated height of row " + rowIndex + " to " + newHeight);
        } catch (NumberFormatException e) {
            showErrorMessage("Invalid height input.");
        }
    }
    // Retrieves the Label for a specific cell in the GridPane by row and column index
    private Label getCellLabelAt(int row, int col) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                int nodeRow = GridPane.getRowIndex(node);
                int nodeCol = GridPane.getColumnIndex(node);

                if (nodeRow == row + 1 && nodeCol == col + 1 && node instanceof Label) {
                    return (Label) node;
                }
            }
        }
        return null;
    }


    private Node getNodeFromGridPane(GridPane gridPane, int row, int col) {
        for (Node node : gridPane.getChildren()) {
            Integer rowIndex = GridPane.getRowIndex(node);
            Integer colIndex = GridPane.getColumnIndex(node);
            if (rowIndex == null) rowIndex = 0;
            if (colIndex == null) colIndex = 0;
            if (rowIndex == row && colIndex == col) {
                return node;
            }
        }
        return null;
    }


    @FXML
    private void handleUpdateColumnAlignment() {
        String columnLetter = columnLetterField.getText().toUpperCase();
        String selectedAlignment = alignmentComboBox.getValue();  // Get selected value from ComboBox

        if (selectedAlignment == null) {
            System.out.println("Please select an alignment option.");
            return;
        }

        if (!isColumnValid(columnLetter, sheet.getNumColumns())) {
            showErrorMessage("Error: Invalid column letter. Please enter a valid column (A to " + (char) ('A' + sheet.getNumColumns() - 1) + ").");
            return;
        }

        // Determine the correct alignment style based on the selected alignment
        String alignmentStyle = "";
        switch (selectedAlignment) {
            case "Left":
                alignmentStyle = "-fx-alignment: center-left;";
                break;
            case "Center":
                alignmentStyle = "-fx-alignment: center;";
                break;
            case "Right":
                alignmentStyle = "-fx-alignment: center-right;";
                break;
            default:
                System.out.println("Invalid alignment selected.");
                return;
        }

        try {
            int columnIndex = columnLetter.charAt(0) - 'A';  // Convert column letter to index

            // Store the alignment style for the specific column
            columnAlignment.put(columnIndex, alignmentStyle);

            // Store the currently selected cell coordinates
            int selectedRowTemp = selectedRow;
            int selectedColTemp = selectedCol;

            displaySheetInGridPane();


            // Reselect the cell using handleCellClick if a valid cell was previously selected
            if (selectedRowTemp >= 0 && selectedColTemp >= 0) {
                Label cellLabel = getCellLabelAt(selectedRowTemp, selectedColTemp);
                if (cellLabel != null) {
                    handleCellClick(selectedRowTemp, selectedColTemp, cellLabel);
                }
            }

            System.out.println("Updated alignment of column " + columnLetter + " to " + selectedAlignment);
        } catch (Exception e) {
            System.out.println("Invalid column input or error updating alignment.");
        }
    }


    private String getColumnAlignment(int colIndex) {
        return columnAlignment.getOrDefault(colIndex, "-fx-alignment: center;");
    }

////    private void resetAll() {
////        // Reset alignment map, width, and height
////        columnAlignment.clear();
////        columnWidths.clear();
////        rowHeights.clear();
////
////        // Reset UI elements
////        selectedCellIdField.clear();
////        originalCellValueField.clear();
////        cellVersionField.clear();
////        alignmentComboBox.setValue(null);  // Clear alignment selection
////        textColorPicker.setValue(Color.BLACK);  // Reset to default text color
////        backgroundColorPicker.setValue(Color.WHITE);  // Reset to default background color
////
////        // Clear the grid pane
////        gridPane.getChildren().clear();
////
////        // Disable controls that depend on a selected cell
////        updateValueButton.setDisable(true);
////        cellUpdateControls.setDisable(true);
////        rangeControlPanel.setDisable(true);
////        leftPane.setDisable(true);
////
////        // Reset column and row input fields
////        columnLetterField.clear();  // Clear column letter input
////        columnWidthField.clear();   // Clear column width input
////        rowNumberField.clear();     // Clear row number input
////        rowHeightField.clear();     // Clear row height input
////    }
////
////
    private void highlightRelatedCells(CellDto selectedCell) {
        // Reset all cell backgrounds to white before highlighting

        resetCellBackgrounds();

        // Highlight cells that this cell depends on (background: light blue)
        List<CellDto> dependsOnCells = selectedCell.getDependsOn();
        if (dependsOnCells != null) {
            for (CellDto cell : dependsOnCells) {
                int row = cell.getRow();
                int col = cell.getCol();
                Label cellLabel = (Label) getNodeFromGridPane(gridPane, row + 1, col + 1);
                if (cellLabel != null) {
                    cellLabel.setStyle(cellLabel.getStyle() + "-fx-background-color: lightblue;");
                }
            }
        }

        // Highlight cells that this cell influences (background: light green)
        List<CellDto> influencingOnCells = selectedCell.getInfluencingOn();
        if (influencingOnCells != null) {
            for (CellDto cell : influencingOnCells) {
                int row = cell.getRow();
                int col = cell.getCol();
                Label cellLabel = (Label) getNodeFromGridPane(gridPane, row + 1, col + 1);
                if (cellLabel != null) {
                    cellLabel.setStyle(cellLabel.getStyle() + "-fx-background-color: lightgreen;");
                }
            }
        }
    }
//
//    @FXML
//    private void handleViewRange() {
//        String rangeName = rangeNameField.getText();  // Get the range name from the text field
//
//        // Check if the range exists in the sheet
//        Range range = sheet.getRange(rangeName);
//        if (range == null) {
//            showErrorMessage("Range not found: " + rangeName);
//            return;
//        }
//
//        // Reset any previous highlights
//        resetCellBackgrounds();
//
//        // Highlight all cells in the selected range
//        List<Coordinate> cellsInRange = range.getAllCellsInRange();
//        for (Coordinate coordinate : cellsInRange) {
//            int row = coordinate.getRow();
//            int col = coordinate.getColumn();
//            Label cellLabel = (Label) getNodeFromGridPane(gridPane, row + 1, col + 1);
//            if (cellLabel != null) {
//                // Highlight the cell with a yellow background
//                cellLabel.setStyle(cellLabel.getStyle() + "-fx-background-color: yellow;");
//            }
//        }
//    }

    private void updateVersionChoiceBox() {
        if (sheet != null) {

            // נקה את האפשרויות הקודמות
            versionChoiceBox.getItems().clear();


            // הוספת גרסאות עם היסט של 1 (כך ש-ver 2 תוצג כ-ver 1), מבלי להציג גרסה 0
            for (int i=1;i<=sheet.getVersion();i++)
            {
                versionChoiceBox.getItems().add(i);  // הפחתת 1 מכל גרסה להצגה
            }
            versionChoiceBox.setValue(sheet.getVersion());

        } else {
            System.out.println("Sheet is not initialized!");
        }
    }

////    // בחירת גרסה להצגה
////    @FXML
////    private void handleVersionChange() {
////        if (sheet != null) {
////            Integer selectedVersion = versionChoiceBox.getValue();
////            if (selectedVersion != null) {
////                // בדוק אם הגרסה שנבחרה היא הגרסה הנוכחית
////                int currentVersion = sheet.getVersionManager().getCurrentVersion() - 1;  // עדכון -1, בהתאם להיסט שלך
////                if (selectedVersion == currentVersion) {
////                    // אם הגרסה הנוכחית נבחרה, אל תעשה כלום
////                    return;
////                }
////
////                // המשך הצגת הגרסה אם היא שונה מהגרסה הנוכחית
////                Map<Coordinate, Cell> versionSnapshot = sheet.getVersionManager().getVersion(selectedVersion);
////                if (versionSnapshot != null) {
////                    displayVersionInNewWindow(versionSnapshot, selectedVersion);  // הצגת הגרסה בחלון נפרד
////
////                    // עדכן את ה-ChoiceBox כדי להחזיר אותו לגרסה הנוכחית (האחרונה)
////                    versionChoiceBox.setValue(currentVersion);  // הגרסה האחרונה היא הנוכחית
////                } else {
////                    System.out.println("Version not found");
////                }
////            }
////        } else {
////            System.out.println("Sheet is not initialized!");
////        }
////    }
////
////
////    private void displayVersionInNewWindow(Map<Coordinate, Cell> versionCells, int versionNumber) {
////        Stage versionStage = new Stage();
////        versionStage.setTitle("Version View - V" + versionNumber);
////
////        GridPane versionGridPane = new GridPane();
////        versionGridPane.setGridLinesVisible(true);
////
////        // הצגת כותרות העמודות (A, B, C, ...)
////        for (int col = 0; col < sheet.getNumCols(); col++) {
////            String columnLetter = String.valueOf((char) ('A' + col));
////            Label headerLabel = new Label(columnLetter);
////            double columnWidth = getColumnWidth(col);
////            headerLabel.setMinWidth(columnWidth);
////            headerLabel.setPrefWidth(columnWidth);
////            headerLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;");
////            versionGridPane.add(headerLabel, col + 1, 0);
////        }
////
////        // הצגת כותרות השורות (1, 2, 3, ...)
////        for (int row = 0; row < sheet.getNumRows(); row++) {
////            Label rowLabel = new Label(Integer.toString(row + 1));
////            double rowHeight = getRowHeight(row);
////            rowLabel.setMinHeight(rowHeight);
////            rowLabel.setPrefHeight(rowHeight);
////            rowLabel.setMinWidth(30);
////            rowLabel.setPrefWidth(30);
////            rowLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;");
////            versionGridPane.add(rowLabel, 0, row + 1);
////        }
////
////        // הצגת כל התאים מהגרסה
////        for (Map.Entry<Coordinate, Cell> entry : versionCells.entrySet()) {
////            Coordinate coord = entry.getKey();
////            Cell cell = entry.getValue();
////
////            String effectiveValue = cell.getEffectiveValue() != null ? cell.getEffectiveValue().toString() : "";
////            Label cellLabel = new Label(effectiveValue);
////
////            double columnWidth = getColumnWidth(coord.getColumn());
////            double rowHeight = getRowHeight(coord.getRow());
////
////            cellLabel.setMinWidth(columnWidth);
////            cellLabel.setPrefWidth(columnWidth);
////            cellLabel.setMinHeight(rowHeight);
////            cellLabel.setPrefHeight(rowHeight);
////
////            cellLabel.setStyle("-fx-border-color: black; -fx-border-width: 0.5px; -fx-alignment: center;");
////            versionGridPane.add(cellLabel, coord.getColumn() + 1, coord.getRow() + 1);
////        }
////
////        Button closeButton = new Button("Close");
////        closeButton.setOnAction(event -> versionStage.close());
////
////
////        // שינוי גודל הכפתור - פי 2
////        closeButton.setMinWidth(100);  // הגדל רוחב
////        closeButton.setMinHeight(50);  // הגדל גובה
////        closeButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר
////
////
////        // עטיפת הגריד ב-VBox עם גבול צבעוני
////        VBox tableLayout = new VBox(versionGridPane);
////        tableLayout.setStyle("-fx-background-color: #edd5d5; -fx-border-color: #f1b7b7; -fx-border-width: 5;"); // מסגרת סביב הטבלה בלבד
////        tableLayout.setSpacing(10);
////
////        // הצבת הטבלה והכפתור יחד, אך בלי לצבוע את ה-VBox החיצוני
////        VBox layout = new VBox(tableLayout, closeButton);
////        layout.setSpacing(10); // רווח בין הטבלה לכפתור ה-close
////
////        ScrollPane scrollPane = new ScrollPane(layout);
////        Scene scene = new Scene(scrollPane, 600, 400);
////        versionStage.setScene(scene);
////        versionStage.show();
////    }
////

    @FXML
    private void handleSort() {
        // Create a new window for sorting
        Stage sortStage = new Stage();
        sortStage.setTitle("Sort Data");

        // Create the form for user input (range and columns to sort)
        VBox sortLayout = new VBox(10);

        // Input fields for range
        TextField rangeField = new TextField();
        rangeField.setPromptText("Enter range (e.g., A3..V9)");

        // Input field for columns to sort by
        TextField columnsField = new TextField();
        columnsField.setPromptText("Enter columns to sort by (e.g., A, B, C)");

        // Sort button
        Button sortButton = new Button("Sort");
        sortButton.setOnAction(e -> {
            String range = rangeField.getText().toUpperCase();
            String columns = columnsField.getText().toUpperCase();
            sortTable(range, columns); // Method to perform sorting
            sortStage.close();
        });


        // שינוי גודל הכפתור - פי 2
        sortButton.setMinWidth(80);  // הגדל רוחב
        sortButton.setMinHeight(40);  // הגדל גובה
        sortButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> sortStage.close());

        // After the window opens, set the focus to another element (like the close button)
        Platform.runLater(() -> sortButton.requestFocus());

        // שינוי גודל הכפתור - פי 2
        closeButton.setMinWidth(80);  // הגדל רוחב
        closeButton.setMinHeight(40);  // הגדל גובה
        closeButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר

        sortLayout.getChildren().addAll(new Label("Set Details:"), rangeField, columnsField, sortButton, closeButton);
        Scene sortScene = new Scene(sortLayout, 300, 200);
        sortStage.setScene(sortScene);
        sortStage.show();
    }

    // Method to perform sorting based on range and multiple columns
    private void sortTable(String range, String columns) {
        // Parse the range (e.g., "A3..V9")
        String[] rangeParts = range.split("\\.\\.");
        if (rangeParts.length != 2) {
            showErrorMessage("Invalid range format.");
            return;
        }

        // Extract top-left and bottom-right indices from the range
        int[] topLeft = parseCell(rangeParts[0]);
        int[] bottomRight = parseCell(rangeParts[1]);

        // Parse the columns to sort by (e.g., "C, D, A")
        String[] columnLetters = columns.split(",");
        List<Integer> columnIndices = new ArrayList<>();
        for (String column : columnLetters) {
            column = column.trim();  // Remove any spaces around the column letters
            int columnIndex = column.charAt(0) - 'A'; // Convert column letter to index (A = 0, B = 1, ...)

            // Adjust for the starting column in the selected range
            columnIndex = columnIndex - topLeft[1]; // Make sure the column index is relative to the selected range
            columnIndices.add(columnIndex);
        }

        // Extract the rows from the specified range
        List<List<String>> rowsInRange = extractRowsInRange(topLeft[0], bottomRight[0], topLeft[1], bottomRight[1]);

        // Sort the rows by the specified columns
        rowsInRange.sort((row1, row2) -> {
            for (Integer columnIndex : columnIndices) {
                // Make sure we handle out-of-bounds issues properly
                if (columnIndex >= 0 && columnIndex < row1.size()) {
                    String value1 = row1.get(columnIndex);
                    String value2 = row2.get(columnIndex);

                    // Try to compare as numbers first
                    try {
                        Double num1 = Double.valueOf(value1);
                        Double num2 = Double.valueOf(value2);
                        int compare = num1.compareTo(num2);
                        if (compare != 0) {
                            return compare;  // If values are not equal, return the comparison result
                        }
                    } catch (NumberFormatException e) {
                        // If not a number, skip this column and continue to the next
                        continue;
                    }
                }
            }
            return 0; // If all compared values are equal or non-numeric, keep the original order
        });

        // Display the sorted rows in a new window
        displaySortedTable(rowsInRange, topLeft[0], topLeft[1]);
    }

    // Method to extract rows from a given range of indices
    private List<List<String>> extractRowsInRange(int startRow, int endRow, int startCol, int endCol) {
        List<List<String>> rows = new ArrayList<>();

        for (int row = startRow; row <= endRow; row++) {
            List<String> rowData = new ArrayList<>();
            for (int col = startCol; col <= endCol; col++) {
                // Retrieve the cell value (assuming your sheet implementation provides a method for this)



                String cellValue = sheet.getCell(row, col);
                rowData.add(cellValue);
            }
            rows.add(rowData);
        }

        return rows;
    }

    private void displaySortedTable(List<List<String>> sortedRows, int startRow, int startCol) {
        Stage sortedStage = new Stage();  // יצירת חלון חדש להצגת הטבלה הממוינת
        sortedStage.setTitle("Sorted Table View");

        GridPane sortedGridPane = new GridPane();  // יצירת גריד להצגת הטבלה
        sortedGridPane.setGridLinesVisible(true);  // הצגת קווי גריד

        // הצגת כותרות העמודות (A, B, C, ...)
        for (int col = 0; col < sheet.getNumColumns(); col++) {
            String columnLetter = String.valueOf((char) ('A' + col));  // המרת אינדקס לעמודה כאות
            Label headerLabel = new Label(columnLetter);
            double columnWidth = sheet.getColumnsWidth() * 2;  // Default width of 100.0 if not set

             // קביעת רוחב עמודה
            headerLabel.setMinWidth(columnWidth);
            headerLabel.setPrefWidth(columnWidth);
            headerLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;");
            sortedGridPane.add(headerLabel, col + 1, 0);  // הצבת הכותרת בגריד
        }

        // הצגת כותרות השורות (1, 2, 3, ...)
        for (int row = 0; row < sheet.getNumRows(); row++) {
            Label rowLabel = new Label(Integer.toString(row + 1));  // מספור השורות
            double rowHeight = sheet.getRowsHeight() * 2;  // Retrieve user-defined or default height

            rowLabel.setMinHeight(rowHeight);
            rowLabel.setPrefHeight(rowHeight);
            rowLabel.setMinWidth(30);
            rowLabel.setPrefWidth(30);
            rowLabel.setStyle("-fx-alignment: center; -fx-border-color: black; -fx-border-width: 0.5px;");
            sortedGridPane.add(rowLabel, 0, row + 1);  // הצבת מספר השורה בגריד
        }

        // הצגת כל התאים מהגיליון המקורי
        for (int row = 0; row < sheet.getNumRows(); row++) {
            for (int col = 0; col < sheet.getNumColumns(); col++) {
                String cellValue;

                // בדיקה אם התא נמצא בטווח הממויין
                if (row >= startRow && row < startRow + sortedRows.size() && col >= startCol && col < startCol + sortedRows.getFirst().size()) {
                    // במקרה שהתא נמצא בטווח, נציג את הערך הממויין
                    cellValue = sortedRows.get(row - startRow).get(col - startCol);
                } else {
                    // במקרה שהתא מחוץ לטווח, נציג את הערך המקורי
                    cellValue = sheet.getCell(row, col);
                }

                Label cellLabel = new Label(cellValue);  // יצירת תווית עבור כל תא
                // Apply user-defined column width and row height for each cell

                double columnWidth =  sheet.getColumnsWidth() * 2;
                double rowHeight =  sheet.getRowsHeight() * 2;


                cellLabel.setMinWidth(columnWidth);
                cellLabel.setPrefWidth(columnWidth);
                cellLabel.setMinHeight(rowHeight);
                cellLabel.setPrefHeight(rowHeight);


                cellLabel.setStyle("-fx-border-color: black; -fx-border-width: 0.5px; -fx-alignment: center;");
                sortedGridPane.add(cellLabel, col + 1, row + 1);  // הצבת התא בגריד
            }
        }

        // כפתור לסגירת החלון
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> sortedStage.close());

        // הגדלת הכפתור
        closeButton.setMinWidth(80);
        closeButton.setMinHeight(40);
        closeButton.setStyle("-fx-font-size: 16px;");

        // עטיפת הגריד ב-VBox עם גבול צבעוני
        VBox tableLayout = new VBox(sortedGridPane);
        tableLayout.setStyle("-fx-background-color: #ede9d5; -fx-border-color: #e4d897; -fx-border-width: 5;");  // הוספת צבע למסגרת
        tableLayout.setSpacing(10);

        // הצבת הטבלה והכפתור יחד ב-VBox החיצוני
        VBox layout = new VBox(tableLayout, closeButton);
        layout.setSpacing(10);  // רווח בין הטבלה לכפתור

        ScrollPane scrollPane = new ScrollPane(layout);
        Scene scene = new Scene(scrollPane, 600, 400);  // קביעת גודל חלון התצוגה
        sortedStage.setScene(scene);
        sortedStage.show();  // הצגת החלון
    }


// Helper function to parse a cell reference (e.g., "A1") into row and column indices
private int[] parseCell(String cell) {
    StringBuilder columnBuilder = new StringBuilder();
    StringBuilder rowBuilder = new StringBuilder();

    for (char ch : cell.toCharArray()) {
        if (Character.isLetter(ch)) {
            columnBuilder.append(ch); // Extract the column part (e.g., "A", "B")
        } else if (Character.isDigit(ch)) {
            rowBuilder.append(ch); // Extract the row part (e.g., "1", "2")
        }
    }

    // Convert column letters to a zero-based index (A = 0, B = 1, ...)
    String columnString = columnBuilder.toString().toUpperCase();
    int columnIndex = 0;
    for (int i = 0; i < columnString.length(); i++) {
        columnIndex = columnIndex * 26 + (columnString.charAt(i) - 'A' + 1);
    }
    columnIndex--; // Convert to zero-based index

    // Convert row part to a zero-based index
    int rowIndex = Integer.parseInt(rowBuilder.toString()) - 1;

    return new int[]{rowIndex, columnIndex}; // Return [row, column]
}



    @FXML
    private void handleFilter() {
        // יצירת חלון חדש לסינון
        Stage filterStage = new Stage();
        filterStage.setTitle("Filter Data");

        // יצירת פריסת טופס להזנת המידע מהמשתמש
        VBox filterLayout = new VBox(10);

        // שדה להזנת הטווח לסינון
        TextField rangeField = new TextField();
        rangeField.setPromptText("Enter range (e.g., A3..V9)");

        // שדה להזנת העמודה לסינון
        TextField columnField = new TextField();
        columnField.setPromptText("Enter column to filter by (e.g., C)");

        // VBox עבור הצגת הערכים הייחודיים כ-Checkboxes
        VBox checkBoxLayout = new VBox(10);

        // עטיפת תיבות הסימון ב-ScrollPane כדי לאפשר גלילה
        ScrollPane scrollPane = new ScrollPane(checkBoxLayout);
        scrollPane.setFitToWidth(true);  // התאמת רוחב הסקרולר לתוכן
        scrollPane.setPrefHeight(100);   // קביעת גובה מקסימלי

        // כפתור להצגת הערכים הייחודיים בעמודה שנבחרה
        Button showUniqueValuesButton = new Button("Show Unique Values");

        showUniqueValuesButton.setOnAction(e -> {
            String range = rangeField.getText().toUpperCase();
            String column = columnField.getText().toUpperCase();
            List<String> uniqueValues = getUniqueValues(range, column);  // פונקציה שתאתר את הערכים הייחודיים
            checkBoxLayout.getChildren().clear();  // ניקוי כל ה-CheckBox מהפעם הקודמת

            // יצירת CheckBox לכל ערך ייחודי (ללא ערכים ריקים)
            uniqueValues.stream()
                    .filter(value -> !value.trim().isEmpty())  // סינון ערכים ריקים
                    .forEach(value -> {
                        CheckBox checkBox = new CheckBox(value);
                        checkBoxLayout.getChildren().add(checkBox);
                    });
        });


        // כפתור לביצוע הסינון
        Button filterButton = new Button("Filter");
        filterButton.setOnAction(e -> {
            String range = rangeField.getText().toUpperCase();
            String column = columnField.getText().toUpperCase();

            // אסיפת הערכים שנבחרו על ידי המשתמש
            List<String> selectedValues = checkBoxLayout.getChildren().stream()
                    .filter(node -> node instanceof CheckBox && ((CheckBox) node).isSelected())
                    .map(node -> ((CheckBox) node).getText())
                    .collect(Collectors.toList());

            filterTableByValues(range, column, selectedValues);  // מתודה שתבצע את הסינון לפי הערכים שנבחרו
            filterStage.close();
        });

        // שינוי גודל הכפתור - פי 2
        filterButton.setMinWidth(80);  // הגדל רוחב
        filterButton.setMinHeight(40);  // הגדל גובה
        filterButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר

        // כפתור לסגירת החלון
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> filterStage.close());

        // שינוי גודל הכפתור - פי 2
        closeButton.setMinWidth(80);  // הגדל רוחב
        closeButton.setMinHeight(40);  // הגדל גובה
        closeButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר

        // After the window opens, set the focus to another element (like the close button)
        Platform.runLater(filterButton::requestFocus);

        // הוספת הרכיבים לפריסה
        filterLayout.getChildren().addAll(
                new Label("Set Details:"),
                rangeField,
                columnField,
                showUniqueValuesButton,
                new Label("Select values to filter by:"),
                scrollPane,  // הצגת תיבות הסימון בתוך סקרולר
                filterButton,
                closeButton
        );

        Scene filterScene = new Scene(filterLayout, 350, 400);  // קביעת גודל חלון כולל
        filterStage.setScene(filterScene);
        filterStage.show();
    }


    // Method to find unique values in the selected column and range
    private List<String> getUniqueValues(String range, String column) {
        // Parse the range (e.g., "A3..V9")
        String[] rangeParts = range.split("\\.\\.");
        if (rangeParts.length != 2) {
            showErrorMessage("Invalid range format.");
            return Collections.emptyList();
        }

        // Extract top-left and bottom-right indices from the range
        int[] topLeft = parseCell(rangeParts[0]);
        int[] bottomRight = parseCell(rangeParts[1]);

        // Parse the column to filter by (e.g., "C")
        int columnIndex = column.trim().charAt(0) - 'A'; // Convert column letter to index (A = 0, B = 1, C = 2, ...)

        // Extract the rows from the specified range
        List<List<String>> rowsInRange = extractRowsInRange(topLeft[0], bottomRight[0], topLeft[1], bottomRight[1]);

        // Collect unique values in the specified column
        Set<String> uniqueValuesSet = new HashSet<>();
        for (List<String> row : rowsInRange) {
            String value = row.get(columnIndex - topLeft[1]);
            if (!value.trim().isEmpty()) {  // סינון ערכים ריקים
                uniqueValuesSet.add(value);
            }
        }


        return new ArrayList<>(uniqueValuesSet);  // Return the unique values as a list
    }

    // Method to filter the table by the selected values
    private void filterTableByValues(String range, String column, List<String> selectedValues) {
        // Parse the range (e.g., "A3..V9")
        String[] rangeParts = range.split("\\.\\.");
        if (rangeParts.length != 2) {
            System.out.println("Invalid range format.");
            return;
        }

        // Extract top-left and bottom-right indices from the range
        int[] topLeft = parseCell(rangeParts[0]);
        int[] bottomRight = parseCell(rangeParts[1]);

        // Parse the column to filter by (e.g., "C")
        int columnIndex = column.trim().charAt(0) - 'A'; // Convert column letter to index (A = 0, B = 1, C = 2, ...)

        // Extract the rows from the specified range
        List<List<String>> rowsInRange = extractRowsInRange(topLeft[0], bottomRight[0], topLeft[1], bottomRight[1]);

        // Filter the rows based on the selected values
        List<List<String>> filteredRows = rowsInRange.stream()
                .filter(row -> selectedValues.contains(row.get(columnIndex - topLeft[1])))  // Filter by values in the selected column
                .collect(Collectors.toList());

        // Display the filtered rows in a new window
        displayFilteredTable(filteredRows);
    }

    // Method to display filtered rows in a new window
    private void displayFilteredTable(List<List<String>> filteredRows) {
        Stage filteredStage = new Stage();
        filteredStage.setTitle("Filtered Table View");

        GridPane filteredGridPane = new GridPane();
        filteredGridPane.setGridLinesVisible(true);

        // הצגת השורות המסוננות
        for (int row = 0; row < filteredRows.size(); row++) {
            List<String> rowData = filteredRows.get(row);
            for (int col = 0; col < rowData.size(); col++) {
                String cellValue = rowData.get(col);
                Label cellLabel = new Label(cellValue);

                // Apply user-defined column width and row height for each cell

                double columnWidth =  sheet.getColumnsWidth() * 2;
                double rowHeight =  sheet.getRowsHeight() * 2;
                cellLabel.setMinWidth(columnWidth);
                cellLabel.setPrefWidth(columnWidth);
                cellLabel.setMinHeight(rowHeight);
                cellLabel.setPrefHeight(rowHeight);


                cellLabel.setStyle("-fx-border-color: black; -fx-border-width: 0.5px; -fx-alignment: center;");
                filteredGridPane.add(cellLabel, col + 1, row + 1);
            }
        }

        // כפתור לסגירת החלון
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> filteredStage.close());

        // הגדלת הכפתור
        // שינוי גודל הכפתור - פי 2
        closeButton.setMinWidth(80);  // הגדל רוחב
        closeButton.setMinHeight(40);  // הגדל גובה
        closeButton.setStyle("-fx-font-size: 16px;");  // גודל גופן גדול יותר

        // עטיפת הגריד ב-VBox עם גבול צבעוני
        VBox tableLayout = new VBox(filteredGridPane);
        tableLayout.setStyle("-fx-background-color: #d5ead5; -fx-border-color: #bef6ba; -fx-border-width: 5;");  // הוספת צבע למסגרת
        tableLayout.setSpacing(10);

        // הצבת הטבלה והכפתור יחד ב-VBox החיצוני
        VBox layout = new VBox(tableLayout, closeButton);
        layout.setSpacing(10);  // רווח בין הטבלה לכפתור


        ScrollPane scrollPane = new ScrollPane(layout);

        Scene scene = new Scene(scrollPane, 600, 400);
        filteredStage.setScene(scene);
        filteredStage.show();
    }

    private boolean isColumnValid(String columnLetter, int maxCols) {
        if (columnLetter.length() != 1 || !Character.isLetter(columnLetter.charAt(0))) {
            return false;  // העמודה לא תקינה אם לא מדובר באות בודדת
        }

        int columnIndex = columnLetter.toUpperCase().charAt(0) - 'A';
        return columnIndex >= 0 && columnIndex < maxCols;  // בדוק שהעמודה בטווח
    }

    private boolean isRowValid(int rowNumber, int maxRows) {
        return rowNumber >= 1 && rowNumber < maxRows;  // מספר השורה צריך להיות בין 1 למס השורות
    }

    public void handleViewRange(ActionEvent actionEvent) {
    }
}