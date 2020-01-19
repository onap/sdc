import {ArtifactFormComponent} from "./artifact-form.component";
import {async, ComponentFixture} from "@angular/core/testing";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {CacheService} from "../../../services/cache.service";
import {TranslateService} from "../../../shared/translator/translate.service";
import {ArtifactModel} from "../../../../models/artifacts";
import {IDropDownOption} from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";
import {Observable, Subject} from "rxjs";
import {getValue} from "@ngxs/store";


describe('artifact form component', () => {

    let fixture: ComponentFixture<ArtifactFormComponent>;
    let cacheServiceMock: Partial<CacheService>;
    let onValidationChangeMock: Partial<Subject<boolean>>;

    let artifactModel = new ArtifactModel();


    beforeEach(
        async(() => {

            onValidationChangeMock = {
                next: jest.fn()
            }

            cacheServiceMock = {
                contains: jest.fn(),
                remove: jest.fn(),
                set: jest.fn(),
                get: jest.fn()
            }

            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ArtifactFormComponent],
                    imports: [TranslateModule],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: CacheService, useValue: cacheServiceMock},
                        {provide: TranslateService, useValue: {}}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(ArtifactFormComponent);
            });
        })
    );


    it('should verify initArtifactTypes for DEPLOYMENT and ArtifactType = HEAT_ENV', () =>{

        cacheServiceMock.get.mockImplementation(() =>{
            return {
                artifacts: {
                    deployment:{
                        resourceInstanceDeploymentArtifacts: [{name: "Dummy Value Returned from ui api"}],
                    }
                }
            }
        });

        fixture.componentInstance.artifactType = 'DEPLOYMENT';
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.artifactType = 'HEAT_ENV';

        fixture.componentInstance.initArtifactTypes();

        expect(fixture.componentInstance.selectedFileType).toEqual(undefined);

    });

    it('should verify initArtifactTypes for DEPLOYMENT and ComponentType = RESOURCE', () =>{

        cacheServiceMock.get.mockImplementation(() =>{
            return {
                artifacts: {
                    deployment:{
                        resourceDeploymentArtifacts: [{name: "Dummy Value Returned from ui api"}],
                    }
                }
            }
        });

        fixture.componentInstance.artifactType = 'DEPLOYMENT';
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.componentType = 'RESOURCE';

        fixture.componentInstance.initArtifactTypes();

        expect(fixture.componentInstance.selectedFileType).toEqual(undefined);

    });

    it('should verify initArtifactTypes for DEPLOYMENT and NOT ComponentType = RESOURCE OR NOT ArtifactType = HEAT_ENV', () =>{

        cacheServiceMock.get.mockImplementation(() =>{
            return {
                artifacts: {
                    deployment:{
                        serviceDeploymentArtifacts: [{name: "Dummy Value Returned from ui api"}],
                    }
                }
            }
        });

        fixture.componentInstance.artifactType = 'DEPLOYMENT';
        fixture.componentInstance.artifact = artifactModel;
        // fixture.componentInstance.componentType = 'RESOURCE';

        fixture.componentInstance.initArtifactTypes();

        expect(fixture.componentInstance.selectedFileType).toEqual(undefined);

    });

    it('should verify initArtifactTypes for INFORMATION', () =>{

        cacheServiceMock.get.mockImplementation(() =>{
            return {
                artifacts: {
                    other: [{name: "Val1"}, {name: "ExpectedValToBeSelected"}, {name: "Val3"}]
                }
            }
        });

        fixture.componentInstance.artifactType = 'INFORMATIONAL';
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.artifactType = 'ExpectedValToBeSelected';

        fixture.componentInstance.initArtifactTypes();

        expect(fixture.componentInstance.selectedFileType).toEqual({"label": "ExpectedValToBeSelected", "value": "ExpectedValToBeSelected"});

    });


    it('should match current snapshot of artifact form component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should verify onUploadFile -> file gets file name', () => {
        let file = {
            filename:'dummyFileName'
        }

        fixture.componentInstance.verifyTypeAndFileWereFilled = jest.fn();
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.onUploadFile(file);

        expect(fixture.componentInstance.artifact.artifactName).toBe('dummyFileName');

        const spy1 = jest.spyOn(fixture.componentInstance,'verifyTypeAndFileWereFilled');
        expect(spy1).toHaveBeenCalled();
    });

    it('should verify onUploadFile -> file is null', () => {
        let file = null;
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.onUploadFile(file);

        expect(fixture.componentInstance.artifact.artifactName).toBe(null);
    });

    it('should verify onTypeChange -> verifyTypeAndFileWereFilled is being called', () => {
        let selectedFileType:IDropDownOption;
        selectedFileType = {"label": "dummyLabel", "value": "dummyValue"};
        fixture.componentInstance.verifyTypeAndFileWereFilled = jest.fn();

        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.onTypeChange(selectedFileType);

        const spy1 = jest.spyOn(fixture.componentInstance,'verifyTypeAndFileWereFilled');
        expect(spy1).toHaveBeenCalled();
    });

    it('should verify onDescriptionChange -> verifyTypeAndFileWereFilled is being called', () => {
        fixture.componentInstance.verifyTypeAndFileWereFilled = jest.fn();
        fixture.componentInstance.onValidationChange.next = jest.fn(() => true);

        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.onDescriptionChange();

        const spy1 = jest.spyOn(fixture.componentInstance,'verifyTypeAndFileWereFilled');
        expect(spy1).toHaveBeenCalled();
    });

    it('should verify onLabelChange -> verifyTypeAndFileWereFilled is being called', () => {
        fixture.componentInstance.verifyTypeAndFileWereFilled = jest.fn();
        fixture.componentInstance.onValidationChange.next = jest.fn(() => true);

        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.onLabelChange(true);

        const spy1 = jest.spyOn(fixture.componentInstance,'verifyTypeAndFileWereFilled');
        expect(spy1).toHaveBeenCalled();
    });

    it('should verify verifyTypeAndFileWereFilled -> verify branch this.artifact.artifactType !== \'DEPLOYMENT\' ==>> onValidationChange.next(false)', () => {
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.artifactType = 'NOT_DEPLOYMENT';
        fixture.componentInstance.artifact.mandatory = true;
        fixture.componentInstance.descriptionIsValid = false;

        let onValidationChangeResult;

        fixture.componentInstance.onValidationChange.subscribe((data) => {
            onValidationChangeResult = data;
            // console.log("Subscriber got data >>>>> "+ data);
        });

        fixture.componentInstance.verifyTypeAndFileWereFilled();

        expect(onValidationChangeResult).toBe(false);
    });

    it('should verify verifyTypeAndFileWereFilled -> verify branch this.artifact.artifactType !== \'DEPLOYMENT\' ==>> onValidationChange.next(true)', () => {
        fixture.componentInstance.artifact = artifactModel;
        fixture.componentInstance.artifact.artifactType = 'NOT_DEPLOYMENT';
        fixture.componentInstance.artifact.mandatory = true;
        fixture.componentInstance.artifact.artifactName = 'Something';
        fixture.componentInstance.descriptionIsValid = true;

        let onValidationChangeResult;

        fixture.componentInstance.onValidationChange.subscribe((data) => {
            onValidationChangeResult = data;
            // console.log("Subscriber got data >>>>> "+ data);
        });

        fixture.componentInstance.verifyTypeAndFileWereFilled();

        expect(onValidationChangeResult).toBe(true);
    });


});