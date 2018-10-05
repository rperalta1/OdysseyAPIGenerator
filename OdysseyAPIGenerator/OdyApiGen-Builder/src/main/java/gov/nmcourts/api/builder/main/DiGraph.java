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
package gov.nmcourts.api.builder.main;

import java.util.ArrayList;
import java.util.List;

import gov.nmcourts.api.builder.model.GraphVertex;

public class DiGraph {
	
	ArrayList<GraphVertex> nodes = new ArrayList<GraphVertex>();
	
	boolean contains(String vertex){
		for(int i =0; i< nodes.size(); i++){
			if(nodes.get(i).getVertex().toLowerCase().equals(vertex.toLowerCase())){ 
				return true;
			}
		}
		return false;
	}

	public void createNode(String vertex) {
		nodes.add(new GraphVertex(vertex));
		
	}

	public int getIndexNode(String vertex) {
		for(int index = 0; index < nodes.size(); index++){
			if(nodes.get(index).getVertex().toLowerCase().equals(vertex.toLowerCase())) 
				return index;
		}
		return -1;
	}

	public void addNode(int index, String to) {
		if(to == null || to.length() == 0)  return;
		if(index >= 0){
			GraphVertex  vertexData = nodes.get(index);
			vertexData.addAdj(to);
		}
	}	
	
	public ArrayList<GraphVertex> getNodes() {
		return nodes;
	}

	public void setAdjNodes(ArrayList<GraphVertex> adjNodes) {
		this.nodes = adjNodes;
	}
	
	public void addDiGraph(String vertex) {
		if(vertex == null || vertex.length() == 0) 
			return;
        if (contains(vertex)) return;
        	else createNode(vertex);
    }
	
	protected void addDiGraph(String from, String to) {
		this.addDiGraph(from); 
		this.addDiGraph(to);
		addNode(getIndexNode(from),to);
	}

	protected void findRoots(List<Integer> rootIndices){
		int index = 0;
		for(GraphVertex node:nodes){
			if(node.getNeighbors().size() == 0){
				rootIndices.add(index);
			}
			index++;
		}
	}
	
	private boolean isAdjNeighbors(GraphVertex node, List<String> packages) {
		if(node.getNeighbors().size() == 0) 
			return false;
		if(isNodeInPackages(node.getVertex(), packages)) 
			return false; 
		if(checkDependencies(node.getNeighbors(), packages)) 
			return true;
		return false;
	}

	private boolean isNodeInPackages(String node, List<String> packages) {		
		for(String pack: packages){
			if(node.toLowerCase().equals(pack.toLowerCase()))
				return true;
		}
		return false;
	}
	
	private boolean checkDependencies(ArrayList<String> neighbors, List<String> packages) {
		for(String neighbor: neighbors){
			if(!isNodeInPackages(neighbor, packages))
				return false;
		}
		return true;
	}
	
	private void addPackage(GraphVertex vertex, List<String> packages) {
		packages.add(vertex.getVertex());
	}	
	
	public void processGraph(List<String> packages) {
		List<Integer> rootIndices = new ArrayList<Integer>();
		findRoots(rootIndices);
		
		for(int rootIndex:rootIndices){
			int lastNumPackages = 0;
			
			addPackage(nodes.get(rootIndex), packages);  
			
			while(packages.size() != lastNumPackages){
				lastNumPackages = packages.size();
				for(GraphVertex node : nodes){
					if(isAdjNeighbors(node, packages) && node.neighbors.size() > 0){
						addPackage(node, packages);
						break;
					}
				}
			}			
		}	
	}
	
}
