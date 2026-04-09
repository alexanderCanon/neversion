import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PlatformDetailComponent } from './platform-detail.component';

describe('PlatformDetailComponent', () => {
  let component: PlatformDetailComponent;
  let fixture: ComponentFixture<PlatformDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [PlatformDetailComponent]
    });
    fixture = TestBed.createComponent(PlatformDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
