package com.example.recipe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class GridAdapter extends BaseAdapter {
    private Context context;
    private final String[] category;
    private final int[] imageCategory;
    private LayoutInflater layoutInflater;


    public GridAdapter(Context context, String[] category, int[] imageCategory) {
        this.context = context;
        this.category = category;
        this.imageCategory = imageCategory;
        this.layoutInflater=(LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return category.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = layoutInflater.inflate(R.layout.single_item,viewGroup, false);
        }
        ImageView imageView = view.findViewById(R.id.imageview);
        TextView textView = view.findViewById(R.id.textview);
        imageView.setImageResource(imageCategory[position]);
        textView.setText(category[position]);

        return view;
    }
}
