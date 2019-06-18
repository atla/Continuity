/*
 * Copyright (C) 2012 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leihwelt.thedailypicture.fragments.diary;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView.ScaleType;

import com.leihwelt.thedailypicture.fragments.diary.DiaryFragmentAdapter.ViewHolder;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.picturetake.PictureTakeHelper;

import org.lucasr.smoothie.ItemLoader;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class DiaryListLoader extends ItemLoader<Entry, CacheableBitmapDrawable> {

    private static final String TAG = DiaryListLoader.class.getSimpleName();
    private final BitmapLruCache mCache;
    private PictureTakeHelper helper;
    private DataModel model;

    public DiaryListLoader(BitmapLruCache cache, DataModel model) {
        mCache = cache;
        helper = new PictureTakeHelper();
        this.model = model;
    }

    @Override
    public CacheableBitmapDrawable loadItemFromMemory(Entry url) {
        return mCache.getFromMemoryCache(url.getPicture());
    }

    @Override
    public Entry getItemParams(Adapter adapter, int position) {
        Entry entry = (Entry) adapter.getItem(position);

        return entry;
    }

    @Override
    public CacheableBitmapDrawable loadItem(Entry entry) {
        CacheableBitmapDrawable wrapper = mCache.get(entry.getPicture());

        if (wrapper == null) {

            File picture = model.getPictureFileForDate(entry.getDate());

            if (picture != null) {
                wrapper = mCache.put(entry.getPicture(), helper.loadPicture(picture.getAbsolutePath(), entry, 240, 240));

            }

        }

        return wrapper;
    }

    @Override
    public void displayItem(View itemView, CacheableBitmapDrawable result, boolean fromMemory) {
        ViewHolder holder = (ViewHolder) itemView.getTag();

        if (fromMemory) {
            holder.picture.setImageDrawable(result);
        } else {

            BitmapDrawable emptyDrawable = new BitmapDrawable(itemView.getResources());

            TransitionDrawable fadeInDrawable = new TransitionDrawable(new Drawable[]{emptyDrawable, result});
            holder.picture.setImageDrawable(fadeInDrawable);
            fadeInDrawable.startTransition(200);

        }

        holder.picture.setBackgroundDrawable(null);
        holder.picture.setScaleType(ScaleType.CENTER_CROP);

    }
}
