package com.hippo.ehviewer.ui.scene.gallery.list;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.widget.TileThumbNew;
import com.hippo.widget.ContentLayout;
import com.hippo.widget.recyclerview.AutoStaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Offline fixture using the homepage's real card layouts and layout manager. */
public class GalleryNavigationTestActivity extends Activity {
    public EasyRecyclerView recyclerView;
    public GalleryAdapterNew adapter;
    public Button toolbar;
    public ContentLayout content;
    public PagingHelper helper;
    public int clicks;
    public int longClicks;
    public int thumbnailClicks;
    public long lastClickedId;
    private final ArrayList<GalleryInfo> galleries = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        toolbar = new Button(this);
        toolbar.setText("Controller navigation test");
        root.addView(toolbar);
        content = new ContentLayout(this);
        content.hideFastScroll();
        content.findViewById(R.id.progress).setVisibility(View.GONE);
        content.findViewById(R.id.tip).setVisibility(View.GONE);
        recyclerView = content.getRecyclerView();
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        for (int i = 0; i < 120; i++) {
            GalleryInfo gallery = new GalleryInfo();
            gallery.gid = i + 1;
            gallery.title = "Gallery " + (i + 1);
            galleries.add(gallery);
        }
        adapter = new GalleryAdapterNew(getLayoutInflater(), getResources(), recyclerView,
                getIntent().getIntExtra("layout", GalleryAdapterNew.TYPE_LIST), false, executor, false) {
            @Override
            public int getItemCount() {
                return helper != null ? helper.size() : galleries.size();
            }

            @Override
            public GalleryInfo getDataAt(int position) {
                return helper != null ? helper.getDataAtEx(position) : galleries.get(position);
            }

            @Override
            public void onBindViewHolder(GalleryHolder holder, int position) {
                holder.thumb.setImageDrawable(new ColorDrawable(Color.rgb(
                        40 + position % 6 * 25, 90, 150)));
                if (holder.title != null) {
                    holder.title.setText(getDataAt(position).title);
                    holder.uploader.setText("Offline test card");
                } else {
                    ((TileThumbNew) holder.thumb).setThumbSize(120, 180 + position % 3 * 20);
                }
            }

            @Override
            protected boolean onItemClick(View view, GalleryInfo gallery) {
                clicks++;
                lastClickedId = gallery.gid;
                return true;
            }

            @Override
            protected boolean onItemLongClick(View view, GalleryInfo gallery) {
                longClicks++;
                lastClickedId = gallery.gid;
                return true;
            }
        };
        if (adapter.getType() == GalleryAdapterNew.TYPE_LIST) {
            // Exercise horizontal navigation without changing the app's preferences.
            ((AutoStaggeredGridLayoutManager) recyclerView.getLayoutManager()).setColumnSize(
                    getResources().getDimensionPixelOffset(R.dimen.gallery_list_column_width_short));
        }
        adapter.setThumbItemClickListener((position, view, gallery) -> thumbnailClicks++);
        recyclerView.setOnItemClickListener((parent, view, position, id) -> {
            clicks++;
            lastClickedId = adapter.getDataAt(position).gid;
            return true;
        });
        recyclerView.setOnItemLongClickListener((parent, view, position, id) -> {
            longClicks++;
            return true;
        });
    }

    public void enablePaging(int pages) {
        helper = new PagingHelper();
        content.setHelper(helper);
        helper.firstRefresh();
        helper.complete(pages, galleries);
    }

    public void completeCurrentPage(int pages) {
        helper.complete(pages, galleries);
    }

    /** Uses the production paging state machine with manually completed offline requests. */
    public class PagingHelper extends ContentLayout.ContentHelper<GalleryInfo> {
        public int requests;
        public int taskId;
        public int requestType;
        public int requestPage;

        @Override
        protected void getPageData(int taskId, int type, int page) {
            requests++;
            this.taskId = taskId;
            requestType = type;
            requestPage = page;
        }

        @Override
        protected void getPageData(int taskId, int type, int page, String append) {
            getPageData(taskId, type, page);
        }

        @Override
        protected void getExPageData(int pageAction, int taskId, int page) {
            getPageData(taskId, pageAction, page);
        }

        public void complete(int pages, List<GalleryInfo> data) {
            onGetPageData(taskId, pages, 0, new ArrayList<>(data));
        }

        @Override
        protected Context getContext() {
            return GalleryNavigationTestActivity.this;
        }

        @Override
        protected void notifyDataSetChanged() {
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void notifyItemRangeRemoved(int positionStart, int itemCount) {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        protected boolean isDuplicate(GalleryInfo d1, GalleryInfo d2) {
            return d1.gid == d2.gid;
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
