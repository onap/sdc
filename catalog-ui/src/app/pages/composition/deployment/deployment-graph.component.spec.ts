import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import * as cytoscape from 'cytoscape/dist/cytoscape';
import 'jest-dom/extend-expect';
import { componentInstancesMock } from '../../../../jest/mocks/component-instance.mock';
import { groupsMock } from '../../../../jest/mocks/groups.mock';
import { ConfigureFn, configureTests } from '../../../../jest/test-config.helper';
import { SdcConfigToken } from '../../../config/sdc-config.config';
import { ComponentInstance } from '../../../models/components-instances/component-instance';
import { NodesFactory } from '../../../models/graph/nodes/nodes-factory';
import { Module } from '../../../models/modules/base-module';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { WorkspaceService } from '../../workspace/workspace.service';
import { CommonGraphUtils } from '../graph/common/common-graph-utils';
import { CompositionGraphLinkUtils } from '../graph/utils';
import { DeploymentGraphComponent } from './deployment-graph.component';
import { DeploymentGraphService } from './deployment-graph.service';

describe('DeploymentGraphComponent', () => {

    let fixture: ComponentFixture<DeploymentGraphComponent>;
    let deploymentGraphServiceMock: Partial<DeploymentGraphService>;
    let nodeFactoryServiceMock: Partial<NodesFactory>;
    let commonGraphUtilsServiceMock: Partial<CommonGraphUtils>;
    let sdcConfigTokenMock: Partial<any>;

    beforeEach(
        async(() => {

            deploymentGraphServiceMock = {
                modules: groupsMock as Module[],
                componentInstances: componentInstancesMock as ComponentInstance[]
            };

            nodeFactoryServiceMock = {
                createModuleNode: jest.fn().mockResolvedValue(() => {
                }),
                createNode: jest.fn().mockResolvedValue(() => {
                })
            };

            commonGraphUtilsServiceMock = {
                addNodeToGraph: jest.fn(),
                addComponentInstanceNodeToGraph: jest.fn()
            };

            sdcConfigTokenMock = {
                imagePath: ''
            };

            const configure: ConfigureFn = (testBed) => {
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

            configureTests(configure).then((testBed) => {
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
