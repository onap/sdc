import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DataTypeWorkspaceComponent } from './data-type-workspace.component';

describe('DataTypeWorkspaceComponent', () => {
  let component: DataTypeWorkspaceComponent;
  let fixture: ComponentFixture<DataTypeWorkspaceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DataTypeWorkspaceComponent ]
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
