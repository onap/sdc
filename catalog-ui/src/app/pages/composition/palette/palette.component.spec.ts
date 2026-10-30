import {async, ComponentFixture, TestBed} from "@angular/core/testing";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {CompositionPaletteService} from "./services/palette.service";
import {EventListenerService} from "../../../services/event-listener.service";
import {PaletteElementComponent} from "./palette-element/palette-element.component";
import {PaletteComponent} from "./palette.component";
import {ConfigureFn, configureTests} from "../../../../jest/test-config.helper";
import {KeyValuePipe} from "../../../pipes/key-value.pipe";
import {ResourceNamePipe} from "../../../pipes/resource-name.pipe";
import {Observable} from "rxjs/Observable";
import {leftPaletteElements} from "../../../../jest/mocks/left-paeltte-elements.mock";
import {NgxsModule, Select} from '@ngxs/store';
import { WorkspaceState } from 'app/store/states/workspace.state';
import { GRAPH_EVENTS } from 'app/utils/constants';


describe('palette component', () => {

    const mockedEvent = <MouseEvent>{ target: {} }
    let fixture: ComponentFixture<PaletteComponent>;
    let eventServiceMock: Partial<EventListenerService>;
    let compositionPaletteMockService: Partial<CompositionPaletteService>;

    beforeEach(
        async(() => {
            localStorage.clear();
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

    describe('resize handle', () => {
        const keydown = (key: string) => <KeyboardEvent>{key, preventDefault: jest.fn()};
        const mouse = (clientX: number) => new MouseEvent('mousemove', {clientX});

        it('should start at the default width', () => {
            expect(fixture.componentInstance.width).toBe(PaletteComponent.DEFAULT_WIDTH);
        });

        it('should restore a stored width, clamped to the allowed range', () => {
            localStorage.setItem(PaletteComponent.WIDTH_STORAGE_KEY, '400');
            expect(TestBed.createComponent(PaletteComponent).componentInstance.width).toBe(400);
            localStorage.setItem(PaletteComponent.WIDTH_STORAGE_KEY, '5000');
            expect(TestBed.createComponent(PaletteComponent).componentInstance.width).toBe(PaletteComponent.MAX_WIDTH);
            localStorage.setItem(PaletteComponent.WIDTH_STORAGE_KEY, 'garbage');
            expect(TestBed.createComponent(PaletteComponent).componentInstance.width).toBe(PaletteComponent.DEFAULT_WIDTH);
        });

        it('should follow a mouse drag, then store the width and tell the graph', () => {
            const palette = fixture.componentInstance;
            palette.onResizeStart(<MouseEvent>{button: 0, clientX: 244, preventDefault: jest.fn()});
            document.dispatchEvent(mouse(400));
            expect(palette.width).toBe(400);
            expect(eventServiceMock.notifyObservers).not.toHaveBeenCalled();

            document.dispatchEvent(new MouseEvent('mouseup'));
            expect(localStorage.getItem(PaletteComponent.WIDTH_STORAGE_KEY)).toBe('400');
            expect(eventServiceMock.notifyObservers).toHaveBeenCalledWith(GRAPH_EVENTS.ON_PALETTE_RESIZED);

            document.dispatchEvent(mouse(500));
            expect(palette.width).toBe(400);
        });

        it('should not shrink below the default width while dragging', () => {
            const palette = fixture.componentInstance;
            palette.onResizeStart(<MouseEvent>{button: 0, clientX: 244, preventDefault: jest.fn()});
            document.dispatchEvent(mouse(10));
            document.dispatchEvent(new MouseEvent('mouseup'));
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH);
        });

        it('should ignore a drag with any button but the primary one', () => {
            const palette = fixture.componentInstance;
            palette.onResizeStart(<MouseEvent>{button: 2, clientX: 244, preventDefault: jest.fn()});
            document.dispatchEvent(mouse(400));
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH);
        });

        it('should resize with the arrow, Home and End keys', () => {
            const palette = fixture.componentInstance;
            palette.onResizeKeydown(keydown('ArrowRight'));
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH + PaletteComponent.KEYBOARD_STEP);
            palette.onResizeKeydown(keydown('ArrowLeft'));
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH);
            palette.onResizeKeydown(keydown('End'));
            expect(palette.width).toBe(PaletteComponent.MAX_WIDTH);
            palette.onResizeKeydown(keydown('Home'));
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH);
            expect(eventServiceMock.notifyObservers).toHaveBeenCalledTimes(4);
        });

        it('should leave other keys alone', () => {
            const event = keydown('Tab');
            fixture.componentInstance.onResizeKeydown(event);
            expect(event.preventDefault).not.toHaveBeenCalled();
            expect(eventServiceMock.notifyObservers).not.toHaveBeenCalled();
        });

        it('should publish its width for the workspace top bar, and withdraw it on destroy', () => {
            const style = document.documentElement.style;
            const setProperty = jest.spyOn(style, 'setProperty');
            const removeProperty = jest.spyOn(style, 'removeProperty');
            fixture.componentInstance.ngOnInit();
            expect(setProperty).toHaveBeenLastCalledWith(PaletteComponent.WIDTH_CSS_PROPERTY, PaletteComponent.DEFAULT_WIDTH + 'px');
            fixture.componentInstance.onResizeKeydown(keydown('End'));
            expect(setProperty).toHaveBeenLastCalledWith(PaletteComponent.WIDTH_CSS_PROPERTY, PaletteComponent.MAX_WIDTH + 'px');
            fixture.componentInstance.ngOnDestroy();
            expect(removeProperty).toHaveBeenCalledWith(PaletteComponent.WIDTH_CSS_PROPERTY);
            setProperty.mockRestore();
            removeProperty.mockRestore();
        });

        it('should reset to the default width', () => {
            const palette = fixture.componentInstance;
            palette.onResizeKeydown(keydown('End'));
            palette.resetWidth();
            expect(palette.width).toBe(PaletteComponent.DEFAULT_WIDTH);
            expect(localStorage.getItem(PaletteComponent.WIDTH_STORAGE_KEY)).toBe(String(PaletteComponent.DEFAULT_WIDTH));
        });
    });
});
