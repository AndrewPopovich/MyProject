package view;

import system.Download;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

public class DownloadManager extends JFrame implements Observer {

    private JTextField addTextField;

    private DownloadsTableModel tableModel;

    private JButton pauseButton, resumeButton, cancelButton, clearButton;

    private Download selectDownload;

    private boolean clearing;

    public DownloadManager() throws HeadlessException {
        setTitle("Download Manager");

        setSize(640, 480);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

            }
        });
    }

    private void updateButtons() {
        if (selectDownload != null) {
            Download.Statuses status = selectDownload.getStatus();

            switch (status){
                case DOWNLOADING:
                    pauseButton.setEnabled(true);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case PAUSED:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    clearButton.setEnabled(false);
                    break;
                case ERROR:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
                    break;
                default:
                    pauseButton.setEnabled(false);
                    resumeButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    clearButton.setEnabled(true);
            }
        } else {
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            clearButton.setEnabled(false);
        }
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
