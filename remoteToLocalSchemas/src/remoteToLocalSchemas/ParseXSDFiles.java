package remoteToLocalSchemas;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ParseXSDFiles{
	
	private static String sourceXmlFileToParse = "S:/sberinger/spatialSamplingFeature.xsd";
	private static final String ROOTDIR = "C:/Users/sberinge/Desktop/test3";
	private static final String CATALOGFILE = ROOTDIR + "/catalog-file.xml";
	
	public static List<String> schemaUrlList = new ArrayList<String>();
	public static List<String> schemaFileList = new ArrayList<String>();
	public static List<String> schemaFilePathList = new ArrayList<String>();
	
	//main Methode zum Starten
	public static void main(String[] args) throws SAXException, IOException, XMLStreamException{
		
		//Proxy setzen
		System.setProperty("http.proxyHost", "ofsquid.dwd.de");
        System.setProperty("http.proxyPort", "8080");
		
	    sourceXmlFileToParse = "S:/sberinger/sigmet_test_kuscherka2.xml";	    
	
	    runParser(sourceXmlFileToParse, ROOTDIR);

		System.out.println("Total files parsed: " + schemaFileList.size());	
		
		for (int i=0; i<schemaFileList.size(); i++){
			System.out.println(schemaFileList.get(i));
		}
		
		//Schreiben der Katalog-Datei
		String xmlHead = "<?xml version=\"1.0\"?>\n"+
					     "<!DOCTYPE catalog PUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\" \"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">\n"+
					     "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n";
		String xmlStringBody = PrintEventContentHandler.getXmlStringBody();
		String xmlFoot ="</catalog>";
		
		writeCatalogFile(xmlHead+xmlStringBody+xmlFoot);							
	}
	
	public static void runParser(String xmlFile, String rootDir) throws SAXException, IOException{
		
		//XMLReader erzeugen
	    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
		
		//InputSource inputSource = new InputSource("S:/sberinger/sigmet_test_kuscherka2.xml");
		InputSource inputSource = new InputSource(xmlFile);
		
		//Handler festlegen und zusätzlich Übergabe eines root Directorys (ohne Slash am Ende) für die lokale Speicherung der xsd-Dateien
		PrintEventContentHandler handler = new PrintEventContentHandler(rootDir);
		
		//Contenthandler
		xmlReader.setContentHandler(handler);
		
		//Parser starten
		xmlReader.parse(inputSource);
		
		//Liste mit Dateipfaden zurückbekommen
		List<String> tempFilePathList = new ArrayList<String>();
		tempFilePathList = handler.getSchemaFilePathList();
		
		//Rekursiver Aufruf des Parsers umm alle abhängigen xsd Dateien zu finden und lokal zu speichern
		for (int i=0; i<tempFilePathList.size(); i++){
			if(!schemaFileList.contains(tempFilePathList.get(i))){
				schemaFileList.add(tempFilePathList.get(i));
				xmlFile = tempFilePathList.get(i);
				runParser(xmlFile, rootDir);
			}
				
		}
		
	}
	
	public static void writeCatalogFile(String xmlStringBody) throws XMLStreamException, IOException{
		
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream(CATALOGFILE), "utf-8"));
		    writer.write(xmlStringBody);
		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
		
		
	}


}
