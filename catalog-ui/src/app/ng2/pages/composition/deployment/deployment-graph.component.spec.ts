import {async, ComponentFixture} from '@angular/core/testing';
import 'jest-dom/extend-expect';
import {DeploymentGraphComponent} from "./deployment-graph.component";
import {DeploymentGraphService} from "./deployment-graph.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import * as cytoscape from "cytoscape/dist/cytoscape"
import {AngularJSBridge} from "../../../../services/angular-js-bridge-service";
import {NodesFactory} from "../../../../models/graph/nodes/nodes-factory";
import {CommonGraphUtils} from "../graph/common/common-graph-utils";
import {groupsMock} from "../../../../../jest/mocks/groups.mock";
import {Module} from "../../../../models/modules/base-module";
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";
import {componentInstancesMock} from "../../../../../jest/mocks/component-instance.mock";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {SdcConfigToken} from "../../../config/sdc-config.config";
import {CompositionGraphLinkUtils} from "../graph/utils";

describe('DeploymentGraphComponent', () => {

    let fixture: ComponentFixture<DeploymentGraphComponent>;
    let deploymentGraphServiceMock: Partial<DeploymentGraphService>;
    let nodeFactoryServiceMock: Partial<NodesFactory>;
    let commonGraphUtilsServiceMock: Partial<CommonGraphUtils>;
    let angularJsBridgeServiceMock: Partial<AngularJSBridge>;
    let sdcConfigTokenMock: Partial<AngularJSBridge>;

    beforeEach(
        async(() => {

            deploymentGraphServiceMock = {
                modules: <Array<Module>>groupsMock,
                componentInstances: <Array<ComponentInstance>>componentInstancesMock
            }

            nodeFactoryServiceMock = {
                createModuleNode: jest.fn().mockResolvedValue(() => {
                }),
                createNode: jest.fn().mockResolvedValue(() => {
                })
            }

            commonGraphUtilsServiceMock = {
                addNodeToGraph: jest.fn(),
                addComponentInstanceNodeToGraph: jest.fn()
            }

            sdcConfigTokenMock = {
                imagePath: ''
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [DeploymentGraphComponent],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: DeploymentGraphService, useValue: deploymentGraphServiceMock},
                        {provide: NodesFactory, useValue: nodeFactoryServiceMock},
                        {provide: TopologyTemplateService, useValue: {}},
                        {provide: WorkspaceService, useValue: {}},
                        {provide: CommonGraphUtils, useValue: commonGraphUtilsServiceMock},
                        {provide: CompositionGraphLinkUtils, useValue: {}},
                        {provide: AngularJSBridge, useValue: angularJsBridgeServiceMock},
                        {provide: SdcConfigToken, useValue: SdcConfigToken}
                    ]
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(DeploymentGraphComponent);
            });
        })
    );

    it('expected deployment graph component to be defined', () => {
        expect(fixture).toBeDefined();
    });


    it('expected to addNodeToGraph to haveBeenCalled 6 times out of 7 cause one of the instances have no parent module', () => {
        fixture.componentInstance._cy = cytoscape({
            zoomingEnabled: false,
            selectionType: 'single',
        });
        jest.spyOn(fixture.componentInstance, 'findInstanceModule');
        fixture.componentInstance.initGraphComponentInstances();
        expect(fixture.componentInstance.findInstanceModule).toHaveBeenCalledTimes(7);
        expect(commonGraphUtilsServiceMock.addComponentInstanceNodeToGraph).toHaveBeenCalledTimes(6);
    });

});