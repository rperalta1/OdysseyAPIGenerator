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

import javax.swing.JDialog;
import javax.xml.bind.DatatypeConverter;

import com.google.common.io.Files;

import generated.com.tylertech.xsdbindings.addcasecrossreferencenumber.LOCALADDCASECROSSREFERENCENUMBER;
import generated.com.tylertech.xsdbindings.addcaseevent.LOCALADDCASEEVENT;
import generated.com.tylertech.xsdbindings.adddocument.LOCALADDDOCUMENT;
import generated.com.tylertech.xsdbindings.editcaseevent.LOCALEDITCASEEVENT;
import generated.com.tylertech.xsdbindings.findcasebycasenumber.LOCALFINDCASEBYCASENUMBER;
import generated.com.tylertech.xsdbindings.getdocument.GetDocumentMessageType;
import generated.com.tylertech.xsdbindings.getdocumentinfobyentity.DocumentByEntityEntityType;
import generated.com.tylertech.xsdbindings.getdocumentinfobyentity.DocumentByEntityMessageType;
import generated.com.tylertech.xsdbindings.getdocumentinfobyentityresult.GETDOCINFODOCUMENT;
import generated.com.tylertech.xsdbindings.getodysseyreleaselevel.GETODYSSEYRELEASELEVELMESSAGETYPENAME;
import generated.com.tylertech.xsdbindings.linkdocument.EntityTypeCode;
import generated.com.tylertech.xsdbindings.linkdocument.LOCALLINKDOCUMENT;
import generated.com.tylertech.xsdbindings.loadcase.LOCALLOADCASE;
import generated.com.tylertech.xsdbindings.loadcase.LoadEntitiesCollection;
import generated.com.tylertech.xsdbindings.loadcaseresult.LoadCaseCaseEvent;
import gov.nmcourts.webservices.exception.OdysseyWebServiceException;
import gov.nmcourts.webservices.odyssey.OdysseyWebServiceInvoker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.log4j.Logger;
//-----------------------------------------------------------------------------------------------------------------------------------
//  For Eclipse users
//  ---------------------------------------------------------------------------------------------------------------------------------
//  If API library generated classes are "missed" go to "Project Explorer" tab -> "Maven Dependencies" and make sure the API library
//  is treat it as JAR file and not as a folder.
//  If there is a problem, go to "Project Explorer" tab -> "Maven Dependencies" (right click)->"Properties"->"Maven Project Settings"
//  and disable "Resolve dependencies from Workspace projects"
//-----------------------------------------------------------------------------------------------------------------------------------

public class ExampleAPIs {
	
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(ExampleAPIs.class);
	
	public static void main(String[] args){		
		ExampleAPIs exampleAPIs = new ExampleAPIs();
		exampleAPIs.run();
	}	
	
	public void run() {
		
		ExampleParameters exampleParameters = new ExampleParameters();
		ExampleAPIs exampleAPIs = new ExampleAPIs();
		exampleAPIs.getParameters(exampleParameters);
		
		OdysseyWebServiceInvoker odysseyWebServiceInvoker = 
				new OdysseyWebServiceInvoker(exampleParameters.getOdysseyApiWdls(), exampleParameters.getOdysseySiteId());
		
		String referenceNumber = "TEST_REFNUM_1234";
		String source = "TEST_SOURCE";
		
		int userId = Integer.parseInt(exampleParameters.getOdysseyUserId());
		
		try {
			System.out.println("---------------------------------------------------------------");
			System.out.println("Testing FindCaseByCaseNumber API -- finding caseId and node Id:");
			System.out.println("---------------------------------------------------------------");
			GetResults results = exampleAPIs.findCaseByCaseNumber(odysseyWebServiceInvoker, exampleParameters.getOdysseyCaseNumber(), 1, 
					userId, referenceNumber, source);
			// GetResult holds caseID and nodeID needed for testing other APIs
			System.out.println(results);
			
			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Testing GetOdysseyReleaseLevel API -- verifiying the release and current patch of an Odyssey server:");
			System.out.println("-------------------------------------------------------------------------");
			exampleAPIs.getOdysseyReleaseLevel(odysseyWebServiceInvoker, userId, referenceNumber, source);
			
			System.out.println("---------------------------------------------------------------");
			System.out.println("Testing LoadCase API -- loading case events from a case number:");
			System.out.println("---------------------------------------------------------------");
			List<LoadCaseCaseEvent> loadEvents = exampleAPIs.loadCase(odysseyWebServiceInvoker, results.getCaseID(), results.getNodeID(), 
					userId, referenceNumber, source);
			for(LoadCaseCaseEvent loadEvent:loadEvents){
				System.out.println(loadEvent.getEventID() + ">>" + loadEvent.getType() + ">>" + loadEvent.getDate() + ">>" + loadEvent.getComment());
			}
			
			System.out.println("---------------------------------------------------------------------------");
			System.out.println("Testing GetDocumentInfoByEntity -- finding documents in loaded case events:");
			System.out.println("---------------------------------------------------------------------------");
			
			int lastCaseEventWithDocument = -1;
			GetDocumentResults getDocumentsResults = null;
			for(int index = 0; index < loadEvents.size(); index++){
				getDocumentsResults = exampleAPIs.getDocumentInfoByEntity(odysseyWebServiceInvoker, loadEvents.get(index).getEventID(), 
						userId, referenceNumber, source);
				if(getDocumentsResults != null) {
					lastCaseEventWithDocument = index;
				}
			}			
			if(lastCaseEventWithDocument < 0){
				System.out.println("Test was not completed since a case event with a document was not found within the case number");
				System.exit(1);
			}
			else{
				System.out.println("Last document:" + getDocumentsResults );
			}
			
			System.out.println("--------------------------------------------");
			System.out.println("Testing GetDocument -- downloading document:");
			System.out.println("--------------------------------------------");
			exampleAPIs.getDocument(odysseyWebServiceInvoker, getDocumentsResults.getDocumentVersionID() , "downtest" , "0", 
					userId, referenceNumber, source);  // File extension will be added by the API, and file will be stored on project's folder
			
			
			System.out.println("-------------------------------------------------------------------");
			System.out.println("Testing AddCaseEvent & LinkDocument APIs -- duplicating last event:");
			System.out.println("-------------------------------------------------------------------");
			int newCaseEventID = exampleAPIs.addCaseEvent(odysseyWebServiceInvoker, results.getCaseID(), 
					loadEvents.get(lastCaseEventWithDocument).getType(), 
					LocalDate.now().toString(),	"Comment for the new case event", results.getNodeID(), 
					userId, referenceNumber, source);
			
			exampleAPIs.linkDocument(odysseyWebServiceInvoker, 
					getDocumentsResults.getDocumentID(), 
					newCaseEventID, 
					results.getNodeID(),
					userId, referenceNumber, source);
			
			System.out.println("-----------------------------------------------------------------");
			System.out.println("Testing EditCaseEvent API -- case event comment has been changed:");
			System.out.println("-----------------------------------------------------------------");
			exampleAPIs.editCaseEvent(odysseyWebServiceInvoker, newCaseEventID, "Comment has been changed by EditCaseEvent API", results.getNodeID(),
					userId, referenceNumber, source);

			System.out.println("-------------------------------------------------------------------------");
			System.out.println("Testing AddCaseCrossReferenceNumber API -- adding one CCR to case number:");
			System.out.println("-------------------------------------------------------------------------");
			String newCCR = "CCR" + LocalDate.now().toString() + LocalTime.now().toString().replaceAll("[-:.]","");
			exampleAPIs.addCaseCrossReferenceNumber(odysseyWebServiceInvoker, results.getCaseID(), 
					newCCR, "SE", results.getNodeID(),
					userId, referenceNumber, source);
			
		} catch (OdysseyWebServiceException e) {
			e.printStackTrace();
		}
		
		System.out.println("-----------");
		System.out.println("End of test");
		System.out.println("-----------");
	}	
	
	private void getParameters(ExampleParameters exampleParameters){		
		
		ValidateParameters validateParameters = new ValidateParameters(exampleParameters);
		validateParameters.setModal(true);
		validateParameters.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		validateParameters.setVisible(true);
		
		if(validateParameters.process()) {
			System.out.println(exampleParameters);
		}
		else{
			System.out.println("No action was performed by the program, bye!");
			System.exit(1);
		}
	}
	
	private GetResults findCaseByCaseNumber(OdysseyWebServiceInvoker odyInvoker, String caseNumber, 
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.findcasebycasenumber.Message message = 
				new generated.com.tylertech.xsdbindings.findcasebycasenumber.Message();
		
		message.setCaseNumber(caseNumber);
		message.setMessageType(LOCALFINDCASEBYCASENUMBER.FIND_CASE_BY_CASE_NUMBER);
		message.setNodeID(nodeID);
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		
		generated.com.tylertech.xsdbindings.findcasebycasenumberresult.Result reply = 
				(generated.com.tylertech.xsdbindings.findcasebycasenumberresult.Result) 
				    odyInvoker.invoker(message, "FindCaseByCaseNumberResult", generated.com.tylertech.xsdbindings.findcasebycasenumberresult.Result.class);
		
		System.out.println("Case Id: " + reply.getCaseID() + " node Id: " + reply.getNodeID());
		
		GetResults getResults = new GetResults();
		getResults.setCaseID(reply.getCaseID());
		getResults.setNodeID(reply.getNodeID());
		return getResults;
	}
	
	private List<LoadCaseCaseEvent> loadCase(OdysseyWebServiceInvoker odyInvoker, int caseID,
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.loadcase.Message message = 
				new generated.com.tylertech.xsdbindings.loadcase.Message();	
		
		message.setNodeID(nodeID);
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setCaseID(caseID);
		message.setMessageType(LOCALLOADCASE.LOAD_CASE);
		
		LoadEntitiesCollection loadEntities = new LoadEntitiesCollection();
		loadEntities.setCaseEvents(String.valueOf(true));
		loadEntities.setCaseCrossReferences(String.valueOf(false));  
		loadEntities.setCaseFlags(String.valueOf(false));
		loadEntities.setCaseParties(String.valueOf(true));  
		loadEntities.setCaseStatuses(String.valueOf(false));
		loadEntities.setCausesOfAction(String.valueOf(false));
		loadEntities.setCharges(String.valueOf(false));
		message.setLoadEntities(loadEntities);		
		
		generated.com.tylertech.xsdbindings.loadcaseresult.Result reply = 
				(generated.com.tylertech.xsdbindings.loadcaseresult.Result)
				odyInvoker.invoker(message, "LoadCaseResult", generated.com.tylertech.xsdbindings.loadcaseresult.Result.class);
		
		return reply.getCase().getEvents().getEvent();
	}
	
	private GetDocumentResults getDocumentInfoByEntity(OdysseyWebServiceInvoker odyInvoker, int entityId,
			int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.getdocumentinfobyentity.Message message = 
				new generated.com.tylertech.xsdbindings.getdocumentinfobyentity.Message();
		
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setEntityType(DocumentByEntityEntityType.EVENT);
		message.setEntityID(entityId);
		message.setIncludeObsolete("false");
		message.setMessageType(DocumentByEntityMessageType.GET_DOCUMENT_INFO_BY_ENTITY);
		message.setNodeID(new BigInteger("0"));
		
		generated.com.tylertech.xsdbindings.getdocumentinfobyentityresult.Result reply = 
				(generated.com.tylertech.xsdbindings.getdocumentinfobyentityresult.Result)
				odyInvoker.invoker(message, "GetDocumentInfoByEntityResult", generated.com.tylertech.xsdbindings.getdocumentinfobyentityresult.Result.class);
		
		GETDOCINFODOCUMENT document = null;
		
		try{
			document = reply.getDocuments().getDocument().get(0);
		}catch(java.lang.IndexOutOfBoundsException e){
			return null;
		}
		GetDocumentResults getDocumentsResults = new GetDocumentResults();
		getDocumentsResults.setDocumentID(document.getDocumentID());
		getDocumentsResults.setDocumentVersionID(document.getCurrentDocumentVersionID());
		return getDocumentsResults;
	}
	
	private void getDocument(OdysseyWebServiceInvoker odyInvoker, Integer documentVersionID, String fileName,
			String nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.getdocument.Message message = 
				new generated.com.tylertech.xsdbindings.getdocument.Message();		
		
		message.setNodeID(new BigInteger(nodeID));
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setVersionID(documentVersionID);
		message.setIncludeEmbeddedDocument(String.valueOf(true));
		message.setMessageType(GetDocumentMessageType.GET_DOCUMENT);
		
		generated.com.tylertech.xsdbindings.getdocumentresult.Result reply = 
				(generated.com.tylertech.xsdbindings.getdocumentresult.Result)
				odyInvoker.invoker(message, "GetDocumentResult", generated.com.tylertech.xsdbindings.getdocumentresult.Result.class);		

		String dataDocument = reply.getEmbeddedDocument().get(0).getDocument();
		byte[] decodedValue = DatatypeConverter.parseBase64Binary(dataDocument);
		
		String extension = reply.getEmbeddedDocument().get(0).getExtension().toString();
		fileName = fileName + "." + extension;
		
		FileOutputStream file;
		try {
			file = new FileOutputStream(fileName);
		} catch (FileNotFoundException e) {
			throw new OdysseyWebServiceException("Unable to create file", e);
		}
        try {
			file.write(decodedValue);
			file.close();
		} catch (IOException e) {
			throw new OdysseyWebServiceException("Unable to write data into a file", e);
		}				
        System.out.println("File was downloaded: " + fileName + " downloaded for document version Id " + documentVersionID);
	}
	
	
	private int addCaseEvent(OdysseyWebServiceInvoker odyInvoker, int caseID, 
			String eventType, String dateEvent, String comment,
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.addcaseevent.Message message = 
				new generated.com.tylertech.xsdbindings.addcaseevent.Message();
		
		message.setCaseID(caseID);
		message.setCaseEventType(eventType);
		message.setDate(dateEvent);
		message.setComment(comment);
		message.setNodeID(nodeID);
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setMessageType(LOCALADDCASEEVENT.ADD_CASE_EVENT);
		
		generated.com.tylertech.xsdbindings.addcaseeventresult.Result reply = 
				(generated.com.tylertech.xsdbindings.addcaseeventresult.Result) 
				    odyInvoker.invoker(message, "AddCaseEventResult", generated.com.tylertech.xsdbindings.addcaseeventresult.Result.class);
		
		System.out.println("New case eventId: " + reply.getCaseEventID());
		return reply.getCaseEventID();
	}
	
	private void linkDocument(OdysseyWebServiceInvoker odyInvoker, int docId, int eventID, 
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException{
		
		generated.com.tylertech.xsdbindings.linkdocument.Message message = 
				new generated.com.tylertech.xsdbindings.linkdocument.Message();

		message.setDocumentID(docId);
		message.setMessageType(LOCALLINKDOCUMENT.LINK_DOCUMENT);
		message.setNodeID(nodeID);							
		message.setUserID(userID); 
		message.setReferenceNumber(referenceNumber);  
		message.setSource(source);		
		
		generated.com.tylertech.xsdbindings.linkdocument.Message.Entities entities = 
				new generated.com.tylertech.xsdbindings.linkdocument.Message.Entities();
		
		generated.com.tylertech.xsdbindings.linkdocument.Message.Entities.Entity entity =
				new generated.com.tylertech.xsdbindings.linkdocument.Message.Entities.Entity();
		entity.setEntityID(eventID);		
		entity.setEntityType(EntityTypeCode.EVENT);
		entities.getEntity().add(entity);
		
		message.setEntities(entities); 
		
		generated.com.tylertech.xsdbindings.linkdocumentresult.Result reply = 
				(generated.com.tylertech.xsdbindings.linkdocumentresult.Result) 
				    odyInvoker.invoker(message, "LinkDocumentResult", generated.com.tylertech.xsdbindings.linkdocumentresult.Result.class);
		
		System.out.println("Document linked to case event: " + reply.getSuccess());		
	}
	
	private void editCaseEvent(OdysseyWebServiceInvoker odyInvoker, int eventID, String comment,
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException {
		
		generated.com.tylertech.xsdbindings.editcaseevent.Message message = 
				new generated.com.tylertech.xsdbindings.editcaseevent.Message();
		
		message.setCaseEventID(eventID);
		message.setNodeID(nodeID);
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setMessageType(LOCALEDITCASEEVENT.EDIT_CASE_EVENT);
		
		generated.com.tylertech.xsdbindings.editcaseevent.EditCaseEventEdit editCaseEventEdit = 
				new generated.com.tylertech.xsdbindings.editcaseevent.EditCaseEventEdit();
		editCaseEventEdit.setComment(comment);
		
		message.setEdit(editCaseEventEdit);
		
		generated.com.tylertech.xsdbindings.editcaseeventresult.Result reply = 
				(generated.com.tylertech.xsdbindings.editcaseeventresult.Result) 
				    odyInvoker.invoker(message, "EditCaseEventResult", generated.com.tylertech.xsdbindings.editcaseeventresult.Result.class);
		
		System.out.println("Event comment was changed: " + reply.getSuccess());		
	}
	
	private void addCaseCrossReferenceNumber(OdysseyWebServiceInvoker odyInvoker, int caseId, 
			String crossRefNumber, String crossRefNumberType,
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException {
		
		generated.com.tylertech.xsdbindings.addcasecrossreferencenumber.Message message = 
				new generated.com.tylertech.xsdbindings.addcasecrossreferencenumber.Message();
		
		message.setCaseID(caseId);
		message.setCrossReferenceNumber(crossRefNumber);
		message.setCrossReferenceNumberType(crossRefNumberType);
		message.setNodeID(nodeID);
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setMessageType(LOCALADDCASECROSSREFERENCENUMBER.ADD_CASE_CROSS_REFERENCE_NUMBER);
					
		generated.com.tylertech.xsdbindings.addcasecrossreferencenumberresult.Result reply = 
				(generated.com.tylertech.xsdbindings.addcasecrossreferencenumberresult.Result) 
				    odyInvoker.invoker(message, "AddCaseCrossReferenceNumberResult", generated.com.tylertech.xsdbindings.addcasecrossreferencenumberresult.Result.class);
		
		System.out.println("CCR was added to case: " + reply.getCaseCrossReferenceNumberID());
	}
	
	/*
	 The following method, addDocument, describes how to use the AddDocument API -- upload a document into Odyssey. In contrast 
	 to previous APIs, Tyler's XSD files' version matters. If XSD files from 2013 or before are used then the extension of the file 
	 must be declared in a LOCALDOCEXTENSION enum. Otherwise, the declaration of the extension of the file can be done directly 
	 in a String. 
	 */
	
	public int addDocument(OdysseyWebServiceInvoker odyInvoker, String fileName,
			String docType,	String fileExtension, String caseEventDescription, int numberOfPages,
			int nodeID, int userID,	String referenceNumber, String source) throws OdysseyWebServiceException {
		
		LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        
		generated.com.tylertech.xsdbindings.adddocument.Message message =
				new generated.com.tylertech.xsdbindings.adddocument.Message();
		
		message.setEffectiveDate(now.format(formatter));
		message.setUserID(userID);
		message.setDocumentName(caseEventDescription);
		message.setNumberOfPages(numberOfPages);
		message.setMessageType(LOCALADDDOCUMENT.ADD_DOCUMENT);
		message.setNodeID(nodeID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setDocumentType(docType);
		
		generated.com.tylertech.xsdbindings.adddocument.LOCALEMBEDDEDDOCUMENT embeddedDoc = 
				new generated.com.tylertech.xsdbindings.adddocument.LOCALEMBEDDEDDOCUMENT();
		
		//--------------------------------------------------------------------------------------------------------------
		// For 2013 xsd files
		/*
		LOCALDOCEXTENSION ext = null;
		if(fileExtension.toLowerCase().equals("tip") || fileExtension.toLowerCase().equals("tif")){
			ext = LOCALDOCEXTENSION.TIF;
		}else if(fileExtension.toLowerCase().equals("pdf")){
			ext = LOCALDOCEXTENSION.PDF;
		}else{
			System.out.println("Unable to upload the file, add more extensions -- just TIF, TIFF, and PDF were enabled");
			System.exit(1);
		}
		embeddedDoc.setExtension(ext);
		*/
		// end 2013 xsd files --------------------------------------------------------------------------------------------
		
		//----------------------------------------------------------------------------------------------------------------
		// For 2014, 2017 xsd files
		/*
		embeddedDoc.setExtension("PDF");
		*/
		// end 2014, 2017 xsd files --------------------------------------------------------------------------------------
		
		message.setEmbeddedDocument(embeddedDoc);
		File file = new File(fileName);
		byte[] imgFileAsBytes;
		try {
			imgFileAsBytes = Files.toByteArray(file);
		} catch (IOException e) {
			throw new OdysseyWebServiceException("Could not load image file " + file.getName(), e);
		}
		String uuencodedDocument = DatatypeConverter.printBase64Binary(imgFileAsBytes);
		embeddedDoc.setDocument(uuencodedDocument);
		
		generated.com.tylertech.xsdbindings.adddocumentresult.Result reply = 
				(generated.com.tylertech.xsdbindings.adddocumentresult.Result) 
				    odyInvoker.invoker(message, "AddDocumentResult", generated.com.tylertech.xsdbindings.adddocumentresult.Result.class);
		
		System.out.println("Document uploaded, docId: " + reply.getDocumentID());
		return reply.getDocumentID();
	}
	
	/**
	 */
	private void getOdysseyReleaseLevel(OdysseyWebServiceInvoker odyInvoker, 
			int userID,	String referenceNumber, String source) throws OdysseyWebServiceException {
		
		generated.com.tylertech.xsdbindings.getodysseyreleaselevel.Message message = 
				new generated.com.tylertech.xsdbindings.getodysseyreleaselevel.Message();
		
		message.setUserID(userID);
		message.setReferenceNumber(referenceNumber);
		message.setSource(source);
		message.setNodeID(new BigInteger("0"));
		message.setMessageType(GETODYSSEYRELEASELEVELMESSAGETYPENAME.GET_ODYSSEY_RELEASE_LEVEL);
					
		generated.com.tylertech.xsdbindings.getodysseyreleaselevelresult.Result reply = 
				(generated.com.tylertech.xsdbindings.getodysseyreleaselevelresult.Result) 
				    odyInvoker.invoker(message, "GetOdysseyReleaseLevelResult", generated.com.tylertech.xsdbindings.getodysseyreleaselevelresult.Result.class);
		
		System.out.println("GetOdysseyReleaseLevel release: " + reply.getRelease());
		System.out.println("GetOdysseyReleaseLevel path: " + reply.getPatch());
	}
	
	private class GetResults {		
		private int nodeID;
		private int caseID;		
		public int getNodeID() { return nodeID;	}		
		public void setNodeID(int nodeID) {	this.nodeID = nodeID;}		
		public int getCaseID() { return caseID;	}		
		public void setCaseID(int caseID) {	this.caseID = caseID;}
		public String toString() {
			return "[Node:" + nodeID + ", caseId:" + caseID + "]";
		}
	}	
	
	private class GetDocumentResults {		
		private int documentID;
		private int documentVersionID;		
		public int getDocumentID() {return documentID;}
		public void setDocumentID(int documentID) {	this.documentID = documentID;}
		public int getDocumentVersionID() {	return documentVersionID;}
		public void setDocumentVersionID(int documentVersionID) {this.documentVersionID = documentVersionID;}
		public String toString() {
			return "[DocumentID:" + documentID + ", documentVersionID:" + documentVersionID + "]";
		}
	}			
}
