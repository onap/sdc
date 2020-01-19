import { async, ComponentFixture } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import 'jest-dom/extend-expect';
import {HierarchyNavigationComponent} from "./hierarchy-navigation.component";
import {HierarchyDisplayOptions} from "./hierarchy-display-options";


describe('hierarchyNavigationComponent', () => {
  let fixture: ComponentFixture<HierarchyNavigationComponent>;
    let hierarchyDisplayOptions: HierarchyDisplayOptions;
  beforeEach(
    async(() => {
      const configure: ConfigureFn = testBed => {
        testBed.configureTestingModule({
          declarations: [HierarchyNavigationComponent]
        });
      };

      configureTests(configure).then(testBed => {
        fixture = testBed.createComponent(HierarchyNavigationComponent);
        hierarchyDisplayOptions = new HierarchyDisplayOptions("id", "name", "children");
          fixture.componentInstance.displayOptions = hierarchyDisplayOptions;
          fixture.detectChanges();
          fixture.componentInstance.displayData = [{name: "aaa", id: "1", children: [{name: "bbb", id: "1.1"}, {name: "ccc", id: "1.2", children: [{name: "aaa", id: "1.2.1"}]}]}, {name: "bbb", id: "2"}];
          fixture.detectChanges();
      });
    })
  );

  it('should have a selected class after user click on a tree node',
    () => {
        let firstNodeElement = fixture.debugElement.query(By.css('.node-item'));
        fixture.componentInstance.updateSelected.subscribe((item) => {
            fixture.componentInstance.selectedItem = item.id;
            fixture.detectChanges();
        });
        firstNodeElement.nativeElement.click();
        fixture.whenStable().then(() => {
            expect(firstNodeElement.children[0].nativeElement).toHaveClass('selected');
        });
    });

    it('should call onClick function when user click on a tree node',
        () => {
            spyOn(fixture.componentInstance, 'onClick');
            let firstNodeElement = fixture.debugElement.query(By.css('.node-item')).nativeElement;
            firstNodeElement.click();
            expect(fixture.componentInstance.onClick).toHaveBeenCalled();
        });

});