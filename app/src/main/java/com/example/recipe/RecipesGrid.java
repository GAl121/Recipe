package com.example.recipe;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class RecipesGrid extends BaseAdapter {

    private Context context;
    private ArrayList<String> recipeName = new ArrayList<String>();
    private final ArrayList<String> categoryOrUser = new ArrayList<String>();
    private final ArrayList<String> usersNames = new ArrayList<String>();
    private ArrayList<Bitmap> imageCategory = new ArrayList<Bitmap>();
    private LayoutInflater layoutInflater;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public RecipesGrid(Context context, ArrayList<Recipe> recipes,ArrayList<String> names, ArrayList<Bitmap> imageCategory){
        this.context = context;

        this.categoryOrUser.addAll(names);
        for (Recipe r : recipes)
        {
            this.recipeName.add(r.name);
        }
        this.imageCategory.addAll(imageCategory);
        this.layoutInflater=(LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return recipeName.size();
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
            view = layoutInflater.inflate(R.layout.item_grids,viewGroup, false);
        }
        ImageView imageView = view.findViewById(R.id.imageview);
        TextView textView = view.findViewById(R.id.textview);
        TextView textView1=view.findViewById(R.id.textview1);
        imageView.setImageBitmap(imageCategory.get(position));
        textView.setText(categoryOrUser.get(position));
        textView1.setText(recipeName.get(position));

        return view;
    }
}
