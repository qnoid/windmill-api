package io.windmill.windmill.web.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public class FormDataMap {

	static public FormDataMap get(MultipartFormDataInput input) {
		return new FormDataMap(input.getFormDataMap());
	}
	
	private Map<String, List<InputPart>> map;

	public FormDataMap(Map<String, List<InputPart>> map) {
		super();
		this.map = map;
	}
	
	public InputStream read(String key) throws IllegalArgumentException {
		
		try {
			List<InputPart> value = this.map.get(key);
			
			return value.get(0).getBody(InputStream.class, null);
		}
		catch (NullPointerException | IOException e) {
			throw new IllegalArgumentException(String.format("Mandatory form parameter '%s' is missing.", key));
		}
	}		
}
