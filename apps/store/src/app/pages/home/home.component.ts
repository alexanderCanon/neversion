import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlatformService } from '../../services/platform.service';
import { ServiceResponse } from '@neversion/api-client';

@Component({
  standalone: false,
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  platforms$!: Observable<ServiceResponse[]>;

  constructor(private _platformService: PlatformService) { }

  ngOnInit(): void {
    // Obtenemos solo los primeros 4 servicios para el home
    this.platforms$ = this._platformService.getPlatforms().pipe(
      map((services) => services.slice(0, 4))
    );
  }
}
