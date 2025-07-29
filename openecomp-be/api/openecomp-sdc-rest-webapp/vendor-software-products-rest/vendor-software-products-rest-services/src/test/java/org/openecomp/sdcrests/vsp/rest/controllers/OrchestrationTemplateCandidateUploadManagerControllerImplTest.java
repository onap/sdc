 /*
  * -
  *  ============LICENSE_START=======================================================
  *  Copyright (C) 2022 Nordix Foundation.
  *  ================================================================================
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  *  SPDX-License-Identifier: Apache-2.0
  *  ============LICENSE_END=========================================================
  */

 package org.openecomp.sdcrests.vsp.rest.controllers;

 import static org.junit.jupiter.api.Assertions.assertEquals;
 import static org.junit.jupiter.api.Assertions.assertTrue;
 import static org.mockito.Mockito.when;

 import java.util.Date;
 import java.util.Optional;
 import java.util.UUID;
 import javax.ws.rs.core.Response;
 import org.apache.http.HttpStatus;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
 import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
 import org.openecomp.sdcrests.vsp.rest.services.OrchestrationTemplateCandidateUploadManager;
 import org.springframework.http.ResponseEntity;

 class OrchestrationTemplateCandidateUploadManagerControllerImplTest {

     @Mock
     private OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager;

     @InjectMocks
     private OrchestrationTemplateCandidateUploadManagerControllerImpl packageUploadManagerControllerImpl;

     @BeforeEach
     void setUp() {
         MockitoAnnotations.openMocks(this);
     }

     @Test
     void getLatestSuccessTest() {
         //given
         final String vspId = "vspId";
         final String vspVersionId = "vspVersionId";
         final String username = "username";
         final VspUploadStatusDto vspUploadStatusDto = buildDefaultVspUploadStatus(vspId, vspVersionId);
         //when
         when(orchestrationTemplateCandidateUploadManager.findLatestStatus(vspId, vspVersionId, username)).thenReturn(Optional.of(vspUploadStatusDto));

         final ResponseEntity response = packageUploadManagerControllerImpl.getLatestStatus(vspId, vspVersionId, username);
         //then
         assertEquals(HttpStatus.SC_OK, response.getStatusCodeValue());
         final Object actualEntity = response.getBody();
         assertTrue(actualEntity instanceof VspUploadStatusDto);
         assertEquals(vspUploadStatusDto, actualEntity);
     }

     private VspUploadStatusDto buildDefaultVspUploadStatus(final String vspId, final String vspVersionId) {
         final var vspUploadStatusDto = new VspUploadStatusDto();
         vspUploadStatusDto.setStatus(VspUploadStatus.UPLOADING);
         vspUploadStatusDto.setLockId(UUID.randomUUID());
         vspUploadStatusDto.setVspId(vspId);
         vspUploadStatusDto.setVspVersionId(vspVersionId);
         vspUploadStatusDto.setCreated(new Date());
         vspUploadStatusDto.setComplete(false);
         return vspUploadStatusDto;
     }

     @Test
     void buildGetUrlSuccessTest() {
         final String vspId = "vspId";
         final String vspVersionId = "vspVersionId";
         final String actualGetUrl = OrchestrationTemplateCandidateUploadManagerControllerImpl.buildGetUrl(vspId, vspVersionId);
         assertEquals("/v1.0/vendor-software-products/vspId/versions/vspVersionId/orchestration-template-candidate/upload", actualGetUrl);
     }

 }