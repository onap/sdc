import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import 'rxjs/add/observable/of';
import { Observable } from 'rxjs/Rx';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { ComponentMetadata } from '../../../../models/component-metadata';
import { ModalsHandler } from '../../../../utils';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { TranslateService } from '../../../shared/translator/translate.service';
import { WorkspaceService } from '../workspace.service';
import { AttributesComponent } from './attributes.component';

describe('attributes component', () => {

    let fixture: ComponentFixture<AttributesComponent>;

    // Mocks
    let workspaceServiceMock: Partial<WorkspaceService>;
    let topologyTemplateServiceMock: Partial<TopologyTemplateService>;
    let loaderServiceMock: Partial<SdcUiServices.LoaderService>;
    let componentMetadataMock: ComponentMetadata;
    let modalServiceMock: Partial<SdcUiServices.ModalService>;

    const mockAttributesList = [
        { uniqueId: '1', name: 'attr1', description: 'description1', type: 'string', hidden: false, defaultValue: 'val1', schema: null },
        { uniqueId : '2', name : 'attr2', description: 'description2', type : 'int', hidden : false, defaultValue : 1, schema : null},
        { uniqueId : '3', name : 'attr3', description: 'description3', type : 'double', hidden : false, defaultValue : 1.0, schema : null},
        { uniqueId : '4', name : 'attr4', description: 'description4', type : 'boolean', hidden : false, defaultValue : true, schema : null},
    ];

    const newAttribute = {
        uniqueId : '5', name : 'attr5', description: 'description5', type : 'string', hidden : false, defaultValue : 'val5', schema : null
    };
    const updatedAttribute = {
        uniqueId : '2', name : 'attr2', description: 'description_new', type : 'string', hidden : false, defaultValue : 'new_val2', schema : null
    };
    const errorAttribute = {
        uniqueId : '99', name : 'attr99', description: 'description_error', type : 'string', hidden : false, defaultValue : 'error', schema : null
    };

    beforeEach(
        async(() => {

            componentMetadataMock = new ComponentMetadata();
            componentMetadataMock.uniqueId = 'fake';
            componentMetadataMock.componentType = 'VL';

            topologyTemplateServiceMock = {
                getComponentAttributes: jest.fn().mockResolvedValue({ attributes : mockAttributesList }),
                addAttributeAsync: jest.fn().mockImplementation(
                    (compType, cUid, attr) => {
                        if (attr === errorAttribute) {
                            return Observable.throwError('add_error').toPromise();
                        } else {
                            return Observable.of(newAttribute).toPromise();
                        }
                    }
                ),
                updateAttributeAsync: jest.fn().mockImplementation(
                    (compType, cUid, attr) => {
                        if (attr === errorAttribute) {
                            return Observable.throwError('update_error').toPromise();
                        } else {
                            return Observable.of(updatedAttribute).toPromise();
                        }
                    }
                ),
                deleteAttributeAsync: jest.fn().mockImplementation((cid, ctype, attr) => Observable.of(attr))
            };

            workspaceServiceMock = {
                metadata: componentMetadataMock
            };

            const customModalInstance = { innerModalContent: { instance: { onValidationChange: { subscribe: jest.fn()}}}};

            modalServiceMock = {
                openInfoModal: jest.fn(),
                openCustomModal: jest.fn().mockImplementation(() => customModalInstance)
            };

            loaderServiceMock = {
                activate: jest.fn(),
                deactivate: jest.fn()
            };

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [AttributesComponent],
                    imports: [NgxDatatableModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: WorkspaceService, useValue: workspaceServiceMock},
                        {provide: TopologyTemplateService, useValue: topologyTemplateServiceMock},
                        {provide: ModalsHandler, useValue: {}},
                        {provide: TranslateService, useValue: { translate: jest.fn() }},
                        {provide: SdcUiServices.ModalService, useValue: modalServiceMock },
                        {provide: SdcUiServices.LoaderService, useValue: loaderServiceMock }
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(AttributesComponent);
            });
        })
    );

    it('should see exactly 1 attributes on init', async () => {
        await fixture.componentInstance.asyncInitComponent();
        expect(fixture.componentInstance.getAttributes().length).toEqual(4);
    });

    it('should see exactly 5 attributes when adding', async () => {
        await fixture.componentInstance.asyncInitComponent();
        expect(fixture.componentInstance.getAttributes().length).toEqual(4);

        await fixture.componentInstance.addOrUpdateAttribute(newAttribute, false);
        expect(fixture.componentInstance.getAttributes().length).toEqual(5);
    });

    it('should see exactly 3 attributes when deleting', async () => {
        await fixture.componentInstance.asyncInitComponent();
        expect(fixture.componentInstance.getAttributes().length).toEqual(4);
        const attrToDelete = mockAttributesList[0];
        expect(fixture.componentInstance.getAttributes().filter((attr) => attr.uniqueId === attrToDelete.uniqueId).length).toEqual(1);
        await fixture.componentInstance.deleteAttribute(attrToDelete);
        expect(fixture.componentInstance.getAttributes().length).toEqual(3);
        expect(fixture.componentInstance.getAttributes().filter((attr) => attr.uniqueId === attrToDelete.uniqueId).length).toEqual(0);
    });

    it('should see updated attribute', async () => {
        await fixture.componentInstance.asyncInitComponent();

        await fixture.componentInstance.addOrUpdateAttribute(updatedAttribute, true);
        expect(fixture.componentInstance.getAttributes().length).toEqual(4);
        const attribute = fixture.componentInstance.getAttributes().filter( (attr) => {
            return attr.uniqueId === updatedAttribute.uniqueId;
        })[0];
        expect(attribute.description).toEqual( 'description_new');
    });

    it('Add fails, make sure loader is deactivated and attribute is not added', async () => {
        await fixture.componentInstance.asyncInitComponent();
        const numAttributes = fixture.componentInstance.getAttributes().length;
        await fixture.componentInstance.addOrUpdateAttribute(errorAttribute, false); // Add
        expect(loaderServiceMock.deactivate).toHaveBeenCalled();
        expect(fixture.componentInstance.getAttributes().length).toEqual(numAttributes);
    });

    it('Update fails, make sure loader is deactivated', async () => {
        await fixture.componentInstance.asyncInitComponent();
        const numAttributes = fixture.componentInstance.getAttributes().length;
        await fixture.componentInstance.addOrUpdateAttribute(errorAttribute, true); // Add
        expect(loaderServiceMock.deactivate).toHaveBeenCalled();
        expect(fixture.componentInstance.getAttributes().length).toEqual(numAttributes);
    });

    it('on delete modal shell be opened', async () => {
        await fixture.componentInstance.asyncInitComponent();
        const event = { stopPropagation: jest.fn() };
        fixture.componentInstance.onDeleteAttribute(event, fixture.componentInstance.getAttributes()[0]);
        expect(event.stopPropagation).toHaveBeenCalled();
        expect(modalServiceMock.openInfoModal).toHaveBeenCalled();
    });

    it('on add modal shell be opened', async () => {
        await fixture.componentInstance.asyncInitComponent();
        fixture.componentInstance.onAddAttribute();
        expect(modalServiceMock.openCustomModal).toHaveBeenCalled();
    });

    it('on edit modal shell be opened', async () => {
        await fixture.componentInstance.asyncInitComponent();
        const event = { stopPropagation: jest.fn() };
        fixture.componentInstance.onEditAttribute(event, fixture.componentInstance.getAttributes()[0]);
        expect(event.stopPropagation).toHaveBeenCalled();
        expect(modalServiceMock.openCustomModal).toHaveBeenCalled();
    });
});
