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

public class XjcEpisode {

	private String xsdFile;
	private String prefix;
	private String episodeFile;
	private List<String> predecessors = new ArrayList<String>();
	
	public XjcEpisode(String xsdFile, String prefix, List<String> predecessors){
		this.xsdFile = xsdFile;
		this.prefix = prefix;		
		this.episodeFile = xsdFile.replace(".xsd", ".xjb");		
		this.predecessors = predecessors;
	}

	public String getXsdFile() {
		return xsdFile;
	}

	public void setXsdFile(String xsdFile) {
		this.xsdFile = xsdFile;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getEpisodeFile() {
		return episodeFile;
	}

	public void setEpisodeFile(String episodeFile) {
		this.episodeFile = episodeFile;
	}
		
	public List<String> getPredecessors() {
		return predecessors;
	}

	public void setPredecessors(List<String> predecessors) {
		this.predecessors = predecessors;
	}

	@Override
	public String toString() {
		return "Episode [xsdFile=" + xsdFile + ", prefix=" + prefix + ", episodeFile=" + episodeFile + ", predecessors="
				+ predecessors + "]";
	}	
}
