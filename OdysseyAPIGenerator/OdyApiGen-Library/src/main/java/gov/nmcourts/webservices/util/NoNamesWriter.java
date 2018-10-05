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

//  Note: the following file was taken from:
//  https://stackoverflow.com/questions/17222902/remove-namespace-prefix-while-jaxb-marshalling

package gov.nmcourts.webservices.util;

import java.io.StringWriter;
import java.util.Iterator;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javax.xml.namespace.NamespaceContext;

import org.apache.cxf.staxutils.DelegatingXMLStreamWriter;

public class NoNamesWriter extends DelegatingXMLStreamWriter {

	private static final NamespaceContext emptyNamespaceContext = new NamespaceContext() {

		public String getNamespaceURI(String prefix) {
			return "";
		}
	
		public String getPrefix(String namespaceURI) {
			return "";
		}
	
		@SuppressWarnings("rawtypes")
		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}

	};

	public static XMLStreamWriter filter(StringWriter writer) throws XMLStreamException {
	    return new NoNamesWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(writer));
	}

	public NoNamesWriter(XMLStreamWriter writer) {
		super(writer);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return emptyNamespaceContext;
	}

}
