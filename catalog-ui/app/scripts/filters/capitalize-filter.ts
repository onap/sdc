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

    export class CapitalizeFilter{

        constructor() {
            let filter = <CapitalizeFilter>( (sentence:string) => {
                if (sentence != null) {
                    let newSentence:string = "";
                    let words = sentence.split(' ');
                    for (let i=0; i < words.length; ++i){
                        let word:string = words[i].toLowerCase();
                        newSentence += word.substring(0,1).toUpperCase()+word.substring(1) + ' ';
                    }
                    return newSentence.trim();
                }else{
                    return sentence;
                }
            });

            return filter;
        }
    }

}
