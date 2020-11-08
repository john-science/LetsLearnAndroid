package net.antineutrino.sqlitedemo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    // other class attributes
    ArrayAdapter <CustomerModel> customerAA;
    DAL dal;

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
        dal = new DAL(MainActivity.this);

        updateCustomerList();

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
                updateCustomerList();
            }
        });

        btn_viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCustomerList();
            }
        });

        lv_customer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomerModel clickedCustomer = (CustomerModel) parent.getItemAtPosition(position);
                dal.deleteOne(clickedCustomer);
                updateCustomerList();
                Toast.makeText(MainActivity.this, "Deleted: " + clickedCustomer.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // display the full customer list in the UI
    private void updateCustomerList() {
        customerAA = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, dal.getAll());
        lv_customer_list.setAdapter(customerAA);
    }
}