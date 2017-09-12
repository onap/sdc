package org.openecomp.core.impl.factory;

import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.factory.ToscaConverterFactory;
import org.openecomp.core.impl.ToscaConverterImpl;

public class ToscaConverterFactoryImpl extends ToscaConverterFactory {
  @Override
  public ToscaConverter createInterface() {
    return new ToscaConverterImpl();
  }
}
