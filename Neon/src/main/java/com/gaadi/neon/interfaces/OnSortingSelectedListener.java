package com.gaadi.neon.interfaces;

import com.gaadi.neon.enumerations.Sorting_Type;

public interface OnSortingSelectedListener {

    void onFolderSortingSelected(Sorting_Type sortingType);
    void onFileSortingSelected(Sorting_Type sortingType);
}
