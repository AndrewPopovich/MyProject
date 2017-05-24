package system;

import java.util.Observable;

public class Download extends Observable implements Runnable{

    private static final int BUFFER = 1024;

    public enum Statuses {
        DOWNLOADING(0), PAUSED(1), COMPLETE(2), CANCELLED(3), ERROR(4);

        Statuses(int i) {
        }
    }


    @Override
    public void run() {

    }
}
