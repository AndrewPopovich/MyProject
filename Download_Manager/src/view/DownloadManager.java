package view;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class DownloadManager extends JFrame implements Observer {

    private JTextField addTextField;

    private DownloadsTableModel tableModel;


    @Override
    public void update(Observable o, Object arg) {

    }
}
