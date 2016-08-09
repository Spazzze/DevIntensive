package com.softdesign.devintensive.data.storage.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
@Entity(active = true, nameInDb = "LIKES")
public class LikeEntity {

    @Id
    private Long id;

    @NotNull
    private String remoteId;

    @NotNull
    private String userRemoteId;

    public LikeEntity(String whoLikedId, String userReceivedLikeId) {
        this.userRemoteId = userReceivedLikeId;
        this.remoteId = whoLikedId;
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Generated
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 104783431)
    private transient LikeEntityDao myDao;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 427491321)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLikeEntityDao() : null;
    }

    public String getUserRemoteId() {
        return this.userRemoteId;
    }

    public void setUserRemoteId(String userRemoteId) {
        this.userRemoteId = userRemoteId;
    }

    public String getRemoteId() {
        return this.remoteId;
    }

    public void setRemoteId(String remoteId) {
        this.remoteId = remoteId;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 1353027191)
    public LikeEntity(Long id, @NotNull String remoteId,
                      @NotNull String userRemoteId) {
        this.id = id;
        this.remoteId = remoteId;
        this.userRemoteId = userRemoteId;
    }

    @Generated(hash = 1383136376)
    public LikeEntity() {
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
