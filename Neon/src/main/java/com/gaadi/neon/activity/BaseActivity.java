package com.gaadi.neon.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.util.FileInfo;
import com.scanlibrary.R;

import java.util.HashMap;
import java.util.List;

/**
 * @author lakshaygirdhar
 * @since 13-08-2016
 * @version 1.0
 *
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected FrameLayout frameLayout;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setTitleMsg(String msg) {
        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(msg);
        setTitle(msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
