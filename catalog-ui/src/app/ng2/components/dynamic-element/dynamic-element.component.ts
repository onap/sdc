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

import { Component, Compiler, EventEmitter, ViewContainerRef, ViewChild, Input, Output, ElementRef, ComponentRef, ComponentFactory, ComponentFactoryResolver } from '@angular/core'
import { UiElementCheckBoxComponent } from './elements-ui/checkbox/ui-element-checkbox.component';
import { UiElementDropDownComponent, DropdownValue } from './elements-ui/dropdown/ui-element-dropdown.component';
import { UiElementInputComponent } from './elements-ui/input/ui-element-input.component';
import {UiElementPopoverInputComponent} from "./elements-ui/popover-input/ui-element-popover-input.component";
import {ValidationConfiguration} from "app/models";
import {UiElementIntegerInputComponent} from "./elements-ui/integer-input/ui-element-integer-input.component";

@Component({
    selector: 'dynamic-element',
    template: `<div #target></div>`,
    styleUrls: ['./dynamic-element.component.less'],
    entryComponents: [
        UiElementInputComponent,
        UiElementDropDownComponent,
        UiElementCheckBoxComponent,
        UiElementPopoverInputComponent,
        UiElementIntegerInputComponent
    ]
})
export class DynamicElementComponent {

    @ViewChild('target', { read: ViewContainerRef }) target: any;
    @Input() type: any;
    @Input() name: string;
    @Input() readonly:boolean;
    @Input() path:string;//optional param. used only for for subnetpoolid type
    value:any;

    // Two way binding for value (need to write the "Change" word like this)
    @Output('valueChange') emitter: EventEmitter<string> = new EventEmitter<any>();
    @Input('value') set setValueValue(value) {
        this.value = value;
    }

    cmpRef: ComponentRef<any>;
    private isViewInitialized: boolean = false;
    validation = ValidationConfiguration.validation;

    constructor(
        private componentFactoryResolver: ComponentFactoryResolver,
        private compiler: Compiler,
        private el: ElementRef) {
    }

    updateComponent() {
        if (!this.isViewInitialized) {
            return;
        }
        if (this.cmpRef) {
            this.cmpRef.destroy();
        }

        // Factory to create component based on type or peroperty name.
        switch(this.type) {
            case 'list':
            case 'integer':
                this.createComponent(UiElementIntegerInputComponent);
                this.cmpRef.instance.pattern = this.validation.validationPatterns.integer;
                break;
            case 'string':
                if (this.path && this.path.toUpperCase().indexOf("SUBNETPOOLID") !== -1) {
                    if(this.name.toUpperCase().indexOf("SUBNETPOOLID") == -1){//if it's an item of subnetpoolid list get the parent name
                        let pathArray = this.path.split("#");
                        this.name = pathArray[pathArray.length - 2];
                    }
                    this.createComponent(UiElementPopoverInputComponent);
                }
                else {
                    this.createComponent(UiElementInputComponent);
                }
                break;
            case 'boolean':
                //this.createComponent(UiElementCheckBoxComponent);

                this.createComponent(UiElementDropDownComponent);

                // Build drop down values
                let tmp = [];
                tmp.push(new DropdownValue('true','TRUE'));
                tmp.push(new DropdownValue('false','FALSE'));
                this.cmpRef.instance.values = tmp;
                break;
            default:
                this.createComponent(UiElementInputComponent);
                console.log("ERROR: No ui component to handle type: " + this.type);
        }

        // Additional attributes in base element class
        if (this.cmpRef) {
            this.cmpRef.instance.name = this.name;
            this.cmpRef.instance.type = this.type;
            this.cmpRef.instance.value = this.value;
            this.cmpRef.instance.readonly = this.readonly;
        }

        // Subscribe to change event of of ui-element-basic and fire event to change the value
        this.cmpRef.instance.baseEmitter.subscribe((value):void => {
            this.emitter.emit(value)
        });

    }

    createComponent(ComponentToCreate:any):void {
        let factory = this.componentFactoryResolver.resolveComponentFactory(ComponentToCreate);
        this.cmpRef = this.target.createComponent(factory);
    }

    ngOnChanges() {
        this.updateComponent();
    }

    ngAfterContentInit() {
        //console.log("DynamicElementComponent: ngAfterContentInit: type: " + this.type + " value: " + this.value);
        this.isViewInitialized = true;
        this.updateComponent();
    }

    ngOnDestroy() {
        if (this.cmpRef) {
            this.cmpRef.destroy();
        }
    }

}
