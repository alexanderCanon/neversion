import { Component, OnInit } from '@angular/core';
import { PlatformService } from '../../services/platform.service';
import { Platforms } from '../../model/platforms.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-platforms',
  templateUrl: './platforms.component.html',
  styleUrls: ['./platforms.component.css'],
})
export class PlatformsComponent implements OnInit {

  platforms$!: Observable<Platforms[]>;

  constructor(private _platformService: PlatformService) { }

  ngOnInit(): void {
    this.platforms$ = this._platformService.getPlatforms();
  }
}
