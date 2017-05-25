package system;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable {

    private static final int BUFFER_SIZE = 1024;
    private URL url;
    private int size;
    private int downloaded;
    private Statuses status;

    public enum Statuses {
        DOWNLOADING(0), PAUSED(1), COMPLETE(2), CANCELLED(3), ERROR(4);

        Statuses(int i) {
        }
    }

    public Download(URL url) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = Statuses.DOWNLOADING;

        download();
    }

    @Override
    public void run() {
        HttpURLConnection connection = getConnection();

        try (RandomAccessFile file = new RandomAccessFile(getFileName(url), "rw");
             BufferedInputStream stream = new BufferedInputStream(connection.getInputStream())) {

            if (size == -1) {
                size = connection.getContentLength();
                stateChanged();
            }

            file.seek(downloaded);

            while (status == Statuses.DOWNLOADING) {
                int read = stream.read();

                if (read == -1) {
                    break;
                }

                file.write(stream.read());
                downloaded += read;
                stateChanged();
            }

            if (status == Statuses.DOWNLOADING) {
                status = Statuses.COMPLETE;
                stateChanged();
            }
        } catch (NullPointerException | IOException e) {
            error();
        }
    }

    private byte[] getBufferCorrectSize() {
        byte[] buffer;

        if (size - downloaded > BUFFER_SIZE) {
            buffer = new byte[BUFFER_SIZE];
        } else {
            buffer = new byte[size - downloaded];
        }

        return buffer;
    }

    private HttpURLConnection getConnection() {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Range", "bytes=" + downloaded + "-");

            connection.connect();

            checkValidConnection(connection);
        } catch (IOException e) {
            error();
        }
        return connection;
    }

    private void checkValidConnection(HttpURLConnection connection) throws IOException {
        int length = connection.getContentLength();

        if (connection.getResponseCode() / 100 != 2) {
            error();
        }

        if (length < 1) {
            error();
        }
    }

    public String getUrl() {
        return url.toString();
    }

    public int size() {
        return size;
    }

    public float getProgress() {
        return ((float) downloaded / size()) * 100;
    }

    public Statuses getStatus() {
        return status;
    }

    public void pause() {
        status = Statuses.PAUSED;
        stateChanged();
    }

    public void resume() {
        status = Statuses.DOWNLOADING;
        stateChanged();
        download();
    }

    public void cancel() {
        status = Statuses.CANCELLED;
        stateChanged();
    }

    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    private void error() {
        status = Statuses.ERROR;
        stateChanged();
    }

    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
