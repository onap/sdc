import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {DataTypeWorkspaceComponent} from './data-type-workspace.component';
import {ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../shared/translator/translate.module";
import {UiElementsModule} from "../../components/ui/ui-elements.module";
import {DataTypeService} from "../../services/data-type.service";
import {TranslateService} from "../../shared/translator/translate.service";
import {WorkspaceMenuComponent} from "./workspace-menu/workspace-menu.component";
import {TypeWorkspaceGeneralComponent} from "./type-workspace-general/type-workspace-general.component";
import {LayoutModule} from "../../components/layout/layout.module";
import {CacheService} from "../../services/cache.service";
import {IAppMenu, SdcMenuToken} from "../../config/sdc-menu.config";
import {AuthenticationService} from "../../services/authentication.service";
import {ISdcConfig, SdcConfigToken} from "../../config/sdc-config.config";
import {States} from "../../../utils/constants";
import {IUserProperties} from "../../../models/user";
import {Observable} from "rxjs/Observable";

describe('DataTypeWorkspaceComponent', () => {
  let component: DataTypeWorkspaceComponent;
  let fixture: ComponentFixture<DataTypeWorkspaceComponent>;
  let translateServiceMock: Partial<TranslateService> = {
    'languageChangedObservable': Observable.of(),
    'translate': jest.fn()
  };
  let dataTypeServiceMock: Partial<DataTypeService>;
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
  let stateMock: Partial<ng.ui.IStateService> = {
    'current': {
      'name': States.TYPE_WORKSPACE
    }
  };
  let stateParamsMock: Partial<ng.ui.IStateParamsService> = {};
  let injectorMock: Partial<ng.auto.IInjectorService> = {
    'get': jest.fn(param => {
      if (param === '$state') {
        return stateMock;
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
  let sdcConfigMock: Partial<ISdcConfig>;
  let user: Partial<IUserProperties> = {
    'role': 'DESIGNER'
  }
  let authenticationService: Partial<AuthenticationService> = {
    'getLoggedinUser': jest.fn(() => {
      return user;
    })
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataTypeWorkspaceComponent, WorkspaceMenuComponent, TypeWorkspaceGeneralComponent ],
      imports: [
        ReactiveFormsModule,
        TranslateModule,
        UiElementsModule,
        LayoutModule
      ],
      providers: [
        {provide: DataTypeService, useValue: dataTypeServiceMock},
        {provide: TranslateService, useValue: translateServiceMock},
        {provide: CacheService, useValue: cacheService},
        {provide: '$state', useValue: stateMock},
        {provide: '$stateParams', useValue: stateParamsMock},
        {provide: '$injector', useValue: injectorMock},
        {provide: SdcMenuToken, useValue: sdcMenuMock},
        {provide: SdcConfigToken, useValue: sdcConfigMock},
        {provide: AuthenticationService, useValue: authenticationService},
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DataTypeWorkspaceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
