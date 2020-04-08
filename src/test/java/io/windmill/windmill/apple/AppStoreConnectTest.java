//
//  Created by Markos Charatzas (markos@qnoid.com)
//  Copyright © 2014-2020 qnoid.com. All rights reserved.
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  Permission is granted to anyone to use this software for any purpose,
//  including commercial applications, and to alter it and redistribute it
//  freely, subject to the following restrictions:
//
//  This software is provided 'as-is', without any express or implied
//  warranty.  In no event will the authors be held liable for any damages
//  arising from the use of this software.
//
//  1. The origin of this software must not be misrepresented; you must not
//     claim that you wrote the original software. If you use this software
//     in a product, an acknowledgment in the product documentation is required.
//  2. Altered source versions must be plainly marked as such, and must not be
//     misrepresented as being the original software.
//  3. This notice may not be removed or altered from any source distribution.

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
