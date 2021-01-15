import { Component, OnInit, ViewChild } from '@angular/core';
import { IDropDownOption } from 'onap-ui-angular/dist/form-elements/dropdown/dropdown-models';
import { InputComponent } from 'onap-ui-angular/dist/form-elements/text-elements/input/input.component';
import { Subject } from 'rxjs/Subject';
import { AttributeModel } from '../../../../models/attributes';
import { ValidationUtils } from '../../../../utils/validation-utils';
import { CacheService } from '../../../services/cache.service';
import { TranslateService } from '../../../shared/translator/translate.service';
import { AttributeOptions } from './attributes-options';

@Component({
    selector: 'attribute-modal',
    templateUrl: './attribute-modal.component.html',
    styleUrls: ['../../../../view-models/workspace/tabs/attributes/attributes.component.less']
})
export class AttributeModalComponent implements OnInit {

    @ViewChild('_default') validatedInput: InputComponent;

    public readonly types = AttributeOptions.types;                         // integer, string, boolean etc.

    public readonly booleanValues = AttributeOptions.booleanValues;         // true / false

    public readonly entrySchemaValues = AttributeOptions.entrySchemaValues; // integer, string, boolean, float

    public onValidationChange: Subject<boolean> = new Subject();

    public validationPatterns: any;
    public readonly listPattern = ValidationUtils.getPropertyListPatterns();
    public readonly mapPattern = ValidationUtils.getPropertyMapPatterns();

    // The current effective default value pattern
    public defaultValuePattern: string;
    public defaultValueErrorMessage: string;

    // Attribute being Edited
    public attributeToEdit: AttributeModel;

    constructor(private translateService: TranslateService, private cacheService: CacheService) {
        this.validationPatterns = this.cacheService.get('validation').validationPatterns;
    }

    ngOnInit() {
        this.revalidateDefaultValue();
    }

    onTypeSelected(selectedElement: IDropDownOption) {
        if (this.attributeToEdit.type !== selectedElement.value && selectedElement.value === 'boolean') {
            this.attributeToEdit._default = ''; // Clean old value in case we choose change type to boolean
        }
        this.attributeToEdit.type = selectedElement.value;
        this.revalidateDefaultValue();
    }

    onBooleanDefaultValueSelected(selectedElement: IDropDownOption) {
        if (this.attributeToEdit.type === 'boolean') {
            this.attributeToEdit._default = selectedElement.value;
        }
    }

    onEntrySchemaTypeSelected(selectedElement: IDropDownOption) {
        this.attributeToEdit.schema.property.type = selectedElement.value;
        this.revalidateDefaultValue();
    }

    onValidityChange(isValid: boolean, field: string) {
        const typeIsValid = this.attributeToEdit.type && this.attributeToEdit.type.length > 0; // Make sure type is defined

        // Make sure name is defined when other fields are changed
        let nameIsValid = true;
        if (field !== 'name') {
            nameIsValid = this.attributeToEdit.name && this.attributeToEdit.name.length > 0;
        }
        this.onValidationChange.next(isValid && nameIsValid && typeIsValid);
    }

    defaultValueChanged() {
        this.revalidateDefaultValue();
    }

    /**
     * Utility function for UI that converts a simple value to IDropDownOption
     * @param val
     * @returns {{value: any; label: any}}
     */
    toDropDownOption(val: string) {
        return { value : val, label: val };
    }

    public isMapUnique = () => {
        if (this.attributeToEdit && this.attributeToEdit.type === 'map' && this.attributeToEdit._default) {
            return ValidationUtils.validateUniqueKeys(this.attributeToEdit._default);
        }
        return true;
    }

    private revalidateDefaultValue() {
        this.setDefaultValuePattern(this.attributeToEdit.type);
        setTimeout(() => {
            if (this.validatedInput) {
                this.validatedInput.onKeyPress(this.attributeToEdit._default);
            } }, 250);
    }

    private setDefaultValuePattern(valueType: string) {
        const selectedSchemaType = this.attributeToEdit.schema.property.type;
        this.defaultValuePattern = '.*';
        switch (valueType) {
            case 'float':
                this.defaultValuePattern = this.validationPatterns.number;
                this.defaultValueErrorMessage = this.translateService.translate('VALIDATION_ERROR_TYPE', { type : 'float' });
                break;
            case 'integer':
                this.defaultValuePattern = this.validationPatterns.integerNoLeadingZero;
                this.defaultValueErrorMessage = this.translateService.translate('VALIDATION_ERROR_TYPE', { type : 'integer' });
                break;
            case 'list':
                if (selectedSchemaType != undefined) {
                    this.defaultValuePattern = this.listPattern[selectedSchemaType];
                    const listTypeStr = `list of ${selectedSchemaType}s (v1, v2, ...) `;
                    this.defaultValueErrorMessage = this.translateService.translate('VALIDATION_ERROR_TYPE', { type : listTypeStr });
                }
                break;
            case 'map':
                if (selectedSchemaType != undefined) {
                    this.defaultValuePattern = this.mapPattern[selectedSchemaType];
                    const mapTypeStr = `map of ${selectedSchemaType}s (k1:v1, k2:v2, ...)`;
                    this.defaultValueErrorMessage = this.translateService.translate('VALIDATION_ERROR_TYPE', { type : mapTypeStr });
                }
                break;
        }
    }

}
