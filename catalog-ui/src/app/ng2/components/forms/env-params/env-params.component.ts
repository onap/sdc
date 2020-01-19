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

import { Component, ElementRef, Input, OnInit, ViewChild } from '@angular/core';
import { SdcUiCommon, SdcUiServices } from 'onap-ui-angular';
import { Subject } from 'rxjs/Rx';
import { ArtifactModel } from '../../../../models/artifacts';
import { CacheService } from '../../../services/cache.service';

export interface IPoint {
    x: number;
    y: number;
}

@Component({
    selector: 'env-params',
    templateUrl: './env-params.component.html',
    styleUrls: ['../../../../../assets/styles/table-style.less', './env-params.component.less']
})
export class EnvParamsComponent implements OnInit {

    @Input() public artifact: ArtifactModel;
    @Input() public isInstanceSelected: boolean;
    @Input() public isViewOnly: boolean;

    @ViewChild('textArea') textArea: ElementRef;
    private copiedWorkingArtifactHeatParameters = [];
    private onValidationChange: Subject<boolean> = new Subject();
    private displayRegexValid = SdcUiCommon.RegexPatterns.numberOrEmpty;

    // Deployment timeout in minutes
    private maxDeploymentTimeout: number = 120;
    private minDeploymentTimeout: number = 1;
    private defaultDeploymentTimeout: number = 60;

    constructor(private cacheService: CacheService, private popoverService: SdcUiServices.PopoverService) {
        const configuration = cacheService.get('UIConfiguration');
        if (configuration && configuration.heatDeploymentTimeout) {
            this.maxDeploymentTimeout = configuration.heatDeploymentTimeout.maxMinutes;
            this.minDeploymentTimeout = configuration.heatDeploymentTimeout.minMinutes;
            this.defaultDeploymentTimeout = configuration.heatDeploymentTimeout.defaultMinutes;
        }
    }

    ngOnInit(): void {
        this.copiedWorkingArtifactHeatParameters = [...this.artifact.heatParameters];
    }

    public clearCurrentValue = (name: string) => {
        this.artifact.heatParameters.filter((param) => param.name === name)[0].currentValue = '';
    }

    public timeoutChanged(timeout) {
        this.artifact.timeout = timeout;
    }

    updateFilter(event) {
        const val = event.target.value.toLowerCase();
        // filter our data
        const temp = this.copiedWorkingArtifactHeatParameters.filter((param) => {
            return !val || param.name ? param.name.toLowerCase().indexOf(val) !== -1 : -1 || param.currentValue ? param.currentValue.toLowerCase().indexOf(val) !== -1 : -1;
        });
        // update the rows
        this.artifact.heatParameters = temp;
    }

    private openPopOver = (title: string, content: string, positionOnPage: IPoint, location: string) => {
        this.popoverService.createPopOver(title, content, positionOnPage, location);
    }

    private onValidityChange = (isValid: boolean): void => {
        this.onValidationChange.next(isValid);
    }
}
