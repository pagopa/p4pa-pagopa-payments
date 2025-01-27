package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.endpoint.PaForNodeEndpoint;
import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.support.WebUtils;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.transport.http.WsdlDefinitionHandlerAdapter;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.xml.xsd.XsdSchemaCollection;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EnableWs
@Slf4j
@Configuration(proxyBeanMethods = false)
public class SoapWebServiceConfig extends WsConfigurerAdapter {

  public static final String WS_PATH_NODE = WebSecurityConfig.SOAP_WS_BASE_PATH+"/node/";
  private static final String SOAP_RESOURCES_FOLDER = "soap";

  public static final String XSD_PA_FOR_NODE = "paForNode";
  public static final String XSD_SAC_COMMON_TYPES = "sac-common-types-1.0";
  public static final String XSD_MARCA_DA_BOLLO = "MarcaDaBollo";
  public static final String XSD_XMLDSIG_CORE_SCHEMA = "xmldsig-core-schema";
  public static final String XSD_COMMON_PATH = "xsd-common/";

  protected static final Set<String> WS_PATH_NAME_SET = new HashSet<>();

  public static final Map<String, String> XSD_NAME_PATH_MAP = Map.of(
    XSD_PA_FOR_NODE, "wsdl/xsd/",
    XSD_SAC_COMMON_TYPES, XSD_COMMON_PATH,
    XSD_MARCA_DA_BOLLO, XSD_COMMON_PATH,
    XSD_XMLDSIG_CORE_SCHEMA, XSD_COMMON_PATH
  );

  private final String pagopaPaymentsWsdlBaseUrl;
  private final ResourceLoader resourceLoader;

  public SoapWebServiceConfig(
    @Value("${soap.pagopa-payments.wsdl-base-url}")String pagopaPaymentsWsdlBaseUrl,
    ResourceLoader resourceLoader) {
    this.pagopaPaymentsWsdlBaseUrl = pagopaPaymentsWsdlBaseUrl;
    this.resourceLoader = resourceLoader;
  }

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet(){
      @Override
      protected WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String name = WebUtils.extractFilenameFromUrlPath(uri);
        String path = extractPathFromUrlPath(uri);
        log.trace("getWsdlDefinition uri:{} path:{} name:{} found:{}", uri, name, path, WS_PATH_NAME_SET.contains(path+name));
        if(WS_PATH_NAME_SET.contains(path+name))
          return super.getWsdlDefinition(request);
        else
          return null;
      }

      @Override
      protected XsdSchema getXsdSchema(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String name = WebUtils.extractFilenameFromUrlPath(uri);
        String path = extractPathFromUrlPath(uri);
        String xsdPath = request.getContextPath()+WS_PATH_NODE+XSD_NAME_PATH_MAP.getOrDefault(name,"__NOT_FOUND__");
        if(xsdPath.equals(path))
          return super.getXsdSchema(request);
        else
          return null;
      }
    };
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, WebSecurityConfig.SOAP_WS_BASE_PATH + "/*");
  }

  private void registerWsdlDefinition(String path){
    log.debug("register ws soap: {}", path);
    WS_PATH_NAME_SET.add(path);
    log.trace("WS_PATH_NAME_SET contains now: {}",WS_PATH_NAME_SET);
  }

  @Bean("wsdlDefinitionHandlerAdapter")
  public WsdlDefinitionHandlerAdapter getWsdlDefinitionHandlerAdapter(){
    return new WsdlDefinitionHandlerAdapter(){
      @Override
      protected String transformLocation(String location, HttpServletRequest request) {
        //do not take url from request, because it may be changed by proxy / ingress. Use application property
        StringBuilder url = new StringBuilder(pagopaPaymentsWsdlBaseUrl);
        if (location.startsWith("/")) {
          url.append(location);
          return url.toString();
        } else {
          log.error("wsdl url in location must start with / : [{}]", request.getRequestURL());
          return super.transformLocation(location, request);
        }
      }
    };
  }

  private static String extractPathFromUrlPath(String urlPath) {
    int end = urlPath.indexOf('?');
    if (end == -1) {
      end = urlPath.indexOf('#');
      if (end == -1) {
        end = urlPath.length();
      }
    }
    int begin = urlPath.lastIndexOf('/', end) + 1;
    //int paramIndex = urlPath.indexOf(';', begin);
    return urlPath.substring(0, begin);
  }

  @Bean
  public XsdSchemaCollection getXsdSchemaCollection() {
    return null;
  }

  @Bean(name = PaForNodeEndpoint.NAME)
  public Wsdl11Definition paForNodeEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_NODE + "wsdl/" + PaForNodeEndpoint.NAME);
    return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:soap/wsdl/paForNode.wsdl"));
  }

  @Bean(name = XSD_PA_FOR_NODE)
  public XsdSchema getPaForNodeXsd() {
    return new SimpleXsdSchema(new ClassPathResource(SOAP_RESOURCES_FOLDER+"/"+XSD_NAME_PATH_MAP.get(XSD_PA_FOR_NODE)+ XSD_PA_FOR_NODE +".xsd"));
  }

  @Bean(name = XSD_MARCA_DA_BOLLO)
  public XsdSchema getMarcaDaBolloXsd() {
    return new SimpleXsdSchema(new ClassPathResource(SOAP_RESOURCES_FOLDER+"/"+XSD_NAME_PATH_MAP.get(XSD_MARCA_DA_BOLLO)+ XSD_MARCA_DA_BOLLO +".xsd"));
  }

  @Bean(name = XSD_SAC_COMMON_TYPES)
  public XsdSchema getSacCommonTypesXsd() {
    return new SimpleXsdSchema(new ClassPathResource(SOAP_RESOURCES_FOLDER+"/"+XSD_NAME_PATH_MAP.get(XSD_SAC_COMMON_TYPES)+ XSD_SAC_COMMON_TYPES +".xsd"));
  }


  @Bean(name = XSD_XMLDSIG_CORE_SCHEMA)
  public XsdSchema getXmldsigCoreSchemaXsd() {
    return new SimpleXsdSchema(new ClassPathResource(SOAP_RESOURCES_FOLDER+"/"+XSD_NAME_PATH_MAP.get(XSD_XMLDSIG_CORE_SCHEMA)+ XSD_XMLDSIG_CORE_SCHEMA +".xsd"));
  }
}
