package com.lwd.routerdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.lwd.router_annotations.Parameter;
import com.lwd.router_annotations.Router;
import com.lwd.router_api.ParameterManager;
import com.lwd.router_api.RouterManager;

@Router(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Parameter
    int age;

    @Parameter
    boolean isBoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParameterManager.getInstance().loadParameter(this);

    }

    public void jumpModule1(View view) {
        RouterManager.getInstance().build("/module1/Module1Activity")
                .withInt("age", 18)
                .navigation(this);
    }

    public void jumpModule2(View view) {
        RouterManager.getInstance().build("/module2/Module2Activity")
                .withString("name", "彭于晏")
                .navigation(this);
    }
}