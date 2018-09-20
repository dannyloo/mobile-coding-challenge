package com.tradeRev.PhotoClientAppDanny;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DetailsActivity extends ActionBarActivity {
    private static final int ANIM_DURATION = 600;
    private TextView titleTextView;
    private ImageView imageView;

    private int mLeftDelta;
    private int mTopDelta;
    private float mWidthScale;
    private float mHeightScale;

    private FrameLayout frameLayout;
    private ColorDrawable colorDrawable;

    private int thumbnailTop;
    private int thumbnailLeft;
    private int thumbnailWidth;
    private int thumbnailHeight;
    ArrayList<GridItem> urlList;
    Context context;
    String image = "";
    String title = "";

    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //Setting details screen layout
        setContentView(R.layout.activity_details_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //retrieves the thumbnail data
        Bundle bundle = getIntent().getExtras();
        thumbnailTop = bundle.getInt("top");
        thumbnailLeft = bundle.getInt("left");
        thumbnailWidth = bundle.getInt("width");
        thumbnailHeight = bundle.getInt("height");

        title = bundle.getString("title");
        image = bundle.getString("image");
        urlList = (ArrayList<GridItem>) getIntent().getSerializableExtra("imageArray");

        if (savedInstanceState != null){
            String savedImage = savedInstanceState.getString(image);
            int savedIndex = savedInstanceState.getInt("index");
            index = savedIndex;
            image = urlList.get(index).getImageRaw();
            title = urlList.get(index).getTitle();

        }

        for(int i = 0; i < urlList.size(); i++){
            if(urlList.get(i).getImageRaw().equals(image))
                index = i;
        }
        System.out.println("index is: " + index);

        //initialize and set the image description
        titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(Html.fromHtml(title));

        //Set image url
        imageView = (ImageView) findViewById(R.id.grid_item_image);
        Picasso.with(this).load(image).into(imageView);
        System.out.println("testeroo " + image);

        //Set the background color to black
        frameLayout = (FrameLayout) findViewById(R.id.main_background);
        colorDrawable = new ColorDrawable(Color.BLACK);
        frameLayout.setBackground(colorDrawable);

        // Only run the animation if we're coming from the parent activity, not if
        // we're recreated automatically by the window manager (e.g., device rotation)
        if (true) {
            ViewTreeObserver observer = imageView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    // Figure out where the thumbnail and full size versions are, relative
                    // to the screen and each other
                    int[] screenLocation = new int[2];
                    imageView.getLocationOnScreen(screenLocation);
                    mLeftDelta = thumbnailLeft - screenLocation[0];
                    mTopDelta = thumbnailTop - screenLocation[1];

                    // Scale factors to make the large version the same aspect ratio as the thumbnail
                    mWidthScale = (float) thumbnailWidth / imageView.getWidth();
                    mHeightScale = (float) thumbnailHeight / imageView.getHeight();

                    enterAnimation();

                    return true;
                }
            });
        }
    }

    /**
     * The enter animation scales the picture in from its previous thumbnail
     * size/location.
     */
    public void enterAnimation() {

        // Set starting values for properties we're going to animate. These
        // values scale and position the full size version down to the thumbnail
        // size/location, from which we'll animate it back up
        imageView.setPivotX(0);
        imageView.setPivotY(0);
        imageView.setScaleX(mWidthScale);
        imageView.setScaleY(mHeightScale);
        imageView.setTranslationX(mLeftDelta);
        imageView.setTranslationY(mTopDelta);

        // interpolator where the rate of change starts out quickly and then decelerates.
        TimeInterpolator sDecelerator = new DecelerateInterpolator();

        // Animate scale and translation to go from thumbnail to full size
        imageView.animate().setDuration(ANIM_DURATION).scaleX(1).scaleY(1).
                translationX(0).translationY(0).setInterpolator(sDecelerator);

        // Fade in the black background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(colorDrawable, "alpha", 0, 255);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();

        imageView.setOnTouchListener(new OnSwipeTouchListener(DetailsActivity.this) {
            public void onSwipeTop() {
            }
            public void onSwipeRight() {
                index--;
                if(index < 0)
                    index = 0;

                titleTextView = (TextView) findViewById(R.id.title);
                titleTextView.setText(Html.fromHtml(urlList.get(index).getTitle()));
                Picasso.with(context).load(urlList.get(index).getImageRaw()).into(imageView);

            }
            public void onSwipeLeft() {
                index++;
                if (index > 9)
                    index = 9;

                titleTextView = (TextView) findViewById(R.id.title);
                titleTextView.setText(Html.fromHtml(urlList.get(index).getTitle()));
                Picasso.with(context).load(urlList.get(index).getImageRaw()).into(imageView);


            }
            public void onSwipeBottom() {
            }

        });


    }

    /**
     * The exit animation is basically a reverse of the enter animation.
     * This Animate image back to thumbnail size/location as relieved from bundle.
     *
     * @param endAction This action gets run after the animation completes (this is
     *                  when we actually switch activities)
     */
    public void exitAnimation(final Runnable endAction) {

        TimeInterpolator sInterpolator = new AccelerateInterpolator();
        imageView.animate().setDuration(ANIM_DURATION).scaleX(mWidthScale).scaleY(mHeightScale).
                translationX(mLeftDelta).translationY(mTopDelta)
                .setInterpolator(sInterpolator).withEndAction(endAction);

        // Fade out background
        ObjectAnimator bgAnim = ObjectAnimator.ofInt(colorDrawable, "alpha", 0);
        bgAnim.setDuration(ANIM_DURATION);
        bgAnim.start();
    }

    @Override
    public void onBackPressed() {
        exitAnimation(new Runnable() {
            public void run() {
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(image,urlList.get(index).getImageRaw());
        outState.putInt("index",index);
    }
}
