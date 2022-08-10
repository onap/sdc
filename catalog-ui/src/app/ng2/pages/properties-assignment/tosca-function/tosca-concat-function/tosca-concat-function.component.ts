import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormArray, FormControl, FormGroup, Validators} from "@angular/forms";
import {ToscaConcatFunction} from "../../../../../models/tosca-concat-function";
import {ToscaFunctionParameter} from "../../../../../models/tosca-function-parameter";
import {ToscaStringParameter} from "../../../../../models/tosca-string-parameter";
import {ToscaFunctionType} from "../../../../../models/tosca-function-type.enum";
import {PropertyBEModel} from "../../../../../models/properties-inputs/property-be-model";
import {PROPERTY_TYPES} from "../../../../../utils/constants";
import {InstanceFeDetails} from "../../../../../models/instance-fe-details";
import {ToscaFunctionValidationEvent} from "../tosca-function.component";

@Component({
    selector: 'app-tosca-concat-function',
    templateUrl: './tosca-concat-function.component.html',
    styleUrls: ['./tosca-concat-function.component.less']
})
export class ToscaConcatFunctionComponent implements OnInit {

    @Input() toscaConcatFunction: ToscaConcatFunction;
    @Input() componentInstanceMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();
    @Output() onValidFunction: EventEmitter<ToscaConcatFunction> = new EventEmitter<ToscaConcatFunction>();
    @Output() onValidityChange: EventEmitter<ToscaConcatFunctionValidationEvent> = new EventEmitter<ToscaConcatFunctionValidationEvent>();

    concatParameterFormArray: FormArray = new FormArray([], Validators.minLength(2));
    formGroup: FormGroup = new FormGroup(
        {
            'concatParameterList': this.concatParameterFormArray
        }
    );

    parameters: ToscaFunctionParameter[] = [];
    propertyInputList: Array<PropertyBEModel> = [];
    stringProperty: PropertyBEModel

    STRING_FUNCTION_TYPE = ToscaFunctionType.STRING

    constructor() {
        this.stringProperty = new PropertyBEModel();
        this.stringProperty.type = PROPERTY_TYPES.STRING
    }

    ngOnInit() {
        this.initForm();
    }

    private initForm(): void {
        this.formGroup.valueChanges.subscribe(() => {
            this.onValidityChange.emit({
                isValid: this.formGroup.valid,
                toscaConcatFunction: this.formGroup.valid ? this.buildConcatFunctionFromForm() : undefined
            })
            if (this.formGroup.valid) {
                this.onValidFunction.emit(this.buildConcatFunctionFromForm());
            }
        });
        if (!this.toscaConcatFunction) {
            return;
        }

        if (this.toscaConcatFunction.parameters) {
            this.parameters = Array.from(this.toscaConcatFunction.parameters);
            for (const parameter of this.parameters) {
                if (parameter.type !== PROPERTY_TYPES.STRING) {
                    this.propertyInputList.push(this.createStringProperty(parameter.value));
                    this.concatParameterFormArray.push(
                        new FormControl(parameter, [Validators.required, Validators.minLength(1)])
                    );
                } else {
                    this.propertyInputList.push(undefined);
                    this.concatParameterFormArray.push(
                        new FormControl(parameter.value, [Validators.required, Validators.minLength(1)])
                    );
                }
            }
        }
    }

    private buildConcatFunctionFromForm(): ToscaConcatFunction {
        const toscaConcatFunction1 = new ToscaConcatFunction();
        this.concatParameterFormArray.controls.forEach(control => {
            const value = control.value;
            if (typeof value === 'string') {
                const stringParameter = new ToscaStringParameter();
                stringParameter.value = value;
                toscaConcatFunction1.parameters.push(stringParameter);
            } else {
                toscaConcatFunction1.parameters.push(control.value);
            }
        });

        return toscaConcatFunction1;
    }

    addFunction(): void {
        this.propertyInputList.push(this.createStringProperty());
        this.parameters.push({} as ToscaFunctionParameter);
        this.concatParameterFormArray.push(
            new FormControl(undefined, [Validators.required, Validators.minLength(1)])
        );
    }

    addStringParameter(): void {
        const toscaStringParameter = new ToscaStringParameter();
        toscaStringParameter.value = ''
        this.parameters.push(toscaStringParameter);
        this.propertyInputList.push(undefined);
        this.concatParameterFormArray.push(
            new FormControl('', [Validators.required, Validators.minLength(1)])
        );
    }

    removeParameter(position): void {
        this.propertyInputList.splice(position, 1);
        this.parameters.splice(position, 1);
        this.concatParameterFormArray.removeAt(position);
    }

    createStringProperty(value?: any): PropertyBEModel {
        const property = new PropertyBEModel();
        property.type = PROPERTY_TYPES.STRING;
        property.value = value ? value : undefined;
        return property;
    }

    onFunctionValidityChange(event: ToscaFunctionValidationEvent, index: number): void {
        if (event.isValid && event.toscaFunction) {
            this.concatParameterFormArray.controls[index].setValue(event.toscaFunction)
        } else {
            this.concatParameterFormArray.controls[index].setValue(undefined);
        }
    }
}

export interface ToscaConcatFunctionValidationEvent {
    isValid: boolean,
    toscaConcatFunction: ToscaConcatFunction,
}
