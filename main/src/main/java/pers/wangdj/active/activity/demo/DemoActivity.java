package pers.wangdj.active.activity.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import pers.wangdj.active.R;
import pers.wangdj.active.activity.pub.BaseActivity;
import pers.wangdj.active.utils.Constants;

/**
 * 项目名：  AndroidCommonProject
 * 包名：    pers.wangdj.active.activity
 * 文件名：  DemoActivity
 * 创建者：  wangdja
 * 创建时间：2018-01-17  10:18 上午
 * 描述：    演示样例
 */

public class DemoActivity extends BaseActivity implements View.OnClickListener {


    protected Button btnDemoButton;
    protected Button btnDemoButtonForResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo_layout);
        initView();
        initListener();
        initData();

    }

    @Override
    protected void initView() {
        btnDemoButton = findViewById(R.id.btn_demo_button);
        btnDemoButton.setOnClickListener(DemoActivity.this);
        btnDemoButtonForResult = findViewById(R.id.btn_demo_button_for_result);
        btnDemoButtonForResult.setOnClickListener(DemoActivity.this);


    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        btnDemoButton.setText(R.string.startActivity);
        btnDemoButtonForResult.setText(R.string.startActivityForResult);
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_demo_button) {
//            startActivity(actionStart("464655465555", "2641"));
            showToast("wangsfdsfsdfx",true);
        } else if (view.getId() == R.id.btn_demo_button_for_result) {

            startActivityForResult(actionStartForResult("464655465555", "2641"), Constants.requestCode350);
        }
    }

    Intent actionStart(String billno, String transtype) {
        Intent intent = new Intent(this, SecondDemoActivity.class);
        intent.putExtra("billno", billno);
        intent.putExtra("transtype", transtype);
        return intent;
    }

    Intent actionStartForResult(String billno, String transtype) {
        Intent intent = new Intent(this, SecondDemoActivity.class);
        intent.putExtra("billno", billno);
        intent.putExtra("transtype", transtype);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.requestCode350 && resultCode == Constants.resultCode340){
            btnDemoButton.setText(data.getExtras().getString("billtype"));
        }
    }




}
