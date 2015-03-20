package borderpaneexample;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Saravanababu
 */
public class BorderPaneExample extends Application {

    int n;

    public BorderPaneExample() {
        n = 0;
    }

    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();

        final Image im[] = new Image[6];
        for (int i = 0; i < 6; i++) {
            im[i] = new Image(getClass().getResourceAsStream((i + 1) + ".jpg"));
        }
        Text top = new Text("Image Viewer");
        top.setFont(Font.font("Times New Roman", 30));
        HBox topbox = new HBox();
        topbox.setAlignment(Pos.CENTER);
        topbox.getChildren().add(top);
        root.setTop(topbox);

        Button left = new Button("<");
        left.setFont(Font.font("Times New Roman", 30));
        VBox leftbox = new VBox();
        leftbox.setAlignment(Pos.CENTER);
        leftbox.getChildren().add(left);
        root.setLeft(leftbox);

        Button right = new Button(">");
        right.setFont(Font.font("Times New Roman", 30));
        VBox rightbox = new VBox();
        rightbox.setAlignment(Pos.CENTER);
        rightbox.getChildren().add(right);
        root.setRight(rightbox);

        Text bottom = new Text("www.javafxapps.in");
        bottom.setFont(Font.font("Times New Roman", 30));
        HBox bottombox = new HBox();
        bottombox.setAlignment(Pos.CENTER);
        bottombox.getChildren().add(bottom);
        root.setBottom(bottombox);

        final ImageView center = new ImageView();
        root.setCenter(center);

        center.setImage(im[n]);
        left.setOnMousePressed((MouseEvent me) -> {
            if (n == 0) {
                n = 5;
            } else {
                n--;
            }
            center.setImage(im[n]);
        });
        right.setOnMousePressed((MouseEvent me) -> {
            if (n == 5) {
                n = 0;
            } else {
                n++;
            }
            center.setImage(im[n]);
        });

        Scene scene = new Scene(root, 700, 600);

        primaryStage.setTitle("Image Viewer with BorderPane");
        primaryStage.setFullScreen(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);

    }
}
