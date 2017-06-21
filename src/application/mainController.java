package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import filters.AbstractBufferedImageOp;
import filters.DiffusionFilter;
import filters.GaussianFilter;
import filters.GrayscaleFilter;
import filters.NoiseFilter;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import paralel.AvailableImagePartProvider;
import paralel.ExecutorThread;
import paralel.SemaphoreThread;

public class mainController {

	@FXML
	private ToggleButton useSemaphores;
	
	//private Boolean useSemaphores = false; //@TODO: to tez do gui podepnij
	
	@FXML
	private TextField conThreadNum;
	//private int concurrentThreadsRunning = 512; //@TODO: moglbys to podpi¹c do GUI ?
	// liczba watkow, ktore maja jednoczesnie dzialac (nie jest to liczba utworzonych watkow).

	@FXML
	private ComboBox<String> filterList; //Lista filtrów na piewrszym oknie

	@FXML
	private ComboBox<String> filterListPerformance; //Lista filtrów na oknie wydajnoœæ

	@FXML
	private LineChart<Integer, Integer> benchChart; //Wykres w oknie wydajnosc

	@FXML
	private ComboBox<String> tList; //Lista wyboru iloœci w¹tków

	@FXML
	private ImageView leftImageView;

	@FXML
	private ImageView rightImageView;

	@FXML
	private Label resultLabel;

	@FXML
	private ProgressBar benchProgressBar;

	@FXML
	private Label benchResultLabel;

	private Image leftImage;

    public void initialize(){
    	System.setProperty("glass.accessible.force", "false");
    	filterList.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	filterListPerformance.getItems().setAll("Filtr Gaussowski", "Filtr Skala Szaroœci", "Filtr Dyfuzyjny", "Filtr Szumu");
    	tList.getItems().setAll("1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024");
    	benchChart.getXAxis().setLabel("Iloœæ w¹tków");
    	benchChart.getYAxis().setLabel("Czas ms");
    }

    public void executeSingleJob() throws InterruptedException{
    	//Kilka walidacji, ¿eby nie sypaæ wyj¹tków
    	if(leftImageView.getImage() == null){
    		this.resultLabel.setText("Nie wybrano obrazu!");
    		return;
    	}else if(filterList.getValue() == null){
    		this.resultLabel.setText("Nie wybrano filtra!");
    		return;
    	}else if(tList.getValue() == null){
    		this.resultLabel.setText("Nie wybrano liczby w¹tków!");
    		return;
    	}

    	long startTime = System.currentTimeMillis();
    	if(leftImage != null){
    		BufferedImage image = SwingFXUtils.fromFXImage(leftImage, null);
			BufferedImage[] imageParts = ImageHandler.split(image, Integer.parseInt(tList.getValue()), 1);  //TODO
			ApplySelectedFilterSemaphore(imageParts, filterList.getValue());
    	}
    	long estimatedTime = System.currentTimeMillis() - startTime;
    	this.resultLabel.setText("Czas wykonania: " + estimatedTime + " ms");
    }

    public void executePerformanceJob() throws InterruptedException{
    	//Kilka walidacji, ¿eby nie sypaæ wyj¹tków
    	if(leftImageView.getImage() == null){
    		this.benchResultLabel.setText("Nie wybrano obrazu!");
    		return;
    	}else if(filterListPerformance.getValue() == null){
    		this.benchResultLabel.setText("Nie wybrano filtra!");
    		return;
    	}
    	Thread t = new Thread(new PerformanceRunner());
    	t.start();
    	//Bez join bo nie odœwiezy progressBaru
    }

    private void SetImage(BufferedImage[] imageParts) {
    	int rows = Integer.parseInt(tList.getValue());
    	Image img = SwingFXUtils.toFXImage(ImageHandler.merge(imageParts, rows, 1), null);
    	rightImageView.setImage(img);
    	System.out.println("Image set");
    }

    //Musialem przeladowac zeby nie aktualizowac obrazu wynikowego przy benchmarku
    public void ApplySelectedFilterSemaphore(BufferedImage[] imageParts, String filterName) throws InterruptedException{
    	ApplySelectedFilter(imageParts, filterName, false);
    }

    private AbstractBufferedImageOp GetSelectedFilter(String filterName, int imageHeight) {
    	AbstractBufferedImageOp filter;

    	switch(filterName){
	    	case "Filtr Gaussowski":
	    		int blur = (int)imageHeight/20;
	    		if(blur<1)
	    			blur = 1;
	    		filter = new GaussianFilter(blur);
	    		break;
	    	case "Filtr Skala Szaroœci":
	    		filter = new GrayscaleFilter();
	    		break;
	    	case "Filtr Dyfuzyjny":
	    		filter = new DiffusionFilter();
	    		break;
	    	case "Filtr Szumu":
	    		filter = new NoiseFilter();
	    		break;
	    	default:
	    		filter = null;
    	}

    	return filter;
    }

    private <T> ArrayList<T> CreatePool(int threads, Callable<T> creatorFunction) {
    	ArrayList<T> pool = new ArrayList<T>();

    	for (int i = 0; i< threads; i++) {
    		try {
				pool.add(creatorFunction.call());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	return pool;
    }

    /// przetwarzanie przy uzyciu semaforow i synchronized
    public void ApplySelectedFilter(BufferedImage[] imageParts, String filterName, boolean isBenchmark) throws InterruptedException {
    	if(useSemaphores.isPressed()) {
    		UseSemaphoreMethod(imageParts, filterName, isBenchmark);
    	} else {
    		UseExecutorMethod(imageParts, filterName, isBenchmark);
    	}
    }

    private void UseExecutorMethod(BufferedImage[] imageParts, String filterName, boolean isBenchmark) throws InterruptedException {
    	int threads = imageParts.length;
    	int imageHeight = (int) leftImage.getHeight();

    	AbstractBufferedImageOp filter = GetSelectedFilter(filterName, imageHeight);

    	ExecutorService poolExecutor = Executors.newFixedThreadPool(Integer.parseInt(conThreadNum.getText()));
    	AvailableImagePartProvider partProvider = new AvailableImagePartProvider(threads);
    	ArrayList<Callable<Void>> pool = CreatePool(threads, () -> new ExecutorThread(filter, imageParts, partProvider));

    	poolExecutor.invokeAll(pool);
    	poolExecutor.shutdown();
    	if(!isBenchmark){
    		SetImage(imageParts);
    	};
    }

    private void UseSemaphoreMethod(BufferedImage[] imageParts, String filterName, boolean isBenchmark) throws InterruptedException {
    	int threads = imageParts.length;
    	int imageHeight = (int) leftImage.getHeight();

    	AbstractBufferedImageOp filter = GetSelectedFilter(filterName, imageHeight);

    	Semaphore sem = new Semaphore(Integer.parseInt(conThreadNum.getText()));
    	AvailableImagePartProvider partProvider = new AvailableImagePartProvider(threads);
    	ArrayList<Thread> pool = CreatePool(threads, () -> new Thread(new SemaphoreThread(sem, filter, imageParts, partProvider)));

    	for(Thread thread: pool)
    		thread.start();

    	for (Thread thread : pool) {
			thread.join();
		}

    	if(!isBenchmark){
    		SetImage(imageParts);
    	};
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

    public void clearChart(){
    	benchChart.getData().clear();
    }

    //Musi byæ na osobnym w¹tku inaczej nie aktualizuje progressbaru bo w¹tek g³ówny jest przyblokowany przez metodê.
    public class PerformanceRunner implements Runnable{
		@Override
		public void run() {
            benchProgressBar.setProgress(0.0);
        	XYChart.Series<Integer, Integer> dataSeries = new XYChart.Series<Integer,Integer>();
            dataSeries.setName(filterListPerformance.getValue());
            long startTimeEntire = System.currentTimeMillis();
        	for(int i = 0; i < 10; i ++){
        		long startTime = System.currentTimeMillis();
            	if(leftImage != null){
            		BufferedImage image = SwingFXUtils.fromFXImage(leftImage, null);
        			BufferedImage[] imageParts = ImageHandler.split(image, (int)Math.pow(2,i), 1);  //TODO
        			try {
						ApplySelectedFilter(imageParts, filterListPerformance.getValue(), true);
					} catch (InterruptedException e) {e.printStackTrace();}
                	Image img = SwingFXUtils.toFXImage(ImageHandler.merge(imageParts, (int)Math.pow(2,i), 1), null);
            	}
            	dataSeries.getData().add(new XYChart.Data<Integer, Integer>((int)Math.pow(2,i), (int)(System.currentTimeMillis() - startTime)));
            	benchProgressBar.setProgress((double)(i+1)/10);
        	}
        	long estimatedTime = System.currentTimeMillis() - startTimeEntire;

        	//Sposob na edycje danych z innego watku
        	Platform.runLater(new Runnable() {
                @Override public void run() {
                	benchChart.getData().add(dataSeries);
                	benchResultLabel.setText("Czas wykonania: " + estimatedTime + " ms");
                }
            });

		}
    }

}
