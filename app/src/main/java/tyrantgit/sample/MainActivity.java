package tyrantgit.sample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import tyrantgit.explosionfield.ExplosionField;
import tyrantgit.explosionfield.Utils;


public class MainActivity extends Activity implements View.OnTouchListener {

    private ExplosionField mExplosionField;
    private View root;
    private int size = Utils.dp2Px(50);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mExplosionField = ExplosionField.attach2Window(this);
        root = findViewById(R.id.root);
        addListener(root);
    }

    private void addListener(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                addListener(parent.getChildAt(i));
            }
        } else {
            root.setClickable(true);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExplosionField.explode(v);
                    v.setOnClickListener(null);
                    v.setClickable(false);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            View root = findViewById(R.id.root);
            reset(root);
            addListener(root);
            mExplosionField.clear();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reset(View root) {
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                reset(parent.getChildAt(i));
            }
        } else {
            root.setScaleX(1);
            root.setScaleY(1);
            root.setAlpha(1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        root.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        root.setOnTouchListener(null);
    }

    float lastX, lastY;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Bitmap tmp = BitmapFactory.decodeResource(getResources(), R.drawable.p5);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastX = event.getX(); lastY = event.getY();
            if (tmp == null) return true;
            int x = (int) lastX, y = (int) lastY;
            Rect r = new Rect(x - size, y - size, x + size, y + size);
            mExplosionField.explode(tmp, r, 0, 0x300);
            return true;
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            float distance = getDistance(lastX, lastY, event);
            if (distance < size/2) {
                // let's not explode for every move you make !!
                // limit the explosion to a movement distance of half the explosion size
                return false;
            }
            lastX = event.getX(); lastY = event.getY();
            if (tmp == null) return true;
            int x = (int) lastX, y = (int) lastY;
            Rect r = new Rect(x - size, y - size, x + size, y + size);
            mExplosionField.explode(tmp, r, 0, 0x300);
            return true;
        }
        return false;
    }

    float getDistance(float startX, float startY, MotionEvent ev) {
        float distanceSum = 0;
        final int historySize = ev.getHistorySize();
        for (int h = 0; h < historySize; h++) {
            // historical point
            float hx = ev.getHistoricalX(0, h);
            float hy = ev.getHistoricalY(0, h);
            // distance between startX,startY and historical point
            float dx = (hx - startX);
            float dy = (hy - startY);
            distanceSum += Math.sqrt(dx * dx + dy * dy);
            // make historical point the start point for next loop iteration
            startX = hx;
            startY = hy;
        }
        // add distance from last historical point to event's point
        float dx = (ev.getX(0) - startX);
        float dy = (ev.getY(0) - startY);
        distanceSum += Math.sqrt(dx * dx + dy * dy);
        return distanceSum;
    }
}
