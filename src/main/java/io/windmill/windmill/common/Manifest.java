package io.windmill.windmill.common;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;

import io.windmill.windmill.services.MustacheWriter;

public class Manifest {

	public static Manifest manifest(InputStream in) throws IOException, ConfigurationException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) != -1) {
			out.write(buffer, 0, len);
		}
	
		return Manifest.manifest(out);
	}

	public static Manifest manifest(ByteArrayOutputStream buffer) throws ConfigurationException {
				
		InputStream is = new ByteArrayInputStream(buffer.toByteArray()); 
		InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
		XMLPropertyListConfiguration xmlPropertyListConfiguration = new XMLPropertyListConfiguration();			
		xmlPropertyListConfiguration.read(new BufferedReader(reader));
		XMLPropertyListConfiguration items = xmlPropertyListConfiguration.get(XMLPropertyListConfiguration.class, "items");

		String bundle_identifier = items.getString("metadata.bundle-identifier");
        String bundle_version = items.getString("metadata.bundle-version");
        String bundle_title = items.getString("metadata.title");

		return new Manifest(buffer, bundle_identifier, bundle_version, bundle_title);
	}
	
	private ByteArrayOutputStream buffer;

    @NotNull
    private String bundle;

	@NotNull
    private String version;

    @NotNull
	private String title;
    
	public Manifest(ByteArrayOutputStream buffer, String bundle, String version, String title) {
		this.buffer = buffer;
		this.bundle = bundle;
		this.version = version;
		this.title = title;
	}

	public String getBundle() {
		return this.bundle;
	}

	public String getVersion() {
		return this.version;
	}

	public String getTitle() {
		return this.title;
	}

	public ByteArrayOutputStream plistWithURLString(String urlString) throws IllegalArgumentException {
		
		try {
			InputStream is = new ByteArrayInputStream(this.buffer.toByteArray());
			InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
			MustacheWriter mustacheWriter = new MustacheWriter();
			
			return mustacheWriter.urlString(reader, urlString);
		} catch (IOException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}
