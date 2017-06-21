package paralel;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.Semaphore;

import filters.AbstractBufferedImageOp;

public class SemaphoreThread implements Runnable {

	private Semaphore _sem;
	private AbstractBufferedImageOp _filter;
	private BufferedImage[] _imageParts;
	private AvailableImagePartProvider _partProvider;


	public SemaphoreThread(Semaphore sem, AbstractBufferedImageOp filter, BufferedImage[] imageParts, AvailableImagePartProvider partProvider) {
		_sem = sem;
		_filter = filter;
		_imageParts = imageParts;
		_partProvider = partProvider;
	}

	@Override
	public void run() {
		try {
			Random rnd = new Random();
			int partNo = _partProvider.getNextPart();
			while(partNo >= 0){
				_sem.acquire();
				_filter.filter(_imageParts[partNo], _imageParts[partNo]);
				partNo = _partProvider.getNextPart();
				_sem.release();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
