package com.data.quetu;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TwoFragment extends  Fragment {
    private static MyAppAdapter myAppAdapter;
    private   static Dataloop dloop;
    private   Context context;
    private static ViewHolder viewHolder;

    private static ListView flingContainer;
    SimpleExoPlayer player;
    private static ArrayList<Data> array;
    private int card_count=12;
    FrameLayout moreFrame;
    LinearLayout cardFrame;
    TextView QwenuText,PingText;
    HlsMediaSource[] hlsMediaSource = new HlsMediaSource[card_count];

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_two, container, false);

        PulsatorLayout mPulsator = rootView.findViewById(R.id.pulsator);
        mPulsator.start();
        cardFrame = rootView.findViewById(R.id.card_frame);
        moreFrame = rootView.findViewById(R.id.more_frame);
        QwenuText = rootView.findViewById(R.id.QwenuText);
        PingText = rootView.findViewById(R.id.PingText);
        flingContainer = rootView.findViewById(R.id.frame);
        //Toast.makeText(getContext(),"STANDUPS", Toast.LENGTH_SHORT).show();
        dloop = new Dataloop();
        dloop.GenerateData();


        return rootView;
    }

    private static class VideoCache {
        private static SimpleCache sDownloadCache;

        public static   SimpleCache getInstance(Context context) {
            //if (sDownloadCache == null) sDownloadCache = new SimpleCache(new File(context.getCacheDir(), "prankCache"), new NoOpCacheEvictor(), new ExoDatabaseProvider(context));
            if (sDownloadCache == null) sDownloadCache = new SimpleCache(new File(context.getCacheDir(), "prankCache"),   new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 100), new ExoDatabaseProvider(context));

            return sDownloadCache;
        }
    }

    private class Dataloop extends  AppCompatActivity {

        //json pipeline to pull data
        private void GenerateData() {

            String tab_value = "PRANKS";
            moreFrame.setVisibility(View.VISIBLE);
            QwenuText.setVisibility(View.VISIBLE);
            cardFrame.setVisibility(View.GONE);
            // start pulsator

            array = new ArrayList<>();
            String requestUrl;

            requestUrl = "http://35.184.65.16:5025/apihook?status=" + tab_value;
            //Toast.makeText(MainActivity.this,requestUrl, Toast.LENGTH_SHORT).show();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .get()
                    .url(requestUrl)
                    .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("getProfileInfo", "FAIL");

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    final String jsonData = response.body().string();
                    Log.i("getProfileInfo", jsonData);
                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //CardView userCard = (CardView) findViewById(R.id.card_user_info);
                                try {
                                    JSONObject rootObj = new JSONObject(jsonData);

                                    if (rootObj != null) {
                                        JSONArray results = (JSONArray) rootObj.get("results");
                                        System.out.println(results);

                                        for (int i = 0; i < results.length(); i++) {
                                            JSONObject resultObject = (JSONObject) results.get(i);

                                            array.add(new Data("http://35.184.65.16" + resultObject.get("video_play_file").toString() + "\n", "http://35.184.65.16" + resultObject.get("video_share_location").toString() + "\n"));

                                        }

                                        //updateSwipeCard();
                                        if (array.size() == card_count) {
                                            myAppAdapter = new MyAppAdapter(array,getActivity().getApplicationContext());
                                            flingContainer.setAdapter(myAppAdapter);
                                            myAppAdapter.notifyDataSetChanged();

                                            moreFrame.setVisibility(View.GONE);
                                            QwenuText.setVisibility(View.GONE);
                                            cardFrame.setVisibility(View.VISIBLE);

                                        }

                                    }

                                } catch (JSONException e) {
                                    Log.e("bad", e.getMessage());
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    private void checkRowItem() {
        if (array.isEmpty()) {
            dloop.GenerateData();
        }
    }

    //download video
    private void download() {
        Downback DB = new Downback();
        DB.execute("");

    }


    private class Downback extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            final String vidurl = "Your video url";
            downloadfile(vidurl);
            return null;

        }


    }

    private void downloadfile(String vidurl) {

        SimpleDateFormat sd = new SimpleDateFormat("yymmhh");
        String date = sd.format(new Date());
        String name = "video" + date + ".mp4";

        try {
            String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + File.separator + "My_Video";
            File rootFile = new File(rootDir);
            rootFile.mkdir();
            URL url = new URL(vidurl);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            FileOutputStream f = new FileOutputStream(new File(rootFile,
                    name));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();
        } catch (IOException e) {
            Log.d("Error....", e.toString());
        }


    }

    private static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public PlayerView cardImage;
        public LinearLayoutCompat videoFrame;
        public ImageView previewImage,fullscreenButton,likeDislikeButton,shareButton;
    }

    private class MyAppAdapter extends BaseAdapter implements ExoPlayer.EventListener{

        public List<Data> parkingList;
        public Context context;
        boolean fullscreen = false;


        private MyAppAdapter(List<Data> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            //return parkingList.size();
            return 12;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;
            LayoutInflater inflater = getLayoutInflater();

            if (rowView == null) {

                rowView = inflater.inflate(R.layout.prank_fragment, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText =  rowView.findViewById(R.id.bookText);
                //viewHolder.background =  rowView.findViewById(R.id.background);
                viewHolder.cardImage =rowView.findViewById(R.id.cardImage);
                viewHolder.previewImage=rowView.findViewById(R.id.exo_backgroud);
                viewHolder.fullscreenButton=rowView.findViewById(R.id.exo_fullscreen_icon);
                viewHolder.videoFrame=rowView.findViewById(R.id.videoFrame);
                viewHolder.likeDislikeButton=rowView.findViewById(R.id.like_dislike_button);
                viewHolder.shareButton=rowView.findViewById(R.id.shareButton);
                viewHolder.previewImage.setVisibility(View.GONE);
                viewHolder.fullscreenButton.setEnabled(false);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.DataText.setText(parkingList.get(position).getDescription() + "");
            //love video
            viewHolder.likeDislikeButton.setOnClickListener(new View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),"Liked Video", Toast.LENGTH_SHORT).show();
                    final Drawable d = ContextCompat.getDrawable(context, R.drawable.heart_on);
                    viewHolder.likeDislikeButton.setImageDrawable(d);
                    //viewHolder.likeDislikeButton.performClick();
                    /*
                    final Drawable _d = ContextCompat.getDrawable(context, R.drawable.heart_off);
                            viewHolder.likeDislikeButton.setImageDrawable(_d);
                     */
                }
            });
            //share video
            viewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                //@Override
                public void onClick(View v) {
                    Toast.makeText(getContext(),"Download & Share Video", Toast.LENGTH_SHORT).show();

                    Uri uri = Uri.parse(myAppAdapter.parkingList.get(position).getDescription());
                    File file= new File(uri.getPath());
                    String filename=file.getName();
                    //begin download of selected file
                    DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                    //get url of the selected file for download
                    DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(myAppAdapter.parkingList.get(position).getDescription()));
                    //set tags for downloaded file
                    request1.setDescription("Quetu video file").setTitle(filename).setVisibleInDownloadsUi(true);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request1.allowScanningByMediaScanner();
                        request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                    }
                    request1.allowScanningByMediaScanner();
                    //set destination of dowloaded file
                    //request1.setDestinationInExternalFilesDir(getActivity().getApplicationContext(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), filename);
                    request1.setDestinationInExternalFilesDir(getActivity().getApplicationContext(), "", filename);
                    request1.allowScanningByMediaScanner();
                    downloadManager.enqueue(request1);

                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                long downloadId = intent.getLongExtra(
                                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                                DownloadManager.Query query = new DownloadManager.Query();
                                query.setFilterById(downloadId);
                                Cursor c = downloadManager.query(query);
                                if (c.moveToFirst()) {
                                    int columnIndex = c
                                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                                    if (DownloadManager.STATUS_SUCCESSFUL == c
                                            .getInt(columnIndex)) {

                                        String uriString = c
                                                .getString(c
                                                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                        if (uriString != null) {
                                            File mFile = new File(Uri.parse(uriString).getPath());
                                            Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",mFile);
                                            Intent _intent = new Intent(Intent.ACTION_SEND);
                                            _intent.setDataAndType(uri,"video/*");
                                            _intent.setPackage("com.instagram.android");
                                            _intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            _intent.putExtra(Intent.EXTRA_STREAM,uri);
                                            startActivity(Intent.createChooser(_intent, "Share Video To Instagram"));
                                        }

                                    }
                                }
                            }
                        }
                    };

                    context.registerReceiver(receiver, new IntentFilter(
                            DownloadManager.ACTION_DOWNLOAD_COMPLETE));


                }
            });

           DataSource.Factory dataSourceFactory = new CacheDataSourceFactory(VideoCache.getInstance(context),new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Quetu")));

           //DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "Quetu"));

            for (int i = 0; i < hlsMediaSource.length; i++) {
                Uri newUrl = Uri.parse( myAppAdapter.parkingList.get(i).getImagePath());
                hlsMediaSource[i] = new HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(newUrl);
                //Toast.makeText(getContext(),myAppAdapter.parkingList.get(i).toString(), Toast.LENGTH_SHORT).show();
            }

            // ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource(hlsMediaSource);
            player = new SimpleExoPlayer.Builder(TwoFragment.myAppAdapter.context).build();

            //player.prepare(concatenatingMediaSource);
            player.prepare(hlsMediaSource[position]);
            //player.seekTo(position, C.TIME_UNSET);
            player.seekTo(position);
            player.setPlayWhenReady(false);
            viewHolder.cardImage.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

            //Toast.makeText(getContext(),Integer.toString(player.getCurrentWindowIndex()), Toast.LENGTH_SHORT).show();

            viewHolder.cardImage.setPlayer(player);

            player.addListener(
                    new  ExoPlayer.EventListener(){
                        @Override
                        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {


                            switch(playbackState) {
                                case ExoPlayer.STATE_BUFFERING:
                                    break;
                                case ExoPlayer.STATE_ENDED:
                                    player.release();
                                    break;
                                case ExoPlayer.STATE_IDLE:
                                    break;
                                case ExoPlayer.STATE_READY:
                                    break;
                                default:
                                    break;
                            }
                        }
                        @Override
                        public void onPlayerError(ExoPlaybackException error) {
                            //Toast.makeText(this, "swipe for next video", Toast.LENGTH_SHORT).show();

                        }


                    });



            viewHolder.fullscreenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(fullscreen) {
                        viewHolder.fullscreenButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fullscreen_open));

                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

                        if(((AppCompatActivity)getActivity()).getSupportActionBar() != null){
                            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
                        }

                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                        //LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams) viewHolder.cardImage.getLayoutParams();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.videoFrame.getLayoutParams();
                        params.width = params.MATCH_PARENT;
                        params.height = (int) ( 250 * myAppAdapter.context.getApplicationContext().getResources().getDisplayMetrics().density);
                        viewHolder.videoFrame.setLayoutParams(params);

                        fullscreen = false;
                    }else{
                        viewHolder.fullscreenButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fullscreen_close));

                        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                                |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                        if(((AppCompatActivity)getActivity()).getSupportActionBar()!= null){
                            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
                        }
                        DisplayMetrics metrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                        Display d = getActivity().getWindowManager().getDefaultDisplay();
                        int heightPixels = Math.max(d.getWidth(),d.getHeight());

                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                        //LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) flingContainer.getLayoutParams();

                        //LinearLayoutCompat.LayoutParams params = (LinearLayoutCompat.LayoutParams) viewHolder.cardImage.getLayoutParams();
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.videoFrame.getLayoutParams();
                        params.width =params.MATCH_PARENT;
                        params.height = viewHolder.cardImage.getWidth()*2;
                        //Toast.makeText(getContext(),Integer.toString(heightPixels), Toast.LENGTH_SHORT).show();
                        viewHolder.videoFrame.setLayoutParams(params);
                        //flingContainer.setLayoutParams(params);

                        fullscreen = true;
                    }
                }
            });
            // Inflate the layout for this fragment
            flingContainer.setOnScrollListener(new AbsListView.OnScrollListener() {
                private int currentVisibleItemCount;
                private int currentScrollState;
                private int currentFirstVisibleItem;
                private int totalItem;
                private LinearLayout lBelow;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    this.currentScrollState = scrollState;
                    this.isScrollCompleted();
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    this.currentFirstVisibleItem = firstVisibleItem;
                    this.currentVisibleItemCount = visibleItemCount;
                    this.totalItem = totalItemCount;


                }
                private void isScrollCompleted() {
                    if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                            && this.currentScrollState == SCROLL_STATE_IDLE) {
                        /** To do code here*/
                        player.release();
                        array.clear();
                        dloop = new TwoFragment.Dataloop();
                        dloop.GenerateData();
                    }
                }
            });

            return rowView;
        }
    }
}
