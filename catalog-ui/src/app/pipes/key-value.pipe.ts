/**
 * Created by ob0695 on 7/3/2018.
 */
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

/**
 * Created by rc2122 on 5/17/2017.
 */
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'keyValue'})
export class KeyValuePipe implements PipeTransform {
    transform(value, field: string): Array<any> {
        if(!value) {
            return null;
        }
        let keyValueObject = [];
        for (let key in value) {
            keyValueObject.push({key:key, value: value[key]});
        }
        return keyValueObject;
    }
}
