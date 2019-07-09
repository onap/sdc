/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
