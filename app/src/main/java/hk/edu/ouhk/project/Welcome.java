package hk.edu.ouhk.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class Welcome extends AppCompatActivity {

    public MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //set up a audio
        player = MediaPlayer.create(this,R.raw.welcome3);
        try{
            player.prepare();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        player.start();

        //set up a timer of 2 seconds for the launch page
        Timer timer=new Timer();
        TimerTask timerTask=new TimerTask() {
            @Override
            public void run() {
                player.release();
                Intent intent1=new Intent(Welcome.this,MainActivity.class);
                startActivity(intent1);
                Welcome.this.finish();
            }
        };
        timer.schedule(timerTask,1000*2);
    }


}
