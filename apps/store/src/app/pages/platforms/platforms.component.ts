import { Component, OnInit } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { Observable } from 'rxjs';
import { ServiceResponse } from '@neversion/api-client';

@Component({
  selector: 'app-platforms',
  templateUrl: './platforms.component.html',
  styleUrls: ['./platforms.component.css'],
})
export class PlatformsComponent implements OnInit {

  platforms$!: Observable<ServiceResponse[]>;

  constructor(private _platformService: PlatformService) { }

  ngOnInit(): void {
    this.platforms$ = this._platformService.getPlatforms();
  }
}
