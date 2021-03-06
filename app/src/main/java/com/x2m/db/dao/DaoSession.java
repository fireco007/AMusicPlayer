package com.x2m.db.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.x2m.db.TB_Music;

import com.x2m.db.dao.TB_MusicDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig tB_MusicDaoConfig;

    private final TB_MusicDao tB_MusicDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        tB_MusicDaoConfig = daoConfigMap.get(TB_MusicDao.class).clone();
        tB_MusicDaoConfig.initIdentityScope(type);

        tB_MusicDao = new TB_MusicDao(tB_MusicDaoConfig, this);

        registerDao(TB_Music.class, tB_MusicDao);
    }
    
    public void clear() {
        tB_MusicDaoConfig.getIdentityScope().clear();
    }

    public TB_MusicDao getTB_MusicDao() {
        return tB_MusicDao;
    }

}
