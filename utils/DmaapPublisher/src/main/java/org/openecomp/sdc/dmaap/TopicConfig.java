package org.openecomp.sdc.dmaap;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TopicConfig {
    
    private String publisherPropertiesFilePath;
    private String[] topicMessages; //messages from file
    private final List<String> incomingTopicMessages = Collections.synchronizedList( new ArrayList<String>() );  //incoming messages from network stream|Main

    public String getPublisherPropertiesFilePath() {
        return publisherPropertiesFilePath;
    }
    public void setPublisherPropertiesFilePath(String publisherPropertiesFilePath) {
        this.publisherPropertiesFilePath = publisherPropertiesFilePath;
    }

    public List<String> getIncomingTopicMessages() {
        return incomingTopicMessages;
    }
    public String[] getTopicMessages() {
        return topicMessages;
    }
    //add incoming message
    public TopicConfig add( String notifications ){
        incomingTopicMessages.add( notifications);
        return this;
    }

    public TopicConfig addAll( Collection<String> notifications ){
        incomingTopicMessages.addAll( notifications );
        return this;
    }

    public void setTopicMessages(String[] topicMessages) {
        this.topicMessages = topicMessages;
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("publisherPropertiesFilePath", publisherPropertiesFilePath)
                .add("topicMessages", topicMessages)
                .toString();
    }   
    
}
