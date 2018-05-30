package com.lzy.sui.client.fx;

import java.io.IOException;

import com.lzy.sui.client.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

	private static Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Main.primaryStage = primaryStage;
		// FXMLLoader fxmlLoader=new FXMLLoader();
		AnchorPane ap = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
		Scene scene = new Scene(ap);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(getClass().getResource("/image/meunicon.png").toURI().toString()));
		primaryStage.setResizable(false);
		primaryStage.setTitle("随小僵客户端");
		primaryStage.setOnCloseRequest(event -> {
			// 后续添加下线通知
			System.out.println("关闭" + Client.newInstance().getSocket().isClosed());
		});
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static Stage waitShow(String message) throws IOException {
		if (primaryStage == null) {
			// 抛异常
		}
		VBox wait = FXMLLoader.load(Main.class.getResource("/view/wait.fxml"));
		Stage stage = new Stage();
		Scene scene = new Scene(wait);
		scene.setFill(null);
		stage.setScene(scene);
		stage.initOwner(primaryStage);
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setWidth(primaryStage.getWidth());
		stage.setHeight(primaryStage.getHeight());
		stage.setX(primaryStage.getX());
		stage.setY(primaryStage.getY());
		Label label = (Label) wait.lookup("#title");
		label.setText(message);
		return stage;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

}
