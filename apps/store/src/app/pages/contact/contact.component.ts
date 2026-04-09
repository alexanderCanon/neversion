import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-contact',
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {

  formContact: FormGroup;
  userActive: string = "Esto se trae desde el backend";
  options: string[] = ['Problemas con servicios', 'Problemas con productos', 'Problemas con pagos'];

  ngOnInit(): void {
    this.formContact.valueChanges.subscribe(value => {
      console.log(value);
    })
  }

  constructor(private formBuilder: FormBuilder) {
    this.formContact = this.formBuilder.group({
      name: ['', Validators.required, Validators.minLength(3)],
      email: ['', Validators.required, Validators.email],
      message: ['', Validators.required]
    })
  }

  hasError(controlName: string, error: string) {
    return this.formContact.get(controlName)?.hasError(error) && this.formContact.get(controlName)?.touched;
  }

  send() {
    console.log(this.formContact.value);
  }


}
