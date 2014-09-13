package com.developi.sbt.extensions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.ibm.sbt.core.configuration.Configuration;
import com.ibm.sbt.security.authentication.oauth.consumer.OAuth2Handler;
import com.ibm.sbt.services.endpoints.OAuth2Endpoint;

public class BoxEndpoint extends OAuth2Endpoint {

	/**
	 * Box endpoint requires a URL parameter passed for requests. We will modify standart behaviour of OAuth2Handler.
	 * 
	 * This is a very common problem for Social App Developers. When we need to extend certain OAUTH endpoint type, here is the way.
	 * 
	 * The actual magic is in the handler. But we need to extend the endpoint to change the handler. We also added a new parameter below.
	 *  
	 */
	
	public BoxEndpoint() {
		super(new OAuth2Handler() {
			@Override
			public String getAuthorizationNetworkUrl() {
				String originalUrl=super.getAuthorizationNetworkUrl();
				
				StringBuilder url = new StringBuilder();
				try {
					url.append(originalUrl);
					url.append('&');
					url.append(Configuration.OAUTH2_REDIRECT_URI);
					url.append('=');
					url.append(URLEncoder.encode(getClient_uri(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
				}
				return url.toString();	
			}
		});
	}

}
