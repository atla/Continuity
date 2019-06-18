package com.leihwelt.thedailypicture.messages;

import com.dropbox.sync.android.DbxAccountManager;

public class SetDropboxAccount {

    private DbxAccountManager mDbxAcctMgr;

    public SetDropboxAccount(DbxAccountManager accountManager) {
        this.mDbxAcctMgr = accountManager;
    }

    public DbxAccountManager getmDbxAcctMgr() {
        return mDbxAcctMgr;
    }

}
