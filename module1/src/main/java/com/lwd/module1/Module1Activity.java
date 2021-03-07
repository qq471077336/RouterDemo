package com.lwd.module1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.lwd.router_annotations.Parameter;
import com.lwd.router_annotations.Router;
import com.lwd.router_api.ParameterManager;

@Router(path = "/module1/Module1Activity")
public class Module1Activity extends AppCompatActivity {

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module1);

        ParameterManager.getInstance().loadParameter(this);

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText("年龄 = " + age);
    }
}