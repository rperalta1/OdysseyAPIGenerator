/**************************************

    Copyright (C) 2018  
    Judicial Information Division,
    Administrative Office of the Courts,
    State of New Mexico

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

***************************************/
package gov.nmcourts.webservices.exception;

import generated.com.tylertech.xsdbindings.errorstream.ERRORSTREAM;

public class OdysseyWebServiceException extends Exception
{
	/**
	 * generated serial id
	 */
	private static final long serialVersionUID = 7648836379407272500L;
	
	private ERRORSTREAM errorResponseObject;

	public OdysseyWebServiceException(String msg, Throwable cause) 
	{
		super(msg, cause);
	}
	
	public OdysseyWebServiceException(ERRORSTREAM errorResponseObject, String msg, Throwable cause) 
	{
		super(msg, cause);
		this.errorResponseObject = errorResponseObject;
	}
	
	public ERRORSTREAM getErrorstream() {
		return errorResponseObject;
	}
	
}
