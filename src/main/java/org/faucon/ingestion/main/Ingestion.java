package org.faucon.ingestion.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.faucon.ingestion.configuration.Configuration;
import org.faucon.ingestion.core.IngestionCore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages={"org.faucon.ingestion.*"})
public class Ingestion implements CommandLineRunner{

	private static final Logger logger = LogManager.getLogger(Ingestion.class);

	@Autowired
	IngestionCore ingestionCore;

	@Autowired
	Configuration configuration;
	
//	@Bean
//	public void runIngestion()
	
	@Override
	public void run(String... args) throws Exception {

        System.out.println("\n\n");
        logger.info("Start FAUCON Dataset Ingestion");

        if ( args.length == 0 ) {
        	logger.info("No arguments");
        }
        else {
        	
        	logger.info("Arguments: "+args[0]);
        	//configuration.setFileName(args[0]);
        	logger.info("Start Dataset Indexing");
        	ingestionCore.ingestion(configuration.getConfiguration(args[0]));
        	logger.info("End Dataset Indexing");
        	
        }
        	
        
        //Long nblines = ingestionCore.coutLine();
        //logger.info("Customers nb lines: "+nblines);

	}

	
	public static void main(String[] args) {
        //SpringApplication.run(Ingestion.class, args);
        
        ConfigurableApplicationContext ctx=new SpringApplication(Ingestion.class).run(args);
        
        //System.out.println("Do what you want");

        
//        IngestionCore ingestionCore = IngestionCore.getInstance();
//        Long nblines = ingestionCore.coutLine();
        
        
        
        int exitCode = SpringApplication.exit(ctx, new ExitCodeGenerator() {
        	@Override
        	public int getExitCode() {
        	        // return the error code
        	        return 0;
        	    }
        	});
        	 
        	System.exit(exitCode);	
	
	}


	
}
