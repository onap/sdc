package org.openecomp.sdc.vendorlicense;


import java.text.ParseException;
import java.text.SimpleDateFormat;

public class VendorLicenseUtil {
  public static String getIsoFormatDate(String inputDate) {
    String isoFormatDate = null;
    SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat inputDateFormat = new SimpleDateFormat("MM/dd/yyyy'T'HH:mm:ss'Z'");

    try {
      isoFormatDate = isoDateFormat.format(inputDateFormat.parse(inputDate));
    } catch (ParseException e) {
      isoFormatDate = null;
    }
    return isoFormatDate;
  }
}
