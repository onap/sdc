/*
 * Before starting the migration, please make sure to create a backup of sdctitan keyspace in cassandra
 *
 * Usage Instructions :
 * 1. Download JanusGraph gremlin in-built package from below URL;
 *    https://github.com/JanusGraph/janusgraph/releases/download/v0.3.1/janusgraph-0.3.1-hadoop2.zip
 * 2. Unzip it and navigate to bin folder.
 * 3. Run below command.
 *    Command : ./gremlin.sh -l <LOG_LEVEL> -e <Path_To_This_Script_File> <Path_To_Properties_File>
 *    Example : ./gremlin.sh -l ERROR -e /data/scripts/TitanToJanusGraphMigration.groovy /data/scripts/titan.properties
 *
 *  Note: Please make sure that the above provided property file have the below field present;
 *  graph.allow-upgrade=true
*/

// Check for open database connections; should be only one
def Object checkAndCloseMultipleInstances(Object mgmt, Object graph, long sleepTime){
    if(mgmt.getOpenInstances().size() > 1) {
        for (String instanceId in mgmt.getOpenInstances())
            if(!instanceId.contains("current"))
                mgmt.forceCloseInstance(instanceId);
        mgmt.commit();
        sleep(sleepTime);
        mgmt = graph.openManagement();
    }
    return mgmt;
}

// Update the ID Store
def updateGraphIDStore(Object mgmt, long sleepTime){
    mgmt.set('ids.store-name', 'titan_ids');
    mgmt.commit();
    sleep(sleepTime);
}

// Verify the ID Store
def verifyUpdatedGraphIDStore(String propertyPath){
    graph = JanusGraphFactory.open(propertyPath);
    mgmt = graph.openManagement();
    if(!mgmt.get('ids.store-name').equals("titan_ids"))
        throw new GroovyRuntimeException("FAILURE -> Error in setting up the ID Store to titan_ids; please contact system administrator... ");
    else
        println("SUCCESS -> Titan ID Store has also been set correctly... ");
}

try {
    graph = JanusGraphFactory.open(args[0]);
    mgmt = graph.openManagement();

    // Check if titan graph is upgraded to Janus Graph compatibility
    if(mgmt.get('graph.titan-version').equals("1.0.0"))
        throw new GroovyRuntimeException("FAILURE -> Titan graph is not upgraded to Janus. please make sure graph.allow-upgrade property is set to true in properties file and re-run the script.");
    println("SUCCESS -> Titan Graph data is upgraded to Janus compatible Graph... ");

    // Update the ID Store if required
    if(mgmt.get('ids.store-name').equals("janusgraph_ids")){
        mgmt = checkAndCloseMultipleInstances(mgmt, graph,2000l);
        updateGraphIDStore(mgmt, 2000l);
        verifyUpdatedGraphIDStore(args[0]);
    }
    println("SUCCESS -> Titan to Janus Graph upgrade process is now complete... ");

} catch(Exception ex){
    println("FAILURE -> Titan to Janus Graph migration process has failed; please check the exception trace for more details.");
    throw ex;
}
