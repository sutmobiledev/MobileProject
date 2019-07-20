
package com.sutmobiledev.bluetoothchat;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.Activity.MainActivity;
import com.sutmobiledev.bluetoothchat.file.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

//import android.support.annotation.RequiresApi;

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
    public static final int STATE_HANDSHAKING = 3;
    public static final int STATE_CONNECTED = 4;

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

        Log.e(TAG, "start: " + String.valueOf(getState()));
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
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, boolean isServer) {
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

        //Handshaking
        setState(STATE_HANDSHAKING);
        connectedThread = new ReadWriteThread(socket);
        if (isServer) {
            connectedThread.write(User.getUser_name().getBytes(), 0);

            byte[] peer_user_name = new byte[FileManager.BUFFER_SIZE];
            int bytecnt = 0;
            try {
                bytecnt = connectedThread.inputStream.read(peer_user_name);
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.obtainMessage(MainActivity.MESSAGE_PEER_USER_NAME, bytecnt, 0, peer_user_name);
        } else {
            byte[] peer_user_name = new byte[FileManager.BUFFER_SIZE];
            int bytecnt = 0;
            try {
                bytecnt = connectedThread.inputStream.read(peer_user_name);
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.obtainMessage(MainActivity.MESSAGE_PEER_USER_NAME, bytecnt, 0, peer_user_name);

            connectedThread.write(User.getUser_name().getBytes(), 0);
        }

        // Start the thread to manage the connection and perform transmissions
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

    public synchronized void write(byte[] out, int shouldWrite) {
        writeThread.write(out, shouldWrite);
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

    public void save_file(String name, byte[] bytes, int len, boolean firstTime) {
        File apkStorage = null;
        File outputFile = null;
        if (new CheckForSDCard().isSDCardPresent()) {

            apkStorage = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/BluetoothChat"
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
            fos = new FileOutputStream(outputFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        try {
            assert fos != null;

            fos.write(bytes, 0, len);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // runs while listening for incoming connections
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

        //@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
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

    private class WriteThread extends Thread {
        private volatile Handler writeHandler = null;

        public WriteThread() {
            start();
        }

        public void write(byte[] buffer, int shouldWrite) {
            writeHandler.obtainMessage(0, shouldWrite, 0, buffer).sendToTarget();
        }

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();

            writeHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    ReadWriteThread r;
                    Log.d(TAG, "WriteThread.write: before sync block");
                    synchronized (MainActivity.lock) {
                        if (state != STATE_CONNECTED)
                            return;
                        r = connectedThread;


                        byte[] bufferToWrite = (byte[]) msg.obj;
                        r.write(bufferToWrite, msg.arg1);

                        try {
                            Log.d(TAG, "WriteThread.write: before wait");
                            MainActivity.lock.wait();
                            Log.d(TAG, "WriteThread.write: after wait");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "WriteThread.write: after sync block");
                }
            };

            Looper.loop();
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

                    Log.i(TAG, "run: bytes = " + String.valueOf(bytes));
                } catch (IOException e) {
                    Log.e(TAG, "run1: " + e.getMessage());
                    connectionLost();
                    // Start the service over to restart listening mode
                    ChatController.this.start();
                    break;
                }

                String code = new String(buffer, 0, bytes);
                Log.d(TAG, "run: " + code);
                switch (code) {
                    case SEND_MESSAGE:
                        byte[] buffer_read = new byte[1024];
                        int nbytes;
                        try {
                            sendNotify();
                            nbytes = inputStream.read(buffer_read);
                            sendNotify();
                        } catch (IOException e) {
                            Log.e(TAG, "run2: " + e.getMessage());
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }

                        Log.d(TAG, "run: " + new String(buffer_read, 0, nbytes));

                        // Send the obtained bytes to the UI Activity
                        handler.obtainMessage(MainActivity.MESSAGE_READ, nbytes, -1, buffer_read).sendToTarget();
                        break;
                    case SEND_NOTIFY:
                        handler.obtainMessage(MainActivity.MESSAGE_NOTIFY, 0, 0, null).sendToTarget();
                        Log.d(TAG, "run: Notify Received");
                        break;
                    case SEND_FILE:
                        int byteCnt = 0;
                        byte[] buffer_Length = new byte[1024];
                        try {
                            sendNotify();
                            byteCnt = inputStream.read(buffer_Length);
                            Log.i(TAG, "run: byteCnt = " + String.valueOf(byteCnt));

                            sendNotify();

                            Log.d(TAG, "run: LEN: " + new String(buffer_Length, 0, byteCnt));
                        } catch (IOException e) {
                            Log.e(TAG, "run3: " + e.getMessage());
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        int length = Integer.parseInt(new String(buffer_Length, 0, byteCnt));

                        byte[] buffer_Type = new byte[1024];
                        try {
                            byteCnt = inputStream.read(buffer_Type);
                            Log.i(TAG, "run: byteCnt = " + String.valueOf(byteCnt));

                            sendNotify();

                            Log.d(TAG, "run: TYPE: " + new String(buffer_Type, 0, byteCnt));
                        } catch (IOException e) {
                            Log.e(TAG, "run4: " + e.getMessage());
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        String type = new String(buffer_Type, 0, byteCnt);

                        byte[] buffer_Name = new byte[1024];
                        try {
                            byteCnt = inputStream.read(buffer_Name);
                            Log.i(TAG, "run: byteCnt = " + String.valueOf(byteCnt));

                            sendNotify();

                            Log.d(TAG, "run: NAME: " + new String(buffer_Name, 0, byteCnt));
                        } catch (IOException e) {
                            Log.e(TAG, "run5: " + e.getMessage());
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        String name = new String(buffer_Name, 0, byteCnt);

                        boolean first = true;
                        try {
                            int tot = 0;
                            byte[] buffer_file = new byte[FileManager.BUFFER_SIZE];

                            if (length > FileManager.BUFFER_SIZE) {
                                byteCnt = inputStream.read(buffer_file);
                                Log.i(TAG, "run: byteCnt = " + String.valueOf(byteCnt));
                                Log.e(TAG, "run: str1 = " + new String(buffer_file, 0, byteCnt));

                                tot += byteCnt;

                                sendNotify();

                                save_file(name, buffer_file, byteCnt, first);
                                first = false;

                                //length += length % 512;
                                length -= byteCnt;

                                while (length > FileManager.BUFFER_SIZE) {
                                    buffer_file = new byte[FileManager.BUFFER_SIZE];

                                    byteCnt = inputStream.read(buffer_file);
                                    Log.i(TAG, "run: byteCnt = " + String.valueOf(byteCnt));
                                    Log.e(TAG, "run: str2 = " + new String(buffer_file, 0, byteCnt));

                                    tot += byteCnt;

                                    sendNotify();

                                    save_file(name, buffer_file, byteCnt, first);

                                    length -= byteCnt;
                                }
                            }

                            buffer_file = new byte[FileManager.BUFFER_SIZE];

                            byteCnt = inputStream.read(buffer_file);
                            Log.i(TAG, "run: byteCnt = " + String.valueOf(length));
                            Log.e(TAG, "run: str3 = " + new String(buffer_file, 0, length));

                            tot += length;

                            sendNotify();

                            save_file(name, buffer_file, length, first);

                            Log.i(TAG, "run: tot = " + String.valueOf(tot));
                        } catch (Exception e) {
                            Log.e(TAG, "run6: " + e.getMessage());
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }

                        String[] obj = new String[2];
                        obj[0] = name;
                        obj[1] = type;

                        handler.obtainMessage(MainActivity.MESSAGE_FILE_SEND, obj).sendToTarget();
                        break;
                    default:
                        assert false;
                        break;
                }
            }
        }

        // write to OutputStream
        public synchronized void write(byte[] buffer, int shouldWrite) {
            try {
                outputStream.write(buffer);
                if (shouldWrite == 1)
                    handler.obtainMessage(MainActivity.MESSAGE_WRITE, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "write: " + e.getMessage());
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
