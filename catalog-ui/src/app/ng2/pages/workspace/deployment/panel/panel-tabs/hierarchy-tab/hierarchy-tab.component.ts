import {Component, Input} from "@angular/core";
import {Component as TopologyTemplate, ComponentInstance, DisplayModule, Module, PropertyModel} from "app/models";
import {TranslateService} from "app/ng2/shared/translator/translate.service";
import {ComponentType} from "app/utils/constants";
import {WorkspaceService} from "../../../../workspace.service";
import {ModulesService} from "../../../../../../services/modules.service";
import * as _ from "lodash";
import {ModalsHandler} from "../../../../../../../utils/modals-handler";
import {ComponentFactory} from "../../../../../../../utils/component-factory";
import {Select, Store} from "@ngxs/store";
import { SdcUiServices } from "onap-ui-angular";
import { EditModuleName } from "../edit-module-name/edit-module-name.component";
import {GraphState} from "../../../../../composition/common/store/graph.state";
import {DeploymentGraphService} from "../../../../../composition/deployment/deployment-graph.service";
import {OnSidebarOpenOrCloseAction} from "../../../../../composition/common/store/graph.actions";

@   Component({
    selector: 'hierarchy-tab',
    templateUrl: './hierarchy-tab.component.html',
    styleUrls: ['./hierarchy-tab.component.less'],
})
export class HierarchyTabComponent {

    @Select(GraphState.withSidebar) withSidebar$: boolean;
    @Input() isViewOnly: boolean;
    public selectedIndex: number;
    public selectedModule: DisplayModule;
    public isLoading: boolean;
    public topologyTemplateName: string;
    public topologyTemplateType: string;
    public modules: Array<Module> = [];
    public componentInstances: Array<ComponentInstance> = [];
    private editPropertyModalTopologyTemplate: TopologyTemplate;

    constructor(private translateService: TranslateService,
                private workspaceService: WorkspaceService,
                private deploymentService: DeploymentGraphService,
                private modulesService: ModulesService,
                private ModalsHandler: ModalsHandler,
                private componentFactory: ComponentFactory,
                private store: Store,
                private popoverService: SdcUiServices.PopoverService) {
        this.isLoading = false;
        this.topologyTemplateName = this.workspaceService.metadata.name;
        this.topologyTemplateType = this.workspaceService.metadata.componentType;
    }

    ngOnInit() {
        this.modules = this.deploymentService.modules;
        this.componentInstances = this.deploymentService.componentInstances;
        this.editPropertyModalTopologyTemplate = this.componentFactory.createEmptyComponent(this.topologyTemplateType);
        this.editPropertyModalTopologyTemplate.componentInstances = this.deploymentService.componentInstances;
    }

    onModuleSelected(module: Module, componentInstanceId?: string): void {

        let onSuccess = (module: DisplayModule) => {
            console.log("Module Loaded: ", module);
            this.selectedModule = module;
            this.isLoading = false;
        };

        let onFailed = () => {
            this.isLoading = false;
        };

        if (!this.selectedModule || (this.selectedModule && this.selectedModule.uniqueId != module.uniqueId)) {
            this.isLoading = true;
            if (this.topologyTemplateType == ComponentType.SERVICE) {
                // this.selectedInstanceId = componentInstanceId;
                this.modulesService.getComponentInstanceModule(this.topologyTemplateType, this.workspaceService.metadata.uniqueId, componentInstanceId, module.uniqueId).subscribe((resultModule: DisplayModule) => {
                    onSuccess(resultModule);
                }, () => {
                    onFailed();
                });
            } else {
                this.modulesService.getModuleForDisplay(this.topologyTemplateType, this.workspaceService.metadata.uniqueId, module.uniqueId).subscribe((resultModule: DisplayModule) => {
                    onSuccess(resultModule);
                }, () => {
                    onFailed();
                });
            }
        }
    }

    updateHeatName(): void {
        this.isLoading = true;
        let originalName: string = this.selectedModule.name;
       
        this.selectedModule.updateName();
        let moduleIndex: number = _.indexOf(this.modules, _.find(this.modules, (module: Module) => {
            return module.uniqueId === this.selectedModule.uniqueId;
        }));
        
        if (moduleIndex !== -1) {
            this.modules[moduleIndex].name = this.selectedModule.name;
            this.modulesService.updateModuleMetadata(this.topologyTemplateType, this.workspaceService.metadata.uniqueId, this.modules[moduleIndex]).subscribe(() => {
                this.isLoading = false;
            }, () => {
                this.updateOriginalHeatName(originalName, moduleIndex);
                this.modules[moduleIndex].name = originalName;
            });
        } else {
                this.updateOriginalHeatName(originalName, moduleIndex);
        }
    };

    private updateOriginalHeatName(originalName: string, moduleIndex: number){
        this.isLoading = false;
        this.selectedModule.name = originalName;
        this.selectedModule.heatName = this.selectedModule.name.split('..')[1];
    }

    openEditPropertyModal(property: PropertyModel): void {
        this.editPropertyModalTopologyTemplate.setComponentMetadata(this.workspaceService.metadata);
        this.ModalsHandler.openEditModulePropertyModal(property, this.editPropertyModalTopologyTemplate, this.selectedModule, this.selectedModule.properties).then(() => {
        });
    }

    private getKeys(map: Map<any, any>) {
        return _.keys(map);
    }

    private toggleSidebarDisplay = () => {
        // this.withSidebar = !this.withSidebar;
        this.store.dispatch(new OnSidebarOpenOrCloseAction());
    }

    public openEditModuleNamePopup($event) {
        const editModuleNameInstance = this.popoverService.createPopOverWithInnerComponent('Edit Module Name', '', {x:$event.x , y:$event.y }, EditModuleName, {selectModule: _.cloneDeep(this.selectedModule)}, 'top');
        editModuleNameInstance.innerPopoverContent.instance.clickButtonEvent.subscribe((newHeatName) => {
            if(newHeatName != null){
                this.selectedModule.heatName = newHeatName;
                this.updateHeatName();
            }
            editModuleNameInstance.closePopover();
        })
    }
}
