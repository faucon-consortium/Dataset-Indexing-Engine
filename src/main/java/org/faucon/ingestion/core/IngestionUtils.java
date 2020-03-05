package org.faucon.ingestion.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.faucon.dataset.cloud.log.SensuLog;
import org.faucon.ingestion.configuration.json.model.ElasticSearch;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("IngestionUtils")
public class IngestionUtils {

	private static final Logger logger = LogManager.getLogger(IngestionUtils.class);
	
	
	public long getNbLines(String file) {

		BufferedReader reader =null;
		long nbLines = 0L;
		String line = null;
		
		// Load the Source file
		try {
				reader = new BufferedReader(new FileReader(file));
				
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			return 0;
		}
		
		
		try {
			
			line = reader.readLine();
			
			while (line != null) {
				
				line = reader.readLine();
				nbLines++;
			}
			
			reader.close();
			return nbLines;
			
		} catch (IOException e1) {
			e1.printStackTrace();
			
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
				return 0;
			}
			
			return 0;
		}
	}


	public void index( String file, String index, Long bulkLines, ElasticSearch elasticSearch ) throws Exception {
		
		BufferedReader reader =null;
		Long nbReadLines = 0L;
		String line = null;
		
		//long nbLines = getNbLines(file);
		long nbLines = 0;
		SensuLog sensuLog = null;
		
		nbLines = getNbLines(file);

		long nbIterations = nbLines / bulkLines;
		long remainingIterations = nbLines % bulkLines;
		
		logger.info("##### "+index+" ##### - Bulk Insert ("+bulkLines+" lines per Bulk)");
		logger.info("- Number of <"+index+"> to index: "+nbLines);
		logger.info("- Number of Bulk Operations: "+nbIterations);
		logger.info("- Remaining <"+index+"> to index: "+remainingIterations);

		
		// Initialize Jackson Serializer / Deserializer 
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// Initialize the RestHighLevelClient
		RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticSearch.getHostName(), elasticSearch.getPort(), elasticSearch.getScheme())));
		
		// Initialize the Bulk API
		BulkRequest bulkRequest = null;
		BulkResponse bulkResponse = null;
		
		// Load the Source file
		try {
				reader = new BufferedReader(new FileReader(file));
				
		} catch (FileNotFoundException e) {
			throw new Exception("The "+file+" was not found");
		}

		if( nbIterations > 0)
		{
			for (int i=1;i<=nbIterations;i++)
			{
				nbReadLines = 0L;
				logger.info("- Bulk Insert n°"+i);
				
				bulkRequest = new BulkRequest();

				// Add the 'bulk lines' records to the BulkRequest
				while (nbReadLines<bulkLines)
				{
					try
					{	
						line = reader.readLine();
						nbReadLines++;
						
						sensuLog = mapper.readValue(line, SensuLog.class);
						rebuild(sensuLog);
						
						line = mapper.writeValueAsString(sensuLog);
						bulkRequest.add(new IndexRequest(index).id(sensuLog.getId()).source(line, XContentType.JSON));
							
					} catch (IOException e) {
						logger.warn("Error: the line: "+line+" could not be marshall");
						//e.printStackTrace();
					}
				}
			
			    // BulkInsert
				try {
						//logger.info("Bulk Insert Execution");
						bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
						
						if (bulkResponse.hasFailures()) {

							for (BulkItemResponse bulkItemResponse : bulkResponse) {
							    if (bulkItemResponse.isFailed()) { 
							        BulkItemResponse.Failure failure = bulkItemResponse.getFailure(); 
							        logger.error("  -> KO: "+failure.getMessage());
							        logger.error("Bulk Insert Exception: "+failure.getCause());
							    }
							}

						}
						else {
							logger.info("  -> OK");
						}
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} // fin if

		
		// Index remaining records
		if ( remainingIterations > 0 )
		{
			logger.info("## "+index+" ## - Remaining to index: "+remainingIterations);
			bulkRequest = new BulkRequest();
			
			for (int i=1;i<=remainingIterations;i++)
			{
				try
				{	
					line = reader.readLine();
					sensuLog = mapper.readValue(line, SensuLog.class);
					rebuild(sensuLog);
					
					line = mapper.writeValueAsString(sensuLog);
					bulkRequest.add(new IndexRequest(index).id(sensuLog.getId()).source(line, XContentType.JSON));
						
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					logger.warn("Error: the line: "+line+" could not be marshall");
				}				
			}
			
			// BulkInsert
			try {
					//logger.info("Execute Bulk Insert");
					bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
					
					if (bulkResponse.hasFailures()) {

						for (BulkItemResponse bulkItemResponse : bulkResponse) {
						    if (bulkItemResponse.isFailed()) { 
						        BulkItemResponse.Failure failure = bulkItemResponse.getFailure(); 
						        logger.error("  -> KO: "+failure.getMessage());
						        logger.error("Bulk Insert Exception: "+failure.getCause());
						    }
						}

					}
					else {
						logger.info("  -> OK");
					}
					
					
					
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}

		
		
		
		// Close the reader
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		// Close the RestHighLevelClient
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void rebuild(SensuLog sensuLog) {
		
		String timestamp = sensuLog.getTimestamp();
		timestamp = timestamp.substring(0,timestamp.indexOf("+"));
		sensuLog.setId(timestamp);
		
		timestamp = timestamp.substring(0, timestamp.lastIndexOf("."));
		sensuLog.setTimestamp(timestamp);
		
		String epochDate = null;
		String stringValue = null;
		Long longValue = null;
		//sensuLog.timestamp
		//sensuLog.event.timestamp
		//sensuLog.event.last_ok
		//sensuLog.event.last_state_change
		//sensuLog.event.timestamp
		//sensuLog.event.client.timestamp
		//sensuLog.event.check.issued
		//sensuLog.event.check.executed
		//sensuLog.payload.issued
		
		if ( sensuLog.getEvent() !=null ) {

			if ( sensuLog.getEvent().getTimestamp()!=null ) {
				
				stringValue = String.valueOf(sensuLog.getEvent().getTimestamp());
				longValue = Long.valueOf(stringValue);
				
				epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
				sensuLog.getEvent().setTimestamp(epochDate);
			} 
			
			if ( sensuLog.getEvent().getLast_ok() !=null ) {
				stringValue = String.valueOf(sensuLog.getEvent().getLast_ok());
				longValue = Long.valueOf(stringValue);
				
				epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
				sensuLog.getEvent().setLast_ok(epochDate);
			}

			if ( sensuLog.getEvent().getLast_state_change() !=null ) {
				stringValue = String.valueOf(sensuLog.getEvent().getLast_state_change());
				longValue = Long.valueOf(stringValue);
				
				epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
				sensuLog.getEvent().setLast_state_change(epochDate);
			}
			
			if ( sensuLog.getEvent().getClient() != null && sensuLog.getEvent().getClient().getTimestamp() != null ) {
				stringValue = String.valueOf(sensuLog.getEvent().getClient().getTimestamp());
				longValue = Long.valueOf(stringValue);
				
				epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
				sensuLog.getEvent().getClient().setTimestamp(epochDate);
			}

			if (sensuLog.getEvent().getCheck() != null ) {
				
				if ( sensuLog.getEvent().getCheck().getIssued() != null ) {
					stringValue = String.valueOf(sensuLog.getEvent().getCheck().getIssued());
					longValue = Long.valueOf(stringValue);

					epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
					sensuLog.getEvent().getCheck().setIssued(epochDate);
				}
				
				if ( sensuLog.getEvent().getCheck().getExecuted() != null ) {
					stringValue = String.valueOf(sensuLog.getEvent().getCheck().getExecuted());
					longValue = Long.valueOf(stringValue);
					epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
					sensuLog.getEvent().getCheck().setExecuted(epochDate);
				}
				
			}
		}
		
		if( sensuLog.getPayload() != null && sensuLog.getPayload().getIssued() != null ) {
			
			stringValue = String.valueOf(sensuLog.getPayload().getIssued());
			longValue = Long.valueOf(stringValue);
			
			epochDate = convertLocalDateTimeToString(epochSecondToLocalDateTime(longValue));
			sensuLog.getPayload().setIssued(epochDate);
		}
		
	}
	
	private LocalDateTime epochSecondToLocalDateTime(Long epochSecond) {
		
		LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC);
		localDateTime = localDateTime.plusHours(1);
		
		return localDateTime;
	}
	

	private String convertLocalDateTimeToString( LocalDateTime localDateTime ) {
		DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.FRENCH);
		String dateTimeStr = localDateTime.format(dateTimeFormatter);
		
		return dateTimeStr;
	}
	
	
	//https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
	private long countLinesNew(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];

	        int readChars = is.read(c);
	        if (readChars == -1) {
	            // bail out if nothing to read
	            return 0L;
	        }

	        // make it easy for the optimizer to tune this loop
	        long count = 0L;
	        while (readChars == 1024) {
	            for (int i=0; i<1024;) {
	                if (c[i++] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        // count remaining characters
	        while (readChars != -1) {
	            System.out.println(readChars);
	            for (int i=0; i<readChars; ++i) {
	                if (c[i] == '\n') {
	                    ++count;
	                }
	            }
	            readChars = is.read(c);
	        }

	        return count == 0 ? 1 : count;
	    } finally {
	        is.close();
	    }
	}

}
