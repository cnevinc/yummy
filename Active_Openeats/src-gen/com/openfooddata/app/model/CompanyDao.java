package com.openfooddata.app.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.openfooddata.app.model.Company;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table COMPANY.
*/
public class CompanyDao extends AbstractDao<Company, Long> {

    public static final String TABLENAME = "COMPANY";

    /**
     * Properties of entity Company.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
        public final static Property Owner_name = new Property(2, String.class, "owner_name", false, "OWNER_NAME");
        public final static Property Address = new Property(3, String.class, "address", false, "ADDRESS");
        public final static Property Phone_no = new Property(4, String.class, "phone_no", false, "PHONE_NO");
        public final static Property Created_at = new Property(5, java.util.Date.class, "created_at", false, "CREATED_AT");
        public final static Property Updated_at = new Property(6, java.util.Date.class, "updated_at", false, "UPDATED_AT");
        public final static Property Company_no = new Property(7, String.class, "company_no", false, "COMPANY_NO");
    };

    private DaoSession daoSession;


    public CompanyDao(DaoConfig config) {
        super(config);
    }
    
    public CompanyDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'COMPANY' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'NAME' TEXT UNIQUE ," + // 1: name
                "'OWNER_NAME' TEXT," + // 2: owner_name
                "'ADDRESS' TEXT," + // 3: address
                "'PHONE_NO' TEXT," + // 4: phone_no
                "'CREATED_AT' INTEGER," + // 5: created_at
                "'UPDATED_AT' INTEGER," + // 6: updated_at
                "'COMPANY_NO' TEXT);"); // 7: company_no
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'COMPANY'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Company entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(2, name);
        }
 
        String owner_name = entity.getOwner_name();
        if (owner_name != null) {
            stmt.bindString(3, owner_name);
        }
 
        String address = entity.getAddress();
        if (address != null) {
            stmt.bindString(4, address);
        }
 
        String phone_no = entity.getPhone_no();
        if (phone_no != null) {
            stmt.bindString(5, phone_no);
        }
 
        java.util.Date created_at = entity.getCreated_at();
        if (created_at != null) {
            stmt.bindLong(6, created_at.getTime());
        }
 
        java.util.Date updated_at = entity.getUpdated_at();
        if (updated_at != null) {
            stmt.bindLong(7, updated_at.getTime());
        }
 
        String company_no = entity.getCompany_no();
        if (company_no != null) {
            stmt.bindString(8, company_no);
        }
    }

    @Override
    protected void attachEntity(Company entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Company readEntity(Cursor cursor, int offset) {
        Company entity = new Company( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // owner_name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // address
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // phone_no
            cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)), // created_at
            cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)), // updated_at
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7) // company_no
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Company entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setOwner_name(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setAddress(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setPhone_no(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setCreated_at(cursor.isNull(offset + 5) ? null : new java.util.Date(cursor.getLong(offset + 5)));
        entity.setUpdated_at(cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)));
        entity.setCompany_no(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Company entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Company entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
