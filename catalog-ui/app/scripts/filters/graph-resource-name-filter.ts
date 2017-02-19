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
/// <reference path="../references"/>
module Sdc.Filters {

    export class GraphResourceNameFilter {


        constructor() {
            let filter = <GraphResourceNameFilter>( (name:string) => {
                let context = document.createElement("canvas").getContext("2d");
                context.font = "13px Arial";

                if(67 < context.measureText(name).width) {
                    let newLen = name.length - 3;
                    let newName = name.substring(0, newLen);

                    while (59 < (context.measureText(newName).width)) {
                        newName = newName.substring(0, (--newLen));
                    }
                    return newName + '...';
                }

                return name;
            });
            return filter;
        }
    }

}
