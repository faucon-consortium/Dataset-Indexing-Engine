package org.faucon.ingestion.configuration.json.model;

public class ElasticSearch {

	private String hostName = null;
	private Integer port = null;
	private String scheme = null;
	
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	
	
	
}
