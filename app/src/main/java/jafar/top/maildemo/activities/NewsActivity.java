package jafar.top.maildemo.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jafar.top.maildemo.R;
import jafar.top.maildemo.fragments.AnotherRightFragment;

public class NewsActivity extends AbstractActivity {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        button = $(R.id.button);
        D("初始化完成");
        setOnClickListener(button);
    }

    @Override
    protected void initDao() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                D("替换fragment");
                replaceFragment(R.id.right_layout, new AnotherRightFragment(), true);
                break;
        }

    }

}
