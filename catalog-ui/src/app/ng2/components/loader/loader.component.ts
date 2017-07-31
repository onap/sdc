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
 * Created by rc2122 on 6/6/2017.
 */
import { Component, Input, ViewContainerRef, SimpleChanges} from "@angular/core";
@Component({
    selector: 'loader',
    templateUrl: './loader.component.html',
    styleUrls: ['./loader.component.less']
})
export class LoaderComponent {

    @Input() display: boolean;
    @Input() size: string;// small || medium || large
    @Input() relative: boolean; // If is relative is set to true, loader will appear over parent element. Otherwise, will be fixed over the entire page.
    @Input() loaderDelay: number; //optional - number of ms to delay loader display.
    
    private isVisible: boolean = false;
    private offset : {
        top: string;
        left: string;
        width: string;
        height: string;
    };

    constructor(private viewContainerRef: ViewContainerRef) { 
    }

    ngOnInit() {
        if (!this.size) {
            this.size = 'large';
        }
        if (this.display === true) {
            this.changeLoaderDisplay(true);
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if(changes.display){
            this.changeLoaderDisplay(this.display);
        }
    }

    private changeLoaderDisplay = (display: boolean): void => {
        if (display) {
            window.setTimeout((): void => {
                this.display && this.showLoader(); //only show loader if this.display has not changed in the meanwhile.
            }, this.loaderDelay || 0);
        } else {
            this.isVisible = false;
        }
    }

    private showLoader = () => {
        if (this.relative === true) {
            let parentElement = this.viewContainerRef.element.nativeElement.parentElement;
            this.offset = {
                left: (parentElement.offsetLeft) ? parentElement.offsetLeft + "px" : undefined,
                top: (parentElement.offsetTop) ? parentElement.offsetTop + "px" : undefined,
                width: (parentElement.offsetWidth) ? parentElement.offsetWidth + "px" : undefined,
                height: (parentElement.offsetHeight) ? parentElement.offsetHeight + "px" : undefined
            };
        }
        this.isVisible = true;
    }

}
