package com.sutmobiledev.bluetoothchat.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sutmobiledev.bluetoothchat.BlankFragment;
import com.sutmobiledev.bluetoothchat.ChatController;
import com.sutmobiledev.bluetoothchat.ChooseImage;
import com.sutmobiledev.bluetoothchat.ChooseVideo;
import com.sutmobiledev.bluetoothchat.Contact;
import com.sutmobiledev.bluetoothchat.DataBaseHelper;
import com.sutmobiledev.bluetoothchat.MessageAdapter;
import com.sutmobiledev.bluetoothchat.R;
import com.sutmobiledev.bluetoothchat.RecordAudio;
import com.sutmobiledev.bluetoothchat.User;
import com.sutmobiledev.bluetoothchat.VideoFragment;
import com.sutmobiledev.bluetoothchat.file.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{

    private TextView status;
    private Button btnConnect;
    private ListView listView;
    private Dialog dialog;
    private TextInputLayout inputLayout;
    ArrayList<com.sutmobiledev.bluetoothchat.Message> messages;
    private MessageAdapter messageAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private DataBaseHelper db;
    ImageView profilePhoto;
    TextView nameTextView;
    public static final String TAG = "SUTBluetoothChatMain";

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_NOTIFY = 6;
    public static final int MESSAGE_FILE_SEND = 7;
    public static final int MESSAGE_FILE_RECEIVE = 8;
    public static final int MESSAGE_PEER_USER_NAME = 9;


    public static final String DEVICE_OBJECT = "device_name";
    public String PEER_USER_NAME = "";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    public static final int CHOOSE_IMAGE = 2;
    public static final int CHOOSE_VIDEO = 3;
    public static final int CHOOSE_VOICE = 4;
    public static final int CHOOSE_FILE = 5;

    public static final Object lock = new Object();
    private com.sutmobiledev.bluetoothchat.Message message;

    public FileManager fileManager;
    public ChooseImage chooseImage;
    public ChooseVideo chooseVideo;
    public ChatController chatController = null;
    BlankFragment blankFragment;
    public RecordAudio recordAudio;
    public FrameLayout fr;
    VideoFragment videoFragment;

    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;
    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: Called:  " + String.valueOf(msg.what));
            switch (msg.what) {
                case MESSAGE_PEER_USER_NAME:
                    PEER_USER_NAME = new String((byte[]) msg.obj, 0, msg.arg1);
                    Log.i(TAG, "handleMessage: " + PEER_USER_NAME);
                    break;
                case MESSAGE_FILE_SEND:
                    String args = (String)msg.obj;

                    message = new com.sutmobiledev.bluetoothchat.Message();
                    message.setName(User.getUser_name());
                    message.setContactId(bluetoothAdapter.getAddress().hashCode());
                    message.setBelongsToCurrentUser(true);
                    message.setBody(args);
                    message.setType(msg.arg1);
                    message.setFileAddress(Environment.getExternalStorageDirectory()+"/BluetoothChat/wallpaper/"+args);
                    db.addMessage(message);
                    messages.add(message);
                    messageAdapter.add(message);
                    break;
                case MESSAGE_FILE_RECEIVE:
                    String args1 = (String)msg.obj;
                    message = new com.sutmobiledev.bluetoothchat.Message();
                    message.setName(PEER_USER_NAME);
                    message.setContactId(connectingDevice.getAddress().hashCode());
                    message.setBelongsToCurrentUser(false);
                    message.setBody(args1);
                    message.setType(msg.arg1);
                    message.setFileAddress(Environment.getExternalStorageDirectory()+"/BluetoothChat/"+args1);
                    db.addMessage(message);
                    messages.add(message);
                    messageAdapter.add(message);
                    break;
                case MESSAGE_NOTIFY:
                    Log.d(TAG, "handleMessage: Notify Message is received.");
                    synchronized (MainActivity.lock) {
                        MainActivity.lock.notify();
                    }
                    break;
                case MESSAGE_STATE_CHANGE:
                    Log.e(TAG, "handleMessage: " + String.valueOf(msg.arg1));
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: " + connectingDevice.getName());
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            btnConnect.setEnabled(false);
                            break;
                        case ChatController.STATE_LISTEN:
                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            btnConnect.setEnabled(true);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    //TODO
                    //add image address
//                    chatMessages.add("Me: " + writeMessage);
//                    save to db message sent by this user
                    message = new com.sutmobiledev.bluetoothchat.Message();
                    message.setName(User.getUser_name());
                    message.setContactId(bluetoothAdapter.getAddress().hashCode());
                    message.setBelongsToCurrentUser(true);
                    message.setBody(writeMessage);
                    message.setType(com.sutmobiledev.bluetoothchat.Message.TYPE_TEXT);
                    db.addMessage(message);
                    messages.add(message);
                    messageAdapter.add(message);
//                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    //TODO
                    //add imageadress
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    message = new com.sutmobiledev.bluetoothchat.Message();
                    message.setName(PEER_USER_NAME);
                    message.setContactId(connectingDevice.getAddress().hashCode());
                    message.setBelongsToCurrentUser(false);
                    message.setBody(readMessage);
                    message.setType(com.sutmobiledev.bluetoothchat.Message.TYPE_TEXT);
                    db.addMessage(message);
                    messages.add(message);
                    messageAdapter.add(message);
//                    chatAdapter.notifyDataSetChanged();
//                    chatMessages.add(connectingDevice.getName() + ":  " + readMessage);
//                    chatAdapter.notifyDataSetChanged();
                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);

                    assert connectingDevice != null;

                    int contactId = connectingDevice.getAddress().hashCode();
                    if (db.firsConnection(contactId)) {
                        db.addContact(new Contact(contactId, PEER_USER_NAME, "jkaldsjfk"));
                    } else {
                        db.deleteContact(contactId);
                        db.addContact(new Contact(contactId, PEER_USER_NAME, null));
                    }

                    Toast.makeText(getApplicationContext(), "Connected to " + PEER_USER_NAME,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            listView.setSelection(listView.getCount() - 1);
            listView.setAdapter(messageAdapter);
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //locate listviews and attatch the adapters
        ListView listView = dialog.findViewById(R.id.pairedDeviceList);
        ListView listView2 = dialog.findViewById(R.id.discoveredDeviceList);
        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //Handling listview item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address, true);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address, true);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setStatus(String s) {
        status.setText(s);
    }

    private void connectToDevice(String deviceAddress, boolean isServer) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device, isServer);
    }

    private void findViewsByIds() {
        status = findViewById(R.id.status);
        btnConnect = findViewById(R.id.btn_connect);
        listView = findViewById(R.id.list);
        inputLayout = findViewById(R.id.input_layout);
        fr = findViewById(R.id.frame);
        final Button btnSend = findViewById(R.id.btn_send);
        Button btnFile = findViewById(R.id.btn_file);

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blankFragment = new BlankFragment();
                blankFragment.setMain(MainActivity.this);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame, blankFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                fr.setVisibility(View.VISIBLE);
                //fileManager.showFileChooser();
            }
        });
//        inputLayout.getEditText().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ValueAnimator colorAnimation = ObjectAnimator.ofInt(btnSend, "backgroundColor", 0xc8c8c8, 0x4FA8DC);
//                colorAnimation.setDuration(300);
//                colorAnimation.setEvaluator(new ArgbEvaluator());
//                colorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
//                colorAnimation.start();
//            }
//        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inputLayout.getEditText().getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(inputLayout.getEditText().getText().toString());
                    inputLayout.getEditText().setText("");
                }
                inputLayout.getEditText().onEditorAction(EditorInfo.IME_ACTION_DONE);

            }
        });
    }

    private void sendMessage(String message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = ChatController.SEND_MESSAGE.getBytes();
            chatController.write(send, 0);

            send = message.getBytes();
            chatController.write(send, 1);
        }
    }

    @Override
    public void onStart() {
        Log.e(TAG, "onStart: ");
        super.onStart();
        if (chatController == null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
            } else {
                chatController = new ChatController(this, handler);
            }

            fileManager = FileManager.getInstance().init(this, handler);
            db = DataBaseHelper.getInstance(this);
            messages = new ArrayList<com.sutmobiledev.bluetoothchat.Message>();
            chooseImage = new ChooseImage();
            chooseVideo = new ChooseVideo();
        }
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();

        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode % 10) {
            case CHOOSE_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode / 10 == 2) {
                        if (data != null) {
                            Uri contentURI = data.getData();
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                                String path = chooseImage.saveImage(bitmap);
                                Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                                fileManager.sendFile(Uri.fromFile(new File(path)), CHOOSE_IMAGE);

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                            }
                            // Get the Uri of the selected file
                            //TODO
                        }
                    }
                    if (requestCode / 10 == 1) {
                        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                        String path = chooseImage.saveImage(thumbnail);
                        Toast.makeText(MainActivity.this, "Image Saved!", Toast.LENGTH_SHORT).show();
                        fileManager.sendFile(Uri.fromFile(new File(path)), CHOOSE_IMAGE);
                    }
                }
                break;
            case CHOOSE_VIDEO:
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode / 10 == 2) {
                        if (data != null) {
                            Uri contentURI = data.getData();
                            String selectedVideoPath = chooseVideo.getPath(contentURI);
                            Log.d("path", selectedVideoPath);
                            String path = chooseVideo.saveVideoToInternalStorage(selectedVideoPath);
                            fileManager.sendFile(Uri.fromFile(new File(path)), CHOOSE_VIDEO);

                            // Get the Uri of the selected file
                            //TODO
                        }
                    }
                    if (requestCode / 10 == 1) {
                        Uri contentURI = data.getData();
                        String recordedVideoPath = chooseVideo.getPath(contentURI);
                        Log.d("frrr", recordedVideoPath);
                        String path = chooseVideo.saveVideoToInternalStorage(recordedVideoPath);
                        fileManager.sendFile(Uri.fromFile(new File(path)), CHOOSE_VIDEO);
                    }
                }
                break;
            case CHOOSE_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri uri = data.getData();
                    if (uri == null) {
                        Log.e(TAG, "onActivityResult: Not Supported");
                        Toast.makeText(this, "This file is not supported.", Toast.LENGTH_LONG).show();
                        break;
                    }

                    Log.d(TAG, "onActivityResult Uri: " + uri.toString());
                    // Get the path
                    Log.d(TAG, "onActivityResult Path: " + uri.getPath());

                    fileManager.sendFile(uri, CHOOSE_FILE);
                }
                break;

            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
        fr.setVisibility(View.GONE);
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        findViewsByIds();

//        check device support bluetooth or not
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (getSharedPreferences("post",MODE_PRIVATE).contains("USER_ID")) {
            User.setUserId(getSharedPreferences("post", MODE_PRIVATE).getInt("USER_ID", 0));
            User.setProfileAddress(getSharedPreferences("post", MODE_PRIVATE).getString("PROFILE_PIC", null));
            User.setUser_name(getSharedPreferences("post", MODE_PRIVATE).getString("USER_NAME", "Unknown"));
        } else {
            User.setUserId((bluetoothAdapter.getName() + String.valueOf(new Random().nextInt())).hashCode());
            getSharedPreferences("post", MODE_PRIVATE).edit().putInt("USER_ID", User.getUserId()).apply();
            getSharedPreferences("post", MODE_PRIVATE).edit().putString("PROFILE_PIC", User.getProfileAddress()).apply();
            User.setUser_name(bluetoothAdapter.getName());
            getSharedPreferences("post", MODE_PRIVATE).edit().putString("USER_NAME", User.getUser_name()).apply();
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        messageAdapter = new MessageAdapter(this);
        listView = findViewById(R.id.list);
        listView.setSelection(listView.getCount() - 1);
        listView.setAdapter(messageAdapter);
//        show bluetooth devices dialog when click connect button
//        btnConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showPrinterPickDialog();
//            }
//        });

        //set chat adapter
//        chatMessages = new ArrayList<String>();
//        chatAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, chatMessages);
//        listView.setAdapter(chatAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (messages.get(i).getType() == CHOOSE_VOICE) {
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath() + messages.get(i).getName();
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(path);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(MainActivity.this.getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(messages.get(i).getType() == CHOOSE_VIDEO){
//                    VideoView videoView = (VideoView) messages.get(i).getMessageViewHolder().getSendedVideo();
//                    MediaController mediaController = new MediaController(MainActivity.this);
//                    videoView.setMediaController(mediaController);
//                    mediaController.setAnchorView(videoView);
////                    videoFragment = new VideoFragment();
////                    videoFragment.setMain(MainActivity.this);
////                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////                    transaction.replace(R.id.frame, videoFragment);
////                    transaction.addToBackStack(null);
////                    transaction.commit();
////                    fr.setVisibility(View.VISIBLE);
                    videoFragment = new VideoFragment();
                    videoFragment.setVideoPath(messages.get(i).getFileAddress());
                    videoFragment.setMain(MainActivity.this);
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.frame, videoFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    fr.setVisibility(View.VISIBLE);
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        profilePhoto = header.findViewById(R.id.imageView1);
        nameTextView = header.findViewById(R.id.user);
        if (getSharedPreferences("post",MODE_PRIVATE).contains("USER_NAME")) {
            Log.i("HEREEEEE",getSharedPreferences("post", MODE_PRIVATE).getString("USER_NAME", "Unknown"));
            User.setUser_name(getSharedPreferences("post", MODE_PRIVATE).getString("USER_NAME", "Unknown"));
        } else {
            getSharedPreferences("post", MODE_PRIVATE).edit().putString("USER_NAME", User.getUser_name()).apply();
        }
        if (getSharedPreferences("post",MODE_PRIVATE).contains("PROFILE_PIC")) {
            User.setProfileAddress(getSharedPreferences("post", MODE_PRIVATE).getString("PROFILE_PIC", null));
        } else {
            getSharedPreferences("post", MODE_PRIVATE).edit().putString("PROFILE_PIC", User.getProfileAddress()).apply();
        }
        nameTextView.setText(User.getUser_name());
        if(User.getProfileAddress()!= null) {
            File folder2 = new File(User.getProfileAddress());
            if (folder2.exists()) {
                String folderpath3 = folder2.getAbsolutePath().trim();
                profilePhoto.setImageBitmap(BitmapFactory.decodeFile(folderpath3));
            } else {
                Log.e("Hereee", "image not exists");
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout2);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.Review) {
            startActivity(new Intent(MainActivity.this, ReviewActivity.class));

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}