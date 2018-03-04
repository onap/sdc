package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.mr.client.MRConsumer;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class Dev2DevDmaapConsumerTest {
    @Autowired
    private ExecutorFactory executorFactory;
    @Autowired
    private DmaapClientFactory dmaapClientFactory;

    static ExecutorService notificationExecutor;

    static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @Test
    public void runConsumer() throws Exception{
        boolean isRunConsumer = false ;  //change this to true if you wish to run consumer,default should be false
        if ( isRunConsumer ){
            consumeDmaapTopic();
        }else{
            System.out.println( "CONSUMER TEST is disabled!!!! ");
        }
        assert true;
    }
    //@Ignore
    //@Test
    public void consumeDmaapTopic() throws Exception {
        Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("uncaughtException -> ");
            }
        };

        DmaapConsumerConfiguration dmaapConsumerParams = configurationManager.getConfiguration().getDmaapConsumerConfiguration();
        String topic = dmaapConsumerParams.getTopic();
        System.out.println(String.format( "Starting to consume topic %s for DMAAP consumer with the next parameters %s. ", topic, dmaapConsumerParams) );
        MRConsumer consumer = dmaapClientFactory.create( dmaapConsumerParams );
        notificationExecutor = executorFactory.create(topic + "Consumer", handler);
        final int LIMIT = 2;
        IntStream.range(0,LIMIT).forEach( i -> {
            System.out.println("Trying to fetch messages from topic: "+ topic);
            try {
                Iterable<String> messages = consumer.fetch();
                if (messages != null) {
                    for (String msg : messages) {
                        System.out.println(String.format( "The DMAAP message %s received. The topic is %s.", msg, topic) );
                        handleMessage(msg);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("The exception occured upon fetching DMAAP message "+ e);
            }
        }
        );


    }
    private void handleMessage(String msg){
        try{
            DmaapNotificationDataImpl notificationData = new GsonBuilder().create().fromJson(msg,DmaapNotificationDataImpl.class);
            System.out.println( "successfully parsed notification for environemnt "+notificationData.getOperationalEnvironmentId());
        }catch (Exception e){
            System.out.println( "failed to parse notification");
        }
    }
    @After
    public void after(){
        if (notificationExecutor!=null && !notificationExecutor.isTerminated())
            notificationExecutor.shutdown();
    }
}