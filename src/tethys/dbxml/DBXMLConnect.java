package tethys.dbxml;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.xml.bind.JAXBException;

import java.nio.file.Files;
import java.nio.file.Path;

import dbxml.uploader.Importer;
import nilus.Deployment;
import nilus.MarshalXML;
import tethys.TethysControl;
import tethys.output.TethysExportParams;
import tethys.output.StreamExportParams;
import PamguardMVC.PamDataBlock;

/**
 * Class containing functions for managing the database connection. Opening, closing,
 * writing, keeping track of performance, etc. 
 * @author Doug Gillespie, Katie O'Laughlin
 *
 */
public class DBXMLConnect {

	private TethysControl tethysControl;

	public DBXMLConnect(TethysControl tethysControl) {
		this.tethysControl = tethysControl;
	}
	
	
	/**
	 * take list of nilus objects loaded with PamGuard data and post them to the Tethys database
	 * all objects must be of the same nilus object
	 * TethysExportParams obj used from UI inputs  
	 * 
	 * @param pamGuardObjs all nilus objects loaded with PamGuard data
	 * @return error string, null string means there are no errors
	 */
	public String postToTethys(List<?> pamGuardObjs) 
	{
		Class objClass = pamGuardObjs.get(0).getClass();
		String collection = getTethysCollection(objClass.getName());
		PamDataBlock defaultPamBlock = null;
		TethysExportParams params = new TethysExportParams();
		String fileError = null;
		try {
			MarshalXML marshal = new MarshalXML();
			marshal.createInstance(objClass);			
			for (Object obj : pamGuardObjs ) 
			{				
				Path tempFile = Files.createTempFile("pamGuardToTethys", ".xml");
				marshal.marshal(obj, tempFile.toString());
				fileError = Importer.ImportFiles(params.getFullServerName(), collection,
						new String[] { tempFile.toString() }, "", "", false);

				System.out.println(fileError);
				
				tempFile.toFile().deleteOnExit();
			}
		} catch(IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		return fileError;
	}
	
	/**
	 * get tethys collection name from nilus collection objects
	 * @param className nilus object Class Name
	 * @return name of Tethys collection
	 */
	private String getTethysCollection(String className) {
		switch(className) {
			case "nilus.Deployment": 
				return "Deployments";				
			case "nilus.Detection": 
				return "Detections";
			case "nilus.Calibration": 
				return "Calibrations";				
			case "nilus.Ensemble": 
				return "Ensembles";			
			case "nilus.Localization": 
				return "Localizations";				
			case "nilus.SpeciesAbbreviation": 
				return "SpeciesAbbreviations";				
			case "nilus.SourceMap": 
				return "SourceMaps";				
			case "nilus.ITIS": 
				return "ITIS";							
			case "nilus.ranks": 
				return "ITIS_ranks";
			default: 
				return "";									
		}
	}
	
	public boolean openDatabase() {
		
		return true;
	}
	
	public void closeDatabase() {
		
	}
	
	// add whatever calls are necessary ... 

}
