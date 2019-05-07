package org.folio.holdingsiq.service.config;

import org.folio.holdingsiq.model.OkapiData;

import com.google.common.collect.ImmutableMap;

public class ConfigTestData {
  public static final String OKAPI_TOKEN_HEADER = "x-okapi-token";
  public static final String OKAPI_URL_HEADER = "x-okapi-url";
  public static final String OKAPI_TENANT_HEADER = "x-okapi-tenant";

  public static final OkapiData OKAPI_DATA = new OkapiData(ImmutableMap.of(
    OKAPI_TOKEN_HEADER, "token",
    OKAPI_TENANT_HEADER, "tenant",
    OKAPI_URL_HEADER, "https://localhost:8080"));
}
