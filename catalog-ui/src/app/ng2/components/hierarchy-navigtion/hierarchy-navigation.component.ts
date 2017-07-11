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

import {Component, Input, Output, EventEmitter} from '@angular/core';
import {HierarchyDisplayOptions} from './hierarchy-display-options';


@Component({
    selector: 'hierarchy-navigation',
    templateUrl: './hierarchy-navigation.component.html',
    styleUrls: ['./hierarchy-navigation.component.less']
})

export class HierarchyNavigationComponent {
    @Input() displayData: Array<any>;
    @Input() selectedItem: any;
    @Input() displayOptions: HierarchyDisplayOptions;

    @Output() updateSelected:EventEmitter<any> =  new EventEmitter();

    onClick = ($event, item) => {
        $event.stopPropagation();
        this.selectedItem = item;
        this.updateSelected.emit(item);
    };

    onSelectedUpdate = ($event) => {
        this.selectedItem = $event;
        this.updateSelected.emit($event);
    }
}
