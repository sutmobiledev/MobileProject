
package com.sutmobiledev.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class ChatController {
    private static final String APP_NAME = "BluetoothChatApp";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ReadWriteThread connectedThread;
    private int state;
    private MainActivity mainActivity;

    static final int STATE_NONE = 0;
    static final int STATE_LISTEN = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;

    public ChatController(Context context, Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        mainActivity = (MainActivity) context;
        this.handler = handler;
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

    public void write(byte[] out) {
        ReadWriteThread r;
        synchronized (this) {
            if (state != STATE_CONNECTED)
                return;
            r = connectedThread;
        }
        r.write(out);
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

    // runs while listening for incoming connections
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket serverSocket;

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
            //TODO
            // Keep listening to the InputStream
            while (true) {
                byte[] buffer = new byte[1024];
                int bytes;
//                try {
                    // Read from the InputStream
                    try {
                        bytes = inputStream.read(buffer);
                    } catch (IOException e) {
                        connectionLost();
                        // Start the service over to restart listening mode
                        ChatController.this.start();
                        break;
                    }
                    String code = new String(buffer);
                    if(code == "message") {
                        byte[] buffer_read = new byte[1024];
                        int nbytes;
                        try {
                            nbytes = inputStream.read(buffer_read);
                        }catch (IOException e) {
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }

                        // Send the obtained bytes to the UI Activity
                        handler.obtainMessage(MainActivity.MESSAGE_READ, nbytes, -1,
                                buffer).sendToTarget();
                    }
                    else if(code == "file"){
                        byte[] buffer_Length = new byte[1024];
                        try {
                            inputStream.read(buffer_Length);
                        } catch (IOException e) {
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        int length = Integer.parseInt(new String(buffer_Length));
                        byte[] buffer_Type = new byte[1024];
                        try {
                            inputStream.read(buffer_Type);
                        } catch (IOException e) {
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        String type = new String(buffer_Type);
                        byte[] buffer_Name = new byte[1024];
                        try {
                            inputStream.read(buffer_Name);
                        } catch (IOException e) {
                            connectionLost();
                            // Start the service over to restart listening mode
                            ChatController.this.start();
                            break;
                        }
                        String name = new String(buffer_Name);
                        // Create output streams & write to file
//                        FileOutputStream fos = new FileOutputStream(
//                                Environment.getExternalStorageDirectory()
//                                        + "/"+name);
                        byte[] buffer_file = new byte[8*1024];
                        int bytesRead;
                        int current = 0;
                        try {
                            if(length > 8*1024) {
                                bytesRead = inputStream.read(buffer_file, 0, 8*1024);
                            }
                            else{
                                bytesRead = inputStream.read(buffer_file, 0, length);
                            }
                            Log.d(TAG, "bytesRead first time =" + bytesRead);
                            current = bytesRead;
                            save_file(name,buffer_file);

                            while (length - current > 0) {
                                buffer_file = new byte[8*1024];
                                Log.d(TAG, "do-while -- current: " + current);
                                if(length - current > 8*1024) {
                                    bytesRead = inputStream.read(buffer_file, 0,
                                            8*1024);
                                }
                                else{
                                    bytesRead = inputStream.read(buffer_file, 0,
                                            length-current);
                                }
                                Log.d(TAG, "bytesRead: =" + bytesRead);

                                if (bytesRead >= 0)
                                    current += bytesRead;
                                save_file(name,buffer_file);
                            }
//                            save_file(name,buffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "do while end:-- buffer len= "
                                    + buffer.length + "  current: " + current);

//                            fos.write(buffer);
                            Log.d(TAG, "fos.write success! buffer: "
                                    + buffer.length + "  current: " + current);

//                            fos.flush();
//                            fos.close();
                        }
                    }

//                    } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//                catch (IOException e) {
//                    connectionLost();
//                    // Start the service over to restart listening mode
//                    ChatController.this.start();
//                    break;
//                }
            }
        }

        // write to OutputStream
        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
                handler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
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
        public void save_file(String name,byte[] bytes){
            File apkStorage = null;
            File outputFile = null;
            if (new CheckForSDCard().isSDCardPresent()) {

                apkStorage = new File(
                        Environment.getExternalStorageDirectory() + "/Download"
                );
            } else
                mainActivity.make_toast();

            //If File is not present create directory
            if (!apkStorage.exists()) {
                apkStorage.mkdir();
                Log.e("tag", "Directory Created.");
            }

            outputFile = new File(apkStorage, name);//Create Output file in Main File

            //Create New File if not present
            if (!outputFile.exists()) {
                try {
                    outputFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.e("tag", "File Created");
            }

            FileOutputStream fos = null;//Get OutputStream for NewFile Location
            try {
                fos = new FileOutputStream(outputFile,true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

//                InputStream is = c.getInputStream();//Get InputStream for connection
//                byte[] buffer = new byte[1024];//Set buffer type
//                int len1 = 0;//init length
//            File file = new File("/sdcard/Download/annie-spratt-01Wa3tPoQQ8-unsplash.jpg");
//            int size = (int) file.length();
//            byte[] bytes = new byte[size];
//            try {
//                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
//                buf.read(bytes, 0, bytes.length);
//                buf.close();
//            } catch (FileNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//                File fi = new File("/Users/sana/Desktop/here");
//                byte[] fileContent = null;
//                try {
//                    fileContent = Files.readAllBytes(fi.toPath());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

            try {
                fos.write(bytes, 0, bytes.length);//Write new file
            } catch (IOException e) {
                e.printStackTrace();
            }


            //Close all connection after doing task
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//                is.close();
//

        }
    }
}
