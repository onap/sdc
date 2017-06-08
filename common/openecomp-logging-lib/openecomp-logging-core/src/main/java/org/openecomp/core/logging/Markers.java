package org.openecomp.core.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * <p>The list of markers that can be used for special logging such as metrics, audit, etc.</p>
 * <p>Although markers can be easily instantiated whenever needed, having constants for them helps
 * eliminate mistakes - misspelling, using a marker that is not handled, etc.</p> <p>Usage:</p>
 * <pre>
 *     Logger log = LogFactory.getLogger(this.getClass());
 *     log.info(Markers.AUDIT, "User '{}' logged out", user);
 * </pre>
 *
 * @see org.slf4j.Marker
 */
public class Markers {

  public static final Marker AUDIT = MarkerFactory.getMarker("AUDIT");
  public static final Marker METRICS = MarkerFactory.getMarker("METRICS");
}
