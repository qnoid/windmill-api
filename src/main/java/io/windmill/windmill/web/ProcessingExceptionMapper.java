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

package io.windmill.windmill.web;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import io.windmill.windmill.services.exceptions.UnauthorizedAccountAccessException;

@Dependent
@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {

	private static final Logger LOGGER = Logger.getLogger(ProcessingExceptionMapper.class);
	
	@Override
	public Response toResponse(ProcessingException exception) {
		
		if (exception.getMessage().equals("RESTEASY008200: JSON Binding deserialization error") ) {
			LOGGER.debug(exception.getMessage(), Optional.ofNullable(exception.getCause()).orElse(exception));
			return Response.status(Status.BAD_REQUEST).entity("The request body was malformed JSON").build();
		} else if (exception instanceof UnauthorizedAccountAccessException) {
			LOGGER.warn(exception.getMessage(), exception);			
			return Response.status(Status.UNAUTHORIZED).build();
		} else if (exception instanceof SubscriptionAuthorizationTokenException) {
			LOGGER.warn(exception.getMessage());			
			return Response.status(Status.UNAUTHORIZED).build();
		} else { 
			LOGGER.error(exception.getMessage(), Optional.ofNullable(exception.getCause()).orElse(exception));
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
