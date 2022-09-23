import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {WorkspaceMenuComponent} from './workspace-menu.component';
import {CacheService} from "../../../services/cache.service";
import {States} from "../../../../utils/constants";
import {IAppMenu} from "../../../../models/app-config";
import {SdcMenuToken} from "../../../config/sdc-menu.config";

describe('WorkspaceMenuComponent', () => {
  let component: WorkspaceMenuComponent;
  let fixture: ComponentFixture<WorkspaceMenuComponent>;
  let cacheService: Partial<CacheService> = {
    'get': jest.fn(param => {
      if (param === 'version') {
        return 'version';
      }
      if (param === 'user') {
        return {};
      }
    })
  };
  let sdcMenuMock: Partial<IAppMenu> = {
    'component_workspace_menu_option': {
      "DataType": [
        {"text": "General", "action": "onMenuItemPressed", "state": "general"}
      ]
    }
  };
  let stateMock: Partial<ng.ui.IStateService> = {
    'current': {
      'name': States.TYPE_WORKSPACE
    }
  };
  let injectorMock: Partial<ng.auto.IInjectorService> = {
    'get': jest.fn(param => {
      if (param === '$state') {
        return stateMock;
      }
    })
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkspaceMenuComponent ],
      providers: [
        {provide: CacheService, useValue: cacheService},
        {provide: '$injector', useValue: injectorMock},
        {provide: SdcMenuToken, useValue: sdcMenuMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkspaceMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
