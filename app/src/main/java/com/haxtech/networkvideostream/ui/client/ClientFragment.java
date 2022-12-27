package com.haxtech.networkvideostream.ui.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.haxtech.networkvideostream.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientFragment extends Fragment {

    private ClientViewModel mViewModel;
    private ImageView mImageView;

    public static ClientFragment newInstance() {
        return new ClientFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(ClientViewModel.class);
        mImageView = view.findViewById(R.id.imageView);
        // TODO: Use the

        new Thread(() -> {
            try {
                Socket clientSocket = new Socket("192.168.1.78", 8000);
                List<Bitmap> frames = new ArrayList<Bitmap>();
                Thread.sleep(5000);

                InputStream stream = clientSocket.getInputStream();
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                Log.i("ClientFragment", "Reading stream");

                while(clientSocket.isConnected() && stream.available() > 0) {
                    int data = stream.read();
                    Log.i("ClientFragment", "Byte: " + data);

                    if (data == 0) {
                        Bitmap bitmap;
                        byte[] bytes = dataStream.toByteArray();

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ImageDecoder.Source source = ImageDecoder.createSource(bytes);
                                bitmap = ImageDecoder.decodeBitmap(source);
                            } else {
                                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            }
                            frames.add(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        dataStream.reset();
                    } else {
                        dataStream.write(data);
                    }
                }
                
                Log.i("ClientFragment", "Disconnected from socket. Read: " + frames.size() + " frames");

                new Handler(Looper.getMainLooper()).post(() -> {
                    frames.forEach(bitmap -> {
                        if (mImageView != null) {
                            mImageView.setImageBitmap(bitmap);
                            Log.i("ClientFragment", "Frame set");
                            try {
                                Thread.sleep(1000/60);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                });

                stream.close();
                clientSocket.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

//        String url = "h264://192.168.1.78:8000/"; // your URL here
//        MediaPlayer mediaPlayer = new MediaPlayer();
//        mediaPlayer.setAudioAttributes(
//                new AudioAttributes.Builder()
//                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
//                        .setUsage(AudioAttributes.USAGE_MEDIA)
//                        .build()
//        );
//        try {
//            mediaPlayer.setDataSource(url);
//            mediaPlayer.prepare(); // might take long! (for buffering, etc)
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mediaPlayer.start();
//        Log.i("ClientFragment", "MediaPlayer started");
    }

}