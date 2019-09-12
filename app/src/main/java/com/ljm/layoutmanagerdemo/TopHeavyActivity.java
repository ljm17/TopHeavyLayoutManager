package com.ljm.layoutmanagerdemo;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ljm.topheavylayoutmanager.TopHeavyLayoutManager;
import com.ljm.topheavylayoutmanager.TopSnapHelper;
import com.ljm.layoutmanagerdemo.utils.DimensionUtil;

public class TopHeavyActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayout mLinearLayout;
    private TextView mTextView;
    private Button mPreButton;
    private Button mNextButton;
    private boolean mInfinite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_heavy);
        mRecyclerView = findViewById(R.id.top_heavy_rv);
        mLinearLayout = findViewById(R.id.msg_ll);
        mTextView = findViewById(R.id.title_tv);
        mPreButton = findViewById(R.id.pre_bt);
        mNextButton = findViewById(R.id.next_bt);

        initRecyclerView();
        initClick();
    }

    private void initRecyclerView() {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.width = DimensionUtil.getWidthPixel(this) - DimensionUtil.dp2valueInt(this, 150) - 60;
        mLinearLayout.setLayoutParams(params);

        Intent intent = getIntent();
        mInfinite = intent.getBooleanExtra("infinite", false);

        mRecyclerView.setLayoutManager(new TopHeavyLayoutManager());
        mRecyclerView.setAdapter(new TopHeavyAdapter(mInfinite ? Integer.MAX_VALUE : 6));
        //设置 开始页对齐snapHelper，限制filing滑动最多2页
        TopSnapHelper helper = new TopSnapHelper(2);
        helper.attachToRecyclerView(mRecyclerView);
        //无限循环时，默认第一个 item 的 position 是 Integer.MAX_VALUE / 2，若需调整可用scrollToPosition
        if (mInfinite) {
            mRecyclerView.scrollToPosition((Integer.MAX_VALUE >> 1) - (Integer.MAX_VALUE >> 1) % 6);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mInfinite) {
                    //通过取余计算出当前真实的position
                    mTextView.setText(String.format(
                            "Title%s", recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0)) % 6));
                } else {
                    //不是无限循环当前position即可
                    mTextView.setText(String.format(
                            "Title%s", recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0))));
                }
            }
        });
    }

    private void initClick() {
        mPreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0)) - 1;
                mRecyclerView.smoothScrollToPosition(pos);
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mRecyclerView.getChildAdapterPosition(mRecyclerView.getChildAt(0)) + 1;
                mRecyclerView.smoothScrollToPosition(pos);
            }
        });
    }
}
