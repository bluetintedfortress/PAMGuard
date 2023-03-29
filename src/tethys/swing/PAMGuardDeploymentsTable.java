package tethys.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamUtils.PamCalendar;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.DeploymentOverview;
import tethys.deployment.RecordingPeriod;
import tethys.niluswraps.PDeployment;

/**
 * Table view of PAMGuard deployments. For a really simple deployment, this may have only
 * one line. For towed surveys where we stop and start a lot, it may have a LOT of lines.
 * @author dg50
 *
 */
public class PAMGuardDeploymentsTable extends TethysGUIPanel {

	private TableModel tableModel;

	private JTable table;

	private JPanel mainPanel;

	private DeploymentOverview deploymentOverview;
	
	private boolean[] selection = new boolean[0];
	
	private ArrayList<DeploymentTableObserver> observers = new ArrayList<>();

	public PAMGuardDeploymentsTable(TethysControl tethysControl) {
		super(tethysControl);
//		deploymentHandler = new DeploymentHandler(getTethysControl());
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("PAMGuard recording periods"));
		tableModel = new TableModel();
		table = new JTable(tableModel);
//		table.setRowSelectionAllowed(true);
		table.addMouseListener(new TableMouse());
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"PAMDeploymensTable", table);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	private class TableMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int aRow = table.getSelectedRow();
			int col = table.getSelectedColumn();
			if (aRow >= 0 && aRow < selection.length && col == 6) {
				selection[aRow] = !selection[aRow];
				for (DeploymentTableObserver obs : observers) {
					obs.selectionChanged();
				}
			}
		}

	}

	public void showPopup() {
		int aRow = table.getSelectedRow();
		int[] selRows = table.getSelectedRows();
		if (selRows == null || selRows.length == 0) {
			if (aRow >= 0) {
				selRows = new int[1];
				selRows[0] = aRow;
			}
			else {
				return;
			}
		}
		// make a list of RecordingPeriods which don't currently have a Deployment document
		ArrayList<RecordingPeriod> newPeriods = new ArrayList<>();
		ArrayList<RecordingPeriod> allPeriods = deploymentOverview.getRecordingPeriods();
		for (int i = 0; i < selRows.length; i++) {
			if (allPeriods.get(selRows[i]).getMatchedTethysDeployment() == null) {
				newPeriods.add(allPeriods.get(i));
			}
		}
		if (newPeriods.size() == 0) {
			return;
		}
		/*
		 *  if we get here, we've one or more rows without a Tethys output, so can have
		 *  a menu to create them. 
		 */
		
	}
	
	/**
	 * Get a list of selected recording periods. 
	 * @return list of selected periods. 
	 */
	public ArrayList<RecordingPeriod> getSelectedDeployments() {
		if (deploymentOverview == null) {
			return null;
		}
		ArrayList<RecordingPeriod> selDeps = new ArrayList<>();
		int n = Math.min(selection.length, deploymentOverview.getRecordingPeriods().size());
		for (int i = 0; i < n; i++) {
			if (selection[i]) {
				selDeps.add(deploymentOverview.getRecordingPeriods().get(i));
			}
		}
		return selDeps;
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch(tethysState.stateType) {
		case NEWPROJECTSELECTION:
		case NEWPAMGUARDSELECTION:
			updateDeployments();
			break;
		case UPDATEMETADATA:
			checkExportMeta();
		}

		tableModel.fireTableDataChanged();
	}

	private void checkExportMeta() {
		String metaErr = getTethysControl().getDeploymentHandler().canExportDeployments();
		if (metaErr != null) {
			mainPanel.setBackground(Color.RED);
		}
		else {
			JPanel anyPanel = new JPanel();
			mainPanel.setBackground(anyPanel.getBackground());
		}
	}

	private void updateDeployments() {
		DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
		deploymentOverview = deploymentHandler.getDeploymentOverview();
		int n = deploymentOverview.getRecordingPeriods().size();
		if (selection.length < n) {
			selection = Arrays.copyOf(selection, n);
//			for (int i = 0; i < setDefaultStores.length; i++) {
//				if (selectBoxes[i] == null) {
//					selectBoxes[i] = new JCheckBox();
//				}
//			}
		}
		tableModel.fireTableDataChanged();
//		DeploymentData deplData = getTethysControl().getGlobalDeplopymentData();
//		ArrayList<Deployment> projectDeployments = getTethysControl().getDbxmlQueries().getProjectDeployments(deplData.getProject());
//		deploymentHandler.matchPamguard2Tethys(deploymentOverview, projectDeployments);
	}
	
	public void addObserver(DeploymentTableObserver observer) {
		observers.add(observer);
	}

	private class TableModel extends AbstractTableModel {

		private String[] columnNames = {"Id", "Start", "Stop", "Duration", "Cycle", "Tethys Deployment", "Select"};

		@Override
		public int getRowCount() {
			if (deploymentOverview == null) {
				return 0;
			}
			else {
				return deploymentOverview.getRecordingPeriods().size();
			}
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 6) {
				return Boolean.class;
//				return JCheckBox.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			RecordingPeriod period = deploymentOverview.getRecordingPeriods().get(rowIndex);
//			DeploymentRecoveryPair deplInfo = deploymentInfo.get(rowIndex);
			if (columnIndex == 4) {
				return deploymentOverview.getDutyCycleInfo();
			}
			return getValueAt(period, rowIndex, columnIndex);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 6;
		}

		private Object getValueAt(RecordingPeriod period, int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return rowIndex;
			case 1:
				return PamCalendar.formatDBDateTime(period.getRecordStart());
//				return TethysTimeFuncs.formatGregorianTime(deplInfo.deploymentDetails.getAudioTimeStamp());
			case 2:
				return PamCalendar.formatDBDateTime(period.getRecordStop());
//				return TethysTimeFuncs.formatGregorianTime(deplInfo.recoveryDetails.getAudioTimeStamp());
			case 3:
//				long t1 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.deploymentDetails.getAudioTimeStamp());
//				long t2 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.recoveryDetails.getAudioTimeStamp());
				return PamCalendar.formatDuration(period.getRecordStop()-period.getRecordStart());
			case 5:
				PDeployment deployment = period.getMatchedTethysDeployment();
				return makeDeplString(period, deployment);
			case 6:
//				return selectBoxes[rowIndex];
				return selection[rowIndex];
			}

			return null;
		}

		private String makeDeplString(RecordingPeriod period, PDeployment deployment) {
			if (deployment == null) {
				return "no match";
			}
			DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
			long overlap = deploymentHandler.getDeploymentOverlap(deployment, period);

			long start = period.getRecordStart();
			long stop = period.getRecordStop();
			double percOverlap = (overlap*100.) / (stop-start);
			return String.format("%s : %3.1f%% overlap", deployment.toString(), percOverlap);
		}

	}
}
