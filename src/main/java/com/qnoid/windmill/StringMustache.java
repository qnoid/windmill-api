/* 
 * This file is part of windmill.
 *
 *  windmill is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  windmill is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with windmill.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * (c) Keith Webster Johnston & Markos Charatzas 
 */
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
