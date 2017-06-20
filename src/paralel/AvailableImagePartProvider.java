package paralel;

public class AvailableImagePartProvider {

	private int _total;
	private int _current;
	public AvailableImagePartProvider(int totalParts) {
		_total = totalParts;
		_current = 0;
	}
	public synchronized int getNextPart() {
		if(_current < _total)
			return _current++;
		else
			return -1;
	}
}
