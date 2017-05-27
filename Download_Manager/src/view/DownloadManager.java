package view;

import system.Download;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

public class DownloadManager extends JFrame implements Observer {

    private JTextField addTextField;

    private DownloadsTableModel tableModel;

    private JButton pauseButton, resumeButton, cancelButton, clearButton;

    private Download selectDownload;


    @Override
    public void update(Observable o, Object arg) {

    }
}
