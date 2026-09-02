import { TestBed, inject } from '@angular/core/testing';

import { ArtifactConfigService } from './artifact-config.service';
import {CacheService} from "./cache.service";

describe('ArtifactConfigService', () => {
  beforeEach(() => {
    const cacheServiceMock = {
      get: jest.fn(() => {
        return {
          artifact: null
        }
      })
    };
    TestBed.configureTestingModule({
      providers: [ArtifactConfigService, {provide: CacheService, useValue: cacheServiceMock}]
    });
  });

  it('should be created', inject([ArtifactConfigService], (service: ArtifactConfigService) => {
    expect(service).toBeTruthy();
  }));
});
