package it.gov.pagopa.pu.pagopapayments.service;

import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import jakarta.xml.bind.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JAXBTransformService {
  final private static Pattern PATTERN_NAMESPACE = Pattern.compile("(\\s*xmlns(?>:\\w*)?\\s*=\\s*\".*?\"\\s*)", Pattern.CASE_INSENSITIVE);

  @Autowired
  private ResourceLoader resourceLoader;

  public <T> String marshalling(T element, Class<T> clazz) {
    return marshallingImpl(element, clazz, baos -> baos.toString(StandardCharsets.UTF_8), null);
  }

  public <T> byte[] marshallingAsBytes(T element, Class<T> clazz) {
    return marshallingImpl(element, clazz, ByteArrayOutputStream::toByteArray, null);
  }

  public <T> byte[] marshallingAsBytes(T element, Class<T> clazz, String jaxbElementName) {
    return marshallingImpl(element, clazz, ByteArrayOutputStream::toByteArray, jaxbElementName);
  }

  private <T, R> R marshallingImpl(T element, Class<T> clazz, Function<ByteArrayOutputStream, R> outConverterFun, String jaxbElementName) {
    if(element == null)
      return null;
    try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
      Marshaller marshaller = JAXBContext.newInstance(clazz).createMarshaller();
      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, false);
      marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true);
      Object objectToMarshall = StringUtils.isBlank(jaxbElementName) ?
        element :
        new JAXBElement<>(new QName("", jaxbElementName), clazz, element);
      marshaller.marshal(objectToMarshall, baos);
      return outConverterFun.apply(baos);
    } catch ( JAXBException | IOException e ) {
      log.error("marshalling - Error due parsing", e);
      throw new ApplicationException(e);
    }
  }

  //workaround because for some reason the NoNamesWriter approach returns empty string
  // on some environments
  public <T> String marshallingNoNamespace(T element, Class<T> clazz) {
    String s = this.marshalling(element, clazz);
    if(s!=null) {
      Matcher matcher = PATTERN_NAMESPACE.matcher(s);
      s = matcher.replaceAll("");
    }
    return s;
  }

  public <T> T unmarshalling(byte[] bytes, Class<T> clazz) {
    return unmarshalling(bytes, clazz, null);
  }

  public <T> T unmarshalling(byte[] bytes, Class<T> clazz, String xsdFile) {
    return unmarshalling(bytes, clazz, xsdFile, true);
  }

  public <T> T unmarshalling(byte[] bytes, Class<T> clazz, String xsdFile, boolean tryStrippingNonValidChars) {
    if(bytes==null || bytes.length==0) {
      log.error("warning: unmarshalling: bytes is null");
      return null;
    }

    try {
      bytes = Base64.getDecoder().decode(bytes);
    } catch (IllegalArgumentException e) {
      //ignore the exception, it means that the bytes are not base64 encoded
    }

    try ( ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
      Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
      if(xsdFile!=null){
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(this.getResourcePath(xsdFile));
        unmarshaller.setSchema(schema);
      }
      Source source = new StreamSource(bais);
      JAXBElement<T> element = unmarshaller.unmarshal(source, clazz);
      return element.getValue();
    } catch (SAXException | IOException | JAXBException e ) {
      if(tryStrippingNonValidChars && e instanceof UnmarshalException && ((UnmarshalException) e).getLinkedException()!=null &&
      StringUtils.containsIgnoreCase(((UnmarshalException) e).getLinkedException().getMessage(), "invalid XML character")) {
        log.warn("detected 'invalid XML character' error unmarhalling XML.. trying to strip invalid characters", e);
        String string = new String(bytes, StandardCharsets.UTF_8);
        string = stripNonValidXMLCharacters(string);
        bytes = string.getBytes(StandardCharsets.UTF_8);
        return unmarshalling(bytes, clazz, xsdFile, false);
      }
      throw new ApplicationException(e);
    }
  }

  private URL getResourcePath(String resourcePath) throws IOException{
    if(resourcePath.startsWith("/"))
      resourcePath = "classpath:"+resourcePath.substring(1);
    return resourceLoader.getResource(resourcePath).getURL();
  }

  public String stripNonValidXMLCharacters(String in) {
    StringBuilder out = new StringBuilder(); // Used to hold the output.
    char current; // Used to reference the current character.

    if (in == null || in.isEmpty()) return ""; // vacancy test.
    for (int i = 0; i < in.length(); i++) {
      current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
      if (current == 0x9 ||
          current == 0xA ||
          current == 0xD ||
          current >= 0x20 && current <= 0xD7FF ||
          current >= 0xE000 && current <= 0xFFFD)
        out.append(current);
      else
        log.warn("stripping non valid char [\\0x{}]", Integer.toHexString(current));
    }
    return out.toString();
  }

}
