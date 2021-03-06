package com.openfooddata.app.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.openfooddata.app.model.Barcode;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table BARCODE.
*/
public class BarcodeDao extends AbstractDao<Barcode, Long> {

    public static final String TABLENAME = "BARCODE";

    /**
     * Properties of entity Barcode.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Barcode = new Property(1, String.class, "barcode", false, "BARCODE");
        public final static Property Created_at = new Property(2, java.util.Date.class, "created_at", false, "CREATED_AT");
        public final static Property Updated_at = new Property(3, java.util.Date.class, "updated_at", false, "UPDATED_AT");
        public final static Property Product_id = new Property(4, Long.class, "product_id", false, "PRODUCT_ID");
    };


    public BarcodeDao(DaoConfig config) {
        super(config);
    }
    
    public BarcodeDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'BARCODE' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'BARCODE' TEXT UNIQUE ," + // 1: barcode
                "'CREATED_AT' INTEGER," + // 2: created_at
                "'UPDATED_AT' INTEGER," + // 3: updated_at
                "'PRODUCT_ID' INTEGER);"); // 4: product_id
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'BARCODE'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Barcode entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String barcode = entity.getBarcode();
        if (barcode != null) {
            stmt.bindString(2, barcode);
        }
 
        java.util.Date created_at = entity.getCreated_at();
        if (created_at != null) {
            stmt.bindLong(3, created_at.getTime());
        }
 
        java.util.Date updated_at = entity.getUpdated_at();
        if (updated_at != null) {
            stmt.bindLong(4, updated_at.getTime());
        }
 
        Long product_id = entity.getProduct_id();
        if (product_id != null) {
            stmt.bindLong(5, product_id);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Barcode readEntity(Cursor cursor, int offset) {
        Barcode entity = new Barcode( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // barcode
            cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)), // created_at
            cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)), // updated_at
            cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4) // product_id
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Barcode entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBarcode(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setCreated_at(cursor.isNull(offset + 2) ? null : new java.util.Date(cursor.getLong(offset + 2)));
        entity.setUpdated_at(cursor.isNull(offset + 3) ? null : new java.util.Date(cursor.getLong(offset + 3)));
        entity.setProduct_id(cursor.isNull(offset + 4) ? null : cursor.getLong(offset + 4));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Barcode entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Barcode entity) {
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
