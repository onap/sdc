import {Component} from '@angular/core';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {Capability, CapabilityTypeModel} from 'app/models';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {Subject} from "rxjs";

@Component({
    selector: 'capabilities-editor',
    templateUrl: './capabilities-editor.component.html',
    styleUrls: ['./capabilities-editor.component.less'],
    providers: [ServiceServiceNg2]
})

export class CapabilitiesEditorComponent {
    input: {
        test: string,
        capability: Capability,
        capabilityTypesList: Array<CapabilityTypeModel>,
        isReadonly: boolean;
    };
    capabilityData: Capability;
    capabilityTypesMappedList: Array<DropdownValue>;
    isUnboundedChecked: boolean;
    isReadonly: boolean;
    translatedUnboundTxt: string;

    public onValidationChange: Subject<boolean> = new Subject();

    constructor(private translateService: TranslateService) {
    }

    ngOnInit() {
        this.capabilityData = new Capability(this.input.capability);
        this.translatedUnboundTxt = '';
        this.capabilityData.minOccurrences = this.capabilityData.minOccurrences || 0;
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.translatedUnboundTxt = this.translateService.translate('REQ_CAP_OCCURRENCES_UNBOUNDED');
            this.capabilityData.maxOccurrences = this.capabilityData.maxOccurrences || this.translatedUnboundTxt;
            this.isUnboundedChecked = this.capabilityData.maxOccurrences === this.translatedUnboundTxt;
        });
        this.capabilityTypesMappedList = _.map(this.input.capabilityTypesList, capType => new DropdownValue(capType.toscaPresentation.type, capType.toscaPresentation.type));
        this.isReadonly = this.input.isReadonly;
        this.validityChanged();
    }

    onUnboundedChanged() {
        this.isUnboundedChecked = !this.isUnboundedChecked;
        this.capabilityData.maxOccurrences = this.isUnboundedChecked ? this.translatedUnboundTxt : null;
        this.validityChanged();
    }

    checkFormValidForSubmit() {
        return this.capabilityData.name && this.capabilityData.name.length &&
            this.capabilityData.type && this.capabilityData.type.length && !_.isEqual(this.capabilityData.minOccurrences, "") && this.capabilityData.minOccurrences >= 0 &&
            (
                this.isUnboundedChecked ||
                (this.capabilityData.maxOccurrences && (this.capabilityData.minOccurrences <= parseInt(this.capabilityData.maxOccurrences)))
            );
    }

    onSelectCapabilityType(selectedCapType: DropdownValue) {
        this.capabilityData.type = selectedCapType && selectedCapType.value;
        if (selectedCapType && selectedCapType.value) {
            let selectedCapabilityTypeObj: CapabilityTypeModel = this.input.capabilityTypesList.find(capType => capType.toscaPresentation.type === selectedCapType.value);
            this.capabilityData.description = selectedCapabilityTypeObj.toscaPresentation.description;
            this.capabilityData.validSourceTypes = selectedCapabilityTypeObj.toscaPresentation.validTargetTypes;
            this.capabilityData.properties = _.forEach(
                _.toArray(selectedCapabilityTypeObj.properties),
                prop => prop.uniqueId = null //a requirement for the BE
            );
        }
        this.validityChanged();
    }

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.onValidationChange.next(validState);
    }

}