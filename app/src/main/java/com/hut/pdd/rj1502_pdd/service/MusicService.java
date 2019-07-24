package com.hut.pdd.rj1502_pdd.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.hut.pdd.rj1502_pdd.MusicInfo;

import java.io.IOException;
import java.util.List;

import static android.net.Uri.parse;

public class MusicService extends Service {
    List<MusicInfo> musicInfos;
    int position;
    private final  MyBinder myBinder=new MyBinder();
    private  String musicPath="";
    private MediaPlayer mplayer=new MediaPlayer();


    @Override
    public boolean onUnbind(Intent intent) {
        mplayer.stop();
        mplayer.release();
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {


        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                position++;
                if(position>musicInfos.size()){
                    position=0;
                }
                reSetDateSource(musicInfos.get(position).getData(),position);
                Intent in=new Intent("com.hut.rj1502.pdd");
                in.putExtra("title",musicInfos.get(position).getTitle());
                sendBroadcast(in);

            }
        });




    }

    public void startMusic(){

        musicPath=musicInfos.get(position).getData();
        try {
            mplayer.setDataSource(musicPath);
            mplayer.prepare();
            mplayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //判断是否处于播放状态
    public boolean isPlaying(){
        return mplayer.isPlaying();
    }
    //播放或暂停歌曲
    public void play() {
        if (mplayer.isPlaying()) {
            mplayer.pause();
        } else {
            mplayer.start();
        }

    }

    //返回歌曲的长度，单位为毫秒
    public int getDuration(){
        return mplayer.getDuration();
    }

    //返回歌曲目前的进度，单位为毫秒
    public int getCurrenPostion(){
        return mplayer.getCurrentPosition();
    }

    //设置歌曲播放的进度，单位为毫秒
    public void seekTo(int msecond){
        mplayer.seekTo(msecond);
    }
    //设置播放资源
    public  void reSetDateSource(String path,int nowpos){
        mplayer.reset();
        position=nowpos;
        try {
            mplayer.setDataSource(path);
            mplayer.prepare();
            mplayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this,"播放",Toast.LENGTH_LONG).show();
    }

    public class MyBinder extends Binder{
        public MusicService getService(){return  MusicService.this;}
        public void setMuscInfos(List<MusicInfo> musicInfos1){
           musicInfos=musicInfos1;

        }
        public void setPosition1(int i){


            position=i;
        }






    }
}
