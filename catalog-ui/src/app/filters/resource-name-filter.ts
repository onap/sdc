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

export class ResourceNameFilter {


    constructor() {
        let filter = <ResourceNameFilter>( (name:string) => {
            if (name) {
                //let newName:string =  _.last(name.split('.'));
                let newName =
                    _.last(_.last(_.last(_.last(_.last(_.last(_.last(_.last(name.split('tosca.nodes.'))
                        .split('network.')).split('relationships.')).split('org.openecomp.')).split('resource.nfv.'))
                        .split('nodes.module.')).split('cp.')).split('vl.'));
                if (newName) {
                    return newName;
                }
                return name;
            }
        });

        return filter;
    }
}
