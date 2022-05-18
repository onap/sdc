import { Component, Input, OnInit } from '@angular/core';
import { Store } from '@ngxs/store';
import {
    AttributeModel,
    AttributesGroup,
    Component as TopologyTemplate,
    ComponentInstance,
    ComponentMetadata,
    FullComponentInstance,
    PropertiesGroup,
    PropertyModel,
    InputsGroup,
    InputModel
} from 'app/models';
import {ToscaGetFunctionType} from "app/models/tosca-get-function-type";
import { CompositionService } from 'app/ng2/pages/composition/composition.service';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { GroupByPipe } from 'app/ng2/pipes/groupBy.pipe';
import { ResourceNamePipe } from 'app/ng2/pipes/resource-name.pipe';
import { TopologyTemplateService } from 'app/ng2/services/component-services/topology-template.service';
import { ComponentInstanceServiceNg2 } from "app/ng2/services/component-instance-services/component-instance.service";
import { DropdownValue } from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import { ComponentGenericResponse } from 'app/ng2/services/responses/component-generic-response';
import { TranslateService } from 'app/ng2/shared/translator/translate.service';
import { ModalsHandler } from 'app/utils';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import {SelectedComponentType, TogglePanelLoadingAction} from "../../../common/store/graph.actions";

@Component({
    selector: 'properties-tab',
    templateUrl: './properties-tab.component.html',
    styleUrls: ['./properties-tab.component.less']
})
export class PropertiesTabComponent implements OnInit {
    attributes: AttributesGroup;
    isComponentInstanceSelected: boolean;
    properties: PropertiesGroup;
    groupPropertiesByInstance: boolean;
    propertiesMessage: string;
    metadata: ComponentMetadata;
    objectKeys = Object.keys;
    isUnboundedChecked: boolean;
    isOccurrencesEnabled: boolean = false;
    inputs: InputsGroup;
    selectInputs: DropdownValue[] = [];
    isLoading: boolean;

    @Input() isViewOnly: boolean;
    @Input() componentType: SelectedComponentType;
    @Input() component: FullComponentInstance | TopologyTemplate;
    @Input() input: {title: string};

    constructor(private store: Store,
                private workspaceService: WorkspaceService,
                private compositionService: CompositionService,
                private modalsHandler: ModalsHandler,
                private topologyTemplateService: TopologyTemplateService,
                private componentInstanceService: ComponentInstanceServiceNg2,
                private modalService: SdcUiServices.ModalService,
                private translateService: TranslateService,
                private groupByPipe: GroupByPipe) {
    }

    ngOnInit() {
        this.metadata = this.workspaceService.metadata;
        this.isComponentInstanceSelected = this.componentType === SelectedComponentType.COMPONENT_INSTANCE;
        this.getComponentInstancesPropertiesAndAttributes();
    }

    public isPropertyOwner = (): boolean => {
        return this.component instanceof TopologyTemplate && this.component.isResource();
    }

    public updateProperty = (property: PropertyModel): void => {
        this.openEditPropertyModal(property);
    }

    public deleteProperty = (property: PropertyModel): void => {

        const onOk: Function = (): void => {
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));
            this.topologyTemplateService.deleteProperty(this.component.componentType, this.component.uniqueId, property.uniqueId)
                .subscribe((response) => {
                    this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
                    this.component.properties = this.component.properties.filter((prop) => prop.uniqueId !==  property.uniqueId);
                    this.initComponentProperties();
                }, () => {
                    this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
                });
        };

        const title: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TITLE');
        const message: string = this.translateService.translate('PROPERTY_VIEW_DELETE_MODAL_TEXT', {name: property.name});
        const okButton = {
            testId: 'OK',
            text: 'OK',
            type: SdcUiCommon.ButtonType.info,
            callback: onOk,
            closeModal: true} as SdcUiComponents.ModalButtonComponent;
        this.modalService.openInfoModal(title, message, 'delete-modal', [okButton]);
    }

    public groupNameByKey = (key: string): string => {
        switch (key) {
            case 'derived':
                return 'Derived';

            case this.metadata.uniqueId:
                return ResourceNamePipe.getDisplayName(this.metadata.name);

            default:
                return this.getComponentInstanceNameFromInstanceByKey(key);
        }
    }

    public getComponentInstanceNameFromInstanceByKey = (key: string): string => {
        let instanceName: string = '';
        const componentInstance = this.compositionService.getComponentInstances().find((item) => item.uniqueId === key);
        if (key !== undefined && componentInstance) {

            instanceName = ResourceNamePipe.getDisplayName(componentInstance.name);
        }
        return instanceName;
    }

    private getComponentInstancesPropertiesAndAttributes = () => {
        this.topologyTemplateService.getComponentInstanceAttributesAndPropertiesAndInputs(
            this.workspaceService.metadata.uniqueId,
            this.workspaceService.metadata.componentType)
            .subscribe((genericResponse: ComponentGenericResponse) => {
                this.compositionService.componentInstancesAttributes = genericResponse.componentInstancesAttributes || new AttributesGroup();
                this.compositionService.componentInstancesProperties = genericResponse.componentInstancesProperties;
                this.inputs = genericResponse.inputs;
                this.initPropertiesAndAttributes();
            });
    }

    private initComponentProperties = (): void => {
        let result: PropertiesGroup = {};

        this.propertiesMessage = undefined;
        this.groupPropertiesByInstance = false;
        if (this.component instanceof FullComponentInstance) {
            result[this.component.uniqueId] = _.orderBy(this.compositionService.componentInstancesProperties[this.component.uniqueId], ['name']);
            if (this.component.originType === 'VF') {
                this.groupPropertiesByInstance = true;
                result[this.component.uniqueId] = Array.from(this.groupByPipe.transform(result[this.component.uniqueId], 'path'));
            }
        } else if (this.metadata.isService()) {
            // Temporally fix to hide properties for service (UI stack when there are many properties)
            result = this.compositionService.componentInstancesProperties;
            this.propertiesMessage = 'Note: properties for service are disabled';
        } else {
            const componentUid = this.component.uniqueId;
            result[componentUid] = Array<PropertyModel>();
            const derived = Array<PropertyModel>();
            _.forEach(this.component.properties, (property: PropertyModel) => {
                if (componentUid === property.parentUniqueId) {
                    result[componentUid].push(property);
                } else {
                    property.readonly = true;
                    derived.push(property);
                }
            });
            if (derived.length) {
                result['derived'] = derived;
            }
            this.objectKeys(result).forEach((key) => { result[key] =  _.orderBy(result[key], ['name']); });
        }
        this.properties = result;
    }

    private initComponentAttributes = (): void => {
        let result: AttributesGroup = {};

        if (this.component) {
            if (this.component instanceof FullComponentInstance) {
                result[this.component.uniqueId] = this.compositionService.componentInstancesAttributes[this.component.uniqueId] || [];
            } else if (this.metadata.isService()) {
                result = this.compositionService.componentInstancesAttributes;
            } else {
                result[this.component.uniqueId] = (this.component as TopologyTemplate).attributes;
            }
            this.attributes = result;
            this.objectKeys(this.attributes).forEach((key) => {
                this.attributes[key] =  _.orderBy(this.attributes[key], ['name']);
            });

        }
    }

    private initComponentOccurrences = (): void => {
        if (this.component instanceof FullComponentInstance) {
            if(this.component.minOccurrences != null && this.component.maxOccurrences != null){
                this.isOccurrencesEnabled = true;
            }
            this.isUnboundedChecked = this.component.maxOccurrences == "UNBOUNDED" ? true: false;

            if(!this.component.instanceCount){
                this.component.instanceCount = "";
            }

            _.forEach(this.inputs, (input: InputModel) => {
                if(input.type === "integer"){
                    this.selectInputs.push(new DropdownValue('{' + ToscaGetFunctionType.GET_INPUT.toLowerCase() + ":" + input.name + '}', input.name));
                }
            });

            this.selectInputs.unshift(new DropdownValue('', 'Select Input...'));
        }
    }

    /**
     * This function is checking if the component is the value owner of the current property
     * in order to notify the edit property modal which fields to disable
     */
    private isPropertyValueOwner = (): boolean => {
        return this.metadata.isService() || !!this.component;
    }

    /**
     *  The function opens the edit property modal.
     *  It checks if the property is from the VF or from one of it's resource instances and sends the needed property list.
     *  For create property reasons an empty array is transferd
     *
     * @param property the wanted property to edit/create
     */
    private openEditPropertyModal = (property: PropertyModel): void => {
        this.modalsHandler.newOpenEditPropertyModal(property,
            (this.isPropertyOwner() ?
                this.properties[property.parentUniqueId] :
                this.properties[property.resourceInstanceUniqueId]) || [],
            this.isPropertyValueOwner(), 'component', property.resourceInstanceUniqueId).then((updatedProperty: PropertyModel) => {
                if (updatedProperty) {
                    const oldProp = _.find(this.properties[updatedProperty.resourceInstanceUniqueId],
                                 (prop: PropertyModel) => prop.uniqueId === updatedProperty.uniqueId);
                    oldProp.value = updatedProperty.value;
                }
        });
    }

    private initPropertiesAndAttributes = (): void => {
        this.initComponentProperties();
        this.initComponentAttributes();
        this.initComponentOccurrences();
    }

    onUnboundedChanged(component: ComponentInstance) {
        this.isUnboundedChecked = !this.isUnboundedChecked;
        component.maxOccurrences = this.isUnboundedChecked ? "UNBOUNDED" : "1";
    }

    private updateComponentInstance(component: ComponentInstance) {
        this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));

        this.componentInstanceService.updateComponentInstance(this.workspaceService.metadata.componentType,
                                                              this.workspaceService.metadata.uniqueId, component)
                                                              .subscribe((updatedComponentInstance: ComponentInstance) => {
            component = new ComponentInstance(updatedComponentInstance);
            this.compositionService.getComponentInstances().find((item) => item.uniqueId === component.uniqueId).maxOccurrences = component.maxOccurrences;
            this.compositionService.getComponentInstances().find((item) => item.uniqueId === component.uniqueId).minOccurrences = component.minOccurrences;
            this.compositionService.getComponentInstances().find((item) => item.uniqueId === component.uniqueId).instanceCount = component.instanceCount;
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
        }, (error:any) => {
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
            if (error) {
                console.log(error);
            }});
    }

    private enableOccurrences = () => {
        if(this.component instanceof FullComponentInstance){
            if(!this.isOccurrencesEnabled){
                this.component.minOccurrences = null;
                this.component.maxOccurrences = null;
                this.component.instanceCount = null;
            } else {
                this.component.minOccurrences = "1";
                this.component.maxOccurrences = "1";
                this.component.instanceCount = "";
            }
        }
    }

    private isOccurrencesFormValid(component: FullComponentInstance) {
        if(
            (component.minOccurrences === null && component.maxOccurrences === null && !component.instanceCount) ||
            (component.minOccurrences && parseInt(component.minOccurrences) >= 0 && component.maxOccurrences &&
            (parseInt(component.maxOccurrences) >= parseInt(component.minOccurrences) || component.maxOccurrences === "UNBOUNDED") &&
            component.instanceCount)
        ) {
            return true;
        } else {
            return false;
        }
    }

    private saveOccurrences = () => {
        if(this.component instanceof FullComponentInstance && this.isOccurrencesFormValid(this.component)) {
            this.updateComponentInstance(this.component);
        }
    }
}
