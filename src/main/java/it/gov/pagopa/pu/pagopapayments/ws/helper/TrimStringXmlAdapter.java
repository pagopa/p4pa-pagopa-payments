package it.gov.pagopa.pu.pagopapayments.ws.helper;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import lombok.extern.slf4j.Slf4j;

/*
 * Utility class used to trim all strings during marshalling/unmarshalling of SOAP messages.
 * It is referenced in the .xjb files of single WSDL.
 *
 * Warning: due to internal working of JAXB Gradle plugin, it is necessary that during compilation phase
 * a class with this signature exists. Therefore a jar called <project.name>-XmlAdapter.jar has been put into
 * folder 'libs'.
 * Anyway, at runtime, that jar is not used and the compiled class in the 'main' MyPay4 jar is used. So
 * there is no need to update the mypay4-be-XmlAdapter.jar in case a modification is made on this class,
 * for it to be seen at runtime.
 *
 */
@Slf4j
public class TrimStringXmlAdapter extends XmlAdapter<String, String> {

  @Override
  public String marshal(String text) {
    return this.trim("marshal", text);
  }

  @Override
  public String unmarshal(String v) {
    return this.trim("unmarshal", v);
  }

  private String trim(String oper, String text){
    if(text==null)
      return null;
    String trimmed = text.trim();
    if(log.isDebugEnabled() && trimmed.length()!=text.length())
      log.debug("{}: trim [{}] -> [{}]", oper, text, trimmed);
    return trimmed;
  }
}
