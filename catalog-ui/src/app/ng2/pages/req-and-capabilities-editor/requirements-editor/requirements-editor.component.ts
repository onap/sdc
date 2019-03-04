import {Component} from '@angular/core';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {Requirement, RelationshipTypeModel, NodeTypeModel, CapabilityTypeModel} from 'app/models';
import {TranslateService} from 'app/ng2/shared/translator/translate.service';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
    selector: 'requirements-editor',
    templateUrl: 'requirements-editor.component.html',
    styleUrls: ['requirements-editor.component.less'],
    providers: [ServiceServiceNg2, TranslateService]
})

export class RequirementsEditorComponent {

    input: {
        requirement: Requirement,
        relationshipTypesList: Array<RelationshipTypeModel>;
        nodeTypesList: Array<NodeTypeModel>;
        capabilityTypesList: Array<CapabilityTypeModel>;
        isReadonly: boolean;
        validityChangedCallback: Function;
    };
    requirementData: Requirement;
    capabilityTypesMappedList: Array<DropdownValue>;
    relationshipTypesMappedList: Array<DropdownValue>;
    nodeTypesMappedList: Array<DropdownValue>;
    isUnboundedChecked: boolean;
    isReadonly: boolean;
    translatedUnboundTxt: string;

    constructor(private translateService: TranslateService) {
    }

    ngOnInit() {
        this.requirementData = new Requirement(this.input.requirement);
        this.requirementData.minOccurrences = this.requirementData.minOccurrences || 0;
        this.translatedUnboundTxt = '';
        this.capabilityTypesMappedList = _.map(this.input.capabilityTypesList, capType => new DropdownValue(capType.toscaPresentation.type, capType.toscaPresentation.type));
        this.relationshipTypesMappedList = _.map(this.input.relationshipTypesList, rType => new DropdownValue(rType.toscaPresentation.type, rType.toscaPresentation.type));
        this.nodeTypesMappedList = _.map(this.input.nodeTypesList, nodeType => {
            return new DropdownValue(
                nodeType.componentMetadataDefinition.componentMetadataDataDefinition.toscaResourceName,
                nodeType.componentMetadataDefinition.componentMetadataDataDefinition.toscaResourceName)
        });
        this.translateService.languageChangedObservable.subscribe(lang => {
            this.translatedUnboundTxt = this.translateService.translate('REQ_CAP_OCCURRENCES_UNBOUNDED');
            this.requirementData.maxOccurrences = this.requirementData.maxOccurrences || this.translatedUnboundTxt;
            this.isUnboundedChecked = this.requirementData.maxOccurrences === this.translatedUnboundTxt;
        });
        this.isReadonly = this.input.isReadonly;
        this.validityChanged();
    }

    onUnboundedChanged() {
        this.isUnboundedChecked = !this.isUnboundedChecked;
        this.requirementData.maxOccurrences = this.isUnboundedChecked ? this.translatedUnboundTxt : null;
        this.validityChanged();
    }

    onCapabilityChanged(selectedCapability: DropdownValue) {
        this.requirementData.capability = selectedCapability && selectedCapability.value;
        this.validityChanged();
    }

    onNodeChanged(selectedNode: DropdownValue) {
        this.requirementData.node = selectedNode && selectedNode.value;
    }

    onRelationshipChanged(selectedRelationship: DropdownValue) {
        this.requirementData.relationship = selectedRelationship && selectedRelationship.value;
    }

    checkFormValidForSubmit() {
        return this.requirementData.name && this.requirementData.name.length &&
            this.requirementData.capability && this.requirementData.capability.length && !_.isEqual(this.requirementData.minOccurrences, "") && this.requirementData.minOccurrences >= 0 &&
            (
                this.isUnboundedChecked ||
                (this.requirementData.maxOccurrences && (this.requirementData.minOccurrences <= parseInt(this.requirementData.maxOccurrences)))
            );
    }

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.input.validityChangedCallback(validState);
    }
}