package com.example.egpfy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.egpfy.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.time.Clock;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private FirebaseStorage mStorage;
    private ActivityMainBinding binding;
    private MediaPlayer player;
    private Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        initData();

        testingMood();
//        workingMood();
    }

    private void initData() {
        player = new MediaPlayer();
        mStorage = FirebaseStorage.getInstance();
        playBtnFun();
        pauseBtnFun();
        forward5BtnFun();
        forward10BtnFun();
        backword5BtnFun();
        backword10BtnFun();

    }

    private void backword5BtnFun() {
        binding.backword5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null){
                    if(player.getCurrentPosition() - 5000 >= 0)
                        player.seekTo(player.getCurrentPosition() - 5000);
                }
            }
        });
    }

    private void backword10BtnFun() {
        binding.backword10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null){
                    if(player.getCurrentPosition() - 10000 >= 0)
                        player.seekTo(player.getCurrentPosition() - 10000);
                }
            }
        });
    }

    private void forward5BtnFun() {
        binding.forward5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null){
                    if(player.getDuration() >=  player.getCurrentPosition() + 5000)
                        player.seekTo(player.getCurrentPosition() + 5000);
                }
            }
        });
    }

    private void forward10BtnFun() {
        binding.forward10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null){
                    if(player.getDuration() >=  player.getCurrentPosition() + 10000)
                        player.seekTo(player.getCurrentPosition() + 10000);
                }
            }
        });
    }

    private void workingMood() {
        binding.getMusicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Music"),20);
            }
        });
    }

    private void testingMood() {
        binding.getMusicBtn.setVisibility(View.GONE);
        binding.counter.setVisibility(View.GONE);
        try {
            player.setDataSource("https://firebasestorage.googleapis.com/v0/b/friendly-abb19.appspot.com/o/20?alt=media&token=a271c7f5-24a3-4949-91b0-dad2a6b6269d");
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    player.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void pauseBtnFun() {
        binding.pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null && player.isPlaying()){
                    try{
                        player.pause();
                        binding.pause.setVisibility(View.GONE);
                        binding.play.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e){
                        Toast.makeText(MainActivity.this, "There is no song to pause !!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void playBtnFun() {
        binding.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(player != null && !player.isPlaying()){
                    try{
                        player.start();
                        binding.play.setVisibility(View.GONE);
                        binding.pause.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e){
                        Toast.makeText(MainActivity.this, "There is no song to play !!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 20 && resultCode == RESULT_OK && data.getData() != null){
            upload(data.getData());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void upload(Uri uri) {
        StorageReference ref = mStorage.getReference(uri.getLastPathSegment());
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                binding.counter.setText("Started !!");
                try {
                    Log.d("URL", String.valueOf(uri));
                    player.setDataSource(String.valueOf(uri));
                    player.prepare();
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            player.start();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, taskSnapshot.getMetadata().getReference().getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progress = Math.round(progress * 100.0) / 100.0;
                binding.counter.setText(progress + " %");
            }
        });
    }
}