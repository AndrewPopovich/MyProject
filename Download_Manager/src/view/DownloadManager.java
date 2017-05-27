package view;

import system.Download;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

public class DownloadManager extends JFrame implements Observer {

    private JTextField addTextField;

    private DownloadsTableModel tableModel;

    private JTable table;

    private JButton pauseButton, resumeButton, cancelButton, clearButton;

    private Download selectDownload;

    private boolean clearing;

    public DownloadManager() throws HeadlessException {
        setTitle("Download Manager");

        setSize(640, 480);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                actionExit();
            }
        });

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem fileExitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        fileExitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionExit();
            }
        });
        fileMenu.add(fileExitMenuItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        JPanel addPanel = new JPanel();
        addTextField = new JTextField(30);
        addPanel.add(addTextField);
        JButton addButton = new JButton("Add Download");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionAdd();
            }
        });
        addPanel.add(addButton);


    }

    private void actionAdd() {
        URL verifiedUrl = verifyUrl(addTextField.getText());

        if (verifiedUrl == null) {
            JOptionPane.showMessageDialog(this, "Invalid Download URL", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            tableModel.addDownload(new Download(verifiedUrl));
            addTextField.setText("");
        }
    }

    private URL verifyUrl(String url) {
        URL verifiedUrl = null;

        if (!url.toLowerCase().startsWith("http://")) {
            verifiedUrl = null;
        }

        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            verifiedUrl = null;
        }
        return verifiedUrl;
    }

    private void actionExit() {
        System.exit(0);
    }

    private void actionPause() {
        selectDownload.pause();
        updateButtons();
    }

    private void actionResume() {
        selectDownload.resume();
        updateButtons();
    }

    private void actionCancel() {
        selectDownload.cancel();
        updateButtons();
    }

    private void actionClear() {
        clearing = true;
        tableModel.clearDownload(table.getSelectedRow());
        clearing = false;
        selectDownload = null;
        updateButtons();
    }

    private void updateButtons() {
        if (selectDownload != null) {
            Download.Statuses status = selectDownload.getStatus();

            switch (status) {
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
