package com.lwd.moduleb;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.lwd.router_annotations.Parameter;
import com.lwd.router_annotations.Router;
import com.lwd.router_api.ParameterManager;

@Router(path = "/module2/Module2Activity")
public class Module2Activity extends AppCompatActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module2);

        ParameterManager.getInstance().loadParameter(this);

        TextView tv = (TextView) findViewById(R.id.tv);
        tv.setText("名字 = " + name);
    }
}