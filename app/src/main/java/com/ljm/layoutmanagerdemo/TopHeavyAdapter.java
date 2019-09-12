package com.ljm.layoutmanagerdemo;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

public class TopHeavyAdapter extends RecyclerView.Adapter<TopHeavyAdapter.TopHeavyHolder> {

    private static final String TAG = "TopHeavyAdapter";

    private int[] pictures = {R.drawable.test_1, R.drawable.test_2,
            R.drawable.test_3, R.drawable.test_4, R.drawable.test_5, R.drawable.test_6};
    private RequestOptions roundOptions = new RequestOptions()
            .transform(new CenterCrop(), new RoundedCorners(30));
    private int mItemCount;
    private int mHolderCount;

    public TopHeavyAdapter(int itemCount) {
        mItemCount = itemCount;
    }

    @NonNull
    @Override
    public TopHeavyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mHolderCount++;
        Log.i(TAG, "onCreateViewHolder: " + mHolderCount);

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_image, viewGroup, false);
        return new TopHeavyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TopHeavyHolder topHeavyHolder, int i) {
        Log.i(TAG, "onBindViewHolder: ");

        ImageView view = topHeavyHolder.imageView;
        Glide.with(view.getContext())
                .load(pictures[i % 6])
                .apply(roundOptions)
                .into(view);

        topHeavyHolder.itemView.setTag(i % 6);
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    public class TopHeavyHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public TopHeavyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recycler_item_iv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), String.format("点击了%s", (int)v.getTag()), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
