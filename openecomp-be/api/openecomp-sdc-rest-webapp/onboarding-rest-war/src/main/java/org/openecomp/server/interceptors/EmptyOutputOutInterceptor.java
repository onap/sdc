/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.server.interceptors;

import org.apache.cxf.interceptor.AbstractOutDatabindingInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.Phase;

import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
public class EmptyOutputOutInterceptor extends AbstractOutDatabindingInterceptor {

  public EmptyOutputOutInterceptor() {
    // To be executed in post logical phase before marshal phase
    super(Phase.POST_LOGICAL);
  }

  /**
   * Intercepts a message.
   * Interceptors should NOT invoke handleMessage or handleFault
   * on the next interceptor - the interceptor chain will
   * take care of this.
   *
   * @param message input message.
   */
  public void handleMessage(Message message) throws Fault {
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
