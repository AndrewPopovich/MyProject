package view;

import system.Download;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class DownloadsTableModel extends AbstractTableModel implements Observer {

    private static final String[] columnNames = {"URL", "Size", "Progress", "Status"};

    private static final Class[] columnClasses = {String.class, String.class, JProgressBar.class, String.class};

    private ArrayList<Download> downloadList = new ArrayList<>();

    public void addDownload(Download download) {
        download.addObserver(this);

        downloadList.add(download);

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public Download getDownload(int row) {
        return downloadList.get(row);
    }

    public void clearDownload(int row) {
        downloadList.remove(row);

        fireTableRowsDeleted(row, row);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    public String getColumnName(int i) {
        return columnNames[i];
    }

    public Class getColumnClass(int i) {
        return columnClasses[i];
    }

    public int getRowCount() {
        return downloadList.size();
    }

    @Override
    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);

        fireTableCellUpdated(index, index);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
