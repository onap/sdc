import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TypeWorkspaceGeneralComponent} from './type-workspace-general.component';
import {ReactiveFormsModule} from "@angular/forms";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {TranslateService} from "../../../shared/translator/translate.service";

describe('TypeWorkspaceGeneralComponent', () => {
  let component: TypeWorkspaceGeneralComponent;
  let fixture: ComponentFixture<TypeWorkspaceGeneralComponent>;
  let translateServiceMock: Partial<TranslateService> = {
    'translate': jest.fn()
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeWorkspaceGeneralComponent ],
      imports: [
        ReactiveFormsModule,
        TranslateModule
      ],
      providers: [
        {provide: TranslateService, useValue: translateServiceMock}
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TypeWorkspaceGeneralComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
