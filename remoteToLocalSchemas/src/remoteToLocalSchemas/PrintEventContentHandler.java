package remoteToLocalSchemas;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class PrintEventContentHandler implements ContentHandler {
	
	private String rootDir = null;
	private static String xmlStringBody = "";
	public List<String> schemaUrlList = new ArrayList<String>();
	public List<String> schemaFileList = new ArrayList<String>();
	public List<String> schemaFilePathList = new ArrayList<String>();
	public static List<String> schemaUrlHostsList = new ArrayList<String>();
	
	public PrintEventContentHandler(String rootDir){
		this.rootDir = rootDir;
	}
	

	public static String getXmlStringBody() {
		return xmlStringBody;
	}

	public List<String> getSchemaUrlList() {
		return schemaUrlList;
	}


	public List<String> getSchemaFileList() {
		return schemaFileList;
	}


	public List<String> getSchemaFilePathList() {
		return schemaFilePathList;
	}


	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws SAXException {
		
		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		
		//Erkennen wenn Tags mit import, SIGMET, METAR, TAF oder SPECI beginnen
		if ("import".equals(localName) | "SIGMET".equals(localName) | "METAR".equals(localName) | "TAF".equals(localName) | "SPECI".equals(localName)){
			for (int i=0; i<atts.getLength(); i++){
				if (atts.getQName(i).matches(".*schemaLocation")){
	
					String[] schemaArr   = atts.getValue(i).split("\\s+");
					
					for (int j=0; j<schemaArr.length; j++){
						
						//schemaArr liefert komplette URL der Datei
						if(schemaArr[j].matches("https?.*.xsd")){
							System.out.println(schemaArr[j]);
							String[] urlSplit = schemaArr[j].split("/");
							String xsdFile = urlSplit[urlSplit.length-1];
							if (!schemaUrlList.contains(schemaArr[j])){
								schemaUrlList.add(schemaArr[j]);
							}
							
							if (!schemaFileList.contains(xsdFile)){
								schemaFileList.add(xsdFile);
							}
							
							try {
								saveUrl(xsdFile, schemaArr[j]);
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
				}
			}
		}
		
		//Bei include muss vorher der URL Pfad ermittelt werden, da die Dateien über einen relativen Pfad eingebunden sind
		//Es wird dann über alle möglichen (zur Verfügung stehenden URLs gelaufen und geprüft, ob dort die Datei liegt
		if ("include".equals(localName)){
			String xsdFile;
			String urlPath = null;
			
			for (int i=0; i<atts.getLength(); i++){
			
				if (atts.getQName(i).matches(".*schemaLocation")){
					xsdFile = atts.getValue(i);	//ist nur die Datei	
				
					if(xsdFile.matches(".*.xsd")){
		
						for (int j=0; j<schemaUrlHostsList.size(); j++){
							try {
								urlPath = schemaUrlHostsList.get(j);
								saveUrl(xsdFile.replace("./", ""), urlPath + "/" + xsdFile.replace("./", ""));
							} catch (MalformedURLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								System.out.println("URL Path not existing for file " + xsdFile + " and URL Path " + urlPath);
							}
						}
					}
				}
			}
		}
	}
		


	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// TODO Auto-generated method stub
		
	}
		 	

	public void saveUrl(final String filename, final String urlString)
	         throws MalformedURLException, IOException {
	     BufferedInputStream in = null;
	     FileOutputStream fout = null;
	     
	     String xsdDir = this.rootDir;
	     String filePath = "";
	     
	     try {
	    	 URL inUrl = new URL(urlString);
	         in = new BufferedInputStream(inUrl.openStream());
	         
	         String[] urlHostArr = inUrl.getHost().split("\\.");
	         String[] urlPathArr = inUrl.getPath().split("/");	
	         
	         //Der Hostname wird in einen Pfad übergeführt
	         for (int j=0; j<urlHostArr.length; j++){
	        	 xsdDir =  xsdDir + "/" + urlHostArr[j] ; 
	        	
	         }
	         
	         //Auseinander genommener Pfad aus der URL wird ohne Dateiendung wieder zusammengesetzt 
	         for (int j=0; j<urlPathArr.length-1; j++){
	        	 //System.out.println(urlPathArr[j]);
	        	 //Nur Eintraege ohne Leerzeichen oder Slash
	        	 if (urlPathArr[j].matches(".*\\S.*[^/]*")){
	        		 filePath =  filePath + "/" + urlPathArr[j]; 
	        	 }
	         }
	         
	         //Den URL-Pfad mit zu einer Liste hinzufügen
	         String urlPath = "http://" + inUrl.getHost() + filePath;
	         
	         //Kompletter Pfad der Schema-Datei
	         xsdDir = xsdDir + filePath;
	         
	         //Den URL Pfad zum Verzeichnis einmalig in einer Liste speichern
	         if (!schemaUrlHostsList.contains(urlPath)){
	        	 schemaUrlHostsList.add(urlPath);
	        	 
	        	 //Aufruf des xml-Writer um die Katalog-Datei für das Mapping zu schreiben
	        	 xmlStringBody = xmlStringBody + "<system systemId=\"" + urlString + "\" uri=\"" + xsdDir + "/" + filename +"\"/>\n";
	         }
	         
	         //Den kompletten Pfad in einer Liste speichern
	         if (!schemaFilePathList.contains(xsdDir + "/" + filename)){
	        	 schemaFilePathList.add(xsdDir + "/" + filename);
	         }
	         
	         //Testen ob der Pfad existiert
	         if(Files.isDirectory(Paths.get(xsdDir))){
	        	 System.out.println(xsdDir + " ist vorhanden...");
	        	 fout = new FileOutputStream(xsdDir + "/" + filename);
	         } else {
	        	 Files.createDirectories(Paths.get(xsdDir));
	        	 System.out.println("Directory wurde angelegt...");
	        	 fout = new FileOutputStream(xsdDir + "/" + filename);
	         }
	         

	         final byte data[] = new byte[1024];
	         int count;
	         while ((count = in.read(data, 0, 1024)) != -1) {
	             fout.write(data, 0, count);
	         }
	     } finally {
	         if (in != null) {
	             in.close();
	         }
	         if (fout != null) {
	             fout.close();
	         }
	     }
	 }

}
