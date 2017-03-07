package blinov_ivan.a43241;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStream;
    import java.io.InputStreamReader;
    import java.io.OutputStream;

    import android.bluetooth.BluetoothSocket;
    import android.util.Log;
    import android.widget.TextView;
    import android.widget.Toast;

public class CommunicatorImpl extends Thread implements Communicator {

    interface CommunicationListener {
        void onMessage(String message);
    }

    private final BluetoothSocket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final CommunicationListener listener;
    //private final BufferedReader rader;

    public CommunicatorImpl(BluetoothSocket socket, CommunicationListener listener) {
        this.socket = socket;
        this.listener = listener;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("CommunicatorImpl", e.getLocalizedMessage());
        }
        inputStream = tmpIn;
        //rader = new BufferedReader(new InputStreamReader(inputStream));
        outputStream = tmpOut;
    }

    @Override
    public void startCommunication() {
        byte[] buffer = new byte[1024];

        int bytes;

        Log.d("CommunicatorImpl", "Run the communicator");

        while (true) {
            try {
                //rader.readLine();
                bytes = inputStream.read(buffer); //readline
                Log.d("CommunicatorImpl", "Read " + bytes + " bytes");
                if (listener != null) {
                    listener.onMessage(new String(buffer).substring(0, bytes));
                }
            } catch (IOException e) {
                Log.d("CommunicatorImpl", e.getLocalizedMessage());
                break;
            }
        }
    }

    public void write(String message) {
        try {
            Log.d("CommunicatorImpl", "Write " + message);
            outputStream.write(message.getBytes());
        } catch (IOException e) {
            Log.d("CommunicatorImpl", e.getLocalizedMessage());
        }
    }

    @Override
    public void stopCommunication() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.d("CommunicatorImpl", e.getLocalizedMessage());
        }
    }

}