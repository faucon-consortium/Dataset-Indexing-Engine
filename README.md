# SENSU LOG DATASET - INGESTION ENGINE

The Sensu Ingestion Engine is a [Spring Boot](https://spring.io/projects/spring-boot) application. It is used to load and index the data from the sensu dataset log files. Once the data has been processed, there are then indexed in [ElasticSearch](https://www.elastic.co/fr/products/elasticsearch).

-----------------------------
### Prerequisites
+ openjdk ("11.0.2" 2019-01-15)
+ Maven (maven 3.6.3 used)
+ ElastiSearch (version 7.6.0 used)
+ You must build the **sensu-log-model.jar** (see sensu-log-model repository)
-----------------------------
### Configuration
The Sensu Dataset Ingestion Service is configured through a configuration file (**sensu_indexing_config.json**) in JSON format.  

This file is as follow:

```
{
  "elasticSearch" : {
    "hostName" : "localhost",
    "port" : 9200,
    "scheme" : "http"
  },
  "inputs" : [ {
    "index": "sensulogs",
    "directory" : "/home/chdor/Projects/FAUCON/cloud-maintenance/dataset/2020-02",
    "recursive" : false,
    "files" : [ "*" ],
    "bulkLines" : 1000
  } ]
}
```

- #### elasticSearch
  The **elasticSearch** element ( <span style="color: #fb4141">required</span>) define the connection information to an Elasticsearch Server (or cluster).

  - <ins>hostName</ins>: The FQDN of the Elasticsearch Server
  - <ins>port</ins>: The Elasticsearch port to connect 
  - <ins>scheme</ins>: The protocol used for the connection (you can configure ES to use HTTPS)

- #### Inputs
  This element is *just* a list of Input elements (see below)


- #### Input
  This element define the json files to load and the indexing parameters:
  
  + <ins>index</ins>: The name of the Elasticsearch index to store the files
  + <ins>directory</u>: The directory name which contain the files to load
  + <ins>recursive</u>: If true, the directories are browsed recursively
  + <ins>files</u>: An array of filename pattern. Wildcard are allowed as in the example
  + <ins>bulkLines</ins>: Define the number of simultaneous lines to be indexing at once, in bulk mode.
  
  
  
-----------------------------
### Compile

```
  mvn clean package
```

After the compilation phase has been successfully, you must have the **ingestion-0.1.jar** file in the **target** direcory under the project directory.

The jar generated is an executable jar including all the needed dependencies. 

-----------------------------
### Run
```
  java -jar target/ingestion-0.1.jar /home/chdor/Projects/FAUCON/workspace/sensu_indexing_config.json
```


-----------------------------
### Execution
The following trace log will be generating during the execution of the service.

```
Start FAUCON Dataset Ingestion
Arguments: /home/chdor/Projects/FAUCON/workspace/sensu_indexing_config.json
Start Dataset Indexing
Load Configuration from : /home/chdor/Projects/FAUCON/workspace/sensu_indexing_config.json
Indexing /home/chdor/Projects/FAUCON/cloud-maintenance/dataset/2020/sensu-server-2020-02-26.log
##### sensulogs ##### - Bulk Insert (1000 lines per Bulk)
- Number of <sensulogs> to index: 39259
- Number of Bulk Operations: 39
- Remaining <sensulogs> to index: 259
- Bulk Insert n°1
  -> OK
- Bulk Insert n°2
  -> OK
- Bulk Insert n°3
  -> OK
 ----
## sensulogs ## - Remaining to index: 259
-> OK
```

### Notes
You can speed the indexation by increase the number of bulklines to _insert_. It's depend of your computer configuration.
