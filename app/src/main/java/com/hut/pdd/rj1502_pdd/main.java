package com.hut.pdd.rj1502_pdd;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.hut.pdd.rj1502_pdd.service.MusicService;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int STORAGE = 1;//权限请求码
    private ListView MusicInfoView;
     private ContentResolver contentResolver;
     private int nowpostion=0;
    List<MusicInfo> musicInfos;
    MusicService musicService;
    private Button button2;
    private Button button;
    private ListView MusicInfo;
    private SeekBar seekBar;
    private TextView gequ;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private TextView bofang;
    BroadcastReceiver broadcastReceiver;
//    private int seekBar_pro;





    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        MusicInfoView = (ListView) findViewById(R.id.Music_info);
        button2 = (Button) findViewById(R.id.button2);
        button = (Button) findViewById(R.id.button);
        MusicInfo = (ListView) findViewById(R.id.Music_info);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        gequ = (TextView) findViewById(R.id.gequ);
        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (ImageView) findViewById(R.id.image3);
        bofang = (TextView) findViewById(R.id.bofang);
        //设置播放等按钮事件
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService==null){
                    Toast.makeText(main.this,"服务未启动！",Toast.LENGTH_SHORT).show();
                    return;
                }
                int pos=nowpostion-1;
                if(pos<0){
                    pos=musicInfos.size();

                }
                musicService.reSetDateSource(musicInfos.get(pos).getData(),pos);
                bofang.setText(musicInfos.get(pos).getTitle());
                nowpostion=pos;
                image2.setImageResource(R.drawable.music_pause);

            }
        });
        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService==null){//判断是否为空避免崩溃
                    Toast.makeText(main.this,"服务未启动！",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(musicService.isPlaying()){
                image2.setImageResource(R.drawable.music_play);
                    bofang.setText(musicInfos.get(nowpostion).getTitle());
                musicService.play();
                }
                else
                { image2.setImageResource(R.drawable.music_pause);
                musicService.play();
                    bofang.setText(musicInfos.get(nowpostion).getTitle());
                }
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(musicService==null){
                    Toast.makeText(main.this,"服务未启动！",Toast.LENGTH_SHORT).show();
                    return;
                }
                int pos=nowpostion+1;
                if(pos>musicInfos.size()){
                    pos=0;
                }
                musicService.reSetDateSource(musicInfos.get(pos).getData(),pos);
                bofang.setText(musicInfos.get(pos).getTitle());
                nowpostion=pos;
                image2.setImageResource(R.drawable.music_pause);

            }
        });
//service自动切歌广播
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
          bofang.setText(intent.getStringExtra("title"));
            }
        };

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.hut.rj1502.pdd");
        registerReceiver(broadcastReceiver,intentFilter);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//             seekBar_pro=progress;
                if(fromUser){
                    seekBar.setProgress(progress);
                float m_Sec= (float) ((progress*1.0/seekBar.getMax())*musicService.getDuration());//计算快进到毫秒数
                musicService.seekTo((int) m_Sec);}
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        //检查是否具有公共数据存取权限
        int Check=checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(Check!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE);//申请权限
        }
        else {
            init();
        }


    }

    private void init() {//初始化列表
        contentResolver=getContentResolver();//获取系统contentresolver
//        歌曲在系统中的id
//        MediaStore.Audio.Media._ID
//歌曲的歌名
//       MediaStore.Audio.Media.TITLE;
//歌曲所在专辑的id
//        MediaStore.Audio.Media.ALBUM_ID;
//专辑的歌手名
//        MediaStore.Audio.Media.ARTIST
//歌曲的时长
//        MediaStore.Audio.Media.DURATION
//歌曲的大小
//        MediaStore.Audio.Media.SIZE
//获取专辑名
//        MediaStore.Audio.Media.ALBUM
//歌曲路径，如xx/xx/xx.mp3
//        ediaStore.Audio.Media.DATA

        Cursor cursor=contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new  String[]{MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST
                        ,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.SIZE,MediaStore.Audio.Media.DATA
                        },null,null,MediaStore.Audio.Media.TITLE);
       musicInfos=new ArrayList<>();
       List<Map<String,String>> list_map=new ArrayList<>();
       for (int i=0;i<cursor.getCount();i++){
           MusicInfo musicInfo=new MusicInfo();
           Map<String,String>  map=new HashMap<>();
           cursor.moveToNext();
           musicInfo.set_id(cursor.getInt(0));
           musicInfo.setTitle(cursor.getString(1));
           musicInfo.setAlbum(cursor.getString(2));
           musicInfo.setArtist(cursor.getString(3));
           musicInfo.setDuration(cursor.getInt(4));
           musicInfo.setMusicName(cursor.getString(5));
           musicInfo.setSize(cursor.getInt(6));
           musicInfo.setData(cursor.getString(7));
           String name[] ;

           musicInfos.add(musicInfo);
           String Music_name=cursor.getString(5);
           if(Music_name==null){
               Music_name="未知-未知";
           }
           if(Music_name.lastIndexOf("-")!=-1) {
               name= Music_name.split("-");//分割字符
           }
           else {
               name= new String[]{Music_name, Music_name};
           }




           //将获取的音乐大小由Byte转换成mb 并且用float个数的数据表示
           Float size = (float) (cursor.getInt(6) * 1.0 / 1024 / 1024)  ;
           //对size这个Float对象进行保留两位小数处理
           BigDecimal b  =   new  BigDecimal(size);
           Float   f1   =  b.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue();
           map.put("Music_name",name[1].replace(".mp3","")+"  "+f1.toString()+"mb");
           // map.put("Music_name",Music_name);
           map.put("alb",cursor.getString(3));
           list_map.add(map) ;

       }
        SimpleAdapter simpleAdapter=new SimpleAdapter(this,list_map,R.layout.musicinfo_itemlayout,new String[]{"Music_name","alb"},//内容适配器
                new int[]{R.id.MusicName,R.id.alb});
       MusicInfoView.setAdapter(simpleAdapter);
       MusicInfoView.setOnItemClickListener(this);


    }
//处理进度条

    final Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:Map<String,Integer> map= (Map<String, Integer>) msg.obj;
                    gequ.setText(""+map.get("curpos")+" / "+map.get("dur"));
                    seekBar.setProgress(map.get("seek"));//设置进度条位置



            }
        }
    };

    /*
   申请权限处理结果
    */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case STORAGE :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //完成程序的初始化
                    init();
                    System.out.println("程序申请权限成功，完成初始化") ;
                }
                else {
                    System.out.println("程序没有获得相关权限，请处理");
                }
                break ;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        if(position==nowpostion){//判断是否当前播放对象

            return;
        }
        String MusicData = musicInfos.get(position).getData();
        Intent intent = new Intent() ;
        intent.setClass(this ,MusicService.class ) ;


        ServiceConnection myServiceConnection=new ServiceConnection(){


            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MyBinder myBinder= (MusicService.MyBinder) service;
                musicService=((MusicService.MyBinder)service).getService();
                Toast.makeText(main.this,musicInfos.get(position).getData()+position,Toast.LENGTH_LONG).show();
                myBinder.setMuscInfos(musicInfos);//初始化音乐信息
                myBinder.setPosition1(position);//设置播放位置
                musicService.startMusic();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        while (true){
                            int cu=musicService.getCurrenPostion();
                            Date date=new Date(cu);
                            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("mm:ss");
                            String currpos=simpleDateFormat.format(date);
                            int d=musicService.getDuration();
                            Date date1=new Date(d);
                            String dur=simpleDateFormat.format(date1);
                            int m=seekBar.getMax();
                            m= ((m*cu)/d);
                            Map<String,Object> map= new HashMap<>();
                            map.put("curpos",currpos);
                            map.put("dur",dur);
                            map.put("seek", m);

                            Message msg=new Message();
                            msg.what=1;
                            msg.obj=map;
                            handler.sendMessage(msg);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
   //判断服务是否启动
        if(musicService==null){
            bindService(intent,myServiceConnection,Context.BIND_AUTO_CREATE);
        }

        else{
            musicService.reSetDateSource(musicInfos.get(position).getData(),position);


        }
       nowpostion=position;//赋值当前播放对象
        image2.setImageResource(R.drawable.music_pause);
        bofang.setText(musicInfos.get(nowpostion).getTitle());
        MusicInfoView.setSelection(position);


    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
