/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import controller.LoginController;
import java.io.IOException;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import model.Answer;
import model.NavDAOException;
import model.Navigation;
import model.Problem;
import model.Session;
import model.User;
import model.UserSession;
import poiupv.Poi;

/**
 *
 * @author jsoler
 */
public class MainViewController implements Initializable {

    // =======================================
    // hashmap para guardar los puntos de interes POI
    private final HashMap<String, Poi> hm = new HashMap<>();
    private ObservableList<Poi> data;
    // ======================================
    // la variable zoomGroup se utiliza para dar soporte al zoom
    // el escalado se realiza sobre este nodo, al escalar el Group no mueve sus
    // nodos
    private Group zoomGroup;

    private ListView<Poi> map_listview;
    @FXML
    private ScrollPane map_scrollpane;
    @FXML
    private Slider zoom_slider;
    @FXML
    private MenuButton map_pin;
    @FXML
    private MenuItem pin_info;
    @FXML
    private Label mousePosition;
    @FXML
    private Label questionLabel, usernameMain;
    @FXML
    private ToggleGroup toolBarGroup;
    @FXML
    private Pane paneMap;
    @FXML
    private ImageView pfpMain;
    @FXML
    private ListView<String> answerListView;
    
    @FXML
    private ImageView toolField;

    // Add near the top of your class with other fields
    private String currentTool = "move"; // Default tool

    private int hits, misses;
    @FXML
    private StackPane stackPane;
    @FXML
    private MenuButton ruler;
    private MenuButton rulerMenu;
    @FXML
    private ToggleButton rulerToggleButton;
    @FXML
    private MenuButton protractor;
    @FXML
    private ToggleButton protractorToggleButton;
    
    private Color colorChosen = Color.BLACK;
    private int chosenLineThickness = 2;
    @FXML
    private TitledPane exercisesTitledPane;
    @FXML
    private HBox exercisesHbox;
    
    @FXML
    private MenuItem thin;
    
    @FXML 
    private MenuItem medium;
    
    @FXML
    private MenuItem fat;

    @FXML
    void zoomIn(ActionEvent event) {
        // ================================================
        // el incremento del zoom dependerá de los parametros del
        // slider y del resultado esperado
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal += 0.1);
    }

    @FXML
    void zoomOut(ActionEvent event) {
        double sliderVal = zoom_slider.getValue();
        zoom_slider.setValue(sliderVal + -0.1);
    }

    // esta funcion es invocada al cambiar el value del slider zoom_slider
    private void zoom(double scaleValue) {
        // ===================================================
        // guardamos los valores del scroll antes del escalado
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();
        // ===================================================
        // escalamos el zoomGroup en X e Y con el valor de entrada
        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);
        // ===================================================
        // recuperamos el valor del scroll antes del escalado
        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        // ==========================================================
        // inicializamos el slider y enlazamos con el zoom
        zoom_slider.setMin(0.2);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(1.0);
        zoom_slider.valueProperty().addListener((o, oldVal, newVal) -> zoom((Double) newVal));

        // =========================================================================
        // Envuelva el contenido de scrollpane en un grupo para que
        // ScrollPane vuelva a calcular las barras de desplazamiento tras el escalado
        Group contentGroup = new Group();
        zoomGroup = new Group();
        contentGroup.getChildren().add(zoomGroup);
        zoomGroup.getChildren().add(map_scrollpane.getContent());
        map_scrollpane.setContent(contentGroup);
        
        stackPane.getChildren().add(contentGroup);

        

        // Set default tool
        currentTool = "move";
        map_scrollpane.setPannable(true);

        questionLabel.setWrapText(true);
        questionLabel.setMaxWidth(200);

        usernameMain.setText(UserSession.getInstance().getCurrentUser().getNickName());
        pfpMain.setImage(UserSession.getInstance().getCurrentUser().getAvatar());
        
        // To make the titledPane rotate and minimize
        exercisesTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                System.out.println("TitledPane minimized!");

                exercisesTitledPane.setRotate(90);
                
                exercisesTitledPane.setMinSize(0, 0);
                exercisesTitledPane.setPrefSize(0, 0);
                exercisesTitledPane.setMaxSize(0, 0);
                exercisesHbox.setMinSize(0, 0);
                exercisesHbox.setPrefSize(0, 0);
                exercisesHbox.setMaxSize(0, 0);
  
                

                // Optionally, override CSS defaults.
                exercisesTitledPane.setStyle("-fx-min-width: 80; -fx-min-height: 10;");
                exercisesHbox.setStyle("-fx-min-width: 40; -fx-min-height: 1000;");
                

                exercisesTitledPane.setTranslateY(40);
            }else{
                exercisesTitledPane.setTranslateY(0);
                
                exercisesTitledPane.setStyle(null);
                exercisesHbox.setStyle(null);
                
                exercisesTitledPane.setRotate(360);
                exercisesTitledPane.setPrefWidth(250);
                exercisesTitledPane.setMaxWidth(250);
                exercisesTitledPane.setPrefHeight(1000);
                exercisesTitledPane.setMaxHeight(1000);
                
                exercisesHbox.setPrefWidth(250);
                exercisesHbox.setMaxWidth(250);
                
                exercisesTitledPane.setStyle("-fx-min-width: 250; -fx-min-height: 1000;");
                exercisesHbox.setStyle("-fx-min-width: 250; -fx-min-height: 1000;");

            }
        });
        
        
      
        // to asking to exiting
        stackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((windowObs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(event -> {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

                            //Adding the style sheet
                            DialogPane dialogPane = alert.getDialogPane();
                            dialogPane.getStylesheets().add(
                            getClass().getResource("/css/alertStyle.css").toExternalForm());

                            //Setting the css class .myAlert to the alert dialog
                            alert.getDialogPane().getStyleClass().add("myAlert"); 

                            alert.setTitle("Confirmation Dialog");
                            alert.setHeaderText("You are going to leave the APP");
                            alert.setContentText("Do you want to exit?");
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK){
                                // Save session before exit
                                try {
                                    handleSaveSession(new ActionEvent());
                                } catch (NavDAOException e) {
                                    System.err.println("Error saving session during exit: " + e.getMessage());
                                    // Continue with exit even if session save fails
                                }
                                stackPane.getScene().getWindow().hide();
                            } else {
                                event.consume();
                            }
                        });
                    }
                });
            }
        });
    }

    /*
     * Unused methods
     * private void exitAfterInitialize(){
     * Platform.runLater(() -> {
     * Stage stage = (Stage) zoom_slider.getScene().getWindow();
     * stage.close();
     * });
     * }
     * private boolean openLoginWindow() throws IOException {
     * FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("/view/Login.fxml"));
     * Parent root = loader.load();
     * LoginController controller = loader.getController();
     * 
     * Scene scene = new Scene(root);
     * scene.getStylesheets().add(getClass().getResource("/css/loginStyle.css").
     * toExternalForm());
     * 
     * Stage stage = new Stage();
     * stage.setScene(scene);
     * stage.setTitle("login");
     * stage.initModality(Modality.APPLICATION_MODAL);
     * 
     * // enabling closing the window pressing the escape button
     * scene.setOnKeyPressed(e -> {
     * if(e.getCode() == KeyCode.ESCAPE){
     * stage.close();
     * }
     * });
     * // enablig login pressing enter
     * scene.setOnKeyPressed(e -> {
     * if(e.getCode() == KeyCode.ENTER){
     * try {
     * controller.loginButtonClicked(new ActionEvent());
     * } catch (NavDAOException ex) {
     * Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null,
     * ex);
     * }
     * }
     * });
     * 
     * stage.showAndWait();
     * 
     * return controller.LoginSuccessfull;
    }
*/
    
    

   
    private void closeApp(ActionEvent event) {
        ((Stage) zoom_slider.getScene().getWindow()).close();
    }
    private double normalizeXPosition(double x){
        return max(min(x, paneMap.getWidth()),0);
    }
    private double normalizeYPosition(double y){
        return max(min(y, paneMap.getHeight()),0);
    }
    
    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText("sceneX: " + (int) event.getSceneX() + ", sceneY: " + (int) event.getSceneY() + "\n"
                + "         X: " + (int) event.getX() + ",          Y: " + (int) event.getY());
    }

    @FXML
    private void about(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);
        // Acceder al Stage del Dialog y cambiar el icono
        Stage dialogStage = (Stage) mensaje.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        mensaje.setTitle("About");
        mensaje.setHeaderText("IPC Lab Project");
        mensaje.setContentText("This is a project for the IPC subject at UPV.\n"
                + "It is a application to manage POIs.\n"
                + "Developed by: \n"
                + "Abderrahmane Ezzine\n"
                + "Martin Guerrero\n"
                + "Mattia Simoni\n"
                + "Josep Gaya Barrachina\n"
                + "Version 1.0");
        mensaje.showAndWait();
    }

    @FXML
    private void addPoi(MouseEvent event) {

        if (event.isControlDown()) {
            Dialog<Poi> poiDialog = new Dialog<>();
            poiDialog.setTitle("Nuevo POI");
            poiDialog.setHeaderText("Introduce un nuevo POI");
            // Acceder al Stage del Dialog y cambiar el icono
            Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));

            ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
            poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

            TextField nameField = new TextField();
            nameField.setPromptText("Nombre del POI");

            TextArea descArea = new TextArea();
            descArea.setPromptText("Descripción...");
            descArea.setWrapText(true);
            descArea.setPrefRowCount(5);

            VBox vbox = new VBox(10, new Label("Nombre:"), nameField, new Label("Descripción:"), descArea);
            poiDialog.getDialogPane().setContent(vbox);

            poiDialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButton) {
                    return new Poi(nameField.getText().trim(), descArea.getText().trim(), 0, 0);
                }
                return null;
            });
            Optional<Poi> result = poiDialog.showAndWait();

            if (result.isPresent()) {
                Point2D localPoint = zoomGroup.sceneToLocal(event.getSceneX(), event.getSceneY());
                Poi poi = result.get();
                poi.setPosition(localPoint);
                map_listview.getItems().add(poi);
            }
        }
    }

    @FXML
    private void modifyProfile(ActionEvent event) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ModifyProfile.fxml"));
            Parent root = loader.load();

            // Get the controller instance
            ModifyProfileController controller = loader.getController();

            // Create a new scene
            Scene scene = new Scene(root);

            // Create and configure a new stage
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Edit Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            // Make the window draggable
            AtomicReference<Double> xOffset = new AtomicReference<>((double) 0);
            AtomicReference<Double> yOffset = new AtomicReference<>((double) 0);

            root.setOnMousePressed(e -> {
                xOffset.set(e.getSceneX());
                yOffset.set(e.getSceneY());
            });

            root.setOnMouseDragged(e -> {
                stage.setX(e.getScreenX() - xOffset.get());
                stage.setY(e.getScreenY() - yOffset.get());
            });

            // Display the profile window
            stage.showAndWait();
            pfpMain.setImage(UserSession.getInstance().getCurrentUser().getAvatar());

        } catch (Exception e) {
            // Improved error handling with detailed information
            System.err.println("Error opening ModifyProfile: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Opening Profile");
            alert.setHeaderText("Could not open the profile editor");
            alert.setContentText("Error details: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void logout(ActionEvent event) {
        try {
            // Save session before logout
            try {
                handleSaveSession(new ActionEvent());
            } catch (NavDAOException e) {
                System.err.println("Error saving session during logout: " + e.getMessage());
                // Continue with logout even if session save fails
            }

            // Clear the user session
            UserSession.getInstance().logout();

            // Close the current window
            Stage currentStage = (Stage) zoom_slider.getScene().getWindow();
            currentStage.close();

            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/loginStyle.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearToolBarSelection() {
        // Remove all mouse event handlers from zoomGroup
        zoomGroup.setOnMouseClicked(null);
        zoomGroup.setOnMousePressed(null);
        zoomGroup.setOnMouseDragged(null);
        zoomGroup.setOnMouseReleased(null);
        zoomGroup.setOnMouseMoved(null);

        // Add basic handlers for panning when in move mode
        map_scrollpane.setPannable(true);
        paneMap.setCursor(javafx.scene.Cursor.DEFAULT);
    }

    private void updateCursorForTool(String tool) {
        switch (tool) {
            case "move":
                paneMap.setCursor(javafx.scene.Cursor.OPEN_HAND);
                break;
            case "point":
            case "line":
            case "circle":
                paneMap.setCursor(javafx.scene.Cursor.CROSSHAIR);
                break;
            case "text":
                paneMap.setCursor(javafx.scene.Cursor.TEXT);
                break;
            case "ruler":
            case "protractor":
                paneMap.setCursor(javafx.scene.Cursor.HAND);
                break;
            default:
                paneMap.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private void setCurrentTool(String toolName) {
        currentTool = toolName;
        updateCursorForTool(toolName);
        System.out.println("Current tool: " + currentTool); // Helpful for debugging
    }

    @FXML
    private void moveGraph(ActionEvent event) {
        clearToolBarSelection();
        map_scrollpane.setPannable(true);
        setCurrentTool("move");
    }

    private Map<StackPane, Line[]> coordinatesMap = new HashMap<>();
    @FXML
    private void drawPoint(ActionEvent event) {
        clearToolBarSelection();
        if(((ToggleButton)event.getSource()).isSelected()){
            map_scrollpane.setPannable(false);
            setCurrentTool("point");

            zoomGroup.setOnMouseClicked(ev -> {
                if (ev.getButton().equals(MouseButton.PRIMARY)) {
                    Line l1 = new Line(ev.getX() - 10, ev.getY() - 10, ev.getX() + 10, ev.getY() + 10);
                    Line l2 = new Line(ev.getX() + 10, ev.getY() - 10, ev.getX() - 10, ev.getY() + 10);
                    l1.setStroke(colorChosen);
                    l2.setStroke(colorChosen);

                    StackPane sp = new StackPane();
                    Group pointGroup = new Group();

                    sp.getChildren().add(l1);
                    sp.getChildren().add(l2);
                    sp.setTranslateX(ev.getX() - 10);
                    sp.setTranslateY(ev.getY() - 10);
                    sp.setId("drawing"); // Tag it for later removal
                    
                    
                    CheckMenuItem getCordinates = new CheckMenuItem("Get Latitude and Longitude Lines");
                        
                    sp.setOnContextMenuRequested(e -> {
                        ContextMenu menuContext = new ContextMenu();
                        MenuItem deleteItem = new MenuItem("Delete");
                        MenuItem colorItem = new MenuItem("Change Color");

                        menuContext.getItems().add(colorItem);
                        menuContext.getItems().add(getCordinates);
                        menuContext.getItems().add(deleteItem);

                        colorItem.setOnAction(eh -> {
                            for (Node l : sp.getChildren()) {
                                if (l instanceof Line) {
                                    ((Line) l).setStroke(colorChosen);
                                } 
                            }
                        });
                        
                        getCordinates.setOnAction(eh -> {
                            
                            if(getCordinates.isSelected()){
                                Line x = new Line(0, ev.getY(), zoomGroup.getBoundsInParent().getWidth(), ev.getY());
                                Line y = new Line(ev.getX(), 0, ev.getX(), zoomGroup.getBoundsInParent().getHeight());
                                
                                x.getStrokeDashArray().addAll(5d, 10d);
                                y.getStrokeDashArray().addAll(5d, 10d);
                                
                                coordinatesMap.put(sp, new Line[]{x,y});

                                zoomGroup.getChildren().addAll(x,y);
                            }else{
                                Line[] storedLines = coordinatesMap.get(sp);
                                if (storedLines != null) {
                                    zoomGroup.getChildren().removeAll(storedLines);
                                    coordinatesMap.remove(sp); // Clean up entry
                                }
                            }
                        });

                        deleteItem.setOnAction(eh -> {
                            zoomGroup.getChildren().remove(sp);
                            Line[] storedLines = coordinatesMap.get(sp);
                            if (storedLines != null) {
                                    zoomGroup.getChildren().removeAll(storedLines);
                                    coordinatesMap.remove(sp); // Clean up entry
                            }

                            ev.consume();
                        });

                        menuContext.show(sp, e.getSceneX(), e.getSceneY());
                        e.consume();
                    });

                    zoomGroup.getChildren().add(sp);
                }
            });
        }
    }

    private Line linePainting = new Line(0, 0, 0, 0);

    @FXML
    private void drawLine(ActionEvent event) {
        clearToolBarSelection();
        
        
        if(((ToggleButton)event.getSource()).isSelected()){
            // Disable panning when using drawing tools
            map_scrollpane.setPannable(false);

            /*
            // Color cycle counter for RGB rotation
            final int[] colorIndex = { 0 };
            final Color[] colorPalette = {
                    Color.rgb(255, 0, 0, 0.8), // Red
                    Color.rgb(0, 255, 0, 0.8), // Green
                    Color.rgb(0, 0, 255, 0.8), // Blue
                    Color.rgb(255, 0, 255, 0.8), // Magenta
                    Color.rgb(255, 165, 0, 0.8), // Orange
                    Color.rgb(0, 255, 255, 0.8) // Cyan
            };
            */

            zoomGroup.setOnMousePressed(ev -> {
                if (ev.getButton().equals(MouseButton.PRIMARY)) {
                    // Select next color from palette
                    //Color lineColor = colorPalette[colorIndex[0] % colorPalette.length];
                    //colorIndex[0]++;

                    // Create a group to hold the line
                    Group lineGroup = new Group();
                    lineGroup.setId("drawing");

                    // Create the line
                    linePainting = new Line(ev.getX(), ev.getY(), ev.getX(), ev.getY());
                    linePainting.setStroke(colorChosen);
                    linePainting.setStrokeWidth(chosenLineThickness);

                    // Add line to group
                    lineGroup.getChildren().add(linePainting);

                    // Add context menu for deletion
                    addDeleteContextMenu(lineGroup);

                    zoomGroup.getChildren().add(lineGroup);

                    // Set up initial mouse drag handler
                    EventHandler<MouseEvent> dragHandler = e -> {
                        // Update line endpoint
                        linePainting.setEndX(normalizeXPosition(e.getX()));
                        linePainting.setEndY(normalizeYPosition(e.getY()));
                        e.consume();
                    };

                    // Attach the drag handler
                    zoomGroup.setOnMouseDragged(dragHandler);

                    // Handle mouse release to finalize the line
                    zoomGroup.setOnMouseReleased(e -> {
                        if (e.getButton() == MouseButton.PRIMARY) {
                            // Update the line one last time
                            linePainting.setEndX(normalizeXPosition(e.getX()));
                            linePainting.setEndY(normalizeYPosition(e.getY()));
                            e.consume();
                        }
                    });
                }
            });
        }
    }

    @FXML
    private void insertText(ActionEvent event) {
        clearToolBarSelection();
        // Disable panning when using text tool
        map_scrollpane.setPannable(false);

        zoomGroup.setOnMouseClicked(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY)) {
                // Create a background reference rectangle first
                double clickX = ev.getX();
                double clickY = ev.getY();

                // Create visual indicator (subtle circle) where text will be placed
                Circle referencePoint = new Circle(clickX, clickY, 3, Color.RED);
                referencePoint.setFill(Color.rgb(255, 0, 0, 0.5));
                referencePoint.setId("drawing"); // Use drawing ID so it gets cleared with other drawings
                zoomGroup.getChildren().add(referencePoint);

                // Create text field at click position (slightly offset for better visibility)
                TextField textField = new TextField();
                textField.setLayoutX(clickX + 5);
                textField.setLayoutY(clickY - 10);
                textField.setPromptText("Enter text here...");
                textField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-border-color: #cccccc;");
                zoomGroup.getChildren().add(textField);
                textField.requestFocus();

                // Convert to permanent text on Enter key
                textField.setOnAction(e -> convertTextFieldToText(textField, referencePoint, clickX, clickY));

                // Also handle focus loss to confirm text
                textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal && zoomGroup.getChildren().contains(textField)) {
                        convertTextFieldToText(textField, referencePoint, clickX, clickY);
                    }
                });

                // Cancel with Escape key
                textField.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.ESCAPE) {
                        zoomGroup.getChildren().remove(textField);
                        zoomGroup.getChildren().remove(referencePoint);
                    }
                });
            }
        });
    }

    // Enhanced helper method to convert TextField to permanent Text node
    private void convertTextFieldToText(TextField textField, Circle referencePoint, double clickX, double clickY) {
        // Only create text if content isn't empty
        if (!textField.getText().trim().isEmpty()) {
            // Create the actual text node first
            Text finalText = new Text(textField.getText());
            finalText.setStyle("-fx-font-family: Gafata; -fx-font-size:20;");

            // Position text near the reference point with better alignment
            finalText.setX(clickX + 10); // Position to the right of the reference point
            finalText.setY(clickY + 5); // Align vertically with reference point
            finalText.setId("drawing"); // Tag for deletion

            finalText.setStroke(colorChosen);
            // Measure the actual bounds of the text after styling is applied
            // We need to temporarily add it to the scene to get accurate bounds
            Group tempGroup = new Group(finalText);
            zoomGroup.getChildren().add(tempGroup);
            tempGroup.applyCss();
            tempGroup.layout();

            double textWidth = finalText.getBoundsInLocal().getWidth();
            double textHeight = finalText.getBoundsInLocal().getHeight();

            zoomGroup.getChildren().remove(tempGroup);

            // Create a better fitting background for text visibility with adjusted
            // measurements
            javafx.scene.shape.Rectangle textBackground = new javafx.scene.shape.Rectangle(
                    clickX + 8, // Position to right of reference point with padding
                    clickY - textHeight / 1.5, // Better vertical centering
                    textWidth + 10, // Width of text plus adequate padding
                    textHeight + 6 // Height of text plus adequate padding
            );
            textBackground.setFill(Color.rgb(255, 255, 255, 0.6)); // More opaque for better readability
            textBackground.setArcWidth(5);
            textBackground.setArcHeight(5);
            textBackground.setStroke(Color.LIGHTGRAY); // Add subtle border
            textBackground.setStrokeWidth(0.5);
            textBackground.setId("drawing");

            // Create a line connecting reference point to text
            Line connector = new Line(
                    clickX, clickY,
                    clickX + 8, clickY // Line connects to text background
            );
            connector.setStroke(Color.rgb(255, 0, 0, 0.5));
            connector.setStrokeWidth(1.5);
            connector.getStrokeDashArray().addAll(2d, 2d); // Dashed line
            connector.setId("drawing");

            // Group text and background together - order matters for layering
            Group textGroup = new Group(textBackground, finalText, connector, referencePoint);
            textGroup.setId("drawing");

            // Add context menu for deletion to the group
            addDeleteContextMenu(textGroup);

            // Add to view
            zoomGroup.getChildren().add(textGroup);
        }

        // Remove only the temporary text field
        zoomGroup.getChildren().remove(textField);

        // We don't remove referencePoint as it's now part of the textGroup
        zoomGroup.getChildren().remove(referencePoint);
    }

    // Reusable method for adding delete context menu
    private void addDeleteContextMenu(Node node) {
        node.setOnContextMenuRequested(e -> {
            ContextMenu menuContext = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            MenuItem colorItem = new MenuItem("Change Color");
            
            menuContext.getItems().add(colorItem);
            menuContext.getItems().add(deleteItem);
            
            colorItem.setOnAction(ev -> {
                if (node instanceof Group) {
                    // Iterate through all children in the Group
                    for (Node child : ((Group) node).getChildren()) {
                        if (child instanceof Shape) {
                            ((Shape) child).setStroke(colorChosen);
                        } else if (child instanceof Text) {
                            ((Text) child).setStroke(colorChosen);
                        }
                    }
                } else if (node instanceof Shape) {
                    ((Shape) node).setFill(colorChosen);
                } else if (node instanceof Text) {
                    ((Text) node).setFill(colorChosen);
                }
            });
            
            deleteItem.setOnAction(ev -> {
                zoomGroup.getChildren().remove(node);
                ev.consume();
            });

            menuContext.show(node, e.getSceneX(), e.getSceneY());
            e.consume();
        });
    }

    private Circle circlePainting = new Circle(0);
    private double startXedge = 0;

    @FXML
    private void drawCircle(ActionEvent event) {
        clearToolBarSelection();
        // Disable panning when using drawing tools
        map_scrollpane.setPannable(false);

        zoomGroup.setOnMousePressed(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY)) {
                circlePainting = new Circle(1);
                circlePainting.setStroke(colorChosen);
                circlePainting.setStrokeWidth(chosenLineThickness);
                circlePainting.setFill(Color.TRANSPARENT);
                circlePainting.setCenterX(ev.getX());
                circlePainting.setCenterY(ev.getY());
                circlePainting.setId("drawing"); // Tag for deletion
                startXedge = ev.getX();

                circlePainting.setOnContextMenuRequested(e -> {
                    ContextMenu menuContext = new ContextMenu();
                    MenuItem deleteItem = new MenuItem("Delete");
                    MenuItem changeColorItem = new MenuItem("Change Color");
                    menuContext.getItems().add(changeColorItem);
                    menuContext.getItems().add(deleteItem);
                    changeColorItem.setOnAction(ev1 -> {
                        circlePainting.setStroke(colorChosen);
                        ev1.consume();
                    });
                    deleteItem.setOnAction(ev1 -> {
                        zoomGroup.getChildren().remove((Node) e.getSource());
                        ev1.consume();
                    });
                    menuContext.show(circlePainting, e.getSceneX(), e.getSceneY());
                    e.consume();
                });

                zoomGroup.getChildren().add(circlePainting);
            }
        });
        zoomGroup.setOnMouseDragged(ev -> {
            // TODO not let the circle created to go out of the nautical image

            double radio = Math.abs(ev.getX() - startXedge);
            circlePainting.setRadius(radio);
            ev.consume();
        });
    }

    private double initial_ruler_x;
    private double initial_ruler_y;
    private double prev_trans_ruler_x = 0;
    private double prev_trans_ruler_y = 0;
   
    
    
    @FXML
    private void showRuler(ActionEvent event) {
        if(rulerToggleButton.isSelected()){
            Rectangle clip = new Rectangle(map_scrollpane.getWidth(), map_scrollpane.getHeight());
            stackPane.setClip(clip);
            ruler.setOnMousePressed(e -> {
                initial_ruler_x = e.getSceneX() - prev_trans_ruler_x;
                initial_ruler_y = e.getSceneY() - prev_trans_ruler_y;
                
                ruler.setCursor(Cursor.MOVE);
            });
            
            ruler.setOnMouseDragged(e -> {
                ruler.setTranslateX(e.getSceneX() - initial_ruler_x);
                ruler.setTranslateY(e.getSceneY() - initial_ruler_y);
            });
            ruler.setOnMouseReleased(e->{
                prev_trans_ruler_x = e.getSceneX() - initial_ruler_x;
                prev_trans_ruler_y = e.getSceneY() - initial_ruler_y;
                
                ruler.setCursor(Cursor.HAND);
                
                e.consume();
            });
            
            
            ruler.toFront();
            ruler.setVisible(true);
            
        }else{
            ruler.toBack();
            ruler.setVisible(false);
        }
        
        
        
        /*
        
        clearToolBarSelection();
        // Disable panning when using measurement tools
        map_scrollpane.setPannable(false);

        // Setup for ruler measurement
        zoomGroup.setOnMouseClicked(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY)) {
                // Create a new ruler measurement
                createNewRulerMeasurement(ev.getX(), ev.getY());
            }
        });
    }

    // Separate method to create a new ruler measurement
    private void createNewRulerMeasurement(double initialX, double initialY) {
        // Create a fresh group for this ruler
        Group rulerGroup = new Group();
        rulerGroup.setId("drawing");

        // Create new elements for this ruler instance
        Line rulerLine = new Line(initialX, initialY, initialX, initialY);
        rulerLine.setStroke(Color.BLUE);
        rulerLine.setStrokeWidth(2);
        rulerLine.getStrokeDashArray().addAll(5d, 3d);

        Circle startPoint = new Circle(initialX, initialY, 4, Color.BLUE);

        // Add initial components to the ruler group
        rulerGroup.getChildren().addAll(rulerLine, startPoint);
        zoomGroup.getChildren().add(rulerGroup);

        // Save the original click handler to restore it later
        EventHandler<MouseEvent> originalClickHandler = (EventHandler<MouseEvent>) zoomGroup.getOnMouseClicked();

        // Create a handler for the second click
        EventHandler<MouseEvent> secondClickHandler;
        secondClickHandler = secondEv -> {
            if (secondEv.getButton().equals(MouseButton.PRIMARY)) {
                // Get end coordinates
                double endX = secondEv.getX();
                double endY = secondEv.getY();

                // Update the ruler line
                rulerLine.setEndX(endX);
                rulerLine.setEndY(endY);

                // Create an end point marker
                Circle endPoint = new Circle(endX, endY, 4, Color.BLUE);

                // Calculate the distance
                double distance = calculateDistance(initialX, initialY, endX, endY);
                String distanceStr = String.format("%.1f px", distance);

                // Create and configure distance text
                Text distanceText = new Text(distanceStr);
                distanceText.setFill(Color.BLUE);
                distanceText.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

                // Position at midpoint of line
                double midX = (initialX + endX) / 2;
                double midY = (initialY + endY) / 2;

                distanceText.setX(midX + 5);
                distanceText.setY(midY - 5);

                // Create background for text
                Rectangle textBackground = new Rectangle();
                textBackground.setFill(Color.rgb(255, 255, 255, 0.7));
                textBackground.setArcWidth(5);
                textBackground.setArcHeight(5);
                textBackground.setX(midX + 2);
                textBackground.setY(midY - 20);

                // Need to calculate text bounds to size background properly
                double textWidth = distanceText.getBoundsInLocal().getWidth();
                double textHeight = distanceText.getBoundsInLocal().getHeight();
                textBackground.setWidth(textWidth + 10);
                textBackground.setHeight(textHeight + 5);

                // Add the components to the ruler group - each component is new
                rulerGroup.getChildren().addAll(endPoint, textBackground, distanceText);

                // Add tooltip and context menu
                Tooltip info = new Tooltip("Drag the ruler to the map scale to convert this distance");
                info.setShowDelay(Duration.millis(300));
                Tooltip.install(rulerGroup, info);

                addDeleteContextMenu(rulerGroup);

                // Enable dragging
                enableRulerDragging(rulerGroup);

                // Restore original click handler to allow new measurements
                zoomGroup.setOnMouseClicked(originalClickHandler);
                zoomGroup.setOnMouseMoved(null);
            } else if (secondEv.getButton().equals(MouseButton.SECONDARY)) {
                // Cancel this measurement
                zoomGroup.getChildren().remove(rulerGroup);

                // Restore original click handler
                zoomGroup.setOnMouseClicked(originalClickHandler);
                zoomGroup.setOnMouseMoved(null);
            }
        };

        // Create a mouse move handler for preview
        EventHandler<MouseEvent> moveHandler = moveEv -> {
            rulerLine.setEndX(moveEv.getX());
            rulerLine.setEndY(moveEv.getY());
        };

        // Set the handlers for second click and mouse movement
        zoomGroup.setOnMouseClicked(secondClickHandler);
        zoomGroup.setOnMouseMoved(moveHandler);*/
    }

    // Simplified dragging implementation that doesn't need start/end coordinates
    private void enableRulerDragging(Group rulerGroup) {
        final Delta dragDelta = new Delta();

        rulerGroup.setOnMousePressed(e -> {
            // Record starting point of the drag
            dragDelta.x = e.getX();
            dragDelta.y = e.getY();
            e.consume();
        });

        rulerGroup.setOnMouseDragged(e -> {
            // Calculate how much we've moved
            double deltaX = e.getX() - dragDelta.x;
            double deltaY = e.getY() - dragDelta.y;

            // Move the entire group
            rulerGroup.setTranslateX(rulerGroup.getTranslateX() + deltaX);
            rulerGroup.setTranslateY(rulerGroup.getTranslateY() + deltaY);

            // Update drag start point
            dragDelta.x = e.getX();
            dragDelta.y = e.getY();

            e.consume();
        });

        rulerGroup.setOnMouseReleased(e -> e.consume());
    }

    // Calculate distance between two points
    private double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    private boolean rotateRuler = true;
    @FXML
    private void RotateRuler(ActionEvent event) {
        if(rotateRuler){
            ruler.setStyle("-fx-rotate: 90;");
            rotateRuler = false;
        }else{
            ruler.setStyle("-fx-rotate: 0;");
            rotateRuler = true;
        }
    }
    
    @FXML
    private void choseColor(ActionEvent event) {
        colorChosen = ( (ColorPicker)event.getSource() ).getValue();
    }

    @FXML
    private void setThinLineThickness(ActionEvent event) {
        chosenLineThickness = 2;
    }

    @FXML
    private void setMediumLineThickness(ActionEvent event) {
        chosenLineThickness = 4;
    }

    @FXML
    private void setFatLineThickness(ActionEvent event) {
        chosenLineThickness = 6;
    }

    // Helper class to track drag position
    private class Delta {
        double x, y;
    }

    @FXML
    private void clear(ActionEvent event) {
        zoomGroup.getChildren().removeIf(node -> "drawing".equals(node.getId()));
    }

    private double initial_protractor_x;
    private double initial_protractor_y;
    private double prev_trans_protractor_x = 0;
    private double prev_trans_protractor_y = 0;
    @FXML
    
    private void showProtractor(ActionEvent event) {
        if(protractorToggleButton.isSelected()){
            Rectangle clip = new Rectangle(map_scrollpane.getWidth(), map_scrollpane.getHeight());
            stackPane.setClip(clip);
            
                protractor.setOnMousePressed(e -> {
                    initial_protractor_x = e.getSceneX() - prev_trans_protractor_x;
                    initial_protractor_y = e.getSceneY() - prev_trans_protractor_y;

                    protractor.setCursor(Cursor.MOVE);
                });

                protractor.setOnMouseDragged(e -> {
                    /*
                    if(e.getSceneX() - protractor.getWidth()/2 > map_scrollpane.localToScene(map_scrollpane.getBoundsInLocal()).getMinX() &&
                        e.getSceneX() + protractor.getWidth()/2 < map_scrollpane.localToScene(map_scrollpane.getBoundsInLocal()).getMaxX()){
                        protractor.setTranslateX(e.getSceneX() - initial_protractor_x);
                        System.out.println("x out of layout");
                    }
                    if(e.getSceneY() - protractor.getHeight()/2> map_scrollpane.localToScene(map_scrollpane.getBoundsInLocal()).getMinY() &&
                        e.getSceneY() + protractor.getHeight()/2 < map_scrollpane.localToScene(map_scrollpane.getBoundsInLocal()).getMaxY()){
                        protractor.setTranslateY(e.getSceneY() - initial_protractor_y);
                        System.out.println("y out of layout");

                    }*/
                    protractor.setTranslateX(e.getSceneX() - initial_protractor_x);
                    protractor.setTranslateY(e.getSceneY() - initial_protractor_y);
                });
                protractor.setOnMouseReleased(e->{
                    prev_trans_protractor_x = e.getSceneX() - initial_protractor_x;
                    prev_trans_protractor_y = e.getSceneY() - initial_protractor_y;

                    protractor.setCursor(Cursor.HAND);

                    e.consume();
                });


                protractor.toFront();
                protractor.setVisible(true);

            }else{
                protractor.toBack();
                protractor.setVisible(false);
            }

        /*
        // Disable panning when using measurement tools
        map_scrollpane.setPannable(false);

        // Create a protractor
        final Circle protractorOuter = new Circle(100);
        final Circle protractorInner = new Circle(10);
        final Line protractorLine = new Line(0, 0, 100, 0);
        final Arc angleArc = new Arc(0, 0, 30, 30, 0, 0);
        final Text angleText = new Text("");

        // Configure appearance
        protractorOuter.setFill(Color.TRANSPARENT);
        protractorOuter.setStroke(Color.BLUE);
        protractorOuter.setStrokeWidth(1);
        protractorOuter.setStrokeType(StrokeType.INSIDE);
        protractorOuter.getStrokeDashArray().addAll(5d, 5d);

        protractorInner.setFill(Color.TRANSPARENT);
        protractorInner.setStroke(Color.BLUE);
        protractorInner.setStrokeWidth(1);

        protractorLine.setStroke(Color.BLUE);
        protractorLine.setStrokeWidth(2);

        angleArc.setFill(Color.rgb(0, 0, 255, 0.1));
        angleArc.setStroke(Color.BLUE);
        angleArc.setStrokeWidth(1);
        angleArc.setType(ArcType.ROUND);

        angleText.setFill(Color.BLUE);
        angleText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Group to hold all protractor elements
        final Group protractorGroup = new Group(protractorOuter, protractorInner, protractorLine, angleArc, angleText);
        protractorGroup.setId("drawing");

        // Track state of protractor placement
        final boolean[] isPlaced = { false };
        final double[] initialRotation = { 0 };

        // Handle initial placement
        zoomGroup.setOnMousePressed(ev -> {
            if (ev.getButton().equals(MouseButton.PRIMARY) && !isPlaced[0]) {
                double x = ev.getX();
                double y = ev.getY();

                // Set position of protractor
                protractorGroup.setLayoutX(x);
                protractorGroup.setLayoutY(y);

                // Add to scene
                zoomGroup.getChildren().add(protractorGroup);
                isPlaced[0] = true;

                // Add context menu for deletion
                addDeleteContextMenu(protractorGroup);

                // Tooltip for usage instructions
                Tooltip tip = new Tooltip("Drag the outer circle to rotate. Right-click to delete.");
                tip.setShowDelay(Duration.millis(300));
                Tooltip.install(protractorGroup, tip);

                // Setup rotation handling
                protractorOuter.setOnMousePressed(e -> {
                    // Calculate initial angle for rotation reference
                    double angle = Math.toDegrees(Math.atan2(
                            e.getY() - protractorInner.getCenterY(),
                            e.getX() - protractorInner.getCenterX()));
                    initialRotation[0] = angle;
                    e.consume();
                });

                protractorOuter.setOnMouseDragged(e -> {
                    // Get current angle of mouse position
                    double currentAngle = Math.toDegrees(Math.atan2(
                            e.getY() - protractorInner.getCenterY(),
                            e.getX() - protractorInner.getCenterX()));

                    // Calculate rotation delta and apply to line
                    double delta = currentAngle - initialRotation[0];
                    protractorLine.setRotate(protractorLine.getRotate() + delta);

                    // Update arc to show the angle
                    double lineAngle = protractorLine.getRotate() % 360;
                    if (lineAngle < 0)
                        lineAngle += 360;

                    angleArc.setStartAngle(0);
                    angleArc.setLength(lineAngle);

                    // Update angle text
                    angleText.setText(String.format("%.1f°", lineAngle));
                    angleText.setX(30 * Math.cos(Math.toRadians(lineAngle / 2)));
                    angleText.setY(30 * Math.sin(Math.toRadians(lineAngle / 2)));

                    initialRotation[0] = currentAngle;
                    e.consume();
                });

                // Reset handler after placing protractor
                zoomGroup.setOnMousePressed(null);
                zoomGroup.setOnMouseMoved(null);
            }
        });

        // Show placement preview
        zoomGroup.setOnMouseMoved(ev -> {
            if (!isPlaced[0]) {
                // Nothing to do here but could add preview functionality
            }
        });*/
    }

    @FXML
    public void handleRandomExercise(ActionEvent event) throws NavDAOException {
        double random = Math.random();
        Navigation nav = Navigation.getInstance();
        List<Problem> problems = nav.getProblems();
        int randomIndex = (int) ((int) problems.size() * random);
        Problem problem = (Problem) problems.get(randomIndex);
        questionLabel.setText(problem.getText());
        answerListView.getItems().clear();
        loadAnswers(problem);
        
        answerListView.setCellFactory(lv -> new ListCell<String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            setStyle("");
                        }
                    }
                });
                answerListView.refresh();

    }

    private Map<String, Answer> answerMap = new HashMap<>();

    public void loadAnswers(Problem problem) throws NavDAOException {
        answerListView.getItems().clear();
        answerMap.clear();
        List<Answer> answers = problem.getAnswers();
        for (Answer answer : answers) {
            String answerText = answer.getText();
            answerListView.getItems().add(answerText);
            answerMap.put(answerText, answer);
        }
    }

    @FXML
    public void handleAnswerSubmition(ActionEvent event) throws NavDAOException {
        String selectedAnswerText = answerListView.getSelectionModel().getSelectedItem();
        UserSession userSession = UserSession.getInstance();
        Navigation nav = Navigation.getInstance();
        User user = userSession.getCurrentUser();
        if (selectedAnswerText != null) {
            Answer selectedAnswer = answerMap.get(selectedAnswerText);
            if (selectedAnswer != null) {
                // Now you can check if the answer is correct
                if (selectedAnswer.getValidity()) {
                    // Handle correct answer
                    System.out.println("Correct!");
                    hits++;
                } else {
                    // Handle incorrect answer
                    System.out.println("Incorrect!");
                    misses++;
                }
                
                answerListView.setCellFactory(lv -> new ListCell<String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if (answerMap.get(item) != null && answerMap.get(item).getValidity()) {
                                setStyle("-fx-background-color: lightgreen; -fx-text-fill: black;-fx-border-color: black; -fx-border-width: 1px;");
                            } else {
                                setStyle("-fx-background-color: lightcoral; -fx-text-fill: white;-fx-border-color: black; -fx-border-width: 1px;");
                            }
                        }
                    }
                });
                answerListView.refresh();

                
            } else {
                System.err.println("Error: Selected answer text not found in map");
            }
        } else {
            // No answer selected
            System.out.println("Please select an answer");
        }
    }

    @FXML
    public void handleBrowseButton(ActionEvent event) throws NavDAOException {
        Navigation nav = Navigation.getInstance();

        try {
            List<Problem> problems = nav.getProblems();
            Map<String, Problem> problemMap = new HashMap<>();
            List<String> problemNames = new ArrayList<>();
            int id = 0;
            for (Problem problem : problems) {
                String displayText = problem.getText().length() > 50
                        ? problem.getText().substring(0, 50) + "..."
                        : problem.getText();
                String key = displayText + " (ID: " + id + ")";
                id++;
                problemMap.put(key, problem);
                problemNames.add(key);
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>(problemNames.get(0), problemNames);
            dialog.setTitle("Select Problem");
            dialog.setHeaderText("Choose a problem from the list");
            dialog.setContentText("Problem:");

            // Show dialog and process result
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(selectedProblemText -> {
                Problem selectedProblem = problemMap.get(selectedProblemText);
                try {
                    questionLabel.setText(selectedProblem.getText());
                    answerListView.getItems().clear();
                    loadAnswers(selectedProblem);
                } catch (NavDAOException e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                            "Error loading answers: " + e.getMessage(), ButtonType.OK);
                    errorAlert.showAndWait();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR,
                    "Could not load problems: " + e.getMessage(), ButtonType.OK);
            errorAlert.showAndWait();
        }
    }

    @FXML
    public void handleCheckProgress(ActionEvent event) throws NavDAOException {
        UserSession userSession = UserSession.getInstance();
        User user = userSession.getCurrentUser();
        List<Session> sessions = user.getSessions();
        
        if (sessions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Progress Report");
            alert.setHeaderText("No progress available");
            alert.setContentText("You have not completed any problems yet.");
            alert.showAndWait();
            return;
        }
        
        // Create filter options
        List<String> filterOptions = Arrays.asList(
            "All Time", 
            "Today", 
            "Last 7 Days",
            "Last 30 Days",
            "Custom Date Range"
        );
        
        ChoiceDialog<String> filterDialog = new ChoiceDialog<>(filterOptions.get(0), filterOptions);
        filterDialog.setTitle("Filter Progress");
        filterDialog.setHeaderText("Select time period for progress report");
        filterDialog.setContentText("Time period:");
        
        Optional<String> result = filterDialog.showAndWait();
        if (!result.isPresent()) {
            return; // User cancelled
        }
        
        String selectedFilter = result.get();
        List<Session> filteredSessions;
        String periodDescription;
        
        // Get current time
        java.time.LocalDate today = java.time.LocalDate.now();
        
        switch (selectedFilter) {
            case "Today":
                filteredSessions = sessions.stream()
                    .filter(s -> {
                        java.time.LocalDate sessionDate = s.getTimeStamp().toLocalDate();
                        return sessionDate.equals(today);
                    })
                    .collect(java.util.stream.Collectors.toList());
                periodDescription = "Today";
                break;
                
            case "Last 7 Days":
                java.time.LocalDate sevenDaysAgo = today.minusDays(7);
                filteredSessions = sessions.stream()
                    .filter(s -> {
                        java.time.LocalDate sessionDate = s.getTimeStamp().toLocalDate();
                        return sessionDate.isAfter(sevenDaysAgo) || sessionDate.equals(sevenDaysAgo);
                    })
                    .collect(java.util.stream.Collectors.toList());
                periodDescription = "Last 7 Days";
                break;
                
            case "Last 30 Days":
                java.time.LocalDate thirtyDaysAgo = today.minusDays(30);
                filteredSessions = sessions.stream()
                    .filter(s -> {
                        java.time.LocalDate sessionDate = s.getTimeStamp().toLocalDate();
                        return sessionDate.isAfter(thirtyDaysAgo) || sessionDate.equals(thirtyDaysAgo);
                    })
                    .collect(java.util.stream.Collectors.toList());
                periodDescription = "Last 30 Days";
                break;
                
            case "Custom Date Range":
                // Create date picker dialog for custom range
                Dialog<java.time.LocalDate[]> dateDialog = new Dialog<>();
                dateDialog.setTitle("Select Date Range");
                dateDialog.setHeaderText("Please select start and end dates");
                
                ButtonType confirmButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dateDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
                
                javafx.scene.control.DatePicker startDatePicker = new javafx.scene.control.DatePicker(today.minusDays(30));
                javafx.scene.control.DatePicker endDatePicker = new javafx.scene.control.DatePicker(today);
                
                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
                
                grid.add(new javafx.scene.control.Label("Start Date:"), 0, 0);
                grid.add(startDatePicker, 1, 0);
                grid.add(new javafx.scene.control.Label("End Date:"), 0, 1);
                grid.add(endDatePicker, 1, 1);
                
                dateDialog.getDialogPane().setContent(grid);
                
                dateDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == confirmButtonType) {
                        return new java.time.LocalDate[] {
                            startDatePicker.getValue(),
                            endDatePicker.getValue()
                        };
                    }
                    return null;
                });
                
                Optional<java.time.LocalDate[]> dateRange = dateDialog.showAndWait();
                
                if (dateRange.isPresent()) {
                    java.time.LocalDate startDate = dateRange.get()[0];
                    java.time.LocalDate endDate = dateRange.get()[1].plusDays(1); // Include the end day
                    
                    filteredSessions = sessions.stream()
                        .filter(s -> {
                            java.time.LocalDate sessionDate = s.getTimeStamp().toLocalDate();
                            return (sessionDate.isEqual(startDate) || sessionDate.isAfter(startDate)) && 
                                   sessionDate.isBefore(endDate);
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    periodDescription = "Custom Range: " + 
                        startDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy")) + 
                        " to " + 
                        dateRange.get()[1].format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"));
                } else {
                    return; // User cancelled
                }
                break;
                
            default: // "All Time"
                filteredSessions = sessions;
                periodDescription = "All Time";
                break;
        }
        
        // Calculate statistics for filtered sessions
        int totalHits = filteredSessions.stream().mapToInt(Session::getHits).sum();
        int totalMisses = filteredSessions.stream().mapToInt(Session::getFaults).sum();
        int totalProblems = totalHits + totalMisses;
        
        if (totalProblems == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Progress Report");
            alert.setHeaderText("No progress available for selected period");
            alert.setContentText("You have not completed any problems during this period.");
            alert.showAndWait();
            return;
        }
        
        double successRate = (double) totalHits / totalProblems * 100;
        
        // Display results
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Progress Report");
        alert.setHeaderText("Your Progress (" + periodDescription + ")");
        alert.setContentText("Total Tries: " + totalProblems +
                "\nTotal Hits: " + totalHits +
                "\nTotal Misses: " + totalMisses +
                "\nSuccess Rate: " + String.format("%.2f", successRate) + "%");
        
        alert.showAndWait();
    }
    @FXML
    public void handleSaveSession(ActionEvent event) throws NavDAOException {
        UserSession userSession = UserSession.getInstance();
        User user = userSession.getCurrentUser();
        user.addSession(hits, misses);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Saved");
        alert.setHeaderText("Your session has been saved successfully.");
        alert.showAndWait();
    }
}
