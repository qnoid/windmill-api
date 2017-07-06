package io.windmill.windmill.web.common;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriBuilder;

import io.windmill.windmill.services.StorageService;

public interface UriBuilders {

	public static final String DOMAIN = StorageService.BUCKET_CANONICAL_NAME;
	
	public static UriBuilder create(String path) throws URISyntaxException {
		return UriBuilder.fromUri(new URI("https", DOMAIN, null, null)).path(path); 
	}
	
	public static String createITMS(String path) throws URISyntaxException {
		//"itms-services://?action=download-manifest&url=https://ota.windmill.io/{user_identifier}/{windmill_identifier}/{windmill_version}/{windmill_title}.plist"
		final String ITMS_SERVICES_URL_STRING = "itms-services://?action=download-manifest&url=%s";
		
		return String.format(ITMS_SERVICES_URL_STRING, UriBuilders.create(path).build().toString()); 
	}
}
