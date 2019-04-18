package io.windmill.windmill.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.validation.constraints.NotNull;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.plist.XMLPropertyListConfiguration;

public class Manifest {

	public static Manifest read(InputStreamReader inputStreamReader) throws ConfigurationException {
				
		XMLPropertyListConfiguration xmlPropertyListConfiguration = new XMLPropertyListConfiguration();			
		xmlPropertyListConfiguration.read(new BufferedReader(inputStreamReader));
		XMLPropertyListConfiguration items = xmlPropertyListConfiguration.get(XMLPropertyListConfiguration.class, "items");
        String bundle_identifier = items.getString("metadata.bundle-identifier");
        Double bundle_version = items.getDouble("metadata.bundle-version");
        String bundle_title = items.getString("metadata.title");
        
        return new Manifest(bundle_identifier, bundle_version, bundle_title);
	}
	
    @NotNull
    private String bundle;

	@NotNull
    private Double version;

    @NotNull
    private String title;

	public Manifest(String bundle, Double version, String title) {
		this.bundle = bundle;
		this.version = version;
		this.title = title;
	}

	public String getBundle() {
		return bundle;
	}

	public Double getVersion() {
		return version;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Copies the IPA hold by the ipaStream to a temp location. 
	 * 
	 * @param account_identifier the account associated with the given IPA
	 * @param ipaStream a stream to the IPA to copy to
	 * @return the file where the given ipaStream was copied to
	 * @throws IOException in case an I/O error occurs
	 */
	public File copy(String account_identifier, InputStream ipaStream) throws IOException {
		
		Path path = Paths.get("/tmp", account_identifier, bundle, String.valueOf(version));
		Files.createDirectories(path);
		File file = new File(path.toFile(), String.format("%s.ipa", title));
		
		Files.copy(ipaStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		return file;
	}	
}
