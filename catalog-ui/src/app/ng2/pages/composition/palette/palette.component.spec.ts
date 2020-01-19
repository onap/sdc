import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {CompositionPaletteService} from "./services/palette.service";
import {EventListenerService} from "../../../../services/event-listener-service";
import {PaletteElementComponent} from "./palette-element/palette-element.component";
import {PaletteComponent} from "./palette.component";
import {ConfigureFn, configureTests} from "../../../../../jest/test-config.helper";
import {GRAPH_EVENTS} from "../../../../utils/constants";
import {KeyValuePipe} from "../../../pipes/key-value.pipe";
import {ResourceNamePipe} from "../../../pipes/resource-name.pipe";
import {LeftPaletteComponent} from "../../../../models/components/displayComponent";
import {Observable} from "rxjs/Observable";
import {leftPaletteElements} from "../../../../../jest/mocks/left-paeltte-elements.mock";
import {NgxsModule, Select} from '@ngxs/store';
import { WorkspaceState } from 'app/ng2/store/states/workspace.state';


describe('palette component', () => {

    const mockedEvent = <MouseEvent>{ target: {} }
    let fixture: ComponentFixture<PaletteComponent>;
    let eventServiceMock: Partial<EventListenerService>;
    let compositionPaletteMockService: Partial<CompositionPaletteService>;

    beforeEach(
        async(() => {
            eventServiceMock = {
                notifyObservers: jest.fn()
            }
            compositionPaletteMockService = {
                subscribeToLeftPaletteElements:  jest.fn().mockImplementation(()=>  Observable.of(leftPaletteElements)),
                getLeftPaletteElements: jest.fn().mockImplementation(()=>  leftPaletteElements)
            }
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [PaletteComponent, PaletteElementComponent, KeyValuePipe, ResourceNamePipe],
                    imports: [NgxsModule.forRoot([WorkspaceState])],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: CompositionPaletteService, useValue: compositionPaletteMockService},
                        {provide: EventListenerService, useValue: eventServiceMock}
                    ],
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(PaletteComponent);
            });
        })
    );

    it('should match current snapshot of palette component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should call on palette component hover in event', () => {
        let paletteObject =  <LeftPaletteComponent>{categoryType: 'COMPONENT'};
        fixture.componentInstance.onMouseOver(mockedEvent, paletteObject);
        expect(eventServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, paletteObject);
    });

    it('should call on palette component hover out event', () => {
        let paletteObject =  <LeftPaletteComponent>{categoryType: 'COMPONENT'};
        fixture.componentInstance.onMouseOut(paletteObject);
        expect(eventServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT);
    });

    it('should call show popup panel event', () => {
        let paletteObject =  <LeftPaletteComponent>{categoryType: 'GROUP'};
        fixture.componentInstance.onMouseOver(mockedEvent, paletteObject);
        expect(eventServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_PALETTE_COMPONENT_SHOW_POPUP_PANEL, paletteObject, mockedEvent.target);
    });

    it('should call  hide popup panel event', () => {
        let paletteObject =  <LeftPaletteComponent>{categoryType: 'GROUP'};
        fixture.componentInstance.onMouseOut(paletteObject);
        expect(eventServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HIDE_POPUP_PANEL);
    });

    it('should build Palette By Categories without searchText', () => {
        fixture.componentInstance.buildPaletteByCategories();
        expect(fixture.componentInstance.paletteElements["Generic"]["Network"].length).toBe(5);
        expect(fixture.componentInstance.paletteElements["Generic"]["Network"][0].searchFilterTerms).toBe("extvirtualmachineinterfacecp external port for virtual machine interface extvirtualmachineinterfacecp 3.0");
        expect(fixture.componentInstance.paletteElements["Generic"]["Network"][1].searchFilterTerms).toBe("newservice2 asdfasdfa newservice2 0.3");

        expect(fixture.componentInstance.paletteElements["Generic"]["Configuration"].length).toBe(1);
        expect(fixture.componentInstance.paletteElements["Generic"]["Configuration"][0].systemName).toBe("Extvirtualmachineinterfacecp");
    });

    it('should build Palette By Categories with searchText', () => {
        fixture.componentInstance.buildPaletteByCategories("testVal");
        expect(fixture.componentInstance.paletteElements["Generic"]["Network"].length).toBe(1);
        expect(fixture.componentInstance.paletteElements["Generic"]["Network"][0].searchFilterTerms).toBe("testVal and other values");
    });

    it('should change numbers of elements', () => {
        fixture.componentInstance.buildPaletteByCategories();
        expect(fixture.componentInstance.numberOfElements).toEqual(6);
        fixture.componentInstance.buildPaletteByCategories("testVal");
        expect(fixture.componentInstance.numberOfElements).toEqual(1);
    });
});