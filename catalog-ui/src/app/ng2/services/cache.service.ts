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

import {Injectable} from "@angular/core";
import {Dictionary} from "app/utils";

@Injectable()
export class CacheService {
    private storage:Dictionary<string, any>;

    constructor() {
        this.storage = new Dictionary<string, any>();
    };

    public get(key:string): any {
        return this.storage.getValue(key);
    }

    public set(key:string, value:any): void {
        this.storage.setValue(key, value);
    }

    public remove(key:string): void {
        if (this.storage.containsKey(key)) {
            this.storage.remove(key);
        }
    }

    public contains(key:string): boolean {
        return this.storage.containsKey(key);
    }
}
