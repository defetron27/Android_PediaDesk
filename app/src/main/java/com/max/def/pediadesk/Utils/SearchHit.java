package com.max.def.pediadesk.Utils;

import lombok.Getter;

@Getter
public class SearchHit {
  private final String url;

  public SearchHit(String url){
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
