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
package gov.nmcourts.webservices.odyssey;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import generated.com.tylertech.api.APIWebService;
import gov.nmcourts.webservices.exception.OdysseyWebServiceException;
import gov.nmcourts.webservices.util.XmlMarshallingUtil;

import org.apache.log4j.Logger;

public class OdysseyWebServiceInvoker {

	public static final String ERROR_COULD_NOT_MARSHAL_XML       = "Unable to marshall request object to XML";
	public static final String ERROR_COULD_NOT_UNMARSHAL_XML     = "Unable to unmarshall reply XML to object";
	public static final String ERROR_COULD_NOT_INSTANTIATE_CLASS = "Unable to instantiate class for the reply XML object";
	public static final String ERROR_COULD_NOT_GET_API_WEB_STUB  = "Could not get api web service stub";
	
	private static APIWebService apiWebServiceStub;
	private Logger logger = Logger.getLogger(OdysseyWebServiceInvoker.class);
	
	private String endpointAPIWebService;
	private String siteId;
	
	public OdysseyWebServiceInvoker(String endpointAPIWebService, String siteId) {
		super();
		this.endpointAPIWebService = endpointAPIWebService;
		this.siteId = siteId;
		logger.info("API URI is: " + endpointAPIWebService);
		logger.info("Tyler siteId is: " + siteId);
	}

	public <T> Object invoker(Object message, String expectedReplyName, Class<T> expectedReplyClass) 
			throws OdysseyWebServiceException {
		
		String requestXML = null;
		try {
			requestXML = XmlMarshallingUtil.marshallRequest(message);			
			logger.info("XML for request is: \n" + requestXML);
		} catch (JAXBException e) {
			throw new OdysseyWebServiceException(ERROR_COULD_NOT_MARSHAL_XML, e);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}		
				
		String rawResultXml = getApiWebServiceStub().getAPIWebServiceSoap().odysseyMsgExecution(requestXML, siteId);
		logger.info("Odyssey response was: " + rawResultXml);
		
		Object reply = null;
				
		T expected = null;
		try {
			expected = expectedReplyClass.newInstance();
		} catch (InstantiationException e) {
			throw new OdysseyWebServiceException(ERROR_COULD_NOT_INSTANTIATE_CLASS, e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		try {
			reply = XmlMarshallingUtil.unmarshallResponse(rawResultXml, expectedReplyName, expected.getClass());
		} catch (JAXBException e) {
			throw new OdysseyWebServiceException(ERROR_COULD_NOT_UNMARSHAL_XML, e);
		} catch (XMLStreamException e) {			
			e.printStackTrace();
		}
		return reply;
	}
	
	private synchronized APIWebService getApiWebServiceStub() throws OdysseyWebServiceException {
		if(null == apiWebServiceStub) {
			initApiWebService();
		}
		return apiWebServiceStub;
	}

	private void initApiWebService() throws OdysseyWebServiceException
	{
		if(apiWebServiceStub == null) {
			try {
				URL wsdlLocation = new URL(endpointAPIWebService);
				apiWebServiceStub = new APIWebService(wsdlLocation);	
			} catch (MalformedURLException e) {
				throw new OdysseyWebServiceException(ERROR_COULD_NOT_GET_API_WEB_STUB, e);
			}
		}
	}
	
}
