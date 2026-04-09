import { Component, OnInit } from '@angular/core';
import { Platforms } from '../../model/platforms.model';
import { Observable, from } from 'rxjs';
import { map } from 'rxjs/operators';
import { PlatformService } from '../../services/platform.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  platforms$!: Observable<Platforms[]>;

  constructor(private _platformService: PlatformService) { }

  ngOnInit(): void {
    // Obtenemos solo las primeras 4 plataformas ordenadas por ID para mostrar en el home
    this.platforms$ = this._platformService.getPlatforms().pipe(
      map((platforms: Platforms[]) => platforms.sort((a, b) => a.id - b.id).slice(0, 4))
    );
  }
}
