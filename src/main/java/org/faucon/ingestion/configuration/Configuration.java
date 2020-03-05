package org.faucon.ingestion.configuration;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Configuration {

	private static final Logger logger = LogManager.getLogger(Configuration.class);
	private org.faucon.ingestion.configuration.json.model.Configuration config = null;
	//private String fileName = null;
	
	
	
	public org.faucon.ingestion.configuration.json.model.Configuration getConfiguration(String fileName) {
	
		//this.fileName = fileName;
		
		// Initialize the Jackson Deserializer/Serializer
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
		// Creates a new File instance by converting the given pathname string into an abstract pathname
		File file = new File(fileName);
		
		try {
			
			this.config = mapper.readValue(file, org.faucon.ingestion.configuration.json.model.Configuration.class);
			//logger.info("Load Configuration from : "+config.getElasticSearch().getHostName());
			logger.info("Load Configuration from : "+fileName);
			return config;
			
		} catch (JsonParseException e) {
			
			e.printStackTrace();
			return null;
			
		} catch (JsonMappingException e) {

			e.printStackTrace();
			return null;
			
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
		
	}
	
	

	
}
