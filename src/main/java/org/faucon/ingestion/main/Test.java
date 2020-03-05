package org.faucon.ingestion.main;

import java.util.ArrayList;
import java.util.List;

import org.faucon.ingestion.configuration.json.model.Configuration;
import org.faucon.ingestion.configuration.json.model.ElasticSearch;
import org.faucon.ingestion.configuration.json.model.Input;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//genConfiguration();
		
		SensuLogs.getSensu();
		
	}

	
	public static void genConfiguration() {
		
		// Initialize Jackson Serializer / Deserializer 
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Configuration configuration = new Configuration();
		Input input = null;
		String json = null;
		
		ElasticSearch elasticSearch = new ElasticSearch();
		elasticSearch.setHostName("localhost");
		elasticSearch.setPort(9200);
		elasticSearch.setScheme("http");
		
		configuration.setElasticSearch(elasticSearch);
		
		List<Input> listOfInputs = new ArrayList<Input>();
		
		input = new Input();
		input.setDirectory("/home/chdor/Projects/FAUCON/cloud-maintenance/dataset/2020-02");
		input.setFiles(new ArrayList<String>());
		input.getFiles().add("*");
		input.setBulkLines(1000L);
		listOfInputs.add(input);
		
		configuration.setInputs(listOfInputs);
		
		try {
			
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(configuration);
			System.out.println(json);
			
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
