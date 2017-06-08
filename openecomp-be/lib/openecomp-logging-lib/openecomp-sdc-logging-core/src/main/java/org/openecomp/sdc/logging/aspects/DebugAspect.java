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

package org.openecomp.sdc.logging.aspects;

import org.aspectj.lang.JoinPoint;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.Method;


/**
 * Created by TALIO on 12/26/2016.
 */
public class DebugAspect {

  private static final String MESSAGE_TEMPLATE = "'{}' '{}' with '{}'";
  private static final Marker DEBUG = MarkerFactory.getMarker("DEBUG");
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

//  @Autowired
//  private ParameterNameDiscoverer parameterNameDiscoverer;

  public void debugEnterMethod(final JoinPoint joinPoint){

    Class type = joinPoint.getSignature().getDeclaringType();
    Method currentMethod = null;
    for(Method method : type.getMethods()){
      if(method.getName().equals(joinPoint.getSignature().getName())){
        currentMethod = method;
        break;
      }
    }

//    String[] parameterNames = parameterNameDiscoverer.getParameterNames(currentMethod);
//    Object[] args = joinPoint.getArgs();
//    Parameter[] parameters = currentMethod.getParameters();
//    StringBuilder str = new StringBuilder(joinPoint.getSignature().getName() + " with parameters:" +
//        " ");
//    if(args.length == parameters.length) {
//      for (int i = 0; i < args.length; i++) {
//        str.append(parameters[i].getName()).append('=').append(args[i]);
//      }
//    }
    mdcDataDebugMessage.debugEntryMessage(null, null);
  }

  public void debugExitMethod(final JoinPoint joinPoint){
    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}
