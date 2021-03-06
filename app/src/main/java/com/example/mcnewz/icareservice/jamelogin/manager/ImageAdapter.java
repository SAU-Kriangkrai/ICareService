package com.example.mcnewz.icareservice.jamelogin.manager;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.example.mcnewz.icareservice.R;


/**
 * Created by JAME on 19-Dec-16.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private Integer[] mImageIds ={
            R.drawable.test1,
            R.drawable.test2,
            R.drawable.test3
    };

    public ImageAdapter(Context c){
        mContext = c;
    }


    @Override
    public int getCount() {
        return mImageIds.length;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(mImageIds[i]);
        imageView.setLayoutParams(new Gallery.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return imageView;
    }
}
