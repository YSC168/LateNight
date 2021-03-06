package com.liuguilin.latenight.activity;
/*
 *  项目名：  LateNight 
 *  包名：    com.liuguilin.latenight.activity
 *  文件名:   MusicActivity
 *  创建者:   LGL
 *  创建时间:  2016/10/25 13:29
 *  描述：    音乐
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.liuguilin.gankclient.R;
import com.liuguilin.latenight.adapter.MusicAdapter;
import com.liuguilin.latenight.entity.Constants;
import com.liuguilin.latenight.entity.MusicData;
import com.liuguilin.latenight.util.L;
import com.liuguilin.latenight.util.PicassoUtils;
import com.liuguilin.latenight.view.CustomDialog;
import com.liuguilin.latenight.view.ListViewToScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MusicActivity extends BaseActivity {

    private List<String> mListId = new ArrayList<>();

    //适配器
    private MusicAdapter mAdapter;
    //数据集
    private List<MusicData> mList = new ArrayList<>();
    //列表
    private ListViewToScrollView mListView;

    private ProgressBar progressBar;
    private CustomDialog dialog;
    private ImageView iv_picture;
    //支持缩放
    private PhotoViewAttacher mAttacher;
    //屏幕宽高
    private int width, height;
    private WindowManager wm;

    private List<String>mListImg = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.HANDLER_LOFING_MUSIC_LIST:
                    //请求完毕
                    mAdapter = new MusicAdapter(MusicActivity.this, mList);
                    mListView.setAdapter(mAdapter);
                    progressBar.setVisibility(View.GONE);
                    mAdapter.setOnClickListener(new MusicAdapter.PagerItemClickListener() {
                        @Override
                        public void onClickListener(int position) {
                            PicassoUtils.loadImageViewSize(MusicActivity.this, mListImg.get(position), width, height, iv_picture);
                            mAttacher = new PhotoViewAttacher(iv_picture);
                            mAttacher.update();
                            dialog.show();
                        }
                    });
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        initView();
    }

    //初始化View
    private void initView() {
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        L.i("width:" + width + "height:" + height);
        dialog = new CustomDialog(this, 0, 0, R.layout.dialog_picture, R.style.Theme_dialog, Gravity.CENTER, R.style.pop_anim_style);
        iv_picture = (ImageView) dialog.findViewById(R.id.iv_picture);
        mListView = (ListViewToScrollView) findViewById(R.id.mListView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        RxVolley.get(Constants.ONE_MUSIC_LIST, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                L.i("Music:" + t);
                parsingJson(t);
            }
        });
    }

    //解析json
    private void parsingJson(String t) {
        try {
            JSONObject jsonObject = new JSONObject(t);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < jsonArray.length(); i++) {
                //JSONObject json = (JSONObject) jsonArray.get(i);
                String id = jsonArray.get(i).toString();
                mListId.add(id);
            }

            //请求数据集
            for (int i = 0; i < mListId.size(); i++) {
                RxVolley.get(Constants.ONE_MUSIC_MORE + mListId.get(i), new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        L.i("Music More:" + t);
                        getMusicMore(t);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //获取歌曲详情
    private void getMusicMore(String t) {
        MusicData data = new MusicData();
        try {
            JSONObject jsonObject = new JSONObject(t);
            JSONObject jsonData = jsonObject.getJSONObject("data");
            JSONObject jsonAuthor = jsonData.getJSONObject("author");

            data.setImgBgUrl(jsonData.getString("cover"));
            data.setImgPhotoUrl(jsonAuthor.getString("web_url"));
            mListImg.add(jsonData.getString("cover"));
            data.setName(jsonAuthor.getString("user_name"));
            data.setTime(jsonData.getString("last_update_date"));
            data.setMusicUrl(jsonData.getString("web_url"));
            data.setTvTitle(jsonData.getString("title"));
            data.setTvMessage(jsonData.getString("charge_edt"));
            data.setStory_title(jsonData.getString("story_title"));
            data.setTvContent(jsonData.getString("story"));
            mList.add(data);

            if (mList.size() == mListId.size()) {
                handler.sendEmptyMessage(Constants.HANDLER_LOFING_MUSIC_LIST);
            }
        } catch (JSONException e) {
            progressBar.setVisibility(View.GONE);
            //解析失败
        }
    }

}
