import { MenuType, RouteInfo } from './navbar.metadata';

export const ROUTES: RouteInfo[] = [
  { path: 'page1', title: 'Logo', menuType: MenuType.BRAND },
  { path: 'page1', title: 'Page 1', menuType: MenuType.LEFT },
  { path: 'page2', title: 'Page 2', menuType: MenuType.LEFT }
];
