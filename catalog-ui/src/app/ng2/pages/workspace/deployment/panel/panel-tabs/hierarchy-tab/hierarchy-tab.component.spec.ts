import {async, ComponentFixture} from "@angular/core/testing";
import {HierarchyTabComponent} from "./hierarchy-tab.component";
import {ConfigureFn, configureTests} from "../../../../../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {TranslateModule} from "../../../../../../shared/translator/translate.module";
import {TopologyTemplateService} from "../../../../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../../../workspace.service";
import {ModulesService} from "../../../../../../services/modules.service";
import {GlobalPipesModule} from "../../../../../../pipes/global-pipes.module";
import {TranslateService} from "../../../../../../shared/translator/translate.service";
import {ModalsHandler} from "../../../../../../../utils/modals-handler";
import {ComponentFactory} from "../../../../../../../utils/component-factory";
import {NgxsModule} from "@ngxs/store";
import {  SdcUiServices } from "onap-ui-angular";
import {Observable} from "rxjs";
import {DisplayModule, Module} from "../../../../../../../models/modules/base-module";
import {DeploymentGraphService} from "../../../../../composition/deployment/deployment-graph.service";
import {ComponentMetadata} from "../../../../../../../models/component-metadata";

describe('HierarchyTabComponent', () => {

    let fixture: ComponentFixture<HierarchyTabComponent>;
    let workspaceService: Partial<WorkspaceService>;
    let popoverServiceMock: Partial<SdcUiServices.PopoverService>;
    let modulesServiceMock: Partial<ModulesService>;

    let editModuleNameInstanceMock = {innerPopoverContent:{instance: { clickButtonEvent: Observable.of("new heat name")}},
        closePopover: jest.fn()};
    let eventMock  = {x: 1650, y: 350};
    let moduleMock: Array<Module> = [{name: "NewVf2..base_vepdg..module-0", uniqueId: '1'}];
    let selectedModuleMock: DisplayModule = {name: "NewVf2..base_vepdg..module-0", vfInstanceName: "NewVf2", moduleName:"module-0",
                    heatName: "base_vepdg", uniqueId: '1', updateName: jest.fn().mockImplementation(() => {
                    selectedModuleMock.name = selectedModuleMock.vfInstanceName + '..' + selectedModuleMock.heatName + '..' +
                    selectedModuleMock.moduleName;})}
    let updateSelectedModuleMock = () => {
        selectedModuleMock.heatName = "base_vepdg";
        selectedModuleMock.name = "NewVf2..base_vepdg..module-0";
        fixture.componentInstance.selectedModule = selectedModuleMock;
        fixture.componentInstance.modules = moduleMock;
    }
    beforeEach(
        async(() => {

            workspaceService ={
                metadata:  <ComponentMetadata> {
                    name: '',
                    componentType: ''
                }
            }
            popoverServiceMock = {
                createPopOverWithInnerComponent: jest.fn().mockImplementation(() => {return editModuleNameInstanceMock})
            }
            modulesServiceMock = {
                updateModuleMetadata: jest.fn().mockReturnValue(Observable.of({}))
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [HierarchyTabComponent],
                    schemas: [NO_ERRORS_SCHEMA],
                    imports: [TranslateModule, NgxsModule.forRoot([]), GlobalPipesModule],
                    providers: [
                        {provide: DeploymentGraphService, useValue: {}},
                        {provide: ComponentFactory, useValue: {}},
                        {provide: TopologyTemplateService, useValue: {}},
                        {provide: WorkspaceService, useValue: workspaceService},
                        {provide: ModulesService, useValue: modulesServiceMock},
                        {provide: TranslateService, useValue: {}},
                        {provide: ModalsHandler, useValue: {}},
                        {provide: SdcUiServices.PopoverService, useValue: popoverServiceMock}
                    ]
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(HierarchyTabComponent);
            });
        })
    );

    it('expected heirarchy component to be defined', () => {
        expect(fixture).toBeDefined();
    });

    it('Update heat name and name sucessfully', () => {
        updateSelectedModuleMock();
        fixture.componentInstance.openEditModuleNamePopup(eventMock);
        expect(fixture.componentInstance.selectedModule.updateName).toHaveBeenCalled();
        expect(modulesServiceMock.updateModuleMetadata).toHaveBeenCalled();
        expect(fixture.componentInstance.selectedModule.name).toEqual('NewVf2..new heat name..module-0');
        expect(fixture.componentInstance.modules[0].name).toEqual('NewVf2..new heat name..module-0');
        expect(fixture.componentInstance.selectedModule.heatName).toEqual('new heat name');
    })
    it('Try to update heat name and name and get error from server', () => {
        updateSelectedModuleMock();
        modulesServiceMock.updateModuleMetadata.mockImplementation(() => Observable.throwError({}));
        fixture.componentInstance.openEditModuleNamePopup(eventMock);
        expect(fixture.componentInstance.selectedModule.updateName).toHaveBeenCalled();
        expect(modulesServiceMock.updateModuleMetadata).toHaveBeenCalled();
        expect(fixture.componentInstance.modules[0].name).toEqual('NewVf2..base_vepdg..module-0');
        expect(fixture.componentInstance.selectedModule.heatName).toEqual('base_vepdg');
        expect(fixture.componentInstance.selectedModule.name).toEqual('NewVf2..base_vepdg..module-0');
    })
    it('Try to update heat name and name but not find the module with the same uniqueId', () => {
        selectedModuleMock.uniqueId = '2'
        updateSelectedModuleMock();
        fixture.componentInstance.openEditModuleNamePopup(eventMock);
        expect(fixture.componentInstance.selectedModule.updateName).toHaveBeenCalled();
        expect(modulesServiceMock.updateModuleMetadata).not.toHaveBeenCalled();
        expect(fixture.componentInstance.modules[0].name).toEqual('NewVf2..base_vepdg..module-0');
        expect(fixture.componentInstance.selectedModule.heatName).toEqual('base_vepdg');
        expect(fixture.componentInstance.selectedModule.name).toEqual('NewVf2..base_vepdg..module-0');
        selectedModuleMock.uniqueId = '1'
    })
    it('Open edit  module name popover and change the heat name', () => {
        updateSelectedModuleMock();
        spyOn(fixture.componentInstance, 'updateHeatName');
        spyOn(fixture.componentInstance, 'updateOriginalHeatName');
        fixture.componentInstance.openEditModuleNamePopup(eventMock);
        expect(popoverServiceMock.createPopOverWithInnerComponent).toHaveBeenCalled();
        expect(fixture.componentInstance.selectedModule.heatName).toEqual("new heat name");
        expect(fixture.componentInstance.updateHeatName).toHaveBeenCalled();
    })


    it('Open edit  module name popover and not change the heat name', () => {
        updateSelectedModuleMock();
        editModuleNameInstanceMock.innerPopoverContent.instance.clickButtonEvent = Observable.of(null);
        fixture.componentInstance.openEditModuleNamePopup(eventMock);
        expect(popoverServiceMock.createPopOverWithInnerComponent).toHaveBeenCalled();
        expect(fixture.componentInstance.selectedModule.heatName).toEqual("base_vepdg");
    })
});