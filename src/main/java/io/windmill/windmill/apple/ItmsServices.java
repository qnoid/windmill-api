package io.windmill.windmill.apple;

import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import io.windmill.windmill.web.Host;

public class ItmsServices {
	
	public static ItmsServices DOWNLOAD_MANIFEST = 
			new ItmsServices("itms-services://?action=download-manifest", Host.API_DOMAIN);
	
	private String action;
	private Host host;
	
	private ItmsServices(String action, Host host) {
		this.action = action;
		this.host = host;
	}
	
	public String build(Host.URITemplate template) {
		UriBuilder builder = template.apply(this.host.builder());
		return Optional.ofNullable(builder).map(b -> b.build()).map(uri -> String.format(this.action + "&url=%s", uri)).get();
	}
}