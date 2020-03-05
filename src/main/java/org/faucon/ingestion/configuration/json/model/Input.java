package org.faucon.ingestion.configuration.json.model;

import java.util.List;

public class Input {

	private String index = null;
	private String directory = null;
	private Boolean recursive = false;
	private List<String> files = null;
	private Long bulkLines = null;
	
	public String getDirectory() {
		return directory;
	}
	public void setDirectory(String directory) {
		this.directory = directory;
	}
	public Boolean getRecursive() {
		return recursive;
	}
	public void setRecursive(Boolean recursive) {
		this.recursive = recursive;
	}
	public List<String> getFiles() {
		return files;
	}
	public void setFiles(List<String> files) {
		this.files = files;
	}
	public Long getBulkLines() {
		return bulkLines;
	}
	public void setBulkLines(Long bulkLines) {
		this.bulkLines = bulkLines;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	
	
}
