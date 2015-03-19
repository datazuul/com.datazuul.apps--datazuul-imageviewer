package com.datazuul.apps.imageviewer.javafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @see http://www.java2s.com/Tutorials/Java/JavaFX/0530__JavaFX_Image_ImageView.htm
 * @author ralf
 */
public class PreserveRatio extends Application {
 
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Title");
        Group root = new Group();
        Scene scene = new Scene(root, 600, 330, Color.BLACK);
        
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(10);
        gridpane.setVgap(10);
        
        final ImageView imv = new ImageView();
        imv.setPreserveRatio(true);
        final Image image2 = new Image(PreserveRatio.class.getResourceAsStream("lada-niva-across.jpg"));
        imv.setImage(image2);
        
//        double width = image2.getWidth();

        final HBox pictureRegion = new HBox();
        
        pictureRegion.getChildren().add(imv);
        gridpane.add(pictureRegion, 1, 1);
        
        // TODO centering not correct yet, maybe change stage/scene/pane combination...
        gridpane.setLayoutX(scene.getX() + scene.getWidth() / 2 - gridpane.getWidth() / 2);
        gridpane.setLayoutY(scene.getY() + scene.getHeight() / 2 - gridpane.getHeight() / 2);
        
        
        root.getChildren().add(gridpane);        
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.valueOf(KeyCode.ESCAPE.getName()));
        primaryStage.show();
    }   
}
