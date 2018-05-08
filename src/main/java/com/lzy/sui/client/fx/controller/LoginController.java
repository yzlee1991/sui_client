package com.lzy.sui.client.fx.controller;

import com.lzy.sui.client.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController {

	@FXML
	private TextField account;
	
	@FXML
	private PasswordField password;
	
	@FXML
	public void login(){
		try{
			String ac=account.getText();
			String pw=password.getText();
			Client.newInstance().start(ac, pw);
			AnchorPane home=FXMLLoader.load(getClass().getResource("/view/home.fxml"));
			Stage stage = (Stage)account.getScene().getWindow();
			stage.setScene(new Scene(home));
			stage.setResizable(true);
		}catch(Exception e){
			e.printStackTrace();
			Alert alert=new Alert(AlertType.ERROR);
			alert.setHeaderText(e.getMessage());
			alert.showAndWait();
		}
		
	}
	
}
