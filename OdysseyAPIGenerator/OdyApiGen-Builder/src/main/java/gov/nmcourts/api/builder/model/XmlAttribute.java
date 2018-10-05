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
package gov.nmcourts.api.builder.model;
import gov.nmcourts.api.builder.main.Constants.xmlAddReplace;

public class XmlAttribute {
	
	xmlAddReplace addReplace;
	String name;
	String value;
	String prefix;
	
	public XmlAttribute(xmlAddReplace addReplace, String name, String value) {
		this.addReplace = addReplace;
		this.name = name;
		this.value = value;
		this.prefix = null;
	}

	public XmlAttribute(xmlAddReplace addReplace, String name, String value, String prefix) {
		this.addReplace = addReplace;
		this.name = name;
		this.value = value;
		this.prefix = prefix;
	}

	public xmlAddReplace getAddReplace() {
		return addReplace;
	}
	
	public void setAddReplace(xmlAddReplace addReplace) {
		this.addReplace = addReplace;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return "XmlAttribute [addReplace=" + addReplace + ", name=" + name + ", value=" + value + ", prefix=" + prefix
				+ "]";
	}
}
