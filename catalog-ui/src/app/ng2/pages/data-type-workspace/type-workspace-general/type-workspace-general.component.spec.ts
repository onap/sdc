import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TypeWorkspaceGeneralComponent } from './type-workspace-general.component';

describe('TypeWorkspaceGeneralComponent', () => {
  let component: TypeWorkspaceGeneralComponent;
  let fixture: ComponentFixture<TypeWorkspaceGeneralComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TypeWorkspaceGeneralComponent ]
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
