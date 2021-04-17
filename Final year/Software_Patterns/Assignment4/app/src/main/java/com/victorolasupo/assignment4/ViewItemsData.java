package com.victorolasupo.assignment4;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ViewItemsData extends AppCompatActivity {
    ListView listView;
    listadapter adapter;
    TextView textView;
    database databased;
    Cursor data;
    String itemName, itemPrice;

    @Override
    protected void onCreate(Bundle savedInstanceSftate) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_items_data);
        listView = findViewById(R.id.listviewmain);
        textView = findViewById(R.id.textv);
        databased = new database(com.diroidd.app.shopping.ViewItemsData.this);
        data = databased.show_data();

        adapter = new listadapter(com.diroidd.app.shopping.ViewItemsData.this, data, "j");
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (databased.getProfilesCount() == 0) {
            listView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                data = (Cursor) listView.getItemAtPosition(position);
                String id = data.getString(data.getColumnIndexOrThrow("_id"));
                itemName = data.getString(data.getColumnIndexOrThrow("itemname"));
                itemPrice = data.getString(data.getColumnIndexOrThrow("itemprice"));
                Toast.makeText(com.diroidd.app.shopping.ViewItemsData.this, itemName + "\n" + itemPrice, Toast.LENGTH_SHORT).show();
            }
        });
    }
}