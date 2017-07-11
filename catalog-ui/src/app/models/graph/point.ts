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
 * Created by obarda on 11/7/2016.
 */
export class Point {
    /**
     * The two-argument constructor produces the Point(x, y).
     * @param {number} x
     * @param {number} y
     */
    constructor(x?:number, y?:number) {
        this.x = x || 0;
        this.y = y || 0;
    }

    /**Gets or sets the x value of the Point.*/
    x:number;

    /**Gets or sets the y value of the Point.*/
    y:number;
}
