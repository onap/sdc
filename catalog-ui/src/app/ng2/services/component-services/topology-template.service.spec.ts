import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';
import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TopologyTemplateService} from './topology-template.service';
import {SdcConfigToken} from '../../config/sdc-config.config';
import {mockSdcConfig} from '../../../../jest/mocks/sdc-config.mock';
import {ComponentType} from '../../../utils/constants';

describe('TopologyTemplateService', () => {
    let service: TopologyTemplateService;
    let httpMock: HttpTestingController;
    const baseUrl = '/sdc2/rest/v1/catalog/';

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                TopologyTemplateService,
                {provide: SdcConfigToken, useValue: mockSdcConfig}
            ]
        });

        service = TestBed.get(TopologyTemplateService);
        httpMock = TestBed.get(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('getFullComponent', () => {
        it('should GET resource by uniqueId', () => {
            service.getFullComponent(ComponentType.RESOURCE, 'comp-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1');
            expect(req.request.method).toBe('GET');
            req.flush({});
        });

        it('should GET service by uniqueId', () => {
            service.getFullComponent(ComponentType.SERVICE, 'svc-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1');
            expect(req.request.method).toBe('GET');
            req.flush({});
        });
    });

    describe('getComponentMetadata', () => {
        it('should request metadata fields for resource', () => {
            service.getComponentMetadata('comp-1', ComponentType.RESOURCE).subscribe();

            const req = httpMock.expectOne(r =>
                r.url === baseUrl + 'resources/comp-1/filteredDataByParams' &&
                r.params.get('include') === 'metadata'
            );
            expect(req.request.method).toBe('GET');
            req.flush({});
        });
    });

    describe('archiveComponent / restoreComponent', () => {
        it('should POST to archive endpoint', () => {
            service.archiveComponent(ComponentType.SERVICE, 'svc-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/archive');
            expect(req.request.method).toBe('POST');
            req.flush({});
        });

        it('should POST to restore endpoint', () => {
            service.restoreComponent(ComponentType.RESOURCE, 'comp-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1/restore');
            expect(req.request.method).toBe('POST');
            req.flush({});
        });
    });

    describe('getComponentInputs', () => {
        it('should request inputs for component', () => {
            const component = {componentType: ComponentType.SERVICE, uniqueId: 'svc-1'} as any;
            service.getComponentInputs(component).subscribe();

            const req = httpMock.expectOne(r =>
                r.url === baseUrl + 'services/svc-1/filteredDataByParams' &&
                r.params.get('include') === 'inputs'
            );
            req.flush({});
        });
    });

    describe('getComponentProperties', () => {
        it('should request properties for component', () => {
            const component = {componentType: ComponentType.RESOURCE, uniqueId: 'comp-1'} as any;
            service.getComponentProperties(component).subscribe();

            const req = httpMock.expectOne(r =>
                r.url === baseUrl + 'resources/comp-1/filteredDataByParams' &&
                r.params.get('include') === 'properties'
            );
            req.flush({});
        });
    });

    describe('createInput', () => {
        it('should POST to create inputs endpoint', () => {
            const component = {
                getTypeUrl: () => 'services/',
                uniqueId: 'svc-1',
                componentType: ComponentType.SERVICE
            } as any;
            const inputsMap = {componentInstanceProperties: {'inst1': [{name: 'prop1'}]}} as any;

            service.createInput(component, inputsMap, false).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/create/inputs');
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual(inputsMap);
            req.flush({});
        });

        it('should wrap as serviceProperties when isSelf', () => {
            const component = {
                getTypeUrl: () => 'services/',
                uniqueId: 'svc-1',
                componentType: ComponentType.SERVICE
            } as any;
            const inputsMap = {componentInstanceProperties: {'self': [{name: 'p1'}]}} as any;

            service.createInput(component, inputsMap, true).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/create/inputs');
            expect(req.request.body).toEqual({serviceProperties: {'self': [{name: 'p1'}]}});
            req.flush({});
        });
    });

    describe('deleteInput', () => {
        it('should DELETE input by id', () => {
            const component = {getTypeUrl: () => 'resources/', uniqueId: 'comp-1'} as any;
            const input = {uniqueId: 'input-1'} as any;

            service.deleteInput(component, input).subscribe(result => {
                expect(result.uniqueId).toBe('input-1');
            });

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1/delete/input-1/input');
            expect(req.request.method).toBe('DELETE');
            req.flush({uniqueId: 'input-1'});
        });
    });

    describe('getServiceProperties', () => {
        it('should GET service properties', () => {
            service.getServiceProperties('svc-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/properties');
            expect(req.request.method).toBe('GET');
            req.flush([]);
        });

        it('should return empty array when response is null', () => {
            service.getServiceProperties('svc-1').subscribe(result => {
                expect(result).toEqual([]);
            });

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/properties');
            req.flush(null);
        });
    });

    describe('createServiceProperty', () => {
        it('should POST property wrapped with its name as key', () => {
            const property = {name: 'myProp', type: 'string'} as any;
            service.createServiceProperty('svc-1', property).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/properties');
            expect(req.request.method).toBe('POST');
            expect(req.request.body).toEqual({myProp: property});
            req.flush({name: 'myProp', type: 'string'});
        });
    });

    describe('deleteServiceProperty', () => {
        it('should DELETE property by id', () => {
            const property = {uniqueId: 'prop-1', name: 'myProp'} as any;
            service.deleteServiceProperty('svc-1', property).subscribe(result => {
                expect(result).toBe('prop-1');
            });

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/properties/prop-1');
            expect(req.request.method).toBe('DELETE');
            req.flush({});
        });
    });

    describe('updateComponentInputs', () => {
        it('should POST inputs and return InputBEModel array', () => {
            const component = {getTypeUrl: () => 'services/', uniqueId: 'svc-1'} as any;
            const inputs = [{uniqueId: 'i1', name: 'input1'}] as any;

            service.updateComponentInputs(component, inputs).subscribe(result => {
                expect(result.length).toBe(1);
            });

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/update/inputs');
            expect(req.request.method).toBe('POST');
            req.flush([{uniqueId: 'i1', name: 'input1'}]);
        });
    });

    describe('getDependencies', () => {
        it('should GET dependencies for component', () => {
            service.getDependencies(ComponentType.SERVICE, 'svc-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/dependencies');
            expect(req.request.method).toBe('GET');
            req.flush([]);
        });
    });

    describe('createComponentInstance', () => {
        it('should POST to resourceInstance endpoint', () => {
            const instance = {name: 'inst1'} as any;
            service.createComponentInstance(ComponentType.SERVICE, 'svc-1', instance).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/resourceInstance');
            expect(req.request.method).toBe('POST');
            req.flush({name: 'inst1'});
        });
    });

    describe('deleteComponentInstance', () => {
        it('should DELETE component instance by id', () => {
            service.deleteComponentInstance(ComponentType.RESOURCE, 'comp-1', 'inst-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1/resourceInstance/inst-1');
            expect(req.request.method).toBe('DELETE');
            req.flush({});
        });
    });

    describe('addOrUpdateArtifact', () => {
        it('should POST artifact to component', () => {
            const artifact = {artifactName: 'test.yaml', uniqueId: 'art-1'} as any;
            service.addOrUpdateArtifact(ComponentType.SERVICE, 'svc-1', artifact).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/artifacts/art-1');
            expect(req.request.method).toBe('POST');
            req.flush({artifactName: 'test.yaml'});
        });

        it('should POST without artifact ID for new artifacts', () => {
            const artifact = {artifactName: 'new.yaml'} as any;
            service.addOrUpdateArtifact(ComponentType.RESOURCE, 'comp-1', artifact).subscribe();

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1/artifacts');
            expect(req.request.method).toBe('POST');
            req.flush({artifactName: 'new.yaml'});
        });
    });

    describe('deleteArtifact', () => {
        it('should DELETE artifact with operation label', () => {
            service.deleteArtifact('comp-1', ComponentType.RESOURCE, 'art-1', 'HEAT').subscribe();

            const req = httpMock.expectOne(baseUrl + 'resources/comp-1/artifacts/art-1?operation=HEAT');
            expect(req.request.method).toBe('DELETE');
            req.flush({});
        });
    });

    describe('downloadArtifact', () => {
        it('should GET artifact download', () => {
            service.downloadArtifact(ComponentType.SERVICE, 'svc-1', 'art-1').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/artifacts/art-1');
            expect(req.request.method).toBe('GET');
            req.flush({base64Contents: 'abc'});
        });
    });

    describe('URL routing', () => {
        it('should use services/ for SERVICE type', () => {
            service.getFullComponent(ComponentType.SERVICE, 'id').subscribe();
            const req = httpMock.expectOne(baseUrl + 'services/id');
            req.flush({});
        });

        it('should use resources/ for RESOURCE type', () => {
            service.getFullComponent(ComponentType.RESOURCE, 'id').subscribe();
            const req = httpMock.expectOne(baseUrl + 'resources/id');
            req.flush({});
        });

        it('should use services/ for SERVICE_PROXY type', () => {
            service.getFullComponent('ServiceProxy', 'id').subscribe();
            const req = httpMock.expectOne(baseUrl + 'services/id');
            req.flush({});
        });

        it('should use services/ for SERVICE_SUBSTITUTION type', () => {
            service.getFullComponent('ServiceSubstitution', 'id').subscribe();
            const req = httpMock.expectOne(baseUrl + 'services/id');
            req.flush({});
        });
    });

    describe('getComponentCompositionData', () => {
        it('should include forwarding paths for services', () => {
            service.getComponentCompositionData('svc-1', ComponentType.SERVICE).subscribe();

            const req = httpMock.expectOne(r =>
                r.url === baseUrl + 'services/svc-1/filteredDataByParams' &&
                r.params.getAll('include').indexOf('forwardingPaths') !== -1
            );
            req.flush({});
        });

        it('should not include forwarding paths for resources', () => {
            service.getComponentCompositionData('comp-1', ComponentType.RESOURCE).subscribe();

            const req = httpMock.expectOne(r =>
                r.url === baseUrl + 'resources/comp-1/filteredDataByParams' &&
                r.params.getAll('include').indexOf('forwardingPaths') === -1
            );
            req.flush({});
        });
    });

    describe('createRelation / deleteRelation', () => {
        it('should POST to associate endpoint', () => {
            const link = {fromNode: 'a', toNode: 'b'} as any;
            service.createRelation('svc-1', ComponentType.SERVICE, link).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/resourceInstance/associate');
            expect(req.request.method).toBe('POST');
            req.flush({});
        });

        it('should PUT to dissociate endpoint', () => {
            const link = {fromNode: 'a', toNode: 'b'} as any;
            service.deleteRelation('svc-1', ComponentType.SERVICE, link).subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/resourceInstance/dissociate');
            expect(req.request.method).toBe('PUT');
            req.flush({});
        });
    });

    describe('service filter constraints', () => {
        it('should POST to create node filter constraint', () => {
            const constraint = {propertyName: 'p1', operator: 'equal', value: 'v1'} as any;
            service.createServiceFilterConstraints('svc-1', 'inst-1', constraint, ComponentType.SERVICE, 'properties').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/componentInstance/inst-1/properties/nodeFilter');
            expect(req.request.method).toBe('POST');
            req.flush({});
        });

        it('should DELETE node filter constraint by index', () => {
            service.deleteServiceFilterConstraints('svc-1', 'inst-1', 0, ComponentType.SERVICE, 'properties').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/componentInstance/inst-1/properties/0/nodeFilter');
            expect(req.request.method).toBe('DELETE');
            req.flush({});
        });
    });

    describe('substitution filter', () => {
        it('should POST to create substitution filter', () => {
            const constraint = {propertyName: 'p1'} as any;
            service.createSubstitutionFilterConstraints('svc-1', constraint, ComponentType.SERVICE, 'properties').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/substitutionFilter/properties');
            expect(req.request.method).toBe('POST');
            req.flush({});
        });

        it('should DELETE substitution filter by index', () => {
            service.deleteSubstitutionFilterConstraints('svc-1', 2, ComponentType.SERVICE, 'capabilities').subscribe();

            const req = httpMock.expectOne(baseUrl + 'services/svc-1/substitutionFilter/capabilities/2');
            expect(req.request.method).toBe('DELETE');
            req.flush({});
        });
    });
});
