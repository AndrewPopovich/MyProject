package view;

import system.Download;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

        tableModel = new DownloadsTableModel();
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                tableSelectionChanged();
            }
        });

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        renderer.setStringPainted(true);
        table.setDefaultRenderer(JProgressBar.class, renderer);

        table.setRowHeight((int) renderer.getPreferredSize().getHeight());

        JPanel downloadsPanel = new JPanel();
        downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
        downloadsPanel.setLayout(new BorderLayout());
        downloadsPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionPause();
            }
        });
        pauseButton.setEnabled(false);
        buttonPanel.add(pauseButton);

        resumeButton = new JButton("Resume");
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionResume();
            }
        });
        resumeButton.setEnabled(false);
        buttonPanel.add(resumeButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCancel();
            }
        });
        cancelButton.setEnabled(false);
        buttonPanel.add(cancelButton);

        clearButton = new JButton("Clear");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionClear();
            }
        });
        cancelButton.setEnabled(false);
        buttonPanel.add(clearButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(addPanel, BorderLayout.NORTH);
        getContentPane().add(downloadsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void tableSelectionChanged() {
        if (selectDownload != null) {
            selectDownload.deleteObserver(DownloadManager.this);
        }

        if (!clearing && table.getSelectedRow() > -1) {
            selectDownload = tableModel.getDownload(table.getSelectedRow());

            selectDownload.addObserver(DownloadManager.this);
            updateButtons();
        }
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
        if (selectDownload != null && selectDownload.equals(o)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateButtons();
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DownloadManager manager = new DownloadManager();
                manager.setVisible(true);
            }
        });
    }

}
