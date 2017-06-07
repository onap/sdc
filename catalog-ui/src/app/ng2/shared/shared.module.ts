import { NgModule } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarModule } from "./navbar/navbar.module";

@NgModule({
  declarations: [

  ],
  imports: [
    CommonModule,
    RouterModule,
    NavbarModule
  ],
  exports: [
  ]
})

export class SharedModule {}
