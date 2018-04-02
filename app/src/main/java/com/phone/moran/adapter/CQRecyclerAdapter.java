package com.phone.moran.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phone.moran.R;
import com.phone.moran.model.ClassicQuote;
import com.phone.moran.tools.ImageLoader;
import com.phone.moran.view.roundedimageview.RoundedImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ASUS on 2017/11/9.
 * <p/>
 * 画单列表的recycler
 */
public class CQRecyclerAdapter extends BaseRecyclerAdapter<ClassicQuote> {



    public CQRecyclerAdapter(Context context, List<ClassicQuote> datas) {
        super(context, datas);
    }

    @Override
    public BaseRecyclerHolder createViewHoldeHolder(ViewGroup parent, int viewType) {
        return new PictureViewHolder(mInflater.inflate(R.layout.cq_list_item, parent, false));
    }

    @Override
    public void showViewHolder(BaseRecyclerHolder holder, final int position) {

        final PictureViewHolder mHolder = (PictureViewHolder) holder;
        final ClassicQuote cq = mData.get(position);

        ImageLoader.displayImg(mContext, cq.getCq_img_url(), mHolder.cqImage);
        mHolder.cqTitle.setText(cq.getCq_title());
        mHolder.cqText.setText(cq.getCq_content());

        if (itemClickListener != null) {
            mHolder.cqLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v, position, cq);

                }
            });
        }
    }

    public class PictureViewHolder extends BaseRecyclerHolder {
        @BindView(R.id.cq_image)
        RoundedImageView cqImage;
        @BindView(R.id.cq_title)
        TextView cqTitle;
        @BindView(R.id.cq_text)
        TextView cqText;
        @BindView(R.id.cq_LL)
        LinearLayout cqLL;
        View v;

        public PictureViewHolder(View itemView) {
            super(itemView);
            this.v = itemView;
            ButterKnife.bind(this, itemView);

        }
    }

}
