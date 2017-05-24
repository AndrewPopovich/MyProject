package system;

import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable{

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


    @Override
    public void run() {

    }
}
