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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gov.nmcourts.api.builder.main.Constants.xmlAddReplace;
import gov.nmcourts.api.builder.main.Constants.xmlType;
import gov.nmcourts.api.builder.model.GraphDependency;
import gov.nmcourts.api.builder.model.XmlDependency;
import gov.nmcourts.api.builder.model.XsdFileNode;
import gov.nmcourts.api.builder.model.XjcEpisode;
import gov.nmcourts.api.builder.model.XjcParameters;
import gov.nmcourts.api.builder.model.XmlAttribute;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Driver;
import com.sun.tools.xjc.XJCListener;

public class Builder extends DiGraph{

	private List<XjcEpisode> buildEpisodes = new ArrayList<XjcEpisode>();
	private List<XmlDependency> xmlDependencies = new ArrayList<XmlDependency>();
	private List<String> completedEpisodes = new ArrayList<String>();
	
	public static void main(String[] args) {

		// -------------------------------------------------------------------------------------------------------
		// For running this module alone in Eclipse configure dirInit parameter. Then use parent.project/pom.xml
		// file, and from the context menu run as "Maven clean". Finally go Eclipse's "Project" menu and clean 
		// up all projects   
		// --------------------------------------------------------------------------------------------------------
		
		String dirInit  = "C:/tmp/Ody2017/";		
		String dirInput = "./target/preprocessing/";	
		String packageName = "generated.com.tylertech.xsdbindings.";  
		String dirOutput = "../library/src/generated/tyler";
		
		File f = new File(dirInput);
		if(!f.isDirectory()){
			new File(dirInput).mkdirs();
			try {
				copyFile(new File ("src/main/resources/initial.xjb"), new File(dirInput + "/initial.xjb"));
				copyFile(new File ("src/main/resources/errorstream.xsd"), new File(dirInput + "/errorstream.xsd"));
			} catch (IOException e) {
				System.out.println("Unable to copy initial.xjb");
				System.exit(1);
			}
		}
		
		if(args.length == 4){		
			dirInit  = args[0] + "/";
			dirInput = args[1] + "/";
			packageName = args[2] + ".";
			dirOutput = args[3];
		}		
		
		if(args.length != 0 && args.length != 4){
			printUsage();
			System.exit(1);
		}
		
		Builder builder = new Builder();
		builder.preProcessing(dirInit, dirInput, "bt", ".xsd","xs:include", "schemaLocation");
		builder.processing(packageName, "http://common.namespace/", dirInput, dirOutput, ".xsd");
	}
	
	public void preProcessing(String initXsdFolder, String dirInput, String prefix, String fileExtension, 
			String tagName, String tagAttributeName){
		List<GraphDependency> dependencies = findIncludesInXsdFiles(listFiles(initXsdFolder, fileExtension), 
																		tagName, tagAttributeName);
		
		for(GraphDependency dependency:dependencies){
			super.addDiGraph(dependency.getNode(), dependency.getPredecesor());
		}
		
		List<String> orderPackages = new ArrayList<String>();
		super.processGraph(orderPackages);						
		int i = 1;
		for(String orderPackage: orderPackages){			
			XjcEpisode episode = new XjcEpisode(orderPackage, prefix + (i++), getDependencies(orderPackage, dependencies));
			buildEpisodes.add(episode);
		}
		
		for(File file:listFiles(initXsdFolder, ".xsd")){
			if(file.getName().equals("initial.xjb") || file.getName().equals("errorstream.xsd")) continue;					
			try {
				copyFile(file, new File(dirInput + file.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			handleCollisions(dirInput + file.getName());
		}
	}	
	
	private List<GraphDependency> findIncludesInXsdFiles(ArrayList<File> files, String tagName, String tagAttributeName){
		ArrayList<GraphDependency> findings = new ArrayList<GraphDependency>();
		
		for(File file:files){			
			List<String> includes = null;
			try {
				includes = findInclude(file, tagName, tagAttributeName);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				continue;
			} catch (SAXException e) {
				e.printStackTrace();
				continue;
			} catch (IOException e) {				
				e.printStackTrace();
				continue;
			}
			
			if(includes.size() == 0 || includes == null){
				findings.add(new GraphDependency(file.getName(), ""));
			}
			else{
				for(String include: includes){
					findings.add(new GraphDependency(file.getName(), include));
				}
			}
		}
		return findings;
	}

	private List<String> findInclude(File file, String tagName, String tagAttributeName) 
												throws ParserConfigurationException, SAXException, IOException{
		List<String> includeFiles = new ArrayList<String>();
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		
		docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.parse(file);
		
		NodeList nodes = doc.getElementsByTagName(tagName.toLowerCase());
		for(int i = 0 ; i < nodes.getLength(); i++){
			Node node = nodes.item(i);
			NamedNodeMap nm = node.getAttributes();
			for (int j = 0; j < nm.getLength(); j++){
				if(nm.item(j).getNodeName().toLowerCase().equals(tagAttributeName.toLowerCase())){
					includeFiles.add(nm.item(j).getNodeValue());
				}
			}
		}
		return includeFiles;
	}
	
	private ArrayList<File> listFiles(String folder, String fileExtension) {
	    File[] fList = new File(folder).listFiles();
	    ArrayList<File> files = new ArrayList<File>();
	    for (File file : fList) {
	        if (file.isFile() && file.getName().endsWith(fileExtension)) {
	            files.add(file);
	        }
	    }
	    return files;
	}	
	
	private void processing(String rootPackage, String commonSpace, String dirInput, String dirOutput, String fileExtension) {	
		
		System.out.println("Wait, generating files ...");
		for(XjcEpisode episode: buildEpisodes){
			List<XjcParameters> xjcParameters = new ArrayList<XjcParameters>();
			List<XmlAttribute> xmlAttributes = new ArrayList<XmlAttribute>();
				
			if(isEpisodes(episode.getPredecessors())){			
				for(String predecesor: episode.getPredecessors()){
					if(predecesor != null && predecesor.length() > 0){
						XjcEpisode predecesorData = findBuildEpisodes(predecesor);
						String xsdPredecesor = predecesorData.getXsdFile().replace(fileExtension,  "");
						xmlAttributes.add(new XmlAttribute(xmlAddReplace.SCHEMA, "xmlns:" + predecesorData.getPrefix(),	
								commonSpace + xsdPredecesor));
						xmlAttributes.add(new XmlAttribute(xmlAddReplace.INCLUDE, commonSpace + xsdPredecesor, 
								predecesorData.getXsdFile(), predecesorData.getPrefix()));
						xjcParameters.add(new XjcParameters(xsdPredecesor, predecesorData.getPrefix()));							
					}
				}
				xmlAttributes.add(new XmlAttribute(xmlAddReplace.SCHEMA, "xmlns", commonSpace +  
						episode.getXsdFile().replace(fileExtension,  "")));
				xmlAttributes.add(new XmlAttribute(xmlAddReplace.SCHEMA, "targetNamespace", commonSpace +  
						episode.getXsdFile().replace(fileExtension,  "")));
				
				Document document = updateSchema(new File (dirInput + episode.getXsdFile()), xmlAttributes );
				updateInclude(document, xmlAttributes, dirInput + episode.getEpisodeFile());
				loadDependencyTypes(dirInput, xmlAttributes);
				
				updateTypes(document);
				xmlDependencies.clear();
				
				try {
					SaveToFile(document, dirInput + episode.getXsdFile());
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}				
				if(isRunXjc(xjcParameters, episode, rootPackage, dirInput, dirOutput, false)){  // true for displaying desirable xjc commands
					completedEpisodes.add(episode.getXsdFile());
				}		
				else{
					System.out.println("Error in xsd file: " + episode.getXsdFile());
				}
			}
			else{
				System.out.println(episode.getXsdFile() + " was not generated due to the lack of dependencies");
			}
		}
		System.out.println("Customized API XSD files were generated");
	}
	
	private boolean isEpisodes(List<String> predecesors) {
		if(predecesors.get(0).equals("")){
			return true;
		}
		for(String predecesor: predecesors){
			boolean isEpisode = false;
			for(String completeEpisode: completedEpisodes){
				if(predecesor.toLowerCase().equals(completeEpisode.toLowerCase())){
					isEpisode = true;
				}
			}
			if(!isEpisode) return false;
		}
		return true;
	}

	private XjcEpisode findBuildEpisodes(String predecesor) {
		for(XjcEpisode episode: buildEpisodes){
			if(episode.getXsdFile().toLowerCase().equals(predecesor.toLowerCase())){
				return episode;
			}
		}
		return null;
	}
	
	private List<String> getDependencies(String orderPackage, List<GraphDependency> dependencies) {
		List<String> listDependecies = new ArrayList<String>();
		
		for(GraphDependency dependency: dependencies){
			if(orderPackage.toLowerCase().equals(dependency.getNode().toLowerCase())){
				listDependecies.add(dependency.getPredecesor());
			}
		}
		return listDependecies;
	}
	
	private void updateType(Document doc, String tagName, String attributte){
		NodeList nodes = doc.getElementsByTagName(tagName);
		for (int i = 0; i < nodes.getLength(); i++) {
	      Element element = (Element) nodes.item(i);
	      if(tagName.toLowerCase().equals("xs:restriction".toLowerCase())){
	    	  String newValue = findSimpleComplexDependency(element.getAttribute(attributte));
	    	  if(newValue != null){
		    	  List<String> listEnum = loadEnumerations("xs:restriction", element);
		    	  if(listEnum.size() > 0){
		    		  int index = findOneDependency(element.getAttribute(attributte));
		    		  if(index >= 0){
			    		  boolean find = isEnum(listEnum, xmlDependencies.get(index).getEnums());
			    		  if(find && xmlDependencies.get(index).getRestriction() != null &&  
			    				  xmlDependencies.get(index).getRestriction().length()> 0){
			    			  element.setAttribute(attributte, xmlDependencies.get(index).getRestriction());
			    		  }
			    		  else{
			    			  element.setAttribute(attributte, newValue);
			    		  }
		    		  }
		    	  }
		    	  else{
		    		  element.setAttribute(attributte, newValue);
		    	  }
	    	  }
	      }
	      else{
		      String newValue = findSimpleComplexDependency(element.getAttribute(attributte));
		      if(newValue != null){
		    	  element.setAttribute(attributte, newValue);
		      }
	      }
	    }
	}

	private boolean isEnum(List<String> source, List<String> destination) {
		for(String src: source){
			for(String dst:destination){
				if(src.equals(dst)){
					return true;
				}
			}
		}
		return false;
	}

	private int findOneDependency(String candidate) {
		if(candidate == null || candidate.length() == 0){
			return -1;
		}
		int count = 0;
		for(XmlDependency dependencyType: xmlDependencies ){
			if(candidate.trim().toLowerCase().equals(dependencyType.getName().toLowerCase().trim())){
				return count;
			}
			count++;
		}
		return -1;
	}

	private void updateTypes(Document doc) {
		updateType(doc, "xs:element"    , "type");
		updateType(doc, "xs:attribute"  , "type");
		updateType(doc, "xs:simpleType" , "name");
		updateType(doc, "xs:restriction", "base");
		updateType(doc, "xs:extension"  , "base");
	}
	
	private String findSimpleComplexDependency(String candidate) {
		if(candidate == null || candidate.length() == 0){
			return null;
		}
		for(XmlDependency dependencyType: xmlDependencies ){
			if(candidate.trim().toLowerCase().equals(dependencyType.getName().toLowerCase().trim())){
				return dependencyType.getPrefix() + ":" + dependencyType.getName().trim();
			}
		}
		return null;
	}

	private void SaveToFile(Document doc, String filename) throws Exception{
		DOMSource domSource = new DOMSource(doc);
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    Result dest = new StreamResult(new File(filename));
	    transformer.transform(domSource, dest);
	}

	private List<XmlAttribute> filterXmlAttributes( List<XmlAttribute> xmlAttributes, xmlAddReplace criteria ){
		List<XmlAttribute> buffer = new ArrayList<XmlAttribute>();
		for(XmlAttribute xmlAttribute:xmlAttributes){
			if(xmlAttribute.getAddReplace() == criteria){
				buffer.add(xmlAttribute);
			}
		}
		return buffer;
	}
	
	private List<String> loadEnumerations(String node, Node childNode){
		List<String> buffer = new ArrayList<String>();
    	if(node.equals("xs:restriction")){
    		NodeList candidateEnumNodes = childNode.getChildNodes();
  	      	for (int enumIndex = 0; enumIndex < candidateEnumNodes.getLength(); enumIndex++) {
  	      		Node candidate = (Node) candidateEnumNodes.item(enumIndex);
  	      		if(candidate != null && candidate.getNodeName().equals("xs:enumeration")){
  	      			Element en = (Element)candidate;
  	      			String value = en.getAttribute("value");
  	      			if(value != null && value.length() > 0){
  	      				buffer.add(value);
  	      			}
  	      		}
  	      	}
    	}
    	return buffer; 
	}
	
	private void loadDependencyType(Document doc, String tagName, String tagAttribute, String node, 
			String nodeAttribute, XmlAttribute includeFile, xmlType typeOfXml){
		NodeList nodes = doc.getElementsByTagName(tagName);
	    for (int parent = 0; parent < nodes.getLength(); parent++) {
	      boolean addedType = false;
	      Element complexType = (Element) nodes.item(parent);
	      String dependencyName = complexType.getAttribute(tagAttribute);			      
	      NodeList childNodes = complexType.getChildNodes();
	      
	      List<String> listEnum = new ArrayList<String>();
	      for (int child = 0; child < childNodes.getLength(); child++) {
	        Node childNode = (Node) childNodes.item(child);
	        if(childNode != null && childNode.getNodeName().equals(node)){
	        	Element e = (Element)childNode;
	        	String restriction = e.getAttribute(nodeAttribute);
	        	addedType = true;
	        	listEnum = loadEnumerations(node, childNode);		        	
	        	addXmlDependencies(typeOfXml, includeFile.getPrefix(), dependencyName, restriction, listEnum);
	        }
	      }
	      if(!addedType && dependencyName != null && dependencyName.length() > 0){
	    	  addXmlDependencies(typeOfXml, includeFile.getPrefix(), dependencyName, "", listEnum );
	      }
	    }
	}

	private void loadDependencyTypes(String pathXsd, List<XmlAttribute> xmlAttributes){
		List<XmlAttribute> filter = filterXmlAttributes(xmlAttributes, xmlAddReplace.INCLUDE);
		
		for(XmlAttribute includeFile: filter){
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			Document doc = null;
			
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
				doc = docBuilder.parse(pathXsd + includeFile.getValue());
				loadDependencyType(doc, "xs:simpleType",  "name", "xs:restriction", "base", includeFile, xmlType.SIMPLETYPE);
				loadDependencyType(doc, "xs:complexType", "name", "xs:restriction", "base", includeFile, xmlType.COMPLEXTYPE);
				
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	private void addXmlDependencies(xmlType type, String prefix, String name, String restriction, List<String> listEnum) {
		
		for(XmlDependency xmlDependency:xmlDependencies){
			if(xmlDependency.getType() == type && 
				xmlDependency.getPrefix().toLowerCase().equals(prefix.toLowerCase()) &&
					xmlDependency.getName().toLowerCase().equals(name.toLowerCase()) &&					
						xmlDependency.getRestriction().toLowerCase().equals(restriction.toLowerCase())){
							return; 
			}	
		} 
		xmlDependencies.add(new XmlDependency(type, prefix, name, restriction, listEnum));
	}

	private void updateInclude(Document doc, List<XmlAttribute> xmlAttributes, String filename) {		
		List<XmlAttribute> filter = filterXmlAttributes(xmlAttributes, xmlAddReplace.INCLUDE);
		
		NodeList elements = doc.getElementsByTagName("xs:include");
		if(elements.getLength() != filter.size()){
			System.out.println("Number of include elements do not match number of imports elements on file :" + filename);
			System.exit(1);
		}
		
		int index = 0;
		while(elements.getLength() > 0){
			Node parent = elements.item(0).getParentNode();
			Element importElement = doc.createElement("xs:import");
			importElement.setAttribute("namespace", filter.get(index).getName());
			importElement.setAttribute("schemaLocation", filter.get(index).getValue());
			index++;
			parent.replaceChild(importElement, elements.item(0));			
			elements = doc.getElementsByTagName("xs:include");
		}
	}
	
	private Document updateSchema(File file, List<XmlAttribute> xmlAttributes ){
		List<XmlAttribute> filter = filterXmlAttributes(xmlAttributes, xmlAddReplace.SCHEMA);
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(file);
			
			int addSchema = 0 ;
			Element element = (Element) doc.getElementsByTagName("xs:schema").item(0);
			int initNumSchema = doc.getElementsByTagName("xs:schema").item(0).getAttributes().getLength();
					
			for(XmlAttribute xmlAttribute: filter){
				addSchema++;
				element.setAttribute(xmlAttribute.getName(), xmlAttribute.getValue());
			}

			NodeList nodes = doc.getElementsByTagName("xs:schema");
			NamedNodeMap nm = nodes.item(0).getAttributes();
			
			if(addSchema + initNumSchema != nm.getLength()){
				System.out.println("Schema definition was not updated correctly in file :" + file.getName());
				System.exit(1);
			}			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	private boolean isRunXjc(List<XjcParameters> xjcParameters, XjcEpisode episode, String rootPackage, 
			String dirInput, String dirOutput, boolean verbose){
		List<String> binFiles = new ArrayList<String>();
		binFiles.add("-b");
		binFiles.add(dirInput + "initial.xjb");
		for(XjcParameters rep: xjcParameters){
			binFiles.add("-b");
			binFiles.add(dirInput + rep.getIncludeName() + ".xjb");		
		}
		
		if(verbose){
			System.out.print("xjc -nv -d " + dirOutput + " -p " + rootPackage + episode.getXsdFile().replace(".xsd",  "").toLowerCase() + 
				" -episode " +	dirInput +  episode.getEpisodeFile() + " " + dirInput +  episode.getXsdFile() + " -extension ");
			for(String binFile: binFiles){
				System.out.print(" ");
				System.out.print(binFile);
			}
			System.out.println();
		}
		
		String[] initParam = new String[]{
				"-nv",
		          "-d",
		          dirOutput,
	        	  "-p",
	        	  rootPackage + episode.getXsdFile().replace(".xsd",  "").toLowerCase(),
	        	  "-episode",
	        	  dirInput +  episode.getEpisodeFile(),
	        	  "-extension",
	        	  dirInput +  episode.getXsdFile()};
		
		String[] xjcParam = new String[initParam.length + binFiles.size()];
		for(int i = 0; i < initParam.length; i++ ) xjcParam[i] = initParam[i]; 
		for(int i = 0; i < binFiles.size(); i++ ) xjcParam[initParam.length + i ] = binFiles.get(i);
		
		try {
			if(generatePojoFromXsd(xjcParam) == 0){
				return true;
			}	
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return false;
	}
	
	private int generatePojoFromXsd(String [] parameters) throws BadCommandLineException {

		return Driver.run(parameters ,new XJCListener() {

			public void error(SAXParseException e) {
				printError(e, "FATAL");				
			}
			
			public void fatalError(SAXParseException e) {
                printError(e, "FATAL");
            }

            public void warning(SAXParseException e) {
                printError(e, "WARN");
            }

            public void info(SAXParseException e) {
                printError(e, "INFO");
            }

            private void printError(SAXParseException e, String level) {
                System.err.printf("%s: SAX Parse exception", level);
                e.printStackTrace();
            }
        });
	}
	
	private static void copyFile(File source, File dest) throws IOException {
	    Files.copy(source.toPath(), dest.toPath());
	}
	
	private static void printUsage() {		
		System.out.println("usage: java -jar builder.jar dirinit dirinput package diroutput");
		System.out.println("\t dirinit : directory were Tyler Technologies xsd file are located");
		System.out.println("\t dirinput : directory were pre-processing xsd will be generated for the builder");
		System.out.println("\t package : package name for the POJO classes");
		System.out.println("\t diroutput : directory POJO classes will be placed");
	}
	
	public void handleCollisions(String filename){
		
		//System.out.println("Processing XSD file :" + filename); //for debugging
		List<XsdFileNode> xsdFileNodes = new ArrayList<XsdFileNode>();
		
		Document doc = null;
		try {
			doc = loadXsdFile(new File(filename), xsdFileNodes);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		Element elements;
		do{
			xsdFileNodes.add(new XsdFileNode(-1, -1, "DOC ROOT NODE", "")); // Adding root node for searchings
			elements = doc.getDocumentElement();
			NodeList nodeList = elements.getChildNodes();		
			readNode(nodeList, xsdFileNodes, 1);
			XsdFileNode collision = findPaths(xsdFileNodes);
			
			if(collision != null) {
				System.out.println("Solving collision on:" + filename);
				System.out.println(collision); //for debugging
				updateDoc(doc, collision); 
				xsdFileNodes.clear();				
			}
			else{ 
				break;
			}
			
		}while(true);	
		try {
			SaveToFile(doc, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateDoc(Document doc, XsdFileNode collision) {
		NodeList nodes = doc.getElementsByTagName("xs:element");
		int counter = 1;
		String newName =  null;
		boolean isAvailable = true;
		do{
			newName = collision.getName() + counter;
			for (int i = 0; i < nodes.getLength(); i++) {
				Element element = (Element) nodes.item(i);
				if(element.getAttribute("name").equals(newName)){
					isAvailable = false;
				}
			}
			if(isAvailable)	break;
				else{
					counter++;
					isAvailable = true;
				}
		}while(true);
		System.out.println("Changing element name to: " + newName);
		Element elements = doc.getDocumentElement();
		NodeList nodeList = elements.getChildNodes();
		updateNode(nodeList, 1, collision, newName);		
	}
	
	private void updateNode(NodeList nodeList, int level, XsdFileNode collision, String newName ) {
		level++;
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element en = (Element)node;
  	      			String name = en.getAttribute("name");
  	      			if(name != null && name.length() > 0 &&	collision.getLevel() == level && 
  	      					 collision.getName() == name && collision.getPosition() == i){
  	      				en.setAttribute("name", newName);
  	      				return;
  	      			}	
  	      			updateNode(node.getChildNodes(), level, collision, newName);
				}
			}
		}
	}	

	private Document loadXsdFile(File file, List<XsdFileNode> xsdFileNodes) 
			throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		
		docBuilder = docBuilderFactory.newDocumentBuilder();
		doc = docBuilder.parse(file);
		return doc;
	}	
	
	private void readNode(NodeList nodeList, List<XsdFileNode> xsdFileNodes, int level) {
		level++;
		if (nodeList != null && nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element en = (Element)node;
  	      			String name = en.getAttribute("name");
  	      			String type = en.getAttribute("type");
  	      			if(name != null && name.length() > 0){
  	      				if(type != null && type.length() >0){
  	      					xsdFileNodes.add(new XsdFileNode(level, i, name, type));
  	      				}
  	      				else{
  	      					xsdFileNodes.add(new XsdFileNode(level, i, name, ""));
  	      				}
  	      			}	
  	      			readNode(node.getChildNodes(), xsdFileNodes, level);
				}
			}
		}
	}
	
	private XsdFileNode findPaths(List<XsdFileNode> xsdFileNodes){
		
		for(int cursor = 0; cursor < xsdFileNodes.size();  cursor++){
			
			//System.out.println("---------------------------------------------------------------"); //for debugging
			
			List<Integer> path = new ArrayList<Integer>();
			int index = cursor;
			while(index >= 0){
				/*
				System.out.print(" " + xsdFileNodes.get(index).getLevel() + " >> " +
								   xsdFileNodes.get(index).getName()  + " : " +
								   xsdFileNodes.get(index).getType() );  //for debugging
				*/				   
				path.add(index);
				if(index == 0) break;
				index = findParent(index, xsdFileNodes);
			}
			//System.out.println(path); //for debugging
			
			XsdFileNode collision = findCollisions(path, xsdFileNodes);
			if(collision != null){
				return collision; 
			}
		}		
		return null;
	}
	
	private XsdFileNode findCollisions(List<Integer> path, List<XsdFileNode> xsdFileNodes) {
		
		//for debugging
		//for(int i = path.size()-1; i>=0; i--){
		//	System.out.print("->" + xsdFileNodes.get(path.get(i)).getName());
		//}
		//System.out.println();
		
		for(int i = 0; i < path.size(); i++){
			for(int j = 0; j< path.size(); j++){
				if(i == j ) continue;
				if(xsdFileNodes.get(path.get(i)).getName().equals(xsdFileNodes.get(path.get(j)).getName()) &&
						xsdFileNodes.get(path.get(i)).getType().equals(xsdFileNodes.get(path.get(j)).getType())){
					return xsdFileNodes.get(path.get(i));
				}
			}
		}
		return null;
	}

	private int findParent(int index, List<XsdFileNode> xsdFileNodes) {
		
		if(index == 0) return 0;
		int target = xsdFileNodes.get(index).getLevel();
		int previous = xsdFileNodes.get(index-1).getLevel();
		
		if( previous < target ){       // Parent - child
			return index - 1;
		}else if(previous == target){  // Find first sibling
			return findParent(findFirstSibling(index, xsdFileNodes), xsdFileNodes);
		}else{                         // Rollback to first sibling
			int rollback = index - 1;
			do{
				rollback--;
				if(rollback < 0) return 0;
			}while(xsdFileNodes.get(rollback).getLevel() != target);			
			return findParent(findFirstSibling(index, xsdFileNodes), xsdFileNodes);
		}		
	}

	private int findFirstSibling(int index, List<XsdFileNode> xsdFileNodes) {
		for(int i = index - 1; i >= 0; i--){
			if(xsdFileNodes.get(i).getLevel() == xsdFileNodes.get(index).getLevel()){
				return i;
			}
		}
		return 0;
	}
	
}
