import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'contentAfterLastDot' })
export class ContentAfterLastDotPipe implements PipeTransform {
    transform(value:string): string {
        return value.split('.').pop();
    }
}