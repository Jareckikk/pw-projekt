package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import filters.AbstractBufferedImageOp;
import filters.GaussianFilter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import paralel.AvailableImagePartProvider;
import paralel.SemaphoreThread;

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
    	System.setProperty("glass.accessible.force", "false");
    	filterList.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	filterListPerformance.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	tList.getItems().setAll("1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024");
    }

    public void executeSingleJob() throws InterruptedException{
    	long startTime = System.currentTimeMillis();
    	if(leftImage != null){
    		BufferedImage image = SwingFXUtils.fromFXImage(leftImage, null);
			BufferedImage[] imageParts = ImageHandler.split(image, Integer.parseInt(tList.getValue()), 1);  //TODO
			ApplySelectedFilterSemaphore(imageParts);

    	}
    	long estimatedTime = System.currentTimeMillis() - startTime;
    	System.out.println("Took: " + estimatedTime + " ms");
    }

    private void SetImage(BufferedImage[] imageParts) {
    	int rows = Integer.parseInt(tList.getValue());
    	Image img = SwingFXUtils.toFXImage(ImageHandler.merge(imageParts, rows, 1), null);
    	leftImageView.setImage(img);
    	System.out.println("Image set");
    }

    /// przetwarzanie przy uzyciu semaforow i synchronized
    public void ApplySelectedFilterSemaphore(BufferedImage[] imageParts) throws InterruptedException {
    	int threads = imageParts.length;
    	int imageHeight = (int) leftImage.getHeight();
    	AbstractBufferedImageOp filter = new GaussianFilter(imageHeight/10);
    	Semaphore sem = new Semaphore(1024); // TODO: mozna ustawic ile w¹tkow jednoczesnie ma dostep

    	ArrayList<Thread> pool = new ArrayList<Thread>();
    	AvailableImagePartProvider partProvider = new AvailableImagePartProvider(threads);
    	for (int i = 0; i< threads; i++) {
    		pool.add(new Thread(new SemaphoreThread(sem, filter, imageParts, partProvider)));
    		pool.get(i).start();
		}

    	for (Thread thread : pool) {
			thread.join();
		}

    	SetImage(imageParts);
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
