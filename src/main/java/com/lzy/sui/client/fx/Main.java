package com.lzy.sui.client.fx;

import com.lzy.sui.client.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
//		FXMLLoader fxmlLoader=new FXMLLoader();
		AnchorPane ap=FXMLLoader.load(getClass().getResource("/view/login.fxml"));
		Scene scene = new Scene(ap);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(getClass().getResource("/image/meunicon.png").toURI().toString()));
		primaryStage.setResizable(false);
		primaryStage.setTitle("随小僵客户端");
		primaryStage.setOnCloseRequest(event->{
			//后续添加下线通知
			System.out.println("关闭"+Client.newInstance().getSocket().isClosed());
		});
		primaryStage.show();
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
}
