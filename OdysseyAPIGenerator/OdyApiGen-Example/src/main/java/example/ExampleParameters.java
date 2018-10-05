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
package example;

public class ExampleParameters {

    
	private static String odysseyApiWdls  	= "http://dev-config-all.nmcourts.gov/WebServices/APIWebService.asmx?WSDL";
	private static String odysseySiteId   	= "DEVCONFIG";
	private static String odysseyUserId	= "4847";
	private static String odysseyCaseNumber= "D307NH9700003";
			
	private final static String ODYSSEY_AGENCY    = "MIB";
	private final static String ODYSSEY_ENV       = "DEVELOPMENT";
	
	public String getOdysseyApiWdls() {
		return odysseyApiWdls;
	}

	public void setOdysseyApiWdls(String odysseyApiWdls) {
		this.odysseyApiWdls = odysseyApiWdls;
	}

	public String getOdysseySiteId() {
		return odysseySiteId;
	}

	public void setOdysseySiteId(String odysseySiteId) {
		this.odysseySiteId = odysseySiteId;
	}

	public String getOdysseyUserId() {
		return odysseyUserId;
	}

	public void setOdysseyUserId(String odysseyUserId) {
		this.odysseyUserId = odysseyUserId;
	}

	public String getOdysseyCaseNumber() {
		return odysseyCaseNumber;
	}

	public void setOdysseyCaseNumber(String odysseyCaseNumber) {
		this.odysseyCaseNumber = odysseyCaseNumber;
	}

	public static String getOdysseyAgency() {
		return ODYSSEY_AGENCY;
	}

	public static String getOdysseyEnv() {
		return ODYSSEY_ENV;
	}

	@Override
	public String toString() {
		return "odysseyApiWdls=" + odysseyApiWdls + 
				"\nodysseySiteId=" + odysseySiteId + 
				"\nodysseyUserId=" + odysseyUserId + 
				"\nodysseyCaseNumber=" + odysseyCaseNumber;
	}
}
