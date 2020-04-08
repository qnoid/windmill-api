// 
// AwsBucketService.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

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

package io.windmill.windmill.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.jboss.logging.Logger;
import org.jboss.resteasy.util.Hex;

import io.windmill.windmill.common.Condition;
import io.windmill.windmill.common.Manifest;
import io.windmill.windmill.services.exceptions.StorageServiceException;
import io.windmill.windmill.web.common.ManifestBodyReader;
import io.windmill.windmill.web.security.CryptographicHashFunction;
import io.windmill.windmill.web.security.SHA256;
import io.windmill.windmill.web.security.Signed;

/**
 * @author qnoid
 */
@ApplicationScoped
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class);
    
    private final CryptographicHashFunction hash = SHA256.create();
    private final Client client = ClientBuilder.newClient().register(ManifestBodyReader.class);
    
    @PreDestroy
    void destroy() {
    	this.client.close();
    }
    
    /**
     */
	public Status upload(ByteArrayOutputStream outputStream, Signed<URI> signed) throws StorageServiceException {

		byte[] bytes = outputStream.toByteArray();
				
		Builder request = this.client.target(signed.value()).request()
					.header("content-type", "text/xml plist")
					.header("x-amz-content-sha256", Hex.encodeHex(hash.hash(bytes)));

		Response response = request.put(Entity.entity(new ByteArrayInputStream(bytes), MediaType.APPLICATION_OCTET_STREAM_TYPE));
		Status status = Status.fromStatusCode(response.getStatus());
		response.close();
        LOGGER.debug(String.format("Upload at '%s' %s.", signed.value(), status));

        Condition.guard(Family.SUCCESSFUL == status.getFamily(), () -> new StorageServiceException(status.getReasonPhrase()));
        
		return status;
    }
	
    /**
     */
	public Manifest get(Signed<URI> signed) throws StorageServiceException {
				
		Builder request = this.client.target(signed.value()).request()
					.header("content-type", "text/xml plist");

		Response response = request.get();
		Status status = Status.fromStatusCode(response.getStatus());
	
        LOGGER.debug(String.format("Get at '%s' %s.", signed.value(), status));

        Condition.guard(Family.SUCCESSFUL == status.getFamily(), () -> new StorageServiceException(status.getReasonPhrase()));
        
		return response.readEntity(Manifest.class);        
    }

	public Status delete(Signed<URI> signed) throws StorageServiceException {
		
		Builder request = this.client.target(signed.value()).request();

		Response response = request.delete();
		Status status = Status.fromStatusCode(response.getStatus());
		response.close();
        LOGGER.debug(String.format("Delete at '%s' %s.", signed.value(), status));

        Condition.guard(Family.SUCCESSFUL == status.getFamily(), () -> new StorageServiceException(status.getReasonPhrase()));
        
		return status;
	}
}