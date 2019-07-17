
package com.sutmobiledev.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ChatController {
    public static final String SEND_MESSAGE = "1654656513515613135156156132";
    public static final String SEND_FILE = "3165165156464461354616646";
    public static final String SEND_NOTIFY = "3213254865165498451032";

    private static final String APP_NAME = "BluetoothChatApp";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final String TAG = "SUTBluetoothChatContr";

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ReadWriteThread connectedThread;
    private int state;
    private MainActivity mainActivity;

    private WriteThread writeThread;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public ChatController(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        mainActivity = (MainActivity) context;
        this.handler = handler;
        writeThread = new WriteThread();
    }

    // Set the current state of the chat connection
    private synchronized void setState(int state) {
        this.state = state;

        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    // get current connection state
    public synchronized int getState() {
        return state;
    }

    // start service
    public synchronized void start() {
        // Cancel any thread
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any running thresd
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        setState(STATE_LISTEN);
        if (acceptThread == null) {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    // initiate connection to remote device
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    // manage Bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel running thread
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ReadWriteThread(socket);
        connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_OBJECT);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.DEVICE_OBJECT, device);
        msg.setData(bundle);
        handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    // stop all threads
    public synchronized void stop() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out, int shouldWrite) {
        writeThread.writeHandler.obtainMessage(0, shouldWrite, 0, out).sendToTarget();

        if (!writeThread.isAlive())
            writeThread.start();
    }

    public void sendNotify() {
        ReadWriteThread r;

        if (state != STATE_CONNECTED)
            return;
        r = connectedThread;


        r.write(SEND_NOTIFY.getBytes(), 0);

        Log.d(TAG, "sendNotify: done.");
    }

    private void connectionFailed() {
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        ChatController.this.start();
    }

    private void connectionLost() {
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);

        // Start the service over to restart listening mode
        ChatController.this.start();
    }

    private class WriteThread extends Thread {
        private byte[] bufferToWrite;
        private boolean ready = false;
        private int shouldSend = 0;
        public Handler writeHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.d(TAG, "handleMessage: Before");
                while (ready) ;
                Log.d(TAG, "handleMessage: After");
                bufferToWrite = ((byte[]) msg.obj).clone();
                shouldSend = msg.arg1;
                ready = true;

                return true;
            }
        });

        @Override
        public void run() {
            while (true) {
                if (ready) {
                    ReadWriteThread r;
                    synchronized (MainActivity.lock) {
                        if (state != STATE_CONNECTED)
                            return;
                        r = connectedThread;


                        r.write(bufferToWrite, shouldSend);
                        Log.d(TAG, "write: " + new String(bufferToWrite));
                        Log.d(TAG, "write: " + MainActivity.lock.toString());

                        try {
                            ready = false;
                            MainActivity.lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    // runs while listening for incoming connections
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            serverSocket = tmp;
        }

        public void run() {
            setName("AcceptThread");
            BluetoothSocket socket;
            while (state != STATE_CONNECTED) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (ChatController.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate
                                // new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // runs while attempting to make an outgoing connection
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
        }

        public void run() {
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                socket.connect();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (ChatController.this) {
                connectThread = null;
            }

            // Start the connected thread
            connected(socket, device);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    // runs during a connection with a remote device
    private class ReadWriteThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ReadWriteThread(BluetoothSocket socket) {
            this.bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            while (true) {
                byte[] buffer = new byte[1024];
                int bytes;
                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    connectionLost();
                    // Start the service over to restart listening mode
                    ChatController.this.start();
                    break;
                }

                Log.d(TAG, "run: " + new String(buffer));
                String code = new String(buffer);
                if (code.startsWith(SEND_MESSAGE)) {
                    byte[] buffer_read = new byte[1024];
                    int nbytes;
                    try {
                        sendNotify();
                        nbytes = inputStream.read(buffer_read);
                        sendNotify();
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }

                    Log.d(TAG, "run: " + new String(buffer_read));

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(MainActivity.MESSAGE_READ, nbytes, -1, buffer_read).sendToTarget();
                } else if (code.startsWith(SEND_NOTIFY)) {
                        handler.obtainMessage(MainActivity.MESSAGE_NOTIFY, 0, 0,
                                null).sendToTarget();
                } else if (code.startsWith(SEND_FILE)) {
                    sendNotify();

                    int byteCnt = 0;
                    byte[] temp, buffer_Length = new byte[1024];
                    try {
                        byteCnt = inputStream.read(buffer_Length);

                        sendNotify();

                        temp = new byte[byteCnt];
                        System.arraycopy(buffer_Length, 0, temp, 0, byteCnt);
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }
                    int length = Integer.parseInt(new String(temp));

                    byte[] buffer_Type = new byte[1024];
                    try {
                        byteCnt = inputStream.read(buffer_Type);

                        sendNotify();

                        temp = new byte[byteCnt];
                        System.arraycopy(buffer_Type, 0, temp, 0, byteCnt);
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }
                    String type = new String(temp);

                    byte[] buffer_Name = new byte[1024];
                    try {
                        byteCnt = inputStream.read(buffer_Name);

                        sendNotify();

                        temp = new byte[byteCnt];
                        System.arraycopy(buffer_Name, 0, temp, 0, byteCnt);
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }
                    String name = new String(temp);

                    try {
                        byte[] buffer_file = new byte[8 * 1024];
                        byteCnt = inputStream.read(buffer_file);

                        sendNotify();

                        temp = new byte[byteCnt];
                        System.arraycopy(buffer_Name, 0, temp, 0, byteCnt);
                        save_file(name, temp, true);

                        length -= byteCnt;

                        while (length > 0) {
                            buffer_file = new byte[8 * 1024];
                            byteCnt = inputStream.read(buffer_file);

                            sendNotify();

                            temp = new byte[byteCnt];
                            System.arraycopy(buffer_Name, 0, temp, 0, byteCnt);
                            save_file(name, temp, false);

                            length -= byteCnt;
                        }
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }

                    String[] obj = new String[2];
                    obj[0] = name;
                    obj[1] = type;

                    handler.obtainMessage(MainActivity.MESSAGE_FILE_SEND, obj).sendToTarget();
                }
            }
        }

        // write to OutputStream
        public synchronized void write(byte[] buffer, int shouldWrite) {
            Log.d(TAG, "write: " + new String(buffer));
            try {
                outputStream.write(buffer);
                if (shouldWrite == 1)
                    handler.obtainMessage(MainActivity.MESSAGE_WRITE, buffer).sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void save_file(String name, byte[] bytes, boolean firstTime) {
            File apkStorage = null;
            File outputFile = null;
            if (new CheckForSDCard().isSDCardPresent()) {

                apkStorage = new File(
                        Environment.getExternalStorageDirectory() + "/BluetoothChat"
                );
            } else {
                Toast.makeText(mainActivity, "Oops!! There is no SD Card.", Toast.LENGTH_SHORT).show();
                mainActivity.finish();
            }

            //If File is not present create directory
            if (!apkStorage.exists()) {
                apkStorage.mkdir();
                Log.d(TAG, "save_file: Directory Created.");
            }

            outputFile = new File(apkStorage, name);//Create Output file in Main File

            //Create New File if not present
            if (!outputFile.exists()) {
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save_file: File Created");
            } else if (firstTime) {
                try {
                    outputFile.delete();
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "save_file: File Created");
            }

            FileOutputStream fos = null;//Get OutputStream for NewFile Location
            try {
                fos = new FileOutputStream(outputFile,true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }



            try {
                assert fos != null;

                fos.write(bytes, 0, bytes.length);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
