import {async, ComponentFixture} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ConfigureFn, configureTests} from '../../../../../../jest/test-config.helper';
import 'jest-dom/extend-expect';
import {ZoneInstanceType} from '../../../../../../app/models/graph/zones/zone-instance';
import {ZoneContainerComponent} from './zone-container.component';


describe('ZoneContainerComponent', () => {
    let fixture: ComponentFixture<ZoneContainerComponent>;

    beforeEach(
        async(() => {
            const configure: ConfigureFn = testBed => {
                testBed.configureTestingModule({
                    declarations: [ZoneContainerComponent]
                });
            };

            configureTests(configure).then(testBed => {
                fixture = testBed.createComponent(ZoneContainerComponent);
            });
        })
    );


    it('should match current snapshot of palette element component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('should have a group-zone class when the ZoneInstanceType is GROUP',
        () => {
            fixture.componentInstance.type = ZoneInstanceType.GROUP;
            fixture.detectChanges();
            const compiled = fixture.debugElement.query(By.css('.sdc-canvas-zone'));
            expect(compiled.nativeElement).toHaveClass('group-zone');
        });

    it('should have a policy-zone class when the ZoneInstanceType is POLICY',
        () => {
            fixture.componentInstance.type = ZoneInstanceType.POLICY;
            fixture.detectChanges();
            const compiled = fixture.debugElement.query(By.css('.sdc-canvas-zone'));
            expect(compiled.nativeElement).toHaveClass('policy-zone');
        });
});