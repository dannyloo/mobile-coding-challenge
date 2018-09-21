package com.tradeRev.PhotoClientAppDanny;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.tradeRev.PhotoClientAppDanny.R.id.gridView;

public class GridViewActivity extends ActionBarActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    private String FEED_URL = "https://api.unsplash.com/photos/curated/?client_id=55d34af08d928ec06dac447c8e11b58311734cc651f78f62029373afb74a5152";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mGridView = (GridView) findViewById(gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

//        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
//        String [] imageURLs = mGridData.toArray(new String[0]);
//        mViewAdapter = new ViewPagerAdapter(this, imageURLs);
//        viewPager.setAdapter(mViewAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(GridViewActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                // Interesting data to pass across are the thumbnail size/location, the
                // resourceId of the source bitmap, the picture description, and the
                // orientation (to avoid returning back to an obsolete configuration if
                // the device rotates again in the meantime)

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImageRaw());
                intent.putExtra("imageArray", mGridData);

                //Start details activity
                startActivity(intent);
            }
        });

        //Start download
        new AsyncHttpTask().execute(FEED_URL);
        mProgressBar.setVisibility(View.VISIBLE);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if(firstVisibleItem + visibleItemCount >= totalItemCount){
                    // End has been reached
                    Toast.makeText(getApplicationContext(), "This is my Toast message!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState){

            }
        });
    }




    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    String response = streamToString(httpResponse.getEntity().getContent());
                    parseResult(response);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }


    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     *
     * @param result
     */
    private void parseResult(String result) {
        System.out.println("results: " + result);
        try {
            JSONArray response = new JSONArray(result);
            //JSONArray posts = response.optJSONArray("posts");
            GridItem item;
            for (int i = 0; i < response.length(); i++) {
                JSONObject post = response.optJSONObject(i);
                String title = post.optString("description");

                item = new GridItem();
                item.setTitle(title);
                System.out.println("Title: " + item.getTitle());
                if(item.getTitle() == "null")
                    item.setTitle("No description available");
                JSONObject urlRaw = (JSONObject) response.get(i);
                JSONObject urlSmall = (JSONObject) response.get(i);
                item.setImage(urlSmall.getJSONObject("urls").getString("small"));
                item.setImageRaw(urlRaw.getJSONObject("urls").getString("regular"));

                System.out.println("URL: " + item.getImage());

                mGridData.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("urls are: " + mGridData.get(0).getImageRaw());

    }

}