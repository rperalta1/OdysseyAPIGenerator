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

import java.util.ArrayList;
import java.util.List;

import gov.nmcourts.api.builder.main.Constants.xmlType;

public class XmlDependency {

	xmlType type; 
	String prefix;
	String name;
	String restriction;
	List<String> enums = new ArrayList<String>();
	
	public XmlDependency(xmlType type, String prefix, String name, String restriction, List<String> enums) {
		this.type = type;
		this.prefix = prefix;
		this.name = name;
		this.restriction = restriction;
		this.enums = enums;
	}

	public xmlType getType() {
		return type;
	}
	
	public void setType(xmlType type) {
		this.type = type;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getRestriction() {
		return restriction;
	}
	
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	public List<String> getEnums() {
		return enums;
	}

	public void setEnums(List<String> enums) {
		this.enums = enums;
	}

	@Override
	public String toString() {
		return "XmlDependency [type=" + type + ", prefix=" + prefix + ", name=" + name + ", restriction=" + restriction
				+ ", enums=" + enums + "]";
	}
	
}
