package com.aseantech.nghiacao.arduinospeech.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.aseantech.nghiacao.arduinospeech.R;
import com.aseantech.nghiacao.arduinospeech.models.Slide;
import java.util.List;

public class SlidePagerAdapter extends PagerAdapter {
    private Context mContext;
    private List<Slide> mListSlides;

    public SlidePagerAdapter(Context mContext, List<Slide> mListSlides) {
        this.mContext = mContext;
        this.mListSlides = mListSlides;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View slideLayout = inflater.inflate(R.layout.slide_item,null);

        ImageView slideImage = slideLayout.findViewById(R.id.slide_image);
        slideImage.setImageResource(mListSlides.get(position).getImage());

        container.addView(slideLayout);
        return slideLayout;
    }

    @Override
    public int getCount() {
        return mListSlides.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
