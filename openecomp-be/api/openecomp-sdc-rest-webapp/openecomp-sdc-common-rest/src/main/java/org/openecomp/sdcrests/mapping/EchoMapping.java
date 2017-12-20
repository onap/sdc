package org.openecomp.sdcrests.mapping;

public class EchoMapping extends MappingBase <Object,Object>{
  @Override
  public void doMapping(Object source, Object target) {
    target = source;
  }
}
