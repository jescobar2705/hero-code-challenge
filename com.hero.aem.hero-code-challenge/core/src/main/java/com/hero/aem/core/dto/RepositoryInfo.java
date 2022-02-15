package com.hero.aem.core.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author escobar Jorge <jescobar2705@gmail.com>
 */

@Data
@Builder
@ToString
public class RepositoryInfo {

  private String name;
  private String url;
  private String username;
  private String avatar;

}


