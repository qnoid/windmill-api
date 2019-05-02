// 
// AwsBucketService.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//

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
import io.windmill.windmill.services.exceptions.StorageServiceException;
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
    private final Client client = ClientBuilder.newClient();
    
    @PreDestroy
    void destroy() {
    	this.client.close();
    }
    
    /**
     */
	Status upload(ByteArrayOutputStream outputStream, Signed<URI> signed) throws StorageServiceException {

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