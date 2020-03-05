package org.faucon.ingestion.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.faucon.ingestion.configuration.json.model.Configuration;
import org.faucon.ingestion.configuration.json.model.ElasticSearch;
import org.faucon.ingestion.configuration.json.model.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IngestionCore {

	private static final Logger logger = LogManager.getLogger(IngestionCore.class);
	
	@Autowired
	IngestionUtils ingestionUtils;

	@Autowired
	LogToolkit logToolkit;

	public void ingestion(Configuration configuration) {
		
		String index = null; 
		Long bulkLines = 0L;
		ElasticSearch elasticSearch = null;
		
		List<Input> listOfInputs = configuration.getInputs();
		List<String> listOfFiles = new ArrayList<String>();
		
		
		if ( configuration.getElasticSearch() != null && listOfInputs != null ) {
			
			elasticSearch = configuration.getElasticSearch();
			
			for ( Input input:listOfInputs) {
				listOfFiles.addAll(logToolkit.listFiles(input.getDirectory(), input.getFiles()));
				
				bulkLines = input.getBulkLines();
				index = input.getIndex();
				
				for (String fileName:listOfFiles) {
					logger.info("Indexing "+fileName);
					try {
						ingestionUtils.index(fileName, index, bulkLines, elasticSearch);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}

				
			}
			
			
		}
	}
	
	


}
