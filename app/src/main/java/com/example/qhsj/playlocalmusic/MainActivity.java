package com.example.qhsj.playlocalmusic;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qhsj.playlocalmusic.entry.MusicInfo;
import com.example.qhsj.playlocalmusic.util.MusicUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<MusicInfo> musicList;
    private HashMap<String, String> map;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<String> data;
    private Toolbar toolbar;
    private Context mContext;
    private int currentPosition = -1;
    private boolean repeatFlag;
    private MusicInfo clickItem;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer = new MediaPlayer();
    }

    /*
     * 开始播放
     */
    private void startPlaying() {
        mMediaPlayer.reset();

        try {
            mMediaPlayer.setDataSource(clickItem.data);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*
     * 停止播放
     */
    private void stopPlaying() {
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initListener() {
        
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.sr);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(new MyAdapter());

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this,"刷新",Toast.LENGTH_SHORT).show();
            }
        });
    }




    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {  // <MyAdapter.ViewHolder> 这个泛型完全是自己加的，加了后后面的 RecyclerView.ViewHolder全部换成ViewHolder

        /*
        * 自己写，很坑爹
        */
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivStart;
            ImageView ivSelect;
            TextView tvTitle;
            LinearLayout llItemRoot;

            public ViewHolder(View view) {
                super(view);
                ivStart = (ImageView) view.findViewById(R.id.ivStart);
                ivSelect = (ImageView) view.findViewById(R.id.ivSelect);
                tvTitle = (TextView) view.findViewById(R.id.tvTitle);
                llItemRoot = (LinearLayout) view.findViewById(R.id.llItemRoot);
            }
        }

        public MyAdapter() {
            musicList = MusicUtil.queryMusic(MainActivity.this);
            map = new HashMap<String,String>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mContext == null) {
                mContext = parent.getContext();
            }
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_select_music, parent, false);
            final ViewHolder holder = new ViewHolder(view);
//            holder.cardView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = holder.getAdapterPosition();
//                    Fruit fruit = mFruitList.get(position);
//                    Intent intent = new Intent(mContext, FruitActivity.class);
//                    intent.putExtra(FruitActivity.FRUIT_NAME, fruit.getName());
//                    intent.putExtra(FruitActivity.FRUIT_IMAGE_ID, fruit.getImageId());
//                    mContext.startActivity(intent);
//                }
//            });
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) { // RecyclerView.ViewHolder holder, int position
            MusicInfo musicInfo = musicList.get(position);
            holder.tvTitle.setText(musicInfo.musicName); // 显示音乐名

            // 刷新UI选中位置的图片
            if (currentPosition == position){  // 点击的位置

                if (repeatFlag){  // 是同一个条目连续点击双数次
                    holder.ivStart.setImageResource(R.mipmap.iv_play_music);
                }else{ // 点击单次，即正在播放
                    holder.ivStart.setImageResource(R.mipmap.iv_pause_music);
                }

            }else{ // 非点击的位置
                holder.ivStart.setImageResource(R.mipmap.iv_play_music);
            }

            holder.llItemRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentPosition == position){// 标志位，取反,连续点击同一个条目为双数的时候是true
                        repeatFlag = !repeatFlag;
                    }else{
                        repeatFlag = false;
                    }

                    map.put("change",position + "");
                    currentPosition = position;
                    clickItem = musicList.get(position);

                    if (repeatFlag){
                        // 暂停播放
                        mMediaPlayer.reset();
                    }else{
                        // 继续播放
                        startPlaying();
                    }

                    notifyDataSetChanged();

                }
            });
        }

        @Override
        public int getItemCount() {
            return musicList.size();
        }
    }
}
