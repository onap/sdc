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
module Sdc.Filters {

    export class TruncateFilter {
        constructor() {
            let filter = <TruncateFilter> (str:string, length:number) => {
                if (str.length <= length) {
                    return str;
                }

                //if(str[length - 1] === ' '){
                //    return str.substring(0, length - 1) + '...';
                //}

                let char;
                let index = length;
                while (char !== ' ' && index !== 0) {
                    index--;
                    char = str[index];
                }
                if (index === 0) {
                    return (index === 0) ? str : str.substring(0, length - 3) + '...';
                }
                return (index === 0) ? str : str.substring(0, index) + '...';
            };
            return filter;
        }

    }
}
