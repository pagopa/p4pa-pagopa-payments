package it.gov.pagopa.pu.pagopapayments.config;

import it.gov.pagopa.pu.pagopapayments.exception.ApplicationException;
import it.gov.pagopa.pu.pagopapayments.ws.server.PaForNodeEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

  @Value("${app.absolute-base-url}")
  private String appAbsolutePath;

  @Autowired
  private ResourceLoader resourceLoader;


  public static final String WS_PATH_SIL = WebSecurityConfig.SOAP_WS_BASE_PATH+"/sil/";
  public static final String WS_PATH_NODE = WebSecurityConfig.SOAP_WS_BASE_PATH+"/node/";

  public static final String XSD_PagInf_Dovuti_Pagati_6_2_0 = "PagInf_Dovuti_Pagati_6_2_0";
  public static final String XSD_PagInf_RP_Esito_6_2_0 = "PagInf_RP_Esito_6_2_0";
  public static final String XSD_PagInf_RPT_RT_6_2_0 = "PagInf_RPT_RT_6_2_0";
  public static final String XSD_PaForNode = "paForNode";
  public static final String XSD_SacCommonTypes = "sac-common-types-1.0";
  public static final String XSD_MarcaDaBollo = "MarcaDaBollo";
  public static final String XSD_XmldsigCoreSchema = "xmldsig-core-schema";



  public static Set<String> WS_PATH_NAME_SET = new HashSet<>();


  public static final Map<String, String> XSD_NAME_PATH_MAP = Map.of(
      XSD_PagInf_Dovuti_Pagati_6_2_0, WS_PATH_SIL,
      XSD_PagInf_RP_Esito_6_2_0, WS_PATH_SIL,
      XSD_PagInf_RPT_RT_6_2_0, WS_PATH_NODE,
      XSD_PaForNode, WS_PATH_NODE,
      XSD_SacCommonTypes, WS_PATH_NODE,
      XSD_MarcaDaBollo, WS_PATH_NODE,
      XSD_XmldsigCoreSchema, WS_PATH_NODE
  );

  private void registerWsdlDefinition(String path){
    String contextRoot;
    try{
      contextRoot = new URI(appAbsolutePath).getPath().replaceAll("/$", "");
    } catch(Exception e){
      throw new ApplicationException("invalid app.absolute-base-url ["+ appAbsolutePath +"]", e);
    }
    log.debug("register ws soap: {}",contextRoot + path);
    WS_PATH_NAME_SET.add(contextRoot + path);
    log.trace("WS_PATH_NAME_SET contains now: {}",WS_PATH_NAME_SET);
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
        String xsdPath = request.getContextPath()+XSD_NAME_PATH_MAP.getOrDefault(name,"__NOT_FOUND__");
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

  @Bean("wsdlDefinitionHandlerAdapter")
  public WsdlDefinitionHandlerAdapter getWsdlDefinitionHandlerAdapter(){
    return new WsdlDefinitionHandlerAdapter(){
      @Override
      protected String transformLocation(String location, HttpServletRequest request) {
        //do not take url from request, because it may be changed by proxy / ingress. Use application property
        StringBuilder url = new StringBuilder(appAbsolutePath);
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

  @Bean
  public XsdSchemaCollection getXsdSchemaCollection() {
    return null;
  }


  @Bean(name = PaForNodeEndpoint.NAME)
  @ConditionalOnProperty(prefix = "fesp", name = "mode", havingValue = "local")
  public Wsdl11Definition paForNodeEndpoint(XsdSchemaCollection xsdSchemaCollection) {
    registerWsdlDefinition(WS_PATH_NODE +PaForNodeEndpoint.NAME);
    return new SimpleWsdl11Definition(resourceLoader.getResource("classpath:wsdl/fesp/paForNode.wsdl"));
  }

  @Bean(name = XSD_PagInf_Dovuti_Pagati_6_2_0)
  public XsdSchema getPagInf_Dovuti_Pagati_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/pa/"+XSD_PagInf_Dovuti_Pagati_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PagInf_RP_Esito_6_2_0)
  public XsdSchema getPagInf_RP_Esito_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/pa/"+XSD_PagInf_RP_Esito_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PagInf_RPT_RT_6_2_0)
  public XsdSchema getPagInf_RPT_RT_6_2_0Xsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_PagInf_RPT_RT_6_2_0+".xsd"));
  }

  @Bean(name = XSD_PaForNode)
  public XsdSchema getPaForNodeXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_PaForNode+".xsd"));
  }

  @Bean(name = XSD_SacCommonTypes)
  public XsdSchema getSacCommonTypesXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_SacCommonTypes+".xsd"));
  }

  @Bean(name = XSD_MarcaDaBollo)
  public XsdSchema getMarcaDaBolloXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_MarcaDaBollo+".xsd"));
  }

  @Bean(name = XSD_XmldsigCoreSchema)
  public XsdSchema getXmldsigCoreSchemaXsd() {
    return new SimpleXsdSchema(new ClassPathResource("wsdl/fesp/"+XSD_XmldsigCoreSchema+".xsd"));
  }
}