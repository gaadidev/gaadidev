package com.gaadi.neon.util;

import com.gaadi.neon.enumerations.Sorting_Type;

/**
 * @author princebatra
 * @version 1.0
 * @since 17/7/17
 */
public class CustomParameters {

    private boolean hideCameraButtonInNeutral = false;
    private boolean hideGalleryButtonInNeutral = false;
    private boolean locationRestrictive = true;
    private boolean showTagImage;
    private boolean hideTagImageReview;
    private Sorting_Type folderSortingType;
    private Sorting_Type fileSortingType;
    private int imageCompressionValue;

    private CustomParameters(CustomParametersBuilder builder) {
        this.hideCameraButtonInNeutral = builder.hideCameraButtonInNeutral;
        this.hideGalleryButtonInNeutral = builder.hideGalleryButtonInNeutral;
        this.locationRestrictive = builder.locationRestrictive;
        this.showTagImage = builder.showTagImage;
        this.hideTagImageReview = builder.hideTagImageReview;
        this.folderSortingType = builder.folderSortingType;
        this.fileSortingType = builder.fileSortingType;
        this.imageCompressionValue= builder.imageCompressionValue;
    }

    public boolean gethideCameraButtonInNeutral() {
        return hideCameraButtonInNeutral;
    }

    public boolean getHideGalleryButtonInNeutral() {
        return hideGalleryButtonInNeutral;
    }

    public boolean getLocationRestrictive() {
        return locationRestrictive;
    }

    public boolean showTagImage() {
        return showTagImage;
    }

    public boolean hideTagImageReview() {
        return hideTagImageReview;
    }

    public Sorting_Type getFolderSortingType() { return folderSortingType; }

    public Sorting_Type getFileSortingType() { return fileSortingType; }

    public int getImageCompressionValue() { return imageCompressionValue; }

    public static class CustomParametersBuilder {

        private boolean hideCameraButtonInNeutral;
        private boolean hideGalleryButtonInNeutral;
        private boolean locationRestrictive = true;
        private boolean showTagImage;
        private boolean hideTagImageReview;
        private Sorting_Type folderSortingType;
        private Sorting_Type fileSortingType;
        private int imageCompressionValue;

        public CustomParametersBuilder sethideCameraButtonInNeutral(boolean hide) {
            this.hideCameraButtonInNeutral = hide;
            return this;
        }

        public CustomParametersBuilder setHideGalleryButtonInNeutral(boolean hide) {
            this.hideGalleryButtonInNeutral = hide;
            return this;
        }

        public CustomParametersBuilder setLocationRestrictive(boolean locationRestrictive) {
            this.locationRestrictive = locationRestrictive;
            return this;
        }

        public CustomParametersBuilder showTagImagePreview(boolean tagShow) {
            this.showTagImage = tagShow;
            return this;
        }

        public CustomParametersBuilder hideTagImageReview(boolean hideReview) {
            this.hideTagImageReview = hideReview;
            return this;
        }

        public CustomParametersBuilder setFolderSortingType(Sorting_Type sorting){
            this.folderSortingType = sorting;
            return this;
        }

        public CustomParametersBuilder setFileSortingType(Sorting_Type sorting){
            this.fileSortingType = sorting;
            return this;
        }

        public CustomParametersBuilder setImageCompressionValue(int compressionValue){
            this.imageCompressionValue = compressionValue;
            return this;
        }

        public CustomParameters build() {
            CustomParameters user = new CustomParameters(this);
            return user;
        }

    }
}
