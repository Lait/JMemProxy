package jmemproxy.memcache;

public class MemcacheHandler implements Runnable{
	private static final ThreadLocal<MemcacheInteractor> interactor = new ThreadLocal<MemcacheInteractor>();
	
	public MemcacheHandler(MemcacheInteractor i) {
		interactor.set(i);
	}

	@Override
	public void run() {
		interactor.get().mainloop();
	}

}
