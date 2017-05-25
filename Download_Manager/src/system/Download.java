package system;

import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable {

    private static final int BUFFER = 1024;
    private URL url;
    private long size;
    private long downloaded;
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

    public String getUrl() {
        return url.toString();
    }

    public long size() {
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

    private String getFileName(URL url){
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

    @Override
    public void run() {

    }
}
