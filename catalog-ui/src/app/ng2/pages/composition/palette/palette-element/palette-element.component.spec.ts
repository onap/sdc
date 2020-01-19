import {async, ComponentFixture} from "@angular/core/testing";
import {ConfigureFn, configureTests} from "../../../../../../jest/test-config.helper";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {PaletteElementComponent} from "./palette-element.component";
import {ResourceNamePipe} from "../../../../pipes/resource-name.pipe";

describe('palette element component', () => {

    let fixture: ComponentFixture<PaletteElementComponent>;

    beforeEach(
        async(() => {
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [PaletteElementComponent, ResourceNamePipe],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA]
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(PaletteElementComponent);
            });
        })
    );

    it('should match current snapshot of palette element component', () => {
        expect(fixture).toMatchSnapshot();
    });
});