package com.hero.aem.core.models;


import com.day.cq.wcm.api.Page;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.common.collect.ImmutableMap;
import com.hero.aem.core.services.GetReposInfoService;
import com.hero.aem.core.services.impl.GetReposInfoServiceImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@WireMockTest(httpPort = 8080)
public class ReposInformationModelTest {

  private static final String ENDPOINT = "http://localhost:8080/users/adobe-consulting-services/repos";

  private final AemContext context = new AemContext();
  private Page page;
  private ReposInformationModel reposInformationModel;

  @InjectMocks
  private GetReposInfoService infoService = new GetReposInfoServiceImpl();


  private void stubGetReposInfo() throws URISyntaxException, IOException {
    stubFor(get("/users/adobe-consulting-services/repos")
            .willReturn(aResponse()
                    .withStatus(HttpStatus.SC_OK)
                    .withHeader("Content-Type", "application/json")
                    .withBody(getInfoResponse())));

  }

  @BeforeEach
  public void setup() throws Exception {
    page = context.create().page("/content/hero");
    context.registerService(GetReposInfoService.class, infoService);
    stubGetReposInfo();
    context.registerInjectActivateService(infoService, ImmutableMap.of("endpoint", ENDPOINT));

  }

  private String getInfoResponse() throws URISyntaxException, IOException {
    final URL resource = getClass().getClassLoader().getResource("responses/components/repositoriesinfo.json");
    if (resource != null) {
      return IOUtils.toString(resource.toURI(), StandardCharsets.UTF_8.toString());
    } else {
      return StringUtils.EMPTY;
    }
  }

  @Test
  public void successDefaultConfig() {
    final Resource resource = context.create().resource(page, "repositoriesInfo",
            ImmutableMap.of("sling:resourceType", "hero-code-challenge/components/content/repositoriesInfo",
                    "jcr:primaryType", "nt:unstructured",
                    "jcr:lastModifiedBy", "admin"));

    reposInformationModel = context.getService(ModelFactory.class).createModel(resource, ReposInformationModel.class);
    assertEquals("adobe-consulting-services", reposInformationModel.getAuthor());
    assertEquals(1, reposInformationModel.getRepositoryInfo().size());
  }

  @Test
  public void successWithRecords() {
    final Resource resource = context.create().resource(page, "repositoriesInfo2",
            ImmutableMap.of("sling:resourceType", "hero-code-challenge/components/content/repositoriesInfo",
                    "maxResults", 3, "author", "Hero Digital", "jcr:primaryType", "nt:unstructured",
                    "jcr:lastModifiedBy", "admin"));

    reposInformationModel = context.getService(ModelFactory.class).createModel(resource, ReposInformationModel.class);
    assertEquals("Hero Digital", reposInformationModel.getAuthor());
    assertEquals(3, reposInformationModel.getRepositoryInfo().size());
  }

  @Test
  public void sucessWithMaxResultsGreater() {
    final Resource resource = context.create().resource(page, "repositoriesInfo2",
            ImmutableMap.of("sling:resourceType", "hero-code-challenge/components/content/repositoriesInfo",
                    "maxResults", 20, "author", "Hero Digital", "jcr:primaryType", "nt:unstructured",
                    "jcr:lastModifiedBy", "admin"));

    reposInformationModel = context.getService(ModelFactory.class).createModel(resource, ReposInformationModel.class);
    assertEquals("Hero Digital", reposInformationModel.getAuthor());
    assertEquals(18, reposInformationModel.getRepositoryInfo().size());
  }
}
