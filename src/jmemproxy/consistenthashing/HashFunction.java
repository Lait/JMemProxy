package jmemproxy.consistenthashing;

public interface HashFunction {
	public int hash(Object key);
}
