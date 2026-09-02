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

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'contentAfterLastDot' })
export class ContentAfterLastDotPipe implements PipeTransform {
    transform(value:string): string {
        // Guard against a nullish value: an intermediate CREATE state (e.g. a new map/list whose entry-schema
        // type is not yet chosen) feeds this pipe undefined, and value.split('.') would throw and blank the
        // whole value editor. Return '' instead so the row still renders. (BUG 1 / SDC-4829.)
        if (value == null) {
            return '';
        }
        return value.split('.').pop();
    }
}
