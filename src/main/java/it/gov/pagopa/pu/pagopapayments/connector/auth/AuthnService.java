package it.gov.pagopa.pu.pagopapayments.connector.auth;

public interface AuthnService {

  String getAccessToken();

  String getAccessToken(String orgIpaCode);
}
