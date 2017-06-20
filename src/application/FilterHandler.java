package application;

import java.awt.image.BufferedImage;

public class FilterHandler {
	
	public BufferedImage GaussianFilter(BufferedImage src, float radius){	
		filters.GaussianFilter gf = new filters.GaussianFilter();
		gf.setRadius(radius);	
		return gf.filter(src, null);
	}
}
