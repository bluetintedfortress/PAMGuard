package tethys;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import tethys.dbxml.DBXMLConnect;
//import nilus.Deployment;
//import nilus.Deployment.Instrument;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.output.TethysExporter;
import tethys.output.swing.TethysExportDialog;

/**
 * Quick play with a simple system for outputting data to Tethys. At it's start
 * this is simply going to offer a dialog and have a few functions which show how 
 * to access data within PAMGuard. 
 * @author dg50
 *
 */
public class TethysControl extends PamControlledUnit {

	public static final String unitType = "Tethys Interface";
	public static String defaultName = "Tethys";
	public static String xmlNameSpace = "http://tethys.sdsu.edu/schema/1.0";
	

	private TethysExportParams tethysExportParams = new TethysExportParams();
	
	private DBXMLConnect dbxmlConnect;

	public TethysControl(String unitName) {
		super(unitType, unitName);
		dbxmlConnect = new DBXMLConnect(this);
	}

	/**
	 * Get DBXML Connector. This class contains all the functions that are needed
	 * to talk to the database. 
	 * @return DBXML functions. 
	 */
	public DBXMLConnect getDbxmlConnect() {
		return dbxmlConnect;
	}

	@Override
	public JMenuItem createFileMenu(JFrame parentFrame) {
		JMenu tethysMenu = new JMenu("Tethys");
		JMenuItem tethysExport = new JMenuItem("Export ...");
		tethysMenu.add(tethysExport);
		tethysExport.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tethysExport(parentFrame);
			}
		});
		return tethysMenu;
	}

	/**
	 * @return the tethysExportParams
	 */
	public TethysExportParams getTethysExportParams() {
		return tethysExportParams;
	}

	/**
	 * We'll probably want to 
	 * @param parentFrame
	 */
	protected void tethysExport(JFrame parentFrame) {
		TethysExportParams newExportParams = TethysExportDialog.showDialog(parentFrame, this);
		if (newExportParams != null) {
			// dialog returns null if cancel was pressed. 
			tethysExportParams = newExportParams;
			exportTethysData(tethysExportParams);
		}
	}

	/**
	 * We'll arrive here if the dialog has been opened and we want to export Tethys data. 
	 * @param tethysExportParams2
	 */
	private void exportTethysData(TethysExportParams tethysExportParams) {
		TethysExporter tethysExporter = new TethysExporter(this, tethysExportParams);
		tethysExporter.doExport();		
	}
	
	/**
	 * A name for any deta selectors. 
	 * @return
	 */
	public String getDataSelectName() {
		return getUnitName();
	}

}
