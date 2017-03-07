package blinov_ivan.a43241;

    import android.bluetooth.BluetoothSocket;

interface CommunicatorService {
    Communicator createCommunicatorThread(BluetoothSocket socket);
}

