package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class mainController {

	@FXML
	private ComboBox<String> filterList; 
	
	@FXML
	private ComboBox<String> filterListPerformance;
	
	@FXML
	private ComboBox<String> tList; 

	@FXML
	private ImageView leftImageView;
	
	@FXML
	private ImageView rightImageView;
	
	private Image leftImage;
	private Image rigthImage;

	
    public void initialize(){
    	filterList.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	filterListPerformance.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	tList.getItems().setAll("1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024");
    }
    
    public void executeSingleJob(){
    	if(leftImage != null){
    		BufferedImage image = SwingFXUtils.fromFXImage(leftImage, null);
			ImageHandler.split(image, Integer.parseInt(tList.getValue()), 1);  //TODO
    	}
    	
    }
    
    public void executePerformanceJob(){
    	
    }
    
    public void chooseFile(){
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Wybierz obraz");
    	File selectedFile = fileChooser.showOpenDialog(new Stage()); 
    	
    	//TODO walidacja rozszerzenia!!!
    	   	
    	if(selectedFile != null){
        	leftImage = new Image(selectedFile.toURI().toString());   	
        	leftImageView.setImage(leftImage);
    	}

    	
    }



}
