package com.leihwelt.thedailypicture.model;

import android.content.Context;

public class DataModelBuilder {

    private static final String TAG = DataModelBuilder.class.getSimpleName();

    public static DataModel build(String diary, ModelType type, Context ctx) {

//		if (type == ModelType.DROPBOX) {
//
//			DbxAccountManager accountManager = App.getInstance(ctx).getAccountManager();
//
//			try {
//				DropboxDataModel model = new DropboxDataModel(diary, accountManager);
//				return model;
//			} catch (Unauthorized e) {
//				Log.d(TAG, "Dropbox Unauthorized");
//			}
//
//		}

        return new DataModel(diary);

    }

}
