## Library for parsing yaml files

###### Requirements specified under the SDC jira ticket: [SDC-2094](https://jira.onap.org/browse/SDC-2094) - R4 5G U/C SDC: FM Meta Data GUI Display from PNF Onboarded Package

#### GAB Controller

##### Introduction
GABController is a tool for searching inside the [Yaml](https://yaml.org/) file content. At this moment is completely independent from any ONAPs component and can be used in any case where searching full json-path format keywords is needed. To use it, two things are required from the user: yaml document and list of fields to filter the results and nothing more. 

##### Usage
GABController is provided as a standard JAVA archive and can be imported in any application by adding its JAR name into the classpath or using maven dependency management:
```
<dependency>
        <groupId>org.onap.sdc.common</groupId>
        <artifactId>onap-generic-artifact-browser-service</artifactId>
        <version>${project.version}</version>
        <scope>compile</scope>
</dependency>
```
The most usable class is GABServiceImpl implements GABService and expose single method for searching paths inside the [Yaml](https://yaml.org/) file.

GABService operates on 2 different types: GABQuery as an input and GABResults as an output model.
###### Example 1 - searching for multiple keywords:
```
/* First You need to create GABService */
GABService gabService = new GABServiceImpl();

/* Next step is to prepare your query model.
 *  In this example we will use file: 
 *      '/root/test.yml' 
 *  and will search for keywords: 
 *      'event.structure.commonEventHeader' and 'event.presence' 
 */
GABQuery gabQuery = new GABQuery(Arrays.asList(
  "event.structure.commonEventHeader","event.presence"), "/root/test.yml", GABQueryType.PATH);    

/* And at last ask for the results */
GABResults gabResults = gabService.searchFor(gabQuery); 

/* And thats it. Please notice that IOException can be thrown in some cases.
 * For more info please follow specification inside the JavaDocs. 
 */    
``` 

##### Dependencies

###### Runtime:
- lombok: 1.16.16 - [Doc](https://projectlombok.org/features/all)
- snakeyaml: 1.21 - [Doc](https://bitbucket.org/asomov/snakeyaml)
- gson: 2.8.5 - [Doc](https://github.com/google/gson)
- jsurfer: 1.4.3 - [Doc](https://github.com/jsurfer/JsonSurfer)
- guava: 18.0 - [Doc](https://github.com/google/guava/wiki/Release18)
- commons-io: 2.6 - [Doc](https://commons.apache.org/proper/commons-io/)

###### Testing:    
- junit: 5.4.0 - [Doc](https://junit.org/junit5/docs/current/api/)

#### Tests structure
Generic Artifact Browser has got two independent layers of tests:
1. Unit testing using JUnit 5 (integrated in every component)
2. Component testing using Cucumber 2.3 (component-tests module) 

All layers are currently executed during the maven build of the main project.

##### Dependencies

###### Util:
- guava: 18.0 - [Doc](https://github.com/google/guava/wiki/Release18)

###### Testing:    
- junit: 5.4.0 - [Doc](https://junit.org/junit5/docs/current/api/)
- cucumber: 2.3.1 - [Doc](https://docs.cucumber.io/)