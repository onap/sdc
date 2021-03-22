/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.server.interceptors;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.Phase;

/**
 * The type Empty output out interceptor.
 */
@Named
public class EmptyOutputOutInterceptor extends AbstractOutDatabindingInterceptor {

    public EmptyOutputOutInterceptor() {
        // To be executed in post logical phase before marshal phase
        super(Phase.POST_LOGICAL);
    }

    /**
     * Intercepts a message. Interceptors should NOT invoke handleMessage or handleFault on the next interceptor - the interceptor chain will take
     * care of this.
     *
     * @param message input message.
     */
    @Override
    public void handleMessage(Message message) {
        //get the message
        MessageContentsList objs = MessageContentsList.getContentsList(message);
        if (objs.get(0) instanceof Response) {
            //check if response is present but entity inside it is null the set a default entity
            int status = ((Response) objs.get(0)).getStatus();
            Object entity = ((Response) objs.get(0)).getEntity();
            // in case of staus 200 and entity is null send InternalEmptyObject in output.
            if (entity == null && status == 200) {
                DefaultOutput defaultOutput = new DefaultOutput(status, new InternalEmptyObject());
                defaultOutput.addMetadata(((Response) objs.get(0)).getMetadata());
                objs.set(0, defaultOutput);
            }
        }
    }
}
