/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nordix Foundation. All rights reserved.
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

import {Component, Inject, ViewChild} from '@angular/core';
import {
  ButtonModel,
  Component as ComponentData,
  ComponentInstance,
  ModalModel,
  ToscaPresentationData
} from 'app/models';
import {SdcUiCommon, SdcUiServices} from 'onap-ui-angular';
import {TopologyTemplateService} from "../../services/component-services/topology-template.service";
import {Tab, Tabs} from "../../components/ui/tabs/tabs.component";
import * as _ from 'lodash';
import {OutputFEModel} from "../../../models/attributes-outputs/output-fe-model";
import {OutputBEModel} from "../../../models/attributes-outputs/output-be-model";
import {EVENTS, ResourceType, WorkspaceMode} from "../../../utils/constants";
import {ComponentModeService} from "../../services/component-services/component-mode.service";
import {EventListenerService} from "app/services";
import {HierarchyNavService} from "./services/hierarchy-nav.service";
import {ComponentServiceNg2} from "../../services/component-services/component.service";
import {ComponentInstanceServiceNg2} from "../../services/component-instance-services/component-instance.service";
import {KeysPipe} from "../../pipes/keys.pipe";
import {
  InstanceAttributesAPIMap,
  InstanceBeAttributesMap,
  InstanceFeAttributesMap
} from "app/models/attributes-outputs/attribute-fe-map";
import {ModalService} from "../../services/modal.service";
import {InstanceFeDetails} from "../../../models/instance-fe-details";
import {HierarchyDisplayOptions} from "../../components/logic/hierarchy-navigtion/hierarchy-display-options";
import {UnsavedChangesComponent} from "../../components/ui/forms/unsaved-changes/unsaved-changes.component";
import {SimpleFlatAttribute} from "app/models/attributes-outputs/simple-flat-attribute";
import {AttributeFEModel} from "../../../models/attributes-outputs/attribute-fe-model";
import {AttributesUtils} from "./services/attributes.utils";
import {OutputsUtils} from "app/ng2/pages/attributes-outputs/services/outputs.utils";
import {AttributesService} from "app/ng2/services/attributes.service";
import {DerivedFEAttribute} from "../../../models/attributes-outputs/derived-fe-attribute";
import {AttributeBEModel} from "../../../models/attributes-outputs/attribute-be-model";
import {AttributeCreatorComponent} from "app/ng2/pages/attributes-outputs/attribute-creator/attribute-creator.component";
import {AttributeRowSelectedEvent} from "app/ng2/components/logic/attributes-table/attributes-table.component";

const SERVICE_SELF_TITLE = "SELF";

@Component({
  selector: 'attributes-outputs',
  templateUrl: './attributes-outputs.page.component.html',
  styleUrls: ['./attributes-outputs.page.component.less', '../../../../assets/styles/table-style.less']
})
export class AttributesOutputsComponent {
  title = "Attributes & Outputs";

  @ViewChild('componentAttributesTable')
  private table: any;

  component: ComponentData;
  componentInstanceNamesMap: Map<string, InstanceFeDetails> = new Map<string, InstanceFeDetails>();//instanceUniqueId, {name, iconClass}

  attributesNavigationData = [];
  instancesNavigationData = [];

  instanceFeAttributesMap: InstanceFeAttributesMap;
  outputs: Array<OutputFEModel> = [];
  instances: Array<ComponentInstance> = [];
  searchQuery: string;
  attributeStructureHeader: string;

  selectedFlatAttribute: SimpleFlatAttribute = new SimpleFlatAttribute();
  selectedInstanceData: ComponentInstance = null;
  checkedAttributesCount: number = 0;

  hierarchyAttributesDisplayOptions: HierarchyDisplayOptions = new HierarchyDisplayOptions('path', 'name', 'childrens');
  hierarchyInstancesDisplayOptions: HierarchyDisplayOptions = new HierarchyDisplayOptions('uniqueId', 'name', 'archived', null, 'iconClass');
  searchAttributeName: string;
  currentMainTab: Tab;
  isOutputsTabSelected: boolean;
  isAttributesTabSelected: boolean;
  isReadonly: boolean;
  resourceIsReadonly: boolean;
  loadingInstances: boolean = false;
  loadingOutputs: boolean = false;
  loadingAttributes: boolean = false;
  changedData: Array<AttributeFEModel | OutputFEModel>;
  hasChangedData: boolean;
  isValidChangedData: boolean;
  savingChangedData: boolean;
  stateChangeStartUnregister: Function;
  serviceBeAttributesMap: InstanceBeAttributesMap;

  @ViewChild('hierarchyNavTabs') hierarchyNavTabs: Tabs;
  @ViewChild('attributeOutputTabs') attributeOutputTabs: Tabs;


  constructor(private attributesService: AttributesService,
              private hierarchyNavService: HierarchyNavService,
              private attributesUtils: AttributesUtils,
              private outputsUtils: OutputsUtils,
              private componentServiceNg2: ComponentServiceNg2,
              private componentInstanceServiceNg2: ComponentInstanceServiceNg2,
              @Inject("$stateParams") _stateParams,
              @Inject("$scope") private $scope: ng.IScope,
              @Inject("$state") private $state: ng.ui.IStateService,
              @Inject("Notification") private Notification: any,
              private componentModeService: ComponentModeService,
              private EventListenerService: EventListenerService,
              private ModalServiceSdcUI: SdcUiServices.ModalService,
              private ModalService: ModalService,
              private keysPipe: KeysPipe,
              private topologyTemplateService: TopologyTemplateService) {

    this.instanceFeAttributesMap = new InstanceFeAttributesMap();
    /* This is the way you can access the component data, please do not use any data except metadata, all other data should be received from the new api calls on the first time
    than if the data is already exist, no need to call the api again - Ask orit if you have any questions*/
    this.component = _stateParams.component;
    this.EventListenerService.registerObserverCallback(EVENTS.ON_LIFECYCLE_CHANGE, this.onCheckout);
    this.updateViewMode();

    this.changedData = [];
    this.updateHasChangedData();
    this.isValidChangedData = true;
  }

  ngOnInit() {
    this.loadingOutputs = true;
    this.loadingInstances = true;
    this.loadingAttributes = true;
    this.topologyTemplateService
    .getComponentOutputsWithAttributes(this.component.componentType, this.component.uniqueId)
    .subscribe(response => {
      if (response.outputs) {
        response.outputs.forEach(output => {
          const newOutput: OutputFEModel = new OutputFEModel(output);
          this.outputsUtils.resetOutputDefaultValue(newOutput, output.defaultValue);
          this.outputs.push(newOutput);
        });
      }
    }, error => {
      this.Notification.error({
        message: 'Failed to Initialize:' + error,
        title: 'Failure'
      });
    }, () => {
      this.loadingOutputs = false;
    });
    this.componentServiceNg2
    .getComponentResourceAttributesData(this.component)
    .subscribe(response => {
      this.instances = [];
      this.instances.push(...response.componentInstances);

      // add the service self instance to the top of the list.
      const serviceInstance = new ComponentInstance();
      serviceInstance.name = SERVICE_SELF_TITLE;
      serviceInstance.uniqueId = this.component.uniqueId;
      this.instances.unshift(serviceInstance);
      if (this.instances) {
        this.instances.forEach(instance => {
          this.instancesNavigationData.push(instance);
          this.componentInstanceNamesMap[instance.uniqueId] = <InstanceFeDetails>{
            name: instance.name,
            iconClass: instance.iconClass,
            originArchived: instance.originArchived
          };
        });
      }
      this.selectFirstInstanceByDefault();
    }, (error) => {
      this.Notification.error({
        message: 'Failed to Initialize:' + error,
        title: 'Failure'
      });
    }, () => {
      this.loadingInstances = false;
      this.loadingAttributes = false;
    });

    this.stateChangeStartUnregister = this.$scope.$on('$stateChangeStart', (event, toState, toParams) => {
      // stop if has changed attributes
      if (this.hasChangedData) {
        event.preventDefault();
        this.showUnsavedChangesAlert().then(() => {
          this.$state.go(toState, toParams);
        });
      }
    });
  }

  ngOnDestroy() {
    this.EventListenerService.unRegisterObserver(EVENTS.ON_LIFECYCLE_CHANGE);
    this.stateChangeStartUnregister();
  }

  selectFirstInstanceByDefault = () => {
    if (this.instancesNavigationData.length > 0) {
      this.onInstanceSelectedUpdate(this.instancesNavigationData[0]);
    }
  };

  updateViewMode = () => {
    this.isReadonly = this.componentModeService.getComponentMode(this.component) === WorkspaceMode.VIEW;
  }

  onCheckout = (component: ComponentData) => {
    this.component = component;
    this.updateViewMode();
  }

  isSelf = (): boolean => {
    return this.selectedInstanceData && this.selectedInstanceData.uniqueId == this.component.uniqueId;
  }

  getServiceAttributes() {
    this.loadingAttributes = true;
    this.topologyTemplateService
    .getServiceAttributes(this.component.uniqueId)
    .subscribe((response) => {
      this.serviceBeAttributesMap = new InstanceBeAttributesMap();
      this.serviceBeAttributesMap[this.component.uniqueId] = response;
      this.processInstanceAttributesResponse(this.serviceBeAttributesMap, false);
    }, (error) => {
      this.Notification.error({
        message: 'Failed to get Service Attribute:' + error,
        title: 'Failure'
      });
    }, () => {
      this.loadingAttributes = false;
    });
  }

  onInstanceSelectedUpdate = (instance: ComponentInstance) => {
    // stop if has changed attributes
    if (this.hasChangedData) {
      this.showUnsavedChangesAlert().then(() => {
        this.changeSelectedInstance(instance)
      });
      return;
    }
    this.changeSelectedInstance(instance);
  };

  changeSelectedInstance = (instance: ComponentInstance) => {
    this.selectedInstanceData = instance;
    this.loadingAttributes = true;
    if (instance instanceof ComponentInstance) {
      let instanceBeAttributesMap: InstanceBeAttributesMap = new InstanceBeAttributesMap();
      if (this.isOutput(instance.originType)) {
        this.componentInstanceServiceNg2
        .getComponentInstanceOutputs(this.component, instance)
        .subscribe(response => {
          instanceBeAttributesMap[instance.uniqueId] = response;
          this.processInstanceAttributesResponse(instanceBeAttributesMap, true);
        }, error => {
          this.Notification.error({
            message: 'Failed to change Selected Instance:' + error,
            title: 'Failure'
          });
        }, () => {
          this.loadingAttributes = false;
        });
      } else if (this.isSelf()) {
        this.getServiceAttributes();
      } else {
        this.componentInstanceServiceNg2
        .getComponentInstanceAttributes(this.component, instance.uniqueId)
        .subscribe(response => {
          instanceBeAttributesMap[instance.uniqueId] = response;
          this.processInstanceAttributesResponse(instanceBeAttributesMap, false);
        }, error => {
          this.Notification.error({
            message: 'Failed to change Selected Instance:' + error,
            title: 'Failure'
          });
        }, () => {
          this.loadingAttributes = false;
        });
      }

      this.resourceIsReadonly = (instance.componentName === "vnfConfiguration");
    } else {
      this.loadingAttributes = false;
    }

    //clear selected attribute from the navigation
    this.selectedFlatAttribute = new SimpleFlatAttribute();
    this.attributesNavigationData = [];
  };

  /**
   * Entry point handling response from server
   */
  processInstanceAttributesResponse = (instanceBeAttributesMap: InstanceBeAttributesMap, originTypeIsVF: boolean) => {
    this.instanceFeAttributesMap = this.attributesUtils.convertAttributesMapToFEAndCreateChildren(instanceBeAttributesMap, originTypeIsVF, this.outputs); //create flattened children, disable declared attribs, and init values
    this.checkedAttributesCount = 0;
  };


  /*** VALUE CHANGE EVENTS ***/
  dataChanged = (item: AttributeFEModel | OutputFEModel) => {
    let itemHasChanged;
    if (this.isAttributesTabSelected && item instanceof AttributeFEModel) {
      itemHasChanged = item.hasValueObjChanged();
    } else if (this.isOutputsTabSelected && item instanceof OutputFEModel) {
      itemHasChanged = item.hasChanged();
    }

    const dataChangedIdx = this.changedData.findIndex((changedItem) => changedItem === item);
    if (itemHasChanged) {
      if (dataChangedIdx === -1) {
        this.changedData.push(item);
      }
    } else {
      if (dataChangedIdx !== -1) {
        this.changedData.splice(dataChangedIdx, 1);
      }
    }

    if (this.isAttributesTabSelected) {
      this.isValidChangedData = this.changedData.every((changedItem) => (<AttributeFEModel>changedItem).valueObjIsValid);
    } else if (this.isOutputsTabSelected) {
      this.isValidChangedData = this.changedData.every((changedItem) => (<OutputFEModel>changedItem).defaultValueObjIsValid);
    }
    this.updateHasChangedData();
  };


  /*** HEIRARCHY/NAV RELATED FUNCTIONS ***/

  /**
   * Handle select node in navigation area, and select the row in table
   */
  onAttributeSelectedUpdate = ($event) => {
    this.selectedFlatAttribute = $event;
    let parentAttribute: AttributeFEModel = this.attributesService.getParentAttributeFEModelFromPath(this.instanceFeAttributesMap[this.selectedFlatAttribute.instanceName], this.selectedFlatAttribute.path);
    parentAttribute.expandedChildAttributeId = this.selectedFlatAttribute.path;
  };

  /**
   * When user select row in table, this will prepare the hierarchy object for the tree.
   */
  selectAttributeRow = (attributeRowSelectedEvent: AttributeRowSelectedEvent) => {
    let attribute = attributeRowSelectedEvent.attributeModel;
    let instanceName = attributeRowSelectedEvent.instanceName;
    this.attributeStructureHeader = null;

    // Build hierarchy tree for the navigation and update attributesNavigationData with it.
    if (!(this.selectedInstanceData instanceof ComponentInstance) || this.selectedInstanceData.originType !== ResourceType.VF) {
      let simpleFlatAttributes: Array<SimpleFlatAttribute>;
      if (attribute instanceof AttributeFEModel) {
        simpleFlatAttributes = this.hierarchyNavService.getSimpleAttributesTree(attribute, instanceName);
      } else if (attribute instanceof DerivedFEAttribute) {
        // Need to find parent AttributeFEModel
        let parentAttributeFEModel: AttributeFEModel = _.find(this.instanceFeAttributesMap[instanceName], (tmpFeAttribute): boolean => {
          return attribute.attributesName.indexOf(tmpFeAttribute.name) === 0;
        });
        simpleFlatAttributes = this.hierarchyNavService.getSimpleAttributesTree(parentAttributeFEModel, instanceName);
      }
      this.attributesNavigationData = simpleFlatAttributes;
    }

    // Update the header in the navigation tree with attribute name.
    this.attributeStructureHeader = (attribute.attributesName.split('#'))[0];

    // Set selected attribute in table
    this.selectedFlatAttribute = this.hierarchyNavService.createSimpleFlatAttribute(attribute, instanceName);
    this.hierarchyNavTabs.triggerTabChange('Attribute Structure');
  };

  tabChanged = (event) => {
    // stop if has changed attributes
    if (this.hasChangedData) {
      this.attributeOutputTabs.triggerTabChange(this.currentMainTab.title);
      this.showUnsavedChangesAlert().then(() => {
        this.attributeOutputTabs.selectTab(this.attributeOutputTabs.tabs.find((tab) => tab.title === event.title));
      });
      return;
    }

    this.currentMainTab = this.attributeOutputTabs.tabs.find((tab) => tab.title === event.title);
    this.isAttributesTabSelected = this.currentMainTab.title === "Attributes";
    this.isOutputsTabSelected = this.currentMainTab.title === "Outputs";
    this.attributeStructureHeader = null;
    this.searchQuery = '';
  };


  /*** DECLARE ATTRIBUTES/OUTPUTS ***/
  declareAttributes = (): void => {
    let selectedComponentInstancesAttributes: InstanceBeAttributesMap = new InstanceBeAttributesMap();
    let selectedComponentInstancesOutputs: InstanceBeAttributesMap = new InstanceBeAttributesMap();
    let instancesIds = this.keysPipe.transform(this.instanceFeAttributesMap, []);

    angular.forEach(instancesIds, (instanceId: string): void => {
      let selectedInstanceData: any = this.instances.find(instance => instance.uniqueId == instanceId);
      if (selectedInstanceData instanceof ComponentInstance) {
        if (!this.isOutput(selectedInstanceData.originType)) {
          // convert Attribute FE model -> Attribute BE model, extract only checked
          selectedComponentInstancesAttributes[instanceId] = this.attributesService.getCheckedAttributes(this.instanceFeAttributesMap[instanceId]);
        } else {
          selectedComponentInstancesOutputs[instanceId] = this.attributesService.getCheckedAttributes(this.instanceFeAttributesMap[instanceId]);
        }
      }
    });

    let outputsToCreate: InstanceAttributesAPIMap = new InstanceAttributesAPIMap(selectedComponentInstancesOutputs, selectedComponentInstancesAttributes);
    this.topologyTemplateService
    .createOutput(this.component, outputsToCreate, this.isSelf())
    .subscribe((response) => {
      this.setOutputTabIndication(response.length);
      this.checkedAttributesCount = 0;
      response.forEach((output: OutputBEModel) => {
        const newOutput: OutputFEModel = new OutputFEModel(output);
        this.outputsUtils.resetOutputDefaultValue(newOutput, output.defaultValue);
        this.outputs.push(newOutput);
        this.updateAttributeValueAfterDeclare(newOutput);
      });
    });
  };

  saveChangedData = (): Promise<(AttributeBEModel | OutputBEModel)[]> => {
    return new Promise((resolve, reject) => {
      if (!this.isValidChangedData) {
        reject('Changed data is invalid - cannot save!');
        return;
      }
      if (!this.changedData.length) {
        resolve([]);
        return;
      }

      // make request and its handlers
      let request;
      let handleSuccess, handleError;
      if (this.isAttributesTabSelected) {
        const changedAttribs = this.changedData.map((changedAttrib) => {
          changedAttrib = <AttributeFEModel>changedAttrib;
          const attribBE = new AttributeBEModel(changedAttrib);
          attribBE.toscaPresentation = new ToscaPresentationData();
          attribBE.toscaPresentation.ownerId = changedAttrib.parentUniqueId;
          attribBE.value = changedAttrib.getJSONValue();
          attribBE.name = changedAttrib.origName || changedAttrib.name;
          delete attribBE.origName;
          return attribBE;
        });

        if (this.selectedInstanceData instanceof ComponentInstance) {
          if (this.isSelf()) {
            console.log("changedAttribs", changedAttribs);
            request = this.topologyTemplateService.updateServiceAttributes(this.component.uniqueId, _.map(changedAttribs, cp => {
              delete cp.constraints;
              return cp;
            }));
          } else {
            request = this.componentInstanceServiceNg2
            .updateInstanceAttributes(this.component.componentType, this.component.uniqueId, this.selectedInstanceData.uniqueId, changedAttribs);
          }
          handleSuccess = (response) => {
            // reset each changed attribute with new value and remove it from changed attributes list
            response.forEach((resAttrib) => {
              const changedAttrib = <AttributeFEModel>this.changedData.shift();
              this.attributesUtils.resetAttributeValue(changedAttrib, resAttrib.value);
            });
            resolve(response);
            console.log("updated instance attributes: ", response);
          };
        }
      } else if (this.isOutputsTabSelected) {
        const changedOutputs: OutputBEModel[] = this.changedData.map((changedOutput) => {
          changedOutput = <OutputFEModel>changedOutput;
          const outputBE = new OutputBEModel(changedOutput);
          outputBE.defaultValue = changedOutput.getJSONDefaultValue();
          return outputBE;
        });
        request = this.componentServiceNg2.updateComponentOutputs(this.component, changedOutputs);
        handleSuccess = (response) => {
          // reset each changed attribute with new value and remove it from changed attributes list
          response.forEach((resOutput) => {
            const changedOutput = <OutputFEModel>this.changedData.shift();
            this.outputsUtils.resetOutputDefaultValue(changedOutput, resOutput.defaultValue);
            changedOutput.required = resOutput.required;
          });
        }
      }

      this.savingChangedData = true;
      request.subscribe(
          (response) => {
            this.savingChangedData = false;
            handleSuccess && handleSuccess(response);
            this.updateHasChangedData();
            resolve(response);
          },
          (error) => {
            this.savingChangedData = false;
            handleError && handleError(error);
            this.updateHasChangedData();
            reject(error);
          }
      );

    });
  };


  reverseChangedData = (): void => {
    // make reverse item handler
    let handleReverseItem;
    if (this.isAttributesTabSelected) {
      handleReverseItem = (changedItem) => {
        changedItem = <AttributeFEModel>changedItem;
        this.attributesUtils.resetAttributeValue(changedItem, changedItem.value);
        this.checkedAttributesCount = 0;
      };
    } else if (this.isOutputsTabSelected) {
      handleReverseItem = (changedItem) => {
        changedItem = <OutputFEModel>changedItem;
        this.outputsUtils.resetOutputDefaultValue(changedItem, changedItem.defaultValue);
        changedItem.required = changedItem.requiredOrig;
      };
    }

    this.changedData.forEach(handleReverseItem);
    this.changedData = [];
    this.updateHasChangedData();
  };

  updateHasChangedData = (): boolean => {
    const curHasChangedData: boolean = (this.changedData.length > 0);
    if (curHasChangedData !== this.hasChangedData) {
      this.hasChangedData = curHasChangedData;
      if (this.hasChangedData) {
        this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, this.hasChangedData, this.showUnsavedChangesAlert);
      } else {
        this.EventListenerService.notifyObservers(EVENTS.ON_WORKSPACE_UNSAVED_CHANGES, false);
      }
    }
    return this.hasChangedData;
  };

  doSaveChangedData = (onSuccessFunction?: Function, onError?: Function): void => {
    this.saveChangedData().then(
        () => {
          this.Notification.success({
            message: 'Successfully saved changes',
            title: 'Saved'
          });
          if (onSuccessFunction) onSuccessFunction();
          if (this.isAttributesTabSelected) {
            this.checkedAttributesCount = 0;
          }
        },
        () => {
          this.Notification.error({
            message: 'Failed to save changes!',
            title: 'Failure'
          });
          if (onError) onError();
        }
    );
  };

  showUnsavedChangesAlert = (): Promise<any> => {
    let modalTitle: string;
    if (this.isAttributesTabSelected) {
      modalTitle = `Unsaved attributes for ${this.selectedInstanceData.name}`;
    } else if (this.isOutputsTabSelected) {
      modalTitle = `Unsaved outputs for ${this.component.name}`;
    }

    return new Promise<any>((resolve, reject) => {
      this.ModalServiceSdcUI.openCustomModal(
          {
            title: modalTitle,
            size: 'sm',
            type: SdcUiCommon.ModalType.custom,
            testId: "navigate-modal",

            buttons: [
              {
                id: 'cancelButton',
                text: 'Cancel',
                type: SdcUiCommon.ButtonType.secondary,
                size: 'xsm',
                closeModal: true,
                callback: () => reject()
              },
              {
                id: 'discardButton',
                text: 'Discard',
                type: SdcUiCommon.ButtonType.secondary,
                size: 'xsm',
                closeModal: true,
                callback: () => {
                  this.reverseChangedData();
                  resolve()
                }
              },
              {
                id: 'saveButton',
                text: 'Save',
                type: SdcUiCommon.ButtonType.primary,
                size: 'xsm',
                closeModal: true,
                disabled: !this.isValidChangedData,
                callback: () => this.doSaveChangedData(resolve, reject)
              }
            ] as SdcUiCommon.IModalButtonComponent[]
          } as SdcUiCommon.IModalConfig, UnsavedChangesComponent, {isValidChangedData: this.isValidChangedData});
    });

  }

  updateAttributeValueAfterDeclare = (output: OutputFEModel) => {
    const attributeList = this.instanceFeAttributesMap[output.instanceUniqueId];
    if (attributeList) {
      const instanceName = output.instanceUniqueId.slice(output.instanceUniqueId.lastIndexOf('.') + 1);
      const attributeForUpdatingVal = attributeList.find((feAttribute: AttributeFEModel) => {
        return feAttribute.name == output.relatedAttributeName &&
            (feAttribute.name == output.relatedAttributeName || output.name === instanceName.concat('_').concat(feAttribute.name.replace(/[.]/g, '_')));
      });
      const outputPath = (output.outputPath && output.outputPath != attributeForUpdatingVal.name) ? output.outputPath : undefined;
      attributeForUpdatingVal.setAsDeclared(outputPath); //set attribute as declared before assigning value
      // this.attributesService.disableRelatedAttributes(attributeForUpdatingVal, outputPath);
      this.attributesUtils.resetAttributeValue(attributeForUpdatingVal, output.relatedAttributeValue, outputPath);
    }
  }

  //used for declare button, to keep count of newly checked attributes (and ignore declared attributes)
  updateCheckedAttributeCount = (increment: boolean): void => {
    this.checkedAttributesCount += (increment) ? 1 : -1;
  };

  setOutputTabIndication = (numOutputs: number): void => {
    this.attributeOutputTabs.setTabIndication('Outputs', numOutputs);
  };

  deleteOutput = (output: OutputFEModel) => {
    let outputToDelete = new OutputBEModel(output);

    this.componentServiceNg2
    .deleteOutput(this.component, outputToDelete)
    .subscribe(response => {
      this.outputs = this.outputs.filter(output => output.uniqueId !== response.uniqueId);

      //Reload the whole instance for now - TODO: CHANGE THIS after the BE starts returning attributes within the response, use commented code below instead!
      this.changeSelectedInstance(this.selectedInstanceData);
    }, error => {
      this.Notification.error({
        message: 'Failed to delete Output:' + error,
        title: 'Failure'
      });
    });
  };

  deleteAttribute = (attribute: AttributeFEModel) => {
    const attributeToDelete = new AttributeFEModel(attribute);
    this.loadingAttributes = true;
    const feMap = this.instanceFeAttributesMap;
    this.topologyTemplateService
    .deleteServiceAttribute(this.component.uniqueId, attributeToDelete)
    .subscribe((response) => {
      const attribs = feMap[this.component.uniqueId];
      attribs.splice(attribs.findIndex(p => p.uniqueId === response), 1);
    }, (error) => {
      this.Notification.error({
        message: 'Failed to delete Attribute:' + error,
        title: 'Failure'
      });
    }, () => {
      this.loadingAttributes = false;
    });
  }

  addAttribute = () => {
    let modalTitle = 'Add Attribute';
    let modal = this.ModalService.createCustomModal(new ModalModel(
        'sm',
        modalTitle,
        null,
        [
          new ButtonModel('Save', 'blue', () => {
            modal.instance.dynamicContent.instance.isLoading = true;
            const newAttribute: AttributeBEModel = modal.instance.dynamicContent.instance.attributeModel;
            this.topologyTemplateService.createServiceAttribute(this.component.uniqueId, newAttribute)
            .subscribe((response) => {
              modal.instance.dynamicContent.instance.isLoading = false;
              const newAttrib: AttributeFEModel = this.attributesUtils.convertAddAttributeBEToAttributeFE(response);
              this.instanceFeAttributesMap[this.component.uniqueId].push(newAttrib);
              modal.instance.close();
            }, (error) => {
              modal.instance.dynamicContent.instance.isLoading = false;
              this.Notification.error({
                message: 'Failed to add Attribute:' + error,
                title: 'Failure'
              });
            });
          }, () => !modal.instance.dynamicContent.instance.checkFormValidForSubmit()),
          new ButtonModel('Cancel', 'outline grey', () => {
            modal.instance.close();
          }),
        ],
        null
    ));
    this.ModalService.addDynamicContentToModal(modal, AttributeCreatorComponent, {});
    modal.instance.open();
  }

  private isOutput = (instanceType: string): boolean => {
    return instanceType === ResourceType.VF || instanceType === ResourceType.PNF || instanceType === ResourceType.CVFC || instanceType === ResourceType.CR;
  }

}
