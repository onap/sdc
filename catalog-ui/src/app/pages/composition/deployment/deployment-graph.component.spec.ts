import {async, ComponentFixture} from '@angular/core/testing';
import 'jest-dom/extend-expect';
import {DeploymentGraphComponent} from "./deployment-graph.component";
import {DeploymentGraphService} from "./deployment-graph.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import * as cytoscape from "cytoscape/dist/cytoscape"
import {NodesFactory} from "../../../models/graph/nodes/nodes-factory";
import {CommonGraphUtils} from "../graph/common/common-graph-utils";
import {groupsMock} from "../../../../jest/mocks/groups.mock";
import {Module} from "../../../models/modules/base-module";
import {ComponentInstance} from "../../../models/components-instances/component-instance";
import {componentInstancesMock} from "../../../../jest/mocks/component-instance.mock";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {SdcConfigToken} from "../../../config/sdc-config.config";
import {CompositionGraphLinkUtils} from "../graph/utils";

describe('DeploymentGraphComponent', () => {

    let fixture: ComponentFixture<DeploymentGraphComponent>;
    let deploymentGraphServiceMock: Partial<DeploymentGraphService>;
    let nodeFactoryServiceMock: Partial<NodesFactory>;
    let commonGraphUtilsServiceMock: Partial<CommonGraphUtils>;
    let sdcConfigTokenMock: Partial<any>;

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

    it('describes each module with the names of its member instances', () => {
        const modules: any = [{name: 'vf..base..module-0', members: {a: 'id-a', b: 'id-gone'}}, {name: 'empty'}];
        const instances: any = [{uniqueId: 'id-a', name: 'vfc_a'}];
        expect(fixture.componentInstance.describeModules(modules, instances)).toEqual([
            {name: 'vf..base..module-0', members: ['vfc_a', 'id-gone']},
            {name: 'empty', members: []}
        ]);
    });

    it('hides the canvas behind a named image and lists the modules for screen readers', () => {
        // tslint:disable-next-line:no-var-requires
        const template: string = require('./deployment-graph.component.html');
        const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');
        expect(doc.querySelector('.sdc-deployment-graph-wrapper').getAttribute('role')).toBe('img');
        expect(doc.querySelector('section.sr-only li').getAttribute('*ngFor')).toBe('let module of deploymentModules');
    });
});
