package com.lzy.sui.client.fx.controller;

import java.lang.reflect.Proxy;

import com.lzy.sui.client.Client;
import com.lzy.sui.common.inf.FileInf;
import com.lzy.sui.common.inf.HostInf;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.proxy.CommonRequestSocketHandle;
import com.lzy.sui.common.rmi.RmiClient;
import com.lzy.sui.common.service.FileService;

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
			
			HostInf inf=(HostInf) RmiClient.lookup(Client.newInstance().getSocket(), HostInf.class.getName());
			System.out.println(inf.a(123));
//			CommonRequestSocketHandle h=new CommonRequestSocketHandle(Client.newInstance().getSocket(),new FileService() , "1", ProtocolEntity.TARGER_SERVER, ProtocolEntity.Mode.INVOKE);
//			FileInf f=	(FileInf) Proxy.newProxyInstance(FileService.class.getClassLoader(), FileService.class.getInterfaces(), h);
//			System.out.println(f.show("12312"));
			
		}catch(Exception e){
			e.printStackTrace();
			Alert alert=new Alert(AlertType.ERROR);
			alert.setHeaderText(e.getMessage());
			alert.showAndWait();
		}
		
	}
	
}
