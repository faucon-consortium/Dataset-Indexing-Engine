package org.faucon.ingestion.main;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.faucon.dataset.cloud.log.SensuLog;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SensuLogs {

	private static final Logger logger = LogManager.getLogger(SensuLogs.class);
	
	public static void getSensu() {
		
		//https://www.elastic.co/guide/en/elasticsearch/painless/master/painless-datetime.html
		// https://www.elastic.co/guide/en/elasticsearch/painless/current/painless-walkthrough.html
		
		SensuLog sensuLog =null;
		String source = null;
		String json = null;
		String scrollId = null;
		//http://owlapi.sourceforge.net/documentation.html
		//https://www.contentside.com/owl-api-une-api-java-pour-manipuler-des-ontologies/
	
		// Build elasticSearch High level REST client
		// High level REST client that wraps an instance of the low level RestClient and allows to build requests and read responses
		RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));
		
		// Initialize Jackson Serializer / Deserializer 
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		

		//String queryString="_exists_:customer.dob AND customer.dob:{1999-10-23 TO *] AND [* TO 1989-10-23]";  
		String queryString="id:\"2019-03-08T16:34:14.308221\"";  
		
		QueryStringQueryBuilder customerQueryString = new QueryStringQueryBuilder(queryString);
		
		// Define the SearchSource Builder
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		//sourceBuilder.query(customerQueryString).scriptField("age", calculateCustomerAges());;
		//sourceBuilder.query(customerQueryString);

		//sourceBuilder.query(QueryBuilders.matchAllQuery());
		sourceBuilder.query(customerQueryString);
		
		sourceBuilder.from(0); 
		sourceBuilder.size(2); 
		//sourceBuilder.docValueField("customer");
		
		String[] includeFields = new String[] {"*"};
		String[] excludeFields = new String[] {};
		sourceBuilder.fetchSource(includeFields, excludeFields);
		
		
		
		TimeValue timeValue = TimeValue.timeValueSeconds(600L);
		
		// A request to execute search against one or more indices (or all).
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("sensulog");
		searchRequest.source(sourceBuilder);
		searchRequest.scroll(timeValue.toString());
		searchRequest.scroll(timeValue);
		

		// A response of a search request.
		SearchResponse searchResponse = null;

		// Execute and parse result
		try {
			
			searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
			scrollId = searchResponse.getScrollId();
			logger.info("### ScrollID: "+scrollId);

			SearchHits hits = searchResponse.getHits();		
			logger.info("### Nb Customers: "+hits.getTotalHits());
		
			SearchHit[] searchHits = hits.getHits();
			
			Long totalValues = hits.getTotalHits().value;
			// 
			
			for (SearchHit hit : searchHits) {
				
				source = hit.getSourceAsString();
				
				if ( source != null && ! source.isEmpty( )) {
					
					sensuLog = mapper.readValue(source, SensuLog.class);
					json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(sensuLog);
					logger.info(json);
				}
				
				// Cas ou plusieurs champs retournés en plus de celui calculé par script
//				String str = hit.getSourceAsString();
//				mapOfFieldsReturn = hit.getFields();
//				for ( Map.Entry<String, DocumentField> entry:mapOfFieldsReturn.entrySet()) {
//					logger.info(entry.getKey()+" = "+entry.getValue().getValue());
//				}
				
				
//				DocumentField age = hit.field("age");
//				List<Object> listObj = age.getValues();
//				
//				String productJson = hit.field("age").getValue().toString();
//				logger.info("### json: "+productJson);
//				customer = mapper.readValue(productJson, Customer.class);
//			
//				cutomers.add(customer);
				//logger.debug("Customer: "+productJson);
			}
		
			
			// ***************************************************************
			client.close();
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
}
