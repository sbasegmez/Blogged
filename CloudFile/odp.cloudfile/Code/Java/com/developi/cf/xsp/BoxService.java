package com.developi.cf.xsp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.developi.sbt.extensions.BoxItem;
import com.developi.sbt.extensions.BoxPerson;
import com.developi.toolbox.DevelopiUtils;
import com.developi.toolbox.RestUtils;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.sbt.security.authentication.AuthenticationException;
import com.ibm.sbt.security.authentication.oauth.consumer.OAuth2Handler;
import com.ibm.sbt.services.client.ClientServicesException;
import com.ibm.sbt.services.endpoints.Endpoint;
import com.ibm.sbt.services.endpoints.EndpointFactory;
import com.ibm.sbt.services.endpoints.OAuth2Endpoint;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class BoxService implements Serializable {

	/**
	 * Quick and Dirty implementation for Basecamp services.
	 */
	
	private static final long serialVersionUID = 1L;

	private final static String DEFAULT_ENDPOINT_NAME="box";

	private Map<String, BoxPerson> userMap; // authenticated users cached in a map.

	private String consumerKey;
	private String consumerSecret;
	
	public BoxService() {
		userMap=new HashMap<String, BoxPerson>();
		loadAppstore();
	}

	public Endpoint getEndpoint() {
		return EndpointFactory.getEndpoint(DEFAULT_ENDPOINT_NAME);
	}
	
	private void loadAppstore() {
		Database db=ExtLibUtil.getCurrentDatabase();
		View view=null;
		Document appstore=null;
		
		try {
			view=db.getView("apps");
			appstore=view.getDocumentByKey(DEFAULT_ENDPOINT_NAME, true);
			
			if(appstore==null) {
				System.out.println("No Application found for "+DEFAULT_ENDPOINT_NAME);
			} else {
				consumerKey=appstore.getItemValueString("ConsumerKey");
				consumerSecret=appstore.getItemValueString("ConsumerSecret");
			}
		} catch(NotesException ne) {
			ne.printStackTrace();
		} finally {
			DevelopiUtils.recycleObjects(appstore, view);
		}
	}
	
	public String getConsumerKey() {
		return consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public Map<String, BoxPerson> getUserMap() {
		return userMap;
	}

	public void setUserMap(Map<String, BoxPerson> userMap) {
		this.userMap = userMap;
	}

	public synchronized BoxPerson getMe() {
		String userName=DevelopiUtils.getEffectiveUserName();

		BoxPerson me=userMap.get(userName);
		
		if(me==null) {
			me=new BoxPerson();
			userMap.put(userName, me);
		}
		
		try {
			if(me.isEmpty() && getEndpoint().isAuthenticated()) {
				me.setData(RestUtils.xhrGetJson(getEndpoint(), "/users/me"));
			}
		} catch (ClientServicesException e) {
			// So we are not able to determine authentication. Forget it.
		}

		return me; 
	}
	
	public void authenticate(boolean force) {
		try {
			getEndpoint().authenticate(force);
		} catch (ClientServicesException e) {
			System.out.println("Unable to Authenticate: "+e.getMessage());
		}
	}
	
	public boolean isAuthenticated() {
		try {
			return getEndpoint().isAuthenticated();
		} catch (ClientServicesException e) {
			System.out.println("Unable to determine authentication state: "+e.getMessage());
			return false;
		}
	}
	
	/**
	 * Tricky: This is a problem with the current SBT. When you logout, it deletes tokens from the endpoint.
	 * But since the handler class continues to keep the old token, it doesn't lose the authentication.
	 */
	public void logout() {
		String userName=DevelopiUtils.getEffectiveUserName();
		userMap.remove(userName);
		try {
			OAuth2Endpoint endpoint=(OAuth2Endpoint)getEndpoint();
			OAuth2Handler oaHandler=endpoint.getHandler();
			oaHandler.deleteToken();
			oaHandler.setAccessToken(null);
			oaHandler.setAccessTokenObject(null);
		} catch (AuthenticationException e) { 
			System.out.println("Unable to logout: "+e.getMessage());
		}
	}

	public List<BoxItem> getItems(String folderId) {
		String serviceUrl="/folders/"+(StringUtil.isEmpty(folderId)?"0":folderId)+"/items";
		List<BoxItem> boxItems=new ArrayList<BoxItem>();

		Map<String, String> params=new HashMap<String,String>();
		
		params.put("fields", "name,shared_link");
		
		JsonJavaObject root=RestUtils.xhrGetJson(getEndpoint(), serviceUrl, params);

		if(root!=null && root.containsKey("entries")) {
			JsonJavaArray entries=root.getAsArray("entries");
			for(Object item: entries) {
				if(item instanceof JsonJavaObject) {
					boxItems.add(new BoxItem((JsonJavaObject) item));		
				}
			}
		}

		ExtLibUtil.getViewScope().put("debugJson", root.toString());
		return boxItems;

	}

}
