package com.vsn.auth;

import java.net.URL;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class Authorizator {
	private final String apiKey = "GX7Ul3i8riaOg5KZESDcu3Gw6";
	private final String apiSecret = "xHYjrX4tXzQJ4hCUewkGysMV1EcABR2MKCqO02IM1sanDNLxPD";
	private final String baseURL = "https://api.twitter.com";
	private Token token = null;
	private OAuthService service;
	public Authorizator(String token, String tokenSecret, Class<? extends Api> provider){
		if (token != null){
			this.token = new Token(token,tokenSecret);
		}
		this.service = new ServiceBuilder()
		.provider(provider)
        .apiKey(apiKey)
        .apiSecret(apiSecret)
        .build();
	}
	public Token getToken(){
		return this.token;
	}
	public String getAuthenticationUrl(){
		// Send username and password in post request to the authorization url to get the access token directly
		Token requestToken = null;
		try {
			requestToken = service.getRequestToken();
		} catch (Exception e){
			if (requestToken == null)
				throw new NullPointerException();
			return null;
		}
		return service.getAuthorizationUrl(requestToken);
		//return this.service.getAuthorizationUrl(this.service.getRequestToken());
	}
	public void createToken(String verificationCode){
		this.token = this.service.getAccessToken(this.service.getRequestToken(), new Verifier(verificationCode));
	}
	public Response sendRequest(Verb verb, URL apiUrl)
	{
		OAuthRequest request = new OAuthRequest(verb, baseURL + apiUrl);
		this.service.signRequest(token, request);
		return request.send();
	}
}