package com.project.travel.story.Utility.Adapter;

import com.project.travel.story.Utility.Model.PhotoDetails;

public interface OnItemCheckListener {

    void onItemCheck(PhotoDetails item);
    void onItemUncheck(PhotoDetails item);
}