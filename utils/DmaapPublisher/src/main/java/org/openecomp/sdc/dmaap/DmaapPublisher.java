package org.openecomp.sdc.dmaap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.openecomp.sdc.dmaap.Util.*;

public class DmaapPublisher {
    private static final Logger logger = LoggerFactory.getLogger(DmaapPublisher.class);    
    private static RequestManager requestManager ;
    private static final ConcurrentLinkedDeque notificationBuffer = new ConcurrentLinkedDeque();


    private static final List<Long> registeredTasks = new CopyOnWriteArrayList<>();
    private DmaapPublisher() {}

    public static void add(String notification){
        notificationBuffer.add( notification );
    }
    public static void addAll(List<String> notifications){
        notificationBuffer.addAll( notifications );
    }
    public static void main(String[] args) {
        doPublish(args);
    }

    private static void doPublish( String[] args ) {
        CliArgs cliArgs = new CliArgs();
        CmdLineParser parser = new CmdLineParser(cliArgs);

        try {
            // parse the arguments.
            parser.parseArgument( args );
            doPublish( cliArgs );
        }
        catch(CmdLineException e) {
            logger.error("#doPublish - failed to parse arguments.", e);
            printUsage(parser, e);
            return;
        }
    }

    public static void doPublish( CliArgs cliArgs ){
        try {
            // parse the arguments.
            DmaapPublishTool tool = new DmaapPublishTool( toPath(cliArgs.getYamlPath() , cliArgs.getYamlFilename()) , cliArgs.getNotificationData()  );
            Collection<String> notifications = new ArrayList<String>( notificationBuffer );
            tool.addNotifications( notifications );
            notificationBuffer.removeAll(notifications);
            Integer concurrentRequestCount = 1;
            if ( StringUtils.isNotBlank( cliArgs.getConcurrentRequests() ) )
                concurrentRequestCount = Integer.parseInt( cliArgs.getConcurrentRequests() );
            requestManager = new RequestManager( concurrentRequestCount );

            IntStream.range(0,concurrentRequestCount).forEach( it -> {
                                        //region -  report upon finish mechanishem
                                        long ticket = System.nanoTime();
                                        registeredTasks.add( ticket );
                                        Consumer callback = ( uniqueTicket ) -> {
                                            synchronized ( registeredTasks ){
                                                registeredTasks.remove( (long)uniqueTicket );
                                                registeredTasks.notifyAll();
                                            }};

                                        RunnableReporter task = new RunnableReporter( ticket , tool , cliArgs , callback );
                                        requestManager.getExecutor().execute( task ) ;
            });
        }
        catch(NumberFormatException e) {
            logger.error("#doPublish - failed to parse argument CR.", e);
            return;
        }
        catch(Exception e) {
            logger.error("#doPublish - failed to publish.", e);
        }
    }

    public static class RunnableReporter implements Runnable{

            final private long ticket ;
            final private DmaapPublishTool tool;
            final private CliArgs cliArgs;
            final Consumer reporter;

            public RunnableReporter(final long ticket , final DmaapPublishTool tool , final CliArgs args ,  Consumer reporter){
                this.ticket = ticket ;
                this.tool = tool ;
                this.cliArgs = args ;
                this.reporter = reporter;
            }
            @Override
            public void run() {
                try {
                    tool.publish( cliArgs.getYamlPath() );
                    reporter.accept(ticket);
                }catch(IOException e){
                    logger.error("#doPublish - failed to publish.", e);
                }catch(InterruptedException e){
                    logger.error("#doPublish - cannot complete publish, thread interuppted.", e);
                    Thread.currentThread().interrupt();
                }
            }
    }


    public static List<Long> getRegisteredTasks() {
        return registeredTasks;
    }

    public static void preparePublish( String path,  String filename , String concurrentRequests ){

            CliArgs cliArgs = new CliArgs();
            if ( StringUtils.isNotBlank( filename ) )
                cliArgs.setYamlFilename( filename );
            if ( StringUtils.isNotBlank( path ) )
                cliArgs.setYamlPath( path );
            if ( NumberUtils.isCreatable( concurrentRequests ) )
                cliArgs.setConcurrentRequests(  concurrentRequests );

            doPublish( cliArgs );

    }


    private static void printUsage(CmdLineParser parser, CmdLineException e) {
        System.err.println( e.getMessage() );
        System.err.println("java DmaapPublisher [options...] arguments...");
        // print the list of available options
        parser.printUsage(System.err);
        System.err.println();
        // print option sample. This is useful some time
        System.err.println("  Example: java DmaapPublisher " + parser.printExample(OptionHandlerFilter.ALL));
        
    }
}
