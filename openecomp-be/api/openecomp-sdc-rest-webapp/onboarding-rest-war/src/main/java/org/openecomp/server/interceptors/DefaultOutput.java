/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.server.interceptors;


import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DefaultOutput extends Response {

  private static final long serialVersionUID = 8061802931931401706L;

  private final int status;
  private final Object entity;
  private MultivaluedMap<String, Object> metadata;

  public DefaultOutput(int s0, Object e0) {
    this.status = s0;
    this.entity = e0;
  }

  @Override
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
  public boolean hasEntity() {
    return false;
  }

  @Override
  public boolean bufferEntity() {
    return false;
  }

  @Override
  public void close() {
    //close() is not implemented for DefaultOutput
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
    return Collections.emptySet();
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
    return Collections.emptySet();
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

  @Override
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

  @Override
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
