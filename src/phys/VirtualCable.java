package phys;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualCable {
	public volatile byte[] bytes = {0x00, 0x00};
	public volatile Lock[] locks = {new ReentrantLock(), new ReentrantLock()};
}