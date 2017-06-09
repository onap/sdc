import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import { ROUTES } from './navbar-routes.config';
import { MenuType, RouteInfo } from './navbar.metadata';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: [ './navbar.component.less' ],
  encapsulation: ViewEncapsulation.None
})
export class NavbarComponent implements OnInit {
  public menuItems: Array<RouteInfo>;
  public brandMenu: RouteInfo;
  isCollapsed = true;

  constructor() {}

  ngOnInit() {
    this.menuItems = ROUTES.filter(menuItem => menuItem.menuType !== MenuType.BRAND);
    this.brandMenu = ROUTES.filter(menuItem => menuItem.menuType === MenuType.BRAND)[0];
  }

  public get menuIcon(): string {
    return this.isCollapsed ? '☰' : '✖';
  }

  public getMenuItemClasses(menuItem: any) {
    return {
      'pull-xs-right': this.isCollapsed && menuItem.menuType === MenuType.RIGHT
    };
  }
}
