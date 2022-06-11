package com.route.datasource.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends CommonDTO {
  private String name;
  
  public UserDTO() {
    
  }
  
  public UserDTO(Integer worldId, String name) {
    this.name = name;
    super.worldId = worldId;
  }
}
