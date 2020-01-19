/**
 * Created by ob0695 on 4/18/2018.
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

import {Component, Input, Inject, forwardRef} from "@angular/core";
import {TranslateService} from "../../shared/translator/translate.service";
import {ServiceContainerToUpgradeUiObject} from "./automated-upgrade-models/ui-component-to-upgrade";
import {AutomatedUpgradeService} from "./automated-upgrade.service";

@Component({
    selector: 'upgrade-vsp',
    templateUrl: './automated-upgrade.component.html',
    styleUrls: ['./automated-upgrade.component.less']
})
export class AutomatedUpgradeComponent {

    @Input() componentsToUpgrade: Array<ServiceContainerToUpgradeUiObject>;
    @Input() certificationStatusText: string;
    @Input() informationText: string;
    @Input() disabled: string;
    private selectedComponentsToUpgrade: Array<string> = [];

    constructor (@Inject(forwardRef(() => AutomatedUpgradeService)) private automatedUpgradeService: AutomatedUpgradeService) {
    }

    ngOnInit(): void  { // We need to check all elements that needed upgrade as default
        this.selectedComponentsToUpgrade = _.filter(this.componentsToUpgrade, (componentToUpgrade:ServiceContainerToUpgradeUiObject) => {
                return !componentToUpgrade.isAlreadyUpgrade && !componentToUpgrade.isLock;
        }).map(element => element.uniqueId)
    }

    onComponentSelected  = (uniqueId:string):void => {

        if(this.selectedComponentsToUpgrade.indexOf(uniqueId) > -1) {
            this.selectedComponentsToUpgrade = _.without(this.selectedComponentsToUpgrade, uniqueId);
        } else  {
            this.selectedComponentsToUpgrade.push(uniqueId);
        }
        if(this.selectedComponentsToUpgrade.length === 0) {
            this.automatedUpgradeService.changeUpgradeButtonState(true);
        } else {
            this.automatedUpgradeService.changeUpgradeButtonState(false);
        }
    }
}
