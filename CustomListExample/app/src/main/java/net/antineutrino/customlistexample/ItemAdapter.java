package net.antineutrino.customlistexample;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    String[] items;
    String[] prices;
    String[] descriptions;

    public ItemAdapter(Context context, String[] i, String[] p, String[] d) {
        items = i;
        prices = p;
        descriptions = d;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = mInflater.inflate(R.layout.my_listview_layout, null);
        TextView textViewName = (TextView) v.findViewById(R.id.textViewName);
        TextView textViewPrice = (TextView) v.findViewById(R.id.textViewPrice);
        TextView textViewDesc = (TextView) v.findViewById(R.id.textViewDesc);

        String name = items[position];
        String cost = prices[position];
        String desc = descriptions[position];

        textViewName.setText(name);
        textViewPrice.setText(cost);
        textViewDesc.setText(desc);

        return v;
    }
}
