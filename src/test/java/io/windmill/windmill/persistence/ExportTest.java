package io.windmill.windmill.persistence;

import java.net.URI;
import java.util.UUID;

import org.junit.Test;

import junit.framework.Assert;

public class ExportTest {

	@Test
	public void testGivenManifestAssertURIPath() {
		final UUID account_identifier = UUID.fromString("7b4b8a85-a49e-4a48-a4a9-d3423384ce71");
		final UUID export_identifier = UUID.fromString("18658c6f-91aa-4fe6-a9ec-70f349057aa5");
		
		
		Export export = new Export("bundle", "1.0", "title");
		export.setIdentifier(export_identifier);
		
		export.account = new Account(account_identifier);
		
		URI manifest = export.getManifest().path();
		
		//{account_identifier}/{export_identifier}/export/manifest.plist
		Assert.assertEquals(account_identifier + "/" + export_identifier + "/export/manifest.plist", manifest.toString());
	}

}
