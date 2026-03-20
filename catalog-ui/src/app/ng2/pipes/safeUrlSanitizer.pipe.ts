import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Pipe({ name: 'safeUrlSanitizer' })
export class SafeUrlSanitizerPipe implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) {}
    transform(url: string): SafeResourceUrl {
        if (this.isSafeResourceUrl(url)) {
            return this.sanitizer.bypassSecurityTrustResourceUrl(url);
        }
        return this.sanitizer.bypassSecurityTrustResourceUrl('about:blank');
    }

    private isSafeResourceUrl(url: string): boolean {
        if (!url) {
            return false;
        }
        try {
            const parsed = new URL(url);
            return parsed.protocol === 'https:' || parsed.protocol === 'http:';
        } catch (e) {
            return false;
        }
    }
}
