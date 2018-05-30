package io.windmill.windmill.web.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import io.windmill.windmill.common.Manifest;

public class FormDataMap {

	private static final Logger LOGGER = Logger.getLogger(FormDataMap.class.getName());

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

	public File cacheIPA(String account_identifier, Manifest manifest) throws IllegalArgumentException {
		InputStream stream = this.read("ipa");
		
		try {
			Path path = Paths.get("/tmp", account_identifier, manifest.getIdentifier(), String.valueOf(manifest.getVersion()));
			Files.createDirectories(path);
			File file = new File(path.toFile(), String.format("%s.ipa", manifest.getTitle()));
			
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			return file;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e.getCause());
		}
	}

	public Manifest readManifest() throws IllegalArgumentException, ConfigurationException {		
		InputStreamReader reader = new InputStreamReader(this.read("plist"), Charset.forName("UTF-8"));
		
		return Manifest.read(reader);
	}
	
	public ByteArrayOutputStream plistWithURLString(String urlString) throws IllegalArgumentException {		
		InputStream inputStream = this.read("plist");
		
		return InputStreamOperations.replaceURL(urlString).apply(inputStream);
	}
}
