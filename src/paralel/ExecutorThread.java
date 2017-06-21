package paralel;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

import filters.AbstractBufferedImageOp;

public class ExecutorThread implements Callable<Void> {

	private Semaphore _sem;
	private AbstractBufferedImageOp _filter;
	private BufferedImage[] _imageParts;
	private AvailableImagePartProvider _partProvider;


	public ExecutorThread(AbstractBufferedImageOp filter, BufferedImage[] imageParts, AvailableImagePartProvider partProvider) {
		_filter = filter;
		_imageParts = imageParts;
		_partProvider = partProvider;
	}


	@Override
	public Void call() throws Exception {
		Random rnd = new Random();
		int partNo = _partProvider.getNextPart();
		while(partNo >= 0){
			_filter.filter(_imageParts[partNo], _imageParts[partNo]);
			partNo = _partProvider.getNextPart();
		}
		return null;
	}

}
