package org.openecomp.sdc.dmaap;

import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRPublisher.message;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.concurrent.TimeUnit;

public class DmaapPublishTool {

    private static final Logger logger = LoggerFactory.getLogger(DmaapPublishTool.class);   
    final private TopicConfig topicConfig;

    public DmaapPublishTool(String yamlPath) throws FileNotFoundException {
        topicConfig = loadTopicConfig(yamlPath);
        System.out.println("yaml file loaded.");
    }
    public DmaapPublishTool(String yamlPath , String notifications) throws FileNotFoundException {
        topicConfig = loadTopicConfig(yamlPath);
        if (StringUtils.isNotBlank(notifications) )
            topicConfig.add( notifications );
        System.out.println("yaml file loaded.");
    }

    public void addNotifications(Collection<String> notification){
        topicConfig.addAll( notification );
    }

    //safe stream doesn't throw null pointer exception
    public <T> Collection<T> safe(Collection<T> obj){
        return Optional.ofNullable(obj).orElse(Collections.emptySet());
    }
    public <T> List<T> safe(List<T> obj){
        return Optional.ofNullable(obj).orElse(Collections.emptyList());
    }

    public void publish(String path) throws IOException, InterruptedException {
        MRBatchingPublisher pub = createPublisher( topicConfig, path );
        System.out.println( "pending message count -> "+pub.getPendingMessageCount() );
        List<String> list = this.topicConfig.getIncomingTopicMessages();
        for(String msg : safe(list) ){
                publishOne( pub , msg );
        }
        closePublisher(pub);
    }

    private MRBatchingPublisher createPublisher(TopicConfig topicConfig,String path) throws IOException {
        MRBatchingPublisher publisher = MRClientFactory.createBatchingPublisher(Objects.requireNonNull(Util.toPath(path,topicConfig.getPublisherPropertiesFilePath())));
        System.out.println("publisher created.");
        return publisher;
    }

    private TopicConfig loadTopicConfig(String yamlPath) throws FileNotFoundException {
        File yamlFile = new File(Objects.requireNonNull(yamlPath));
        InputStream input = new FileInputStream(yamlFile);
        Yaml yamlHelper = new Yaml();
        return yamlHelper.loadAs(input, TopicConfig.class);
    }

    private void publishOne(MRBatchingPublisher pub, String msg) throws IOException, InterruptedException {
        System.out.println("sending:    " + msg);
        pub.send(msg);
        System.out.println("message sent.");
    }

    private void closePublisher(MRBatchingPublisher pub) throws IOException, InterruptedException {
        System.out.println("closing publisher...");
        // close the publisher to make sure everything's sent before exiting. The batching
        // publisher interface allows the app to get the set of unsent messages. It could
        // write them to disk, for example, to try to send them later.
        final List<message> stuck = pub.close(20, TimeUnit.SECONDS);
        if(!stuck.isEmpty())
        {
            final String errMsg = stuck.size() + " messages unsent";
            logger.error(errMsg);
            System.err.println(errMsg);
        }
        else
        {
            final String successMsg = "Clean exit; all messages sent.";
            logger.info(successMsg);
            System.out.println(successMsg);
        }
    }
}
