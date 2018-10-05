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
package gov.nmcourts.webservices.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import generated.com.tylertech.xsdbindings.errorstream.ERRORSTREAM;

public class XmlMarshallingUtil
{
	// all static methods; private constructor
	private XmlMarshallingUtil() {}

	public static String marshallRequest(Object request) throws JAXBException, XMLStreamException{
		String retVal = null;
		JAXBContext jaxbContext;
		jaxbContext = JAXBContext.newInstance(request.getClass());
		Marshaller jaxbMarshaller;
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter marshalledXml = new StringWriter();		
		jaxbMarshaller.marshal(request, NoNamesWriter.filter(marshalledXml));
		retVal = marshalledXml.toString();
		return retVal;
	}
	
	public static <T> Object unmarshallResponse(String responseXml, String classPackage, Class<T> classToMarshallTo) 
		throws JAXBException, XMLStreamException {		
		JAXBContext jaxbContext = JAXBContext.newInstance(classToMarshallTo, ERRORSTREAM.class);		
		XMLInputFactory xif = XMLInputFactory.newFactory();
		XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(responseXml));
        	xsr = new XsiTypeReader(xsr, classPackage);		
		Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
		return unMarshaller.unmarshal(xsr);
	}
	
	private static class XsiTypeReader extends StreamReaderDelegate {
		
		private String className;
		
        public XsiTypeReader(XMLStreamReader reader, String className) {
            super(reader);
            this.className = className;
        }

        @Override
        public String getNamespaceURI() {
            String retVal = "http://common.namespace/" + this.className;
            return retVal.intern();
        }
    }
	
	public static ERRORSTREAM unmarshallErrorResponse(String responseXml) 
			throws JAXBException
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(ERRORSTREAM.class);
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			StringReader strReader = new StringReader(responseXml);
			return (ERRORSTREAM) unMarshaller.unmarshal(strReader);
		}
}
