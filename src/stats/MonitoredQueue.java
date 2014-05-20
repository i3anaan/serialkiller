package stats;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * A wrapper around ArrayBlockingQueue that publishes statistics to the Stats
 * service every time an element is taken or put.
 */
public class MonitoredQueue <T> {
	private ArrayBlockingQueue<T> queue;
	private String name;
	
	public MonitoredQueue(String name, int capacity) {
		queue = new ArrayBlockingQueue<T>(capacity);
		this.name = name;
	}
	
	public ArrayBlockingQueue<T> getUnmonitoredQueue() {
		return queue;
	}
	
	private void update() {
		Stats.set("queues." + name + ".size", queue.size());
	}
	
	public T take() throws InterruptedException {
		T out = queue.take();
		update();
		return out;
	}
	
	public void put(T elem) throws InterruptedException {
		queue.put(elem);
		update();
		return;
	}
}
