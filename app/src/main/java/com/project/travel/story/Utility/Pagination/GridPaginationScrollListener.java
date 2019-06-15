package com.project.travel.story.Utility.Pagination;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class GridPaginationScrollListener extends RecyclerView.OnScrollListener {

    GridLayoutManager layoutManager;
    public static final int PAGE_SIZE = 12;
    private final int VISIBLE_THRESHOLD = 1;

    public GridPaginationScrollListener(GridLayoutManager layoutManager) {
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
