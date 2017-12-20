package org.openecomp.sdc.vendorlicense;

import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class VendorLicenseUtil {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public static String getIsoFormatDate(String inputDate) {
    mdcDataDebugMessage.debugEntryMessage("inputDate date", inputDate);
    String isoFormatDate = null;
    SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss'Z'");

    try {
      isoFormatDate = isoDateFormat.format(inputDateFormat.parse(inputDate));
    } catch (ParseException e) {
      mdcDataDebugMessage.debugExitMessage("parsing error", isoFormatDate);
      isoFormatDate = null;
    }

    mdcDataDebugMessage.debugExitMessage("formatted date", isoFormatDate);
    return isoFormatDate;
  }
}
