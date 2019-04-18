package io.windmill.windmill.apple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import io.windmill.windmill.apple.AppStoreConnect.Bundle;
import io.windmill.windmill.apple.AppStoreConnect.Product;

public class AppStoreConnectTest {

	@Test
	public void testGivenKnownProductAssertTrue() {
		Optional<Product> product = AppStoreConnect.Product.of(Product.INDIVIDUAL_MONTHLY.toString());
		
		assertTrue(product.isPresent());
		assertEquals(Product.INDIVIDUAL_MONTHLY, product.get());
	}

	@Test
	public void testGivenUnknownProductAssertFalse() {
		Optional<Product> product = AppStoreConnect.Product.of("unknown");
		
		assertFalse(product.isPresent());
	}

	@Test
	public void testGivenKnownBundleAssertTrue() {
		Optional<Bundle> bundle = AppStoreConnect.Bundle.of(Bundle.WINDMILL.toString());
		
		assertTrue(bundle.isPresent());
		assertEquals(Bundle.WINDMILL, bundle.get());
	}

	@Test
	public void testGivenUnknownBundleAssertFalse() {
		Optional<Bundle> bundle = AppStoreConnect.Bundle.of("unknown");
		
		assertFalse(bundle.isPresent());
	}
}
