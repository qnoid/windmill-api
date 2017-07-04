package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import io.windmill.windmill.common.WindmillMetadata;

public class FormDataMap {

	static public FormDataMap get(MultipartFormDataInput input) {
		return new FormDataMap(input.getFormDataMap());
	}
	
	private Map<String, List<InputPart>> map;

	public FormDataMap(Map<String, List<InputPart>> map) {
		super();
		this.map = map;
	}
	
	public InputStream get(String key) {
		
		try {
			List<InputPart> value = this.map.get(key);
			
			return value.get(0).getBody(InputStream.class, null);
		}
		catch (NullPointerException | IOException e) {
			throw new IllegalArgumentException(String.format("Mandatory form parameter '%s' not present.", key));
		}
	}

	public WindmillMetadata readWindmillMetadata() throws ConfigurationException {
		
		InputStream stream = this.get("plist");
		
		return WindmillMetadata.read( new InputStreamReader(stream, Charset.forName("UTF-8")) );
	}
}
