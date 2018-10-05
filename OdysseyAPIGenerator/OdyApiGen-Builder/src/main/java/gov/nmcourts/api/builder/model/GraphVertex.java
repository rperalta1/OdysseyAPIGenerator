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

public class GraphVertex {
	
	String node;
	public ArrayList<String> neighbors;
	
	public GraphVertex(String vertex) {
		this.node = vertex;
		neighbors = new ArrayList<String>();
	}
	
	public String getVertex() {
		return node;
	}
	
	public void setVertex(String vertex) {
		this.node = vertex;
	}
	
	public ArrayList<String> getNeighbors() {
		return neighbors;
	}
	
	public void setNeighbors(ArrayList<String> neighbors) {
		this.neighbors = neighbors;
	}
	
	public void addAdj(String to) {
		neighbors.add(to);		
	}

	@Override
	public String toString() {
		return "VertexData [node=" + node + ", neighbors=" + neighbors + "]";
	}
}
