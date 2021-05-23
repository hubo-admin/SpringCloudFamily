package com.thoughtmechanix.licenses.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfig{

  @Value("${example.property}")
  private String exampleProperty;

  /**
   * 获取配置文件中签名密钥
   */
  @Value("${signing.key}")
  private String jwtSigningKey="";

  public String getExampleProperty(){
    return exampleProperty;
  }

  public String getJwtSigningKey() {
    return jwtSigningKey;
  }
}
