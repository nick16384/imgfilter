package filters.base;

import java.util.concurrent.Future;

// TODO: Implement FilterStatusFuture in MultiPassFilter

public interface FilterStatusFuture<T> extends Future<T> {
	public T getLiveImage();
	public int getPassGroup();
	public int getSubPass();
	public double getProgress();
}