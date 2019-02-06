package io.windmill.windmill.persistence;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

public class WindmillTest {

	@Test
	public void testGivenExportAssertValidURL() {
		String account_identifier = "14810686-4690-4900-ada5-8b0b7338aa39";
		String identifier = "io.windmill.windmill";
		Double version = 1.0;
		String title = "windmill";
		
		Export export = new Export(identifier, version, title);
		export.account = new Account(UUID.fromString(account_identifier));
		
		assertEquals(
				"itms-services://?action=download-manifest&url=https://ota.windmill.io/14810686-4690-4900-ada5-8b0b7338aa39/io.windmill.windmill/1.0/windmill.plist", 
				export.getURL());
	}

}
