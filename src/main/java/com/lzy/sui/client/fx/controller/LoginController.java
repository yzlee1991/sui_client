package com.lzy.sui.client.fx.controller;

import com.lzy.sui.client.Client;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
		}catch(Exception e){
			Alert alert=new Alert(AlertType.ERROR);
			alert.setHeaderText(e.getMessage());
			alert.showAndWait();
		}
		
	}
	
}
