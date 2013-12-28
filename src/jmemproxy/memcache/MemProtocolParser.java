package jmemproxy.memcache;

public class MemProtocolParser {
	private String action;
	private String paramName;
	private String paramValue;
	
	public MemProtocolParser() {
		this.action = null;
		this.paramName = null;
		this.paramValue = null;
	}
	
	public Boolean setMemString(String req) {
		Boolean success = true;
		return success;
	}
	
	public String getAction() {
		return this.action;
	}
	
	public String getParamName() {
		return this.paramName;
	}
	
	public String paramValue() {
		return this.paramValue;
	}
}