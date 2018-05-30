package io.windmill.windmill.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WindmillTest {

	@Test
	public void testGivenExportAssertValidURL() {
		String account_identifier = "14810686-4690-4900-ADA5-8B0B7338AA39";
		String identifier = "io.windmill.windmill";
		Double version = 1.0;
		String title = "windmill";
		
		Export export = new Export(identifier, version, title);
		export.account = new Account(account_identifier);
		
		assertEquals(
				"itms-services://?action=download-manifest&url=https://ota.windmill.io/14810686-4690-4900-ADA5-8B0B7338AA39/io.windmill.windmill/1.0/windmill.plist", 
				export.getURL());
	}

}
