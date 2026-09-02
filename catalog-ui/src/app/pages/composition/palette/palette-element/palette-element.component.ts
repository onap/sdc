/**
 * Created by ob0695 on 6/28/2018.
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

import {Component, Input} from "@angular/core";
import {LeftPaletteComponent} from "app/models/components/displayComponent";

@Component({
    selector: 'palette-element',
    templateUrl: './palette-element.component.html',
    styleUrls: ['./palette-element.component.less']
})
export class PaletteElementComponent {

    @Input() paletteElement: LeftPaletteComponent;
}
