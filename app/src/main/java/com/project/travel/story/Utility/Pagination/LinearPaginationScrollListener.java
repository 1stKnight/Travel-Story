package com.project.travel.story.Utility.Pagination;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class LinearPaginationScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager layoutManager;
    public static final int PAGE_SIZE = 5;
    private final int VISIBLE_THRESHOLD = 2;

    public LinearPaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int totalItemCount = layoutManager.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if (totalItemCount <= lastVisibleItemPosition + VISIBLE_THRESHOLD && totalItemCount != 0) {
                loadMoreItems();
            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();
}
