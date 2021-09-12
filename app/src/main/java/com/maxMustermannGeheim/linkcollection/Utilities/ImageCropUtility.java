package com.maxMustermannGeheim.linkcollection.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.finn.androidUtilities.CustomDialog;
import com.finn.androidUtilities.CustomUtility;
import com.maxMustermannGeheim.linkcollection.Activities.Main.MainActivity;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Image;
import com.maxMustermannGeheim.linkcollection.Daten.ParentClass_Tmdb;
import com.maxMustermannGeheim.linkcollection.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;

public interface ImageCropUtility {

    /**  ------------------------- Getter & Setter ------------------------->  */
    default ImageCropUtility setImageCrop (ImageCrop imageCrop) {
        try {
            Field field = getClass().getDeclaredField("imageCrop");
            field.setAccessible(true);
            field.set(this, imageCrop);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
        return this;
    }

    default ImageCrop getImageCrop () {
        try {
            Field field = getClass().getDeclaredField("imageCrop");
            field.setAccessible(true);
            return (ImageCrop) field.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }

    default ImageCrop getImageCrop (String imagePath) {
        try {
            Field field = getClass().getDeclaredField("imageCrop");
            field.setAccessible(true);
            ImageCrop imageCrop = (ImageCrop) field.get(this);
            if (imageCrop != null)
                imageCrop._setPathName(imagePath);
            return imageCrop;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
    /**  <------------------------- Getter & Setter -------------------------  */



    /**  ------------------------- Convenience ------------------------->  */
    default void selectCropForImage(AppCompatActivity context, @Nullable ImageCrop oldImageCrop, Utility.GenericInterface<ImageCrop> onCropSelect) {
        String imagePath;
        if (this instanceof ParentClass_Image && CustomUtility.stringExists(((ParentClass_Image) this).getImagePath())) {
            if (this instanceof ParentClass_Tmdb)
                imagePath = Utility.getTmdbImagePath_ifNecessary(((ParentClass_Image) this).getImagePath(), true);
            else
                imagePath = ((ParentClass_Image) this).getImagePath();
            showCropImageDialog(context, imagePath, oldImageCrop, (cropRect, wholeRect) -> {
                ImageCrop imageCrop = new ImageCrop(imagePath, cropRect, wholeRect);
                onCropSelect.run(imageCrop);
            });
        }
    }

    default void showCropImageDialog(AppCompatActivity context, String path, @Nullable ImageCrop oldImageCrop, Utility.DoubleGenericInterface<Rect, Rect> onCrop) {
        CustomDialog.Builder(context)
                .setView(R.layout.dialog_crop_image)
                .setDimensionsFullscreen()
                .removeBackground_and_margin()
                .disableScroll()
                .setSetViewContent((customDialog, view, reload) -> {
                    view.findViewById(R.id.dialog_cropImage_back).setOnClickListener(v -> customDialog.dismiss());
                    CropImageView cropImageView = view.findViewById(R.id.dialog_cropImage_cropView);

                    RequestBuilder<Bitmap> builder = Glide.with(context).asBitmap();
                    if (path.startsWith(context.getFilesDir().getAbsolutePath())) {
                        Uri imageUri = Uri.fromFile(new File(path));
                        builder = builder.load(imageUri);
                    } else
                        builder = builder.load(path);
                    builder.into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    view.findViewById(R.id.dialog_cropImage_loading).setVisibility(View.GONE);
                                    cropImageView.setImageBitmap(resource);
                                    cropImageView.setCropRect(oldImageCrop != null ? oldImageCrop.toRect() : null);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });

                    view.findViewById(R.id.dialog_cropImage_crop).setOnClickListener(v -> {
                        customDialog.dismiss();
                        Rect wholeImageRect = cropImageView.getWholeImageRect();
                        Rect cropRect = cropImageView.getCropRect();
                        onCrop.run(cropRect, wholeImageRect);
                    });
                })
                .show();

    }

    @SuppressLint("CheckResult")
    static Utility.GenericInterface<RequestBuilder<Drawable>> applyCropTransformation(ParentClass parentClass) {
        return drawableRequestBuilder -> {
            ImageCropUtility.ImageCrop imageCrop;
            if (parentClass instanceof ImageCropUtility && (imageCrop = ((ImageCropUtility) parentClass).getImageCrop(((ParentClass_Image) parentClass).getImagePath())) != null) {
                drawableRequestBuilder.transform(new ImageCropUtility.CustomCropTransformation(imageCrop));
            }
        };
    }
    /**  <------------------------- Convenience -------------------------  */

    // ---------------

    class CustomCropTransformation extends BitmapTransformation {
        private static final String TAG = "CustomCropTransformation";
        @Nullable
        private ImageCrop crop;

        public CustomCropTransformation(@Nullable ImageCrop crop) {
            this.crop = crop;
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            if (crop == null || crop._getPathName() == null)
                return toTransform;

            Log.d(TAG, String.format("transform: x:%f, y:%f, w:%f, h:%f", crop.getX(), crop.getY(), crop.getHeight(), crop.getHeight()));

//            Date start = new Date();
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(crop._getPathName(), options);
//            double width = options.outWidth;
//            double height = options.outHeight;
//            Date end = new Date();
//            Log.d(TAG, "transform: " + (end.getTime() - start.getTime()));
//
//            double xScale = toTransform.getWidth() / width;
//            double yScale = toTransform.getHeight() / height;

//            return Bitmap.createBitmap(
//                    toTransform,
//                    (int) (crop.getX() * xScale),
//                    (int) (crop.getY() * yScale),
//                    (int) (crop.getWidth() * xScale),
//                    (int) (crop.getHeight() * yScale));

            return Bitmap.createBitmap(
                    toTransform,
                    (int) (crop.getX() * toTransform.getWidth()),
                    (int) (crop.getY() * toTransform.getHeight()),
                    (int) (crop.getWidth() * toTransform.getWidth()),
                    (int) (crop.getHeight() * toTransform.getHeight()));
        }

        @Override
        public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
            messageDigest.update(("crop" + crop.getX() + crop.getY() + crop.getHeight() + crop.getHeight()).getBytes());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomCropTransformation that = (CustomCropTransformation) o;
            return Objects.equals(crop, that.crop);
        }

        @Override
        public int hashCode() {
            return Objects.hash(crop);
        }
    }

    class ImageCrop {
        // ToDo: zu Prozentual umwandeln
        private String pathName;
        private double x;
        private double y;
        private double width;
        private double height;
        private int fullWidth;
        private int fullHeight;

        /**  ------------------------- Constructor ------------------------->  */
        public ImageCrop() {
        }

        public ImageCrop(String pathName, Rect rect, Rect wholeRect) {
            applyRect(rect, wholeRect);
            this.pathName = pathName;
        }
        /**  <------------------------- Constructor -------------------------  */




        /**  ------------------------- Getter & Setter ------------------------->  */
        public double getX() {
            return x;
        }

        public ImageCrop setX(double x) {
            this.x = x;
            return this;
        }

        public double getY() {
            return y;
        }

        public ImageCrop setY(double y) {
            this.y = y;
            return this;
        }

        public double getWidth() {
            return width;
        }

        public ImageCrop setWidth(double width) {
            this.width = width;
            return this;
        }

        public double getHeight() {
            return height;
        }

        public ImageCrop setHeight(double height) {
            this.height = height;
            return this;
        }

        public int getFullWidth() {
            return fullWidth;
        }

        public ImageCrop setFullWidth(int fullWidth) {
            this.fullWidth = fullWidth;
            return this;
        }

        public int getFullHeight() {
            return fullHeight;
        }

        public ImageCrop setFullHeight(int fullHeight) {
            this.fullHeight = fullHeight;
            return this;
        }

        public String _getPathName() {
            return pathName;
        }

        public ImageCrop _setPathName(String pathName) {
            this.pathName = pathName;
            return this;
        }
        /**  <------------------------- Getter & Setter -------------------------  */


        /**  ------------------------- Convenience ------------------------->  */
        public Rect toRect() {
            int left = (int) (x * fullWidth);
            int top = (int) (y * fullHeight);
            return new Rect(left, top,(int) (left + width * fullWidth), (int) (top + height * fullHeight));
        }

        public ImageCrop applyRect(Rect rect, Rect wholeRect) {
            fullWidth = wholeRect.width();
            fullHeight = wholeRect.height();

            this.x = rect.left / (double) fullWidth;
            this.y = rect.top / (double) fullHeight;
            this.width = rect.width() / (double) fullWidth;
            this.height = rect.height() / (double) fullHeight;
            return this;
        }
        /**  <------------------------- Convenience -------------------------  */


        /**  ------------------------- Equals & Hash ------------------------->  */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ImageCrop imageCrop = (ImageCrop) o;
            return getX() == imageCrop.getX() &&
                    getY() == imageCrop.getY() &&
                    getWidth() == imageCrop.getWidth() &&
                    getHeight() == imageCrop.getHeight();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getX(), getY(), getWidth(), getHeight());
        }
        /**  <------------------------- Equals & Hash -------------------------  */
    }
}
