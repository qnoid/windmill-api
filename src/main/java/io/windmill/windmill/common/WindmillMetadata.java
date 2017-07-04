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

public class WindmillMetadata {

	public static WindmillMetadata read(InputStreamReader inputStreamReader) throws ConfigurationException {
				
		XMLPropertyListConfiguration xmlPropertyListConfiguration = new XMLPropertyListConfiguration();			
		xmlPropertyListConfiguration.read(new BufferedReader(inputStreamReader));
		XMLPropertyListConfiguration items = xmlPropertyListConfiguration.get(XMLPropertyListConfiguration.class, "items");
        String windmill_identifier = items.getString("metadata.bundle-identifier");
        Double windmill_version = items.getDouble("metadata.bundle-version");
        String windmill_title = items.getString("metadata.title");
        
        return new WindmillMetadata(windmill_identifier, windmill_version, windmill_title);
	}
	
    @NotNull
    private String identifier;

	@NotNull
    private Double version;

    @NotNull
    private String title;

	public WindmillMetadata(String identifier, Double version, String title) {
		this.identifier = identifier;
		this.version = version;
		this.title = title;
	}

	public String getIdentifier() {
		return identifier;
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
		
		Path path = Paths.get("/tmp", account_identifier, identifier, String.valueOf(version));
		Files.createDirectories(path);
		File file = new File(path.toFile(), String.format("%s.ipa", title));
		
		Files.copy(ipaStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		return file;
	}	
}
