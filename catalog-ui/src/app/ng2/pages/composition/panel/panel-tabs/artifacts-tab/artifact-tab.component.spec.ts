import { async, ComponentFixture } from '@angular/core/testing';
import { ConfigureFn, configureTests } from '../../../../../../../jest/test-config.helper';
import { NgxsModule, Store } from '@ngxs/store';
import { WorkspaceState } from '../../../../../store/states/workspace.state';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ArtifactsTabComponent } from './artifacts-tab.component';
import { CompositionService } from '../../../composition.service';
import { WorkspaceService } from '../../../../workspace/workspace.service';
import { ComponentInstanceServiceNg2 } from '../../../../../services/component-instance-services/component-instance.service';
import { TopologyTemplateService } from '../../../../../services/component-services/topology-template.service';
import { ArtifactsService } from '../../../../../components/forms/artifacts-form/artifacts.service';
import { ArtifactModel } from '../../../../../../models/artifacts';
import { ArtifactType } from '../../../../../../utils/constants';
import { FullComponentInstance } from '../../../../../../models/componentsInstances/fullComponentInstance';
import { ComponentInstance } from '../../../../../../models/componentsInstances/componentInstance';
import { Component } from '../../../../../../models/components/component';
import { GetInstanceArtifactsByTypeAction } from '../../../../../store/actions/instance-artifacts.actions';
import { Observable } from 'rxjs';


describe('artifact-tab component', () => {

    let fixture: ComponentFixture<ArtifactsTabComponent>;
    let compositionMockService: Partial<CompositionService>;
    const workspaceMockService: Partial<WorkspaceService>;
    const componentInstanceMockService: Partial<ComponentInstanceServiceNg2>;
    const topologyTemplateMockService: Partial<TopologyTemplateService>;
    let artifactsServiceMockService: Partial<ArtifactsService>;
    let store: Store;

    beforeEach(
        async(() => {
            compositionMockService = {
                updateInstance:  jest.fn()
            }

            artifactsServiceMockService = {
                deleteArtifact: jest.fn(),
                openUpdateEnvParams: jest.fn(),
                openArtifactModal: jest.fn()
            }

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [ArtifactsTabComponent],
                    imports: [NgxsModule.forRoot([WorkspaceState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: CompositionService, useValue: compositionMockService},
                        {provide: WorkspaceService, useValue: workspaceMockService},
                        {provide: ComponentInstanceServiceNg2, useValue: componentInstanceMockService},
                        {provide: TopologyTemplateService, useValue: topologyTemplateMockService},
                        {provide: ArtifactsService, useValue: artifactsServiceMockService}
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(ArtifactsTabComponent);
                store = testBed.get(Store);
            });
        })
    );

    it ('on delete -> deleteArtifact is being called from artifactService', () => {
        const artifact = new ArtifactModel();
        const topologyTemplateType: string = undefined;
        const topologyTemplateId: string = undefined;

        fixture.componentInstance.delete(artifact);
        expect(artifactsServiceMockService.deleteArtifact).toHaveBeenCalledWith(topologyTemplateType, topologyTemplateId, artifact);
    });

    it('should match current snapshot of artifact-tab component', () => {
        expect(fixture).toMatchSnapshot();
    });


    it ('should get API Artifacts as Title', () => {
        const artifactType = ArtifactType.SERVICE_API;

        const res = fixture.componentInstance.getTitle(artifactType);
        expect(res).toBe('API Artifacts');
    });


    it ('should get Deployment Artifacts as Title', () => {
        const artifactType = ArtifactType.DEPLOYMENT;

        const res = fixture.componentInstance.getTitle(artifactType);
        expect(res).toBe('Deployment Artifacts');
    });

    it ('should get Informational Artifacts as Title', () => {
        const artifactType = ArtifactType.INFORMATION;

        const res = fixture.componentInstance.getTitle(artifactType);
        expect(res).toBe('Informational Artifacts');
    });

    it ('should get SomeString as Title - This is the default case (return the last val)', () => {
        // So the last value will be "SomeString"
        fixture.componentInstance.getTitle('SomeString');

        const res = fixture.componentInstance.getTitle('SomeString');
        expect(res).toBe('SomeString Artifacts');
    });


    it ('should return isLicenseArtifact false', () => {
        const artifact = new ArtifactModel();
        const componentInstance = new ComponentInstance();
        const component = new Component();
        fixture.componentInstance.component = new FullComponentInstance(componentInstance, component);

        let res = fixture.componentInstance.isLicenseArtifact(artifact);
        expect(res).toBe(false);
    });

    it ('should return isLicenseArtifact true', () => {
        const artifact = new ArtifactModel();
        const componentInstance = new ComponentInstance();
        const component = new Component();
        fixture.componentInstance.component = new FullComponentInstance(componentInstance, component);
        fixture.componentInstance.component.isResource =  jest.fn(() => true);
        fixture.componentInstance.component.isCsarComponent =  true;

        artifact.artifactType = ArtifactType.VENDOR_LICENSE;
        const res = fixture.componentInstance.isLicenseArtifact(artifact);
        expect(res).toBe(true);
    });

    it ('should verify getEnvArtifact with match', () => {
        const artifact = new ArtifactModel();
        artifact.uniqueId = 'matchUniqueID';

        const testItem1 = new ArtifactModel();
        testItem1.generatedFromId = 'matchUniqueID';

        const testItem2 = new ArtifactModel();
        testItem2.generatedFromId = '123456';

        const artifacts: ArtifactModel[] = [testItem1, testItem2];

        const res = fixture.componentInstance.getEnvArtifact(artifact, artifacts);
        expect(res.generatedFromId).toBe('matchUniqueID');
    });

    it ('should verify getEnvArtifact with no match', () => {
        const artifact = new ArtifactModel();
        artifact.uniqueId = 'matchUniqueID';

        const testItem1 = new ArtifactModel();
        testItem1.generatedFromId = '654321';

        const testItem2 = new ArtifactModel();
        testItem2.generatedFromId = '123456';

        const artifacts: ArtifactModel[] = [testItem1, testItem2];

        const res = fixture.componentInstance.getEnvArtifact(artifact, artifacts);
        expect(res).toBe(undefined);
    });

    it ('on updateEnvParams -> openUpdateEnvParams is being called from artifactService when isComponentInstanceSelected = true', () => {
        const artifact = new ArtifactModel();
        artifact.envArtifact = new ArtifactModel();

        const topologyTemplateType: string = undefined;
        const topologyTemplateId: string = undefined;

        const component = new Component();
        component.uniqueId = 'id';

        const isComponentInstanceSelected = true;

        fixture.componentInstance.component = component;
        fixture.componentInstance.isComponentInstanceSelected = isComponentInstanceSelected;
        fixture.componentInstance.updateEnvParams(artifact);

        expect(artifactsServiceMockService.openUpdateEnvParams).toHaveBeenCalledWith(topologyTemplateType, topologyTemplateId, undefined, component.uniqueId);
    });

    it ('on updateEnvParams -> openUpdateEnvParams is being called from artifactService when isComponentInstanceSelected = false', () => {
        const artifact = new ArtifactModel();

        const topologyTemplateType: string = undefined
        const topologyTemplateId: string = undefined;

        const component = new Component();

        const isComponentInstanceSelected = false;

        fixture.componentInstance.component = component;
        fixture.componentInstance.isComponentInstanceSelected = isComponentInstanceSelected;
        fixture.componentInstance.updateEnvParams(artifact);

        expect(artifactsServiceMockService.openUpdateEnvParams).toHaveBeenCalledWith(topologyTemplateType, topologyTemplateId, artifact);
    });

    it ('on addOrUpdate -> openArtifactModal is being called from artifactService when isComponentInstanceSelected = true', () => {
        const artifact = new ArtifactModel();

        const topologyTemplateType: string = 'testType';
        const topologyTemplateId: string = 'testID';
        const type: string = 'testType';
        const isViewOnly: boolean = false;

        const component = new Component();
        component.uniqueId = 'id';

        const isComponentInstanceSelected = true;

        fixture.componentInstance.component = component;
        fixture.componentInstance.type = type;
        fixture.componentInstance.topologyTemplateId = topologyTemplateId;
        fixture.componentInstance.topologyTemplateType = topologyTemplateType;
        fixture.componentInstance.isComponentInstanceSelected = isComponentInstanceSelected;
        fixture.componentInstance.isViewOnly = isViewOnly;
        fixture.componentInstance.addOrUpdate(artifact);


        expect(artifactsServiceMockService.openArtifactModal).toHaveBeenCalledWith(topologyTemplateId, topologyTemplateType, artifact, type, isViewOnly, component.uniqueId);
    });

    it ('on addOrUpdate -> openArtifactModal is being called from artifactService when isComponentInstanceSelected = false', () => {
        const artifact = new ArtifactModel();

        const topologyTemplateType: string = 'testType';
        const topologyTemplateId: string = 'testID';
        const type: string = 'testType';
        const isViewOnly: boolean = false;

        const isComponentInstanceSelected = false;

        fixture.componentInstance.type = type;
        fixture.componentInstance.isComponentInstanceSelected = isComponentInstanceSelected;
        fixture.componentInstance.topologyTemplateId = topologyTemplateId;
        fixture.componentInstance.topologyTemplateType = topologyTemplateType;
        fixture.componentInstance.isViewOnly = isViewOnly;
        fixture.componentInstance.addOrUpdate(artifact);

        expect(artifactsServiceMockService.openArtifactModal).toHaveBeenCalledWith(topologyTemplateId, topologyTemplateType, artifact, type, isViewOnly);
    });


    it ('verify allowDeleteAndUpdateArtifact return false since isViewOnly=true', () => {
        const artifact = new ArtifactModel();
        fixture.componentInstance.isViewOnly = true;

        const res = fixture.componentInstance.allowDeleteAndUpdateArtifact(artifact);
        expect(res).toBe(false)
    });

    it ('verify allowDeleteAndUpdateArtifact return artifact.isFromCsar since isViewOnly=false && artifactGroupType = DEPLOYMENT', () => {
        const artifact = new ArtifactModel();
        artifact.artifactGroupType = ArtifactType.DEPLOYMENT;
        artifact.isFromCsar = false;

        fixture.componentInstance.isViewOnly = false;

        const res = fixture.componentInstance.allowDeleteAndUpdateArtifact(artifact);
        expect(res).toBe(!artifact.isFromCsar);
    });

    it ('verify allowDeleteAndUpdateArtifact return !artifact.isHEAT() && !artifact.isThirdParty() &&' +
        ' !this.isLicenseArtifact(artifact) since isViewOnly=false && artifactGroupType != DEPLOYMENT', () => {
        const artifact = new ArtifactModel();
        artifact.artifactGroupType = 'NOT_DEPLOYMENT';
        artifact.isHEAT = () =>  false;
        artifact.isThirdParty = () =>  false;

        fixture.componentInstance.isLicenseArtifact = jest.fn(() => false);

        fixture.componentInstance.isViewOnly = false;

        const res = fixture.componentInstance.allowDeleteAndUpdateArtifact(artifact);
        expect(res).toBe(true )
    });

    it('verify action on loadArtifacts in case isComponentInstanceSelected = true', () => {
        fixture.componentInstance.isComponentInstanceSelected = true;
        fixture.componentInstance.topologyTemplateType = 'topologyTemplateType';
        fixture.componentInstance.topologyTemplateId = 'topologyTemplateId';
        const component = new Component();
        component.uniqueId = 'uniqueId';
        fixture.componentInstance.component = component;
        fixture.componentInstance.type = 'type';

        const action = new GetInstanceArtifactsByTypeAction(({
            componentType: 'topologyTemplateType',
            componentId: 'topologyTemplateId',
            instanceId: 'uniqueId',
            artifactType: 'type'
        }))

        fixture.componentInstance.store.dispatch = jest.fn(() => Observable.of(true));
        fixture.componentInstance.loadArtifacts();

        expect(store.dispatch).toBeCalledWith(action);

    });
});
