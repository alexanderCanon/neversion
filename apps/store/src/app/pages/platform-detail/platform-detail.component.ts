import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Platforms } from 'src/app/model/platforms.model';

@Component({
  selector: 'app-platform-detail',
  templateUrl: './platform-detail.component.html',
  styleUrls: ['./platform-detail.component.css']
})
export class PlatformDetailComponent implements OnInit {
  platform?: Platforms;
  platformList: Platforms[] = [];
  loading: boolean = true;

  constructor(private _route: ActivatedRoute) { }

  ngOnInit(): void {
    setTimeout(() => {
      this._route.params.subscribe(params => {
        this.platform = this.platformList.find(platform => platform.id == params['platformId']);
        this.loading = false;
      });
    }, 1000);
  }
}
