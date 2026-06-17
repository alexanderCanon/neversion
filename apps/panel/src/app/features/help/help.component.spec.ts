import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HelpComponent } from './help.component';

describe('HelpComponent', () => {
  let component: HelpComponent;
  let fixture: ComponentFixture<HelpComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HelpComponent],
    }).compileComponents();
    
    fixture = TestBed.createComponent(HelpComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the help component', () => {
    expect(component).toBeTruthy();
  });

  it('should default to faq tab', () => {
    expect(component.activeTab()).toBe('faq');
  });

  it('should change tabs correctly', () => {
    component.selectTab('glossary');
    expect(component.activeTab()).toBe('glossary');
    
    component.selectTab('workflow');
    expect(component.activeTab()).toBe('workflow');
  });

  it('should filter faqs based on search query', () => {
    component.searchQuery.set('Cuenta Maestra');
    fixture.detectChanges();
    
    const filtered = component.filteredFaqs();
    expect(filtered.length).toBeGreaterThan(0);
    expect(filtered.every(f => 
      f.question.toLowerCase().includes('cuenta') || 
      f.answer.toLowerCase().includes('cuenta')
    )).toBeTrue();
  });

  it('should submit support form successfully', () => {
    component.supportName.set('Test User');
    component.supportEmail.set('test@example.com');
    component.supportMessage.set('This is a test message.');
    
    const event = new Event('submit');
    component.onSubmitSupport(event);
    
    expect(component.supportSuccess()).toBeTrue();
  });
});
