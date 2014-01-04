package jmemproxy.memcache;

import java.io.IOException;

public class MemcacheHandler implements Runnable{
	//��֤ÿ���̶߳����Լ�������
	private static final ThreadLocal<MemcacheInteractor> interactor = new ThreadLocal<MemcacheInteractor>();
	
	public MemcacheHandler(MemcacheInteractor i) {
		interactor.set(i);
	}
	
	public MemcacheInteractor getInteractor() {
		return interactor.get();
	}

	@Override
	public void run() {
		try {
			interactor.get().mainloop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
