package com.hero.aem.core.services.impl;

import com.hero.aem.core.services.GetReposInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;

/**
 * @author escobar Jorge <jescobar2705@gmail.com>
 */
@Component(service = GetReposInfoService.class, immediate = true)
@Designate(ocd = GetReposInfoServiceImpl.GetReposInfoServiceConfig.class)
@Slf4j
public class GetReposInfoServiceImpl implements GetReposInfoService{

  private String endpoint;


  @Activate
  @Modified
  protected void activate(final GetReposInfoServiceConfig config) {
    this.endpoint = config.endpoint();
  }

  @Override
  public String getReposInformation() {
    final HttpGet request = new HttpGet(endpoint);
    String response = StringUtils.EMPTY;
    try (final CloseableHttpClient httpClient = HttpClients.createDefault();
         final CloseableHttpResponse closeableHttpResponse = httpClient.execute(request)) {

        if (closeableHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          final HttpEntity entity = closeableHttpResponse.getEntity();
          response = EntityUtils.toString(entity);
          log.info("Raw response::: {}", response);
        }

    } catch (ClientProtocolException e) {
      log.error("Impossible to connect with the service. See error -> : {}", e.getMessage(), e);
    } catch (IOException e) {
      log.error("An error has occurred while trying get the information. See error -> : {}", e.getMessage(), e);
    }
    return response;
  }

  @ObjectClassDefinition(name = "Hero Digital - ACS repositories' service",
          description = "Fetches the information about ACS repositories")
  public @interface GetReposInfoServiceConfig {
    @AttributeDefinition(name = "Service Endpoint",
            description = "The service endpoint to fetch the repositories' information")

    String endpoint() default "https://api.github.com/users/adobe-consulting-services/repos";
  }
}
