package com.victorolasupo.assignment4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class InsertData extends AppCompatActivity {
    String getName;
    TextView DisplayName;
    Button Insertitem, ViewItems,RemoveItems,CheckOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_data);
        getName = getIntent().getStringExtra("getname");
        DisplayName = findViewById(R.id.DisplayUserName);
        Insertitem = findViewById(R.id.inseritem);
        ViewItems = findViewById(R.id.viewItems);
        RemoveItems=findViewById(R.id.RemoveItems);
        CheckOut=findViewById(R.id.CheckOut);
        DisplayName.setText(getName);nsertitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.diroidd.app.shopping.InsertData.this, InsertItemsToList.class));
            }
        });
        ViewItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.diroidd.app.shopping.InsertData.this, ViewItemsData.class));
            }
        });
        RemoveItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.diroidd.app.shopping.InsertData.this, RemoveItemFromList.class));
            }
        });
        CheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.diroidd.app.shopping.InsertData.this, CheckOut.class));
            }
        });

        Insertitem = findViewById(R.id.inseritem);
        ViewItems = findViewById(R.id.viewItems);
        RemoveItems=findViewById(R.id.RemoveItems);
        CheckOut=findViewById(R.id.CheckOut);

        inseritem
    }

}