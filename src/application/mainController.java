package application;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class mainController {
	
	@FXML
    private Label filePathLabel;
	
	@FXML
	private Label ipLabel;
	
	@FXML
	private Label statusNote;
	
	@FXML
	private TextField varName;
	
	@FXML
	private TextField varValue;
	
	@FXML
	private TextField inputCmd;
	
	@FXML
	private ListView varList;
	

	
    public void initialize(){

  	   	
    }



}
