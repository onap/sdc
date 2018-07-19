/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdcrests.vsp.rest.services;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * @author evitaliy
 * @since 19 Jul 2018
 */
public class VnfPackageRepositoryImplTest {

    private static final String GET_PATH = "/get";
    private static final String DOWNLOAD_PATH = "/download";

    @ClassRule
    public static final WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());
    private static final String VSP = "anyVsp";
    private static final String VERSION = "anyVersion";
    private static final String USER = "anyUser";
    private static final String CSAR = "anyCsar";

    private static VnfPackageRepositoryImpl.Configuration config;

    @BeforeClass
    public static void initConfiguration() {
         config = new DynamicConfiguration(wireMockRule.port());
    }

    @Test
    public void versionFoundWhenInList() {
        VnfPackageRepositoryImpl vnfRepository = new VnfPackageRepositoryImpl();
        List<Version> versions = new ArrayList<>(3);
        versions.add(new Version("1243"));
        versions.add(new Version("3434"));
        versions.add(new Version("398"));
        assertTrue("Expected to find the version", vnfRepository.findVersion(versions, "3434").isPresent());
    }

    @Test
    public void versionNotFoundWhenInList() {
        VnfPackageRepositoryImpl vnfRepository = new VnfPackageRepositoryImpl();
        List<Version> versions = new ArrayList<>(1);
        versions.add(new Version("1243"));
        assertFalse("Did not expect to find the version", vnfRepository.findVersion(versions, "3434").isPresent());
    }

    @Test
    public void configurationLoadedFromFile() {
        final String prefix = "http://10.57.30.20:1111/";
        assertEquals(prefix + "download-vnf-31", new VnfPackageRepositoryImpl.FileConfiguration().getDownloadUri());
        assertEquals(prefix + "get-vnf-13", new VnfPackageRepositoryImpl.FileConfiguration().getGetUri());
    }

    @Test
    public void listVnfsReturnsInternalServerErrorWhenRemoteClientError() {
        stubFor(get(GET_PATH).willReturn(aResponse().withStatus(403)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.getVnfPackages(VSP, VERSION, USER);
        assertEquals(500, response.getStatus());
        verify(getRequestedFor(urlEqualTo(GET_PATH)));
    }

    @Test
    public void listVnfsReturnsInternalServerErrorWhenRemoteReturnsNotOk() {
        stubFor(get(GET_PATH).willReturn(aResponse().withStatus(204)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.getVnfPackages(VSP, VERSION, USER);
        assertEquals(500, response.getStatus());
        verify(getRequestedFor(urlEqualTo(GET_PATH)));
    }

    @Test
    public void listVnfsReturnsUnchangedResponse() {
        final String vnfList = "this is a response body for list of VNFs";
        stubFor(get(GET_PATH).willReturn(aResponse().withStatus(200).withBody(vnfList)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.getVnfPackages(VSP, VERSION, USER);
        assertEquals(200, response.getStatus());
        assertEquals(vnfList, response.getEntity());
        verify(getRequestedFor(urlEqualTo(GET_PATH)));
    }

    @Test
    public void downloadVnfsReturnsInternalServerErrorWhenRemoteClientError() {
        stubFor(get(DOWNLOAD_PATH).willReturn(aResponse().withStatus(403)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.downloadVnfPackage(VSP, VERSION, CSAR, USER);
        assertEquals(500, response.getStatus());
        verify(getRequestedFor(urlEqualTo(DOWNLOAD_PATH)));
    }

    @Test
    public void downloadVnfsReturnsInternalServerErrorWhenRemoteReturnsNotOk() {
        stubFor(get(DOWNLOAD_PATH).willReturn(aResponse().withStatus(204)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.downloadVnfPackage(VSP, VERSION, CSAR, USER);
        assertEquals(500, response.getStatus());
        verify(getRequestedFor(urlEqualTo(DOWNLOAD_PATH)));
    }

    @Test
    public void downloadVnfsReturnsUnchangedBytes() {
        final byte[] body = "this is the content of a VNF archive (.csar) file".getBytes(StandardCharsets.ISO_8859_1);
        stubFor(get(DOWNLOAD_PATH).willReturn(aResponse().withStatus(200).withBody(body)));
        VnfPackageRepositoryImpl repository = new VnfPackageRepositoryImpl(config);
        Response response = repository.downloadVnfPackage(VSP, VERSION, CSAR, USER);
        assertEquals(200, response.getStatus());
        assertTrue(Arrays.equals(body, response.readEntity(byte[].class)));
        assertNotNull(response.getHeaderString("Content-Disposition"));
        verify(getRequestedFor(urlEqualTo(DOWNLOAD_PATH)));
    }

    private static class DynamicConfiguration implements VnfPackageRepositoryImpl.Configuration {

        private final int port;

        private DynamicConfiguration(int port) {
            this.port = port;
        }

        @Override
        public String getGetUri() {
            return toUri(GET_PATH);
        }

        @Override
        public String getDownloadUri() {
            return toUri(DOWNLOAD_PATH);
        }

        private String toUri(String path) {
            return "http://localhost:" + port + path;
        }
    }

}