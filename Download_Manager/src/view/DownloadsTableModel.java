package view;

import system.Download;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class DownloadsTableModel extends AbstractTableModel implements Observer {

    public enum ColumnNames{
        URL, SIZE, PROGRESS, STATUS
    }

    private static final Class[] columnClasses = {String.class, String.class, JProgressBar.class, String.class};

    private ArrayList<Download> downloadList = new ArrayList<>();

    @Override
    public void update(Observable o, Object arg) {

    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }
}
