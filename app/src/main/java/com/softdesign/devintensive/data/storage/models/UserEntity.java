package com.softdesign.devintensive.data.storage.models;

import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.storage.viewmodels.RepoViewModel;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@Entity(active = true, nameInDb = "USERS")
public class UserEntity {

    @Id
    private Long id;

    @NotNull
    @Unique
    private String remoteId;

    @NotNull
    private String fullName;

    @NotNull
    private String searchName;

    private int rating;
    private int codeLines;
    private int projects;
    private String homeTask;
    private String bio;
    private String photo;
    private String avatar;
    private int internalId;

    @ToMany(joinProperties = {
            @JoinProperty(name = "remoteId", referencedName = "userRemoteId")
    })
    private List<LikeEntity> likesBy;

    @ToMany(joinProperties = {
            @JoinProperty(name = "remoteId", referencedName = "userRemoteId")
    })
    private List<RepositoryEntity> repositoryEntities;

    public UserEntity(@NotNull UserListRes user, int index) {

        this.remoteId = user.getId();
        this.photo = user.getPublicInfo().getPhoto();
        this.avatar = user.getPublicInfo().getAvatar();
        this.fullName = user.getFullName();
        this.searchName = user.getFullName().toUpperCase();
        this.homeTask = user.getProfileValues().getHomeTask();
        this.rating = Integer.parseInt(user.getProfileValues().getRating());
        this.codeLines = Integer.parseInt(user.getProfileValues().getCodeLines());
        this.projects = Integer.parseInt(user.getProfileValues().getProjects());
        this.bio = user.getPublicInfo().getBio();
        this.internalId = index;
    }

    public List<RepoViewModel> getRepoViewModelsList() {
        if (getRepositoryEntities().size() > 0) {
            return new ArrayList<RepoViewModel>() {{
                for (RepositoryEntity r : getRepositoryEntities()) {
                    add(new RepoViewModel(r.getRemoteId(), r.getRepositoryName()));
                }
            }};
        } else {
            return new ArrayList<RepoViewModel>() {{
                add(new RepoViewModel("", ""));
            }};
        }
    }

    public List<String> getLikesList() {
        if (getLikesBy() == null) return new ArrayList<>();
        else
            return new ArrayList<String>() {{
                for (LikeEntity l : getLikesBy()) {
                    add(l.getRemoteId());
                }
            }};
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Generated

    //region :::::::::::::::::::::::::::::::::::::::::: Standard Getters And Setters
    public String getBio() {
        return this.bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getHomeTask() {
        return this.homeTask;
    }

    public void setHomeTask(String homeTask) {
        this.homeTask = homeTask;
    }

    public int getProjects() {
        return this.projects;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public int getCodeLines() {
        return this.codeLines;
    }

    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getSearchName() {
        return this.searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoto() {
        return this.photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public int getInternalId() {
        return this.internalId;
    }

    public void setInternalId(int internalId) {
        this.internalId = internalId;
    }

    public void setLikesBy(List<LikeEntity> likesBy) {
        this.likesBy = likesBy;
    }

    public void setRepositoryEntities(List<RepositoryEntity> repositoryEntities) {
        this.repositoryEntities = repositoryEntities;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1814575071)
    private transient UserEntityDao myDao;

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
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 2040968928)
    public synchronized void resetRepositoryEntities() {
        repositoryEntities = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 646789757)
    public List<RepositoryEntity> getRepositoryEntities() {
        if (repositoryEntities == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            RepositoryEntityDao targetDao = daoSession.getRepositoryEntityDao();
            List<RepositoryEntity> repositoryEntitiesNew = targetDao._queryUserEntity_RepositoryEntities(remoteId);
            synchronized (this) {
                if (repositoryEntities == null) {
                    repositoryEntities = repositoryEntitiesNew;
                }
            }
        }
        return repositoryEntities;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 287999134)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserEntityDao() : null;
    }

    @Generated(hash = 587082005)
    public UserEntity(Long id, @NotNull String remoteId, @NotNull String fullName, @NotNull String searchName, int rating,
            int codeLines, int projects, String homeTask, String bio, String photo, String avatar, int internalId) {
        this.id = id;
        this.remoteId = remoteId;
        this.fullName = fullName;
        this.searchName = searchName;
        this.rating = rating;
        this.codeLines = codeLines;
        this.projects = projects;
        this.homeTask = homeTask;
        this.bio = bio;
        this.photo = photo;
        this.avatar = avatar;
        this.internalId = internalId;
    }

    @Generated(hash = 1433178141)
    public UserEntity() {
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1778225727)
    public synchronized void resetLikesBy() {
        likesBy = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1156693726)
    public List<LikeEntity> getLikesBy() {
        if (likesBy == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LikeEntityDao targetDao = daoSession.getLikeEntityDao();
            List<LikeEntity> likesByNew = targetDao._queryUserEntity_LikesBy(remoteId);
            synchronized (this) {
                if (likesBy == null) {
                    likesBy = likesByNew;
                }
            }
        }
        return likesBy;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
