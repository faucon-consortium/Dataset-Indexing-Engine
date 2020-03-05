package org.faucon.ingestion.configuration.json.model;

import java.util.List;

public class Configuration {

	private ElasticSearch elasticSearch = null;
	private List<Input> inputs = null;
	
	public ElasticSearch getElasticSearch() {
		return elasticSearch;
	}
	public void setElasticSearch(ElasticSearch elasticSearch) {
		this.elasticSearch = elasticSearch;
	}
	public List<Input> getInputs() {
		return inputs;
	}
	public void setInputs(List<Input> inputs) {
		this.inputs = inputs;
	}
	
	
}
