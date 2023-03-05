package st0rm.ireels.Activity;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import st0rm.ireels.Helper.Roasty;
import st0rm.ireels.R;



public class Home extends AppCompatActivity {


//    VIEWS
    private Button pasteBTN, downloadBTN;
    private ImageView clearBTN, refreshBTN, activityBackground;
    private EditText uriETV;


//    CLIPBOARD MANAGER
    private ClipboardManager clipBoardManager;


//    PROGRESS DIALOG
    private ProgressDialog progressDialog;


//    URL LIST
    Map<String, String> post_uri_list = new HashMap<>();


//    DOWNLOAD MANAGER
    private DownloadManager downloadManager;


//    ARRAY
    private String[] arrayBG;


    @Override
    protected void onRestart() {
        super.onRestart();
        initViews();
        initGlide();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        initGlide();
        getWindow().getDecorView().setSystemUiVisibility(0);

        if (getIntent().getClipData() != null)
            getURI(getIntent().getClipData().toString());


        refreshBTN.setOnClickListener(v -> initGlide());

        clearBTN.setOnClickListener(v -> uriETV.setText(null));

        pasteBTN.setOnClickListener(v -> uriETV.setText(getClipBoardText()));

        downloadBTN.setOnClickListener(v -> {
            if (isInternetAvailable())
                getURI(uriETV.getText().toString());
        });


        uriETV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count <= 0)
                    clearBTN.setVisibility(View.GONE);
                else clearBTN.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}

        });


    }

    private void initGlide() {

        Glide.with(this)
                .load(arrayBG[new Random().nextInt(arrayBG.length)])
                .into(activityBackground);

    }


    private void downloadFile() {

        if (!post_uri_list.isEmpty()) {

            for (Map.Entry<String, String> entry : post_uri_list.entrySet()) {
                String fileName = entry.getKey();
                String uri = entry.getValue();

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(true);
                request.setTitle("iReel-" + fileName);
                request.setDescription("Downloading...");
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                downloadManager.enqueue(request);

            }

            Roasty.showToast(this, -1, "Download Started!");
            progressDialog.dismiss();
            post_uri_list.clear();

        }

    }




    private void getURI(String text) {

        if (text.isEmpty())
            Roasty.showToast(this, R.drawable.ic_warning, "NO LINK FOUND!  🐾");

        else {

            if (text.contains("instagram")) {

                progressDialog.show();
                new Handler().postDelayed(() -> getURIList(text), 2000);

            } else Roasty.showToast(this, R.drawable.ic_warning, "Make sure you've copied the link from Instagram.");

        }

    } // getURI()



    private void getURIList(String stringData) {

//        ArrayList<String> post_uri_list = new ArrayList<>();
        progressDialog.show();

        if (stringData.matches("https://www.instagram.com/(.*)")) {

            String[] data = stringData.split(Pattern.quote("?"));
            String string = data[0];

            if (isInternetAvailable()) {

                AsyncHttpClient client = new AsyncHttpClient();
                client.addHeader("Accept", "application/json");
                client.addHeader("Content-Type", "application/json;charset=UTF-8");
                client.addHeader("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
                client.addHeader("x-requested-with", "XMLHttpRequest");
                client.get(string + "?__a=1&__d=dis", null, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        String res = new String(responseBody);

                        try {

                            JSONObject jsonObject = new JSONObject(res);
                            String link;

                            JSONObject objectGraphql = jsonObject.getJSONObject("graphql");
                            JSONObject objectMedia = objectGraphql.getJSONObject("shortcode_media");
                            boolean isVideo = objectMedia.getBoolean("is_video");
                            if (isVideo) {
                                link = objectMedia.getString("video_url");
                                post_uri_list.put(new SimpleDateFormat(getResources()
                                                .getString(R.string.date_format), Locale.getDefault())
                                                .format(new Date()) + new Random().nextInt(1000) + ".mp4", link);
                            }
                            else {
                                link = objectMedia.getString("display_url");
                                post_uri_list.put(new SimpleDateFormat(getResources()
                                        .getString(R.string.date_format), Locale.getDefault())
                                        .format(new Date()) + new Random().nextInt(1000) + ".jpg", link);
                            }


                            try {

                                JSONObject objectSidecar = objectMedia.getJSONObject("edge_sidecar_to_children");
                                JSONArray jsonArray = objectSidecar.getJSONArray("edges");

                                post_uri_list.clear();

                                String multiLinks;

                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject object = jsonArray.getJSONObject(i);
                                    JSONObject node = object.getJSONObject("node");
                                    boolean is_video_group = node.getBoolean("is_video");
                                    if (is_video_group) {
                                        multiLinks = node.getString("video_url");
                                        post_uri_list.put(new SimpleDateFormat(getResources()
                                                .getString(R.string.date_format), Locale.getDefault())
                                                .format(new Date()) + new Random().nextInt(1000) + ".mp4", multiLinks);
                                    }
                                    else {
                                        multiLinks = node.getString("display_url");
                                        post_uri_list.put(new SimpleDateFormat(getResources()
                                                .getString(R.string.date_format), Locale.getDefault())
                                                .format(new Date()) + new Random().nextInt(1000) + ".jpg", multiLinks);
                                    }

                                }

                            } catch (Exception e) {
                                Log.e("error_show", e.toString());
                            }

                            downloadFile();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Roasty.showToast(Home.this, R.drawable.ic_warning, statusCode + " - JSON Exception");
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Roasty.showToast(Home.this, R.drawable.ic_warning, statusCode + " - " + error);
                    }
                });

            } else Roasty.showToast(Home.this, R.drawable.ic_warning, "NO INTERNET CONNECTION!");
        }

    }







    private String getClipBoardText() {
        return clipBoardManager.getPrimaryClip().getItemAt(0).getText().toString();
    } // getClipBoardText()


    private void initViews() {

//        VIEWS
        pasteBTN = findViewById(R.id.pasteBTN);
        downloadBTN = findViewById(R.id.downloadBTN);
        clearBTN = findViewById(R.id.clearBTN);
        uriETV = findViewById(R.id.uriETV);
        refreshBTN = findViewById(R.id.refreshBTN);
        activityBackground = findViewById(R.id.activityBackground);
        arrayBG = getResources().getStringArray(R.array.BG);

//        CLIPBOARD MANAGER
        clipBoardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

//        PROGRESS DIALOG
        progressDialog = new ProgressDialog(Home.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Getting POST For YOUUU..");

//        DOWNLOAD MANAGER
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

    } // initViews()


    public boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    } // isInternetAvailable()


}