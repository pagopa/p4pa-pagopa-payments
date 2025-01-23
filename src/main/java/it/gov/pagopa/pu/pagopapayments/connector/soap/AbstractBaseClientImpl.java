package it.gov.pagopa.pu.pagopapayments.connector.soap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
public abstract class AbstractBaseClientImpl extends WebServiceGatewaySupport {

  protected WebServiceMessageCallback getMessageCallback(){
    return getMessageCallback(null);
  }

  protected WebServiceMessageCallback getMessageCallback(Object header){
    return message -> {
      try {
        //fix SoapAction header not included automatically by Spring WS
        Arrays.stream(Thread.currentThread().getStackTrace())
          .filter(i -> {
            try {
              return !i.getClassName().equals(AbstractBaseClientImpl.class.getName()) && AbstractBaseClientImpl.class.isAssignableFrom(Class.forName(i.getClassName()));
            } catch (ClassNotFoundException e) {
              return false;
            } })
          .findFirst()
          .ifPresent(ste -> {
            String methodName = ste.getMethodName();
            methodName = StringUtils.firstNonBlank(StringUtils.substringAfter(methodName, "lambda$"), methodName);
            methodName = StringUtils.substringBefore(methodName, "$");
            log.debug("ste: {} - method: {} [{}]",ste, methodName, ste.getMethodName());
            ((SoapMessage)message).setSoapAction(methodName);
          });


        if(header==null)
          return;
        SoapHeader soapHeader = ((SoapMessage) message).getSoapHeader();
        Class objFactoryClass = Class.forName(header.getClass().getPackageName()+".ObjectFactory");
        Method createMethod = objFactoryClass.getMethod("create"+ StringUtils.capitalize(header.getClass().getSimpleName()));
        Object headerObj = createMethod.invoke(objFactoryClass.getConstructor().newInstance());
        BeanUtils.copyProperties(headerObj, header);
        JAXBContext context = JAXBContext.newInstance(header.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.marshal(headerObj, soapHeader.getResult());
      } catch(Exception e){
        log.error("error during marshalling of the SOAP headers", e);
        throw new RuntimeException("error during marshalling of the SOAP headers", e);
      }
    };
  }

}
