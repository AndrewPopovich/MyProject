package system;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

public class Download extends Observable implements Runnable {

    public static void main(String[] args) throws MalformedURLException {
        Download download = new Download(new URL("https://r4---sn-bpb5oxu-vqne.googlevideo.com/videoplayback?source=youtube&ip=130.180.216.101&itag=22&key=yt6&mime=video%2Fmp4&mt=1495731391&mv=m&signature=9D254988AAFB02041250048240D7138A16A94C07.2EE7AAF15873A8A69AA92D50D06D1AC99F9020E1&ms=au&mm=31&mn=sn-bpb5oxu-vqne&pl=20&id=o-AMdIDmssa0oyLcP8Uv1AT-DYTO57YLeD-Zw18vpgTs6t&requiressl=yes&dur=26.076&beids=%5B9466592%5D&sparams=dur%2Cei%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&ipbits=0&ratebypass=yes&expire=1495753108&lmt=1492240026642114&initcwndbps=196250&ei=NA0nWavgDofrdK_FqeAD&title=%D0%AD%D1%82%D0%BE%20%D0%BC%D0%BE%D1%8F%20%D0%B3%D1%80%D1%83%D1%88%D0%B0%2C%20%D0%BF%D0%B8%D0%B4%D1%80%D0%B8%D0%BB%D0%B0!%20%D0%9D%D0%B5%D0%BE%D1%81%D0%BF%D0%BE%D1%80%D0%B8%D0%BC%D1%8B%D0%B9%204.%20This%20is%20my%20pear%2C%20asshole!%20(ost)%20Undisputed%204"));
    }

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
                byte[] buffer = new byte[BUFFER_SIZE];

                int read = stream.read(buffer);

                if (read == -1) {
                    if (size != downloaded) {
                        continue;
                    } else {
                        break;
                    }
                }

                file.write(stream.read(buffer, 0, read));
                downloaded += read;
                stateChanged();
            }

            if (status == Statuses.DOWNLOADING) {
                status = Statuses.COMPLETE;
                stateChanged();
            }
        } catch (NullPointerException | IOException e) {
            e.printStackTrace();
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

        if (fileName.length() > 200) {
            fileName = "downloads.mp4";
        }
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
