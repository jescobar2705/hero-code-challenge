package com.hero.aem.core.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.aem.core.dto.RepositoryInfo;
import com.hero.aem.core.services.GetReposInfoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author escobar Jorge
 */
@Model(adaptables = Resource.class)
@Slf4j
public class ReposInformationModel {

  @OSGiService
  private GetReposInfoService infoService;

  @ValueMapValue(name="author", injectionStrategy= InjectionStrategy.OPTIONAL)
  @Default(values = "adobe-consulting-services")
  private String author;

  @ValueMapValue(name="maxResults", injectionStrategy= InjectionStrategy.OPTIONAL)
  @Default(intValues = 1)
  private int maxResults;

  @Getter
  private List<RepositoryInfo> repositoryInfo;

  @PostConstruct
  protected void init() {
    repositoryInfo = buildResponseObject();
  }

  /**
   * Collect the data required to be displayed in the markup.
   * @return a List of all repositories' information.
   */
  private List<RepositoryInfo> buildResponseObject() {
    final String serviceResponse = infoService.getReposInformation();
    if (StringUtils.isNotEmpty(serviceResponse)) {
      try {
        final List<RepositoryInfo> tmpList = new ArrayList<>();
        final JsonNode servicesArray = new ObjectMapper().readTree(serviceResponse);
        servicesArray.forEach(item -> tmpList.add(RepositoryInfo.builder()
                .name(item.get("name").asText(StringUtils.EMPTY))
                .url(item.get("owner").get("url").asText(StringUtils.EMPTY))
                .username(item.get("owner").get("repos_url").asText(StringUtils.EMPTY))
                .avatar(item.get("owner").get("avatar_url").asText(StringUtils.EMPTY))
                .build()));
        return tmpList.stream().limit(maxResults).collect(Collectors.toList());
      } catch (IOException e) {
        log.error("An error has occurred while trying to process the data. See error -> {}", e.getMessage(), e);
      }
    }
    return new ArrayList<>();
  }


}
