package net.antineutrino.sqlitedemo;
import java.util.List;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    // references to all of the buttons and controls on the layout
    Button btn_add, btn_viewAll;
    EditText edtName, edtAge;
    Switch sw_active;
    ListView lv_customer_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_add = findViewById(R.id.btn_add);
        btn_viewAll = findViewById(R.id.btn_viewAll);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        sw_active = findViewById(R.id.sw_active);
        lv_customer_list = findViewById(R.id.lv_customerList);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerModel cust;

                try {
                    cust = new CustomerModel(-1, edtName.getText().toString(), Integer.parseInt(edtAge.getText().toString()), sw_active.isChecked());
                } catch (Exception e) {
                    cust = new CustomerModel(-1, "error", 0, false);
                }

                DAL dal = new DAL(MainActivity.this);
                boolean success = dal.addOne(cust);
                if (success) {
                    Toast.makeText(MainActivity.this, "Successfully inserted into the DB", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DAL dal = new DAL(MainActivity.this);
                List <CustomerModel> everyone = dal.getAll();
                Toast.makeText(MainActivity.this, everyone.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }
}