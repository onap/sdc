'use strict';
import {ModalsHandler} from "app/utils";
import {PropertyModel, DisplayModule, Component, ComponentInstance, Tab, Module} from "app/models";
import {ExpandCollapseListData} from "app/directives/utils/expand-collapse-list-header/expand-collapse-list-header";

export interface IHierarchyScope extends ng.IScope {
    component:Component;
    selectedIndex:number;
    selectedModule:DisplayModule;
    singleTab:Tab;
    isLoading:boolean;
    expandCollapseArtifactsList:ExpandCollapseListData;
    expandCollapsePropertiesList:ExpandCollapseListData;
    selectedInstanceId:string;

    onModuleSelected(moduleId:string, selectedIndex:number):void;
    onModuleNameChanged(module:DisplayModule):void;
    updateHeatName():void;
    loadInstanceModules(instance:ComponentInstance):ng.IPromise<boolean>;
    openEditPropertyModal(property:PropertyModel):void;
}

export class HierarchyViewModel {

    static '$inject' = [
        '$scope',
        '$q',
        'ModalsHandler'
    ];

    constructor(private $scope:IHierarchyScope, private $q:ng.IQService, private ModalsHandler:ModalsHandler) {
        this.$scope.component = this.$scope.singleTab.data;
        this.$scope.isLoading = false;
        this.$scope.expandCollapseArtifactsList = new ExpandCollapseListData();
        this.$scope.expandCollapsePropertiesList = new ExpandCollapseListData();
        this.initScopeMethods();
    }

    private initScopeMethods():void {

        let collapseModuleData = ():void => {
            this.$scope.expandCollapseArtifactsList.expandCollapse = false;
            this.$scope.expandCollapsePropertiesList.expandCollapse = false;
            this.$scope.expandCollapseArtifactsList.orderByField = "artifactName";
            this.$scope.expandCollapsePropertiesList.orderByField = "name";
        };

        this.$scope.onModuleSelected = (moduleId:string, selectedIndex:number, componentInstanceId?:string):void => {

            let onSuccess = (module:DisplayModule) => {
                console.log("Module Loaded: ", module);
                this.$scope.selectedModule = module;
                this.$scope.isLoading = false;
                collapseModuleData();
            };

            let onFailed = () => {
                this.$scope.isLoading = false;
            };

            this.$scope.selectedIndex = selectedIndex;
            if (!this.$scope.selectedModule || (this.$scope.selectedModule && this.$scope.selectedModule.uniqueId != moduleId)) {
                this.$scope.isLoading = true;
                if (this.$scope.component.isService()) {
                    this.$scope.selectedInstanceId = componentInstanceId;
                    this.$scope.component.getModuleInstanceForDisplay(componentInstanceId, moduleId).then(onSuccess, onFailed);
                } else {
                    this.$scope.component.getModuleForDisplay(moduleId).then(onSuccess, onFailed);
                }
            }
        };

        this.$scope.updateHeatName = () => {
            this.$scope.isLoading = true;

            let originalName:string = this.$scope.selectedModule.name;

            let onSuccess = (module:Module) => {
                console.log("Module name updated:", module.name);
                this.$scope.selectedModule.name = module.name;
                this.$scope.isLoading = false;
            };

            let onFailed = () => {
                this.$scope.isLoading = false;
                this.$scope.selectedModule.name = originalName;
            };

            this.$scope.selectedModule.updateName();
            this.$scope.component.updateGroupMetadata(new DisplayModule(this.$scope.selectedModule)).then(onSuccess, onFailed);
        };

        this.$scope.openEditPropertyModal = (property:PropertyModel):void => {
            this.ModalsHandler.openEditModulePropertyModal(property, this.$scope.component, this.$scope.selectedModule).then(() => {
            });
        }
    }
}
