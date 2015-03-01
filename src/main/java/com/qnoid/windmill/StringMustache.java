// 
// StringMustache.java
// windmill
//  
// Created by Markos Charatzas on ${date}.
// Copyright (c) 2014 qnoid.com. All rights reserved.
//
package com.qnoid.windmill;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author qnoid
 *
 */
public interface StringMustache
{
  public ByteArrayOutputStream parse(String string, Map<String, Object> scopes) throws IOException;
}
