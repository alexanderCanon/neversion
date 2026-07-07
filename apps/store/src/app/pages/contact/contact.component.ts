import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  standalone: false,
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent {

  formContact: FormGroup;
  options: string[] = ['Problemas con servicios', 'Problemas con productos', 'Problemas con pagos'];

  constructor(private formBuilder: FormBuilder) {
    this.formContact = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      option: [null, Validators.required],
      message: ['', Validators.required]
    })
  }

  hasError(controlName: string, error: string): boolean {
    const control = this.formContact.get(controlName);
    return !!control && control.hasError(error) && control.touched;
  }

  send(): void {
    if (this.formContact.valid) {
      console.log('Form Submitted:', this.formContact.value);
      alert('¡Mensaje enviado con éxito! Nos pondremos en contacto contigo pronto.');
      this.formContact.reset();
    }
  }


}
