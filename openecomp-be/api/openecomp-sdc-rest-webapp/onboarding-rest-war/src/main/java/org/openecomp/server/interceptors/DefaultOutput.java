/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.server.interceptors;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public class DefaultOutput extends Response implements Serializable {

  private static final long serialVersionUID = 8061802931931401706L;

  private final int status;
  private final Object entity;
  private MultivaluedMap<String, Object> metadata;

  public DefaultOutput(int s0, Object e0) {
    this.status = s0;
    this.entity = e0;
  }
  public Object getEntity() {
    return entity;
  }

  @Override
  public <T> T readEntity(Class<T> asClass) {
    return null;
  }

  @Override
  public <T> T readEntity(GenericType<T> genericType) {
    return null;
  }

  @Override
  public <T> T readEntity(Class<T> asClass, Annotation[] annotations) {
    return null;
  }

  @Override
  public <T> T readEntity(GenericType<T> var1, Annotation[] var2) {
    return null;
  }

  @Override
  public boolean hasEntity() throws IllegalStateException {
    return false;
  }

  @Override
  public boolean bufferEntity() {
    return false;
  }

  @Override
  public void close() {
  }

  @Override
  public MediaType getMediaType() {
    return null;
  }

  @Override
  public Locale getLanguage() {
    return null;
  }

  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public Set<String> getAllowedMethods() {
    return null;
  }

  @Override
  public Map<String, NewCookie> getCookies() {
    return null;
  }

  @Override
  public EntityTag getEntityTag() {
    return null;
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public Date getLastModified() {
    return null;
  }

  @Override
  public URI getLocation() {
    return null;
  }

  @Override
  public Set<Link> getLinks() {
    return null;
  }

  @Override
  public boolean hasLink(String s0) {
    return false;
  }

  @Override
  public Link getLink(String s0) {
    return null;
  }

  @Override
  public Link.Builder getLinkBuilder(String s0) {
    return null;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public StatusType getStatusInfo() {
    return null;
  }

  void addMetadata(MultivaluedMap<String, Object> meta) {
    this.metadata = meta;
  }

  public MultivaluedMap<String, Object> getMetadata() {
    // don't worry about cloning for now
    return metadata;
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders() {
    return null;
  }

  @Override
  public String getHeaderString(String s0) {
    return null;
  }

}
