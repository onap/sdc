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
 * Modifications copyright (c) 2018 Nokia
 * ================================================================================
 */

import * as _ from "lodash";
import { Component, Compiler, EventEmitter, ViewContainerRef, ViewChild, Input, Output, ElementRef, ComponentRef, ComponentFactoryResolver } from '@angular/core'
import {ValidationConfiguration} from "app/models";
import {IUiElementChangeEvent} from "../form-components/ui-element-base.component";
import {UiElementInputComponent} from "../form-components/input/ui-element-input.component";
import {UiElementPopoverInputComponent} from "../form-components/popover-input/ui-element-popover-input.component";
import {UiElementIntegerInputComponent} from "../form-components/integer-input/ui-element-integer-input.component";
import {UiElementDropDownComponent, DropdownValue} from "../form-components/dropdown/ui-element-dropdown.component";
import {PROPERTY_DATA} from "../../../../utils/constants";

enum DynamicElementComponentCreatorIdentifier {
    STRING,
    INTEGER,
    FLOAT,
    BOOLEAN,
    SUBNETPOOLID,
    DEFAULT
}

@Component({
    selector: 'dynamic-element',
    template: `<div #target></div>`,
    styleUrls: ['./dynamic-element.component.less'],
    entryComponents: [
        UiElementInputComponent,
        UiElementDropDownComponent,
        UiElementPopoverInputComponent,
        UiElementIntegerInputComponent
    ]
})
export class DynamicElementComponent {

    @ViewChild('target', { read: ViewContainerRef }) target: any;
    @Input() type: any;
    @Input() childType: any;
    @Input() name: string;
    @Input() testId: string;
    @Input() readonly:boolean;
    @Input() path:string;//optional param. used only for for subnetpoolid type

    @Input() value: any;
    @Output() valueChange: EventEmitter<any> = new EventEmitter<any>();
    @Output('elementChanged') emitter: EventEmitter<IUiElementChangeEvent> = new EventEmitter<IUiElementChangeEvent>();

    cmpRef: ComponentRef<any>;
    private isViewInitialized: boolean = false;
    private elementCreatorIdentifier: DynamicElementComponentCreatorIdentifier;
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

        // Factory to create component based on type or other property attributes.
        const prevElementCreatorIdentifier: DynamicElementComponentCreatorIdentifier = this.elementCreatorIdentifier;
        switch(true) {
            case this.path && this.path.toUpperCase().indexOf("SUBNETPOOLID") !== -1:
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.SUBNETPOOLID;
                break;
            case this.type === 'integer':
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.INTEGER;
                break;
            case this.type === 'float':
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.FLOAT;
                break;
            case PROPERTY_DATA.SCALAR_TYPES.indexOf(this.type) > -1:
            case this.type === 'string':
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.STRING;
                break;
            case this.type === 'boolean':
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.BOOLEAN;
                break;
          case this.type === 'map':
                this.createElementCreatorIdentifierForChild();
                break;
            default:
                this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.DEFAULT;
        }

        // In case the dynamic element creator is changed, then destroy old and build new.
        if (this.elementCreatorIdentifier !== prevElementCreatorIdentifier) {
            if (this.cmpRef) {
                this.cmpRef.destroy();
            }
            this.createComponentByIdentifier();
        }

        // Update attributes in base element class
        if (this.cmpRef) {
            this.cmpRef.instance.name = this.name;
            this.cmpRef.instance.type = this.type;
            this.cmpRef.instance.testId = this.testId;
            this.cmpRef.instance.value = this.value;
            this.cmpRef.instance.readonly = this.readonly;
        }
    }

    createElementCreatorIdentifierForChild():void{
      switch(this.childType) {
        case 'integer':
          this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.INTEGER;
          break;
        case 'float':
          this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.FLOAT;
          break;
        case 'string':
          this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.STRING;
          break;
        case 'boolean':
          this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.BOOLEAN;
          break;
        default:
          this.elementCreatorIdentifier = DynamicElementComponentCreatorIdentifier.DEFAULT;
      }
    }

    createComponentByIdentifier() {
        switch(this.elementCreatorIdentifier) {
            case DynamicElementComponentCreatorIdentifier.SUBNETPOOLID:
                if(this.name.toUpperCase().indexOf("SUBNETPOOLID") == -1){//if it's an item of subnetpoolid list get the parent name
                    let pathArray = this.path.split("#");
                    this.name = pathArray[pathArray.length - 2];
                }
                this.createComponent(UiElementPopoverInputComponent);
                break;

            case DynamicElementComponentCreatorIdentifier.INTEGER:
                this.createComponent(UiElementIntegerInputComponent);
                this.cmpRef.instance.pattern = this.validation.validationPatterns.integer;
                break;

            case DynamicElementComponentCreatorIdentifier.FLOAT:
                this.createComponent(UiElementIntegerInputComponent);
                this.cmpRef.instance.pattern = /^[-+]?[0-9]+(\.[0-9]+)?([eE][-+]?[0-9]+)?$/.source;
                break;

            case DynamicElementComponentCreatorIdentifier.STRING:
                this.createComponent(UiElementInputComponent);
                break;

            case DynamicElementComponentCreatorIdentifier.BOOLEAN:
                this.createComponent(UiElementDropDownComponent);

                // Build drop down values
                let tmp = [];
                tmp.push(new DropdownValue(true,'TRUE'));
                tmp.push(new DropdownValue(false,'FALSE'));
                this.cmpRef.instance.values = tmp;
                try {
                    if (typeof this.value === 'string') {
                        this.value = JSON.parse(this.value);
                    }
                } catch(err) {
                    this.value = null;
                }
                break;

            case DynamicElementComponentCreatorIdentifier.DEFAULT:
            default:
                this.createComponent(UiElementInputComponent);
                console.log("ERROR: No ui-models component to handle type: " + this.type);
        }

        // Subscribe to change event of of ui-models-element-basic and fire event to change the value
        this.cmpRef.instance.baseEmitter.subscribe((event) => { this.emitter.emit(event); });
        this.cmpRef.instance.valueChange.subscribe((event) => { this.valueChange.emit(event); });
    }

    createComponent(ComponentToCreate:any):void {
        let factory = this.componentFactoryResolver.resolveComponentFactory(ComponentToCreate);
        this.cmpRef = this.target.createComponent(factory);
    }

    ngOnChanges() {
        this.updateComponent();
    }

    ngAfterContentInit() {
        this.isViewInitialized = true;
        this.updateComponent();
    }

    ngOnDestroy() {
        if (this.cmpRef) {
            this.cmpRef.destroy();
        }
    }

}
