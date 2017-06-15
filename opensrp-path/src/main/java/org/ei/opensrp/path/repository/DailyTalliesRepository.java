package org.ei.opensrp.path.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.ei.opensrp.Context;
import org.ei.opensrp.path.application.VaccinatorApplication;
import org.ei.opensrp.path.domain.Hia2Indicator;
import org.ei.opensrp.path.domain.Tally;
import org.ei.opensrp.path.service.HIA2Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyTalliesRepository extends BaseRepository {
    private static final String TAG = DailyTalliesRepository.class.getCanonicalName();
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final String TABLE_NAME = "daily_tallies";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PROVIDER_ID = "provider_id";
    public static final String COLUMN_INDICATOR_ID = "indicator_id";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String[] TABLE_COLUMNS = {
            COLUMN_ID, COLUMN_INDICATOR_ID, COLUMN_PROVIDER_ID,
            COLUMN_VALUE, COLUMN_DAY, COLUMN_UPDATED_AT
    };
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
            COLUMN_INDICATOR_ID + " INTEGER NOT NULL," +
            COLUMN_PROVIDER_ID + " VARCHAR NOT NULL," +
            COLUMN_VALUE + " VARCHAR NOT NULL," +
            COLUMN_DAY + " DATETIME NOT NULL," +
            COLUMN_UPDATED_AT + " TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP)";
    private static final String INDEX_PROVIDER_ID = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_PROVIDER_ID + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_PROVIDER_ID + " COLLATE NOCASE);";
    private static final String INDEX_INDICATOR_ID = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_INDICATOR_ID + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_INDICATOR_ID + " COLLATE NOCASE);";
    private static final String INDEX_UPDATED_AT = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_UPDATED_AT + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_UPDATED_AT + ");";
    private static final String INDEX_DAY = "CREATE INDEX " + TABLE_NAME + "_" + COLUMN_DAY + "_index" +
            " ON " + TABLE_NAME + "(" + COLUMN_DAY + ");";


    public DailyTalliesRepository(PathRepository pathRepository) {
        super(pathRepository);
    }

    protected static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_QUERY);
        database.execSQL(INDEX_PROVIDER_ID);
        database.execSQL(INDEX_INDICATOR_ID);
        database.execSQL(INDEX_UPDATED_AT);
        database.execSQL(INDEX_DAY);
    }

    /**
     * Saves a set of tallies
     *
     * @param day        The day the tallies correspond to
     * @param hia2Report Object holding the tallies, the first key in the map holds the indicator
     *                   code, and the second the DHIS id for the indicator. It's expected that
     *                   the inner most map will always hold one value
     */
    public void save(Date day, Map<String, Map<String, Object>> hia2Report) {
        SQLiteDatabase database = getPathRepository().getWritableDatabase();
        try {
            String userName = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
            database.beginTransaction();
            for (String indicatorCode : hia2Report.keySet()) {
                Map<String, Object> indicatorMap = hia2Report.get(indicatorCode);
                if (!indicatorMap.isEmpty()) {
                    String dhisId = (String) indicatorMap.keySet().toArray()[0];
                    String indicatorValue = (String) indicatorMap.get(dhisId);

                    // Get the HIA2 Indicator corresponding to the current tally
                    Hia2Indicator indicator = VaccinatorApplication.getInstance()
                            .hIA2IndicatorsRepository()
                            .findByIndicatorCodeDhisId(indicatorCode, dhisId);

                    if (indicator != null) {
                        Long id = checkIfExists(indicator.getId(), day);

                        ContentValues cv = new ContentValues();
                        cv.put(DailyTalliesRepository.COLUMN_INDICATOR_ID, indicator.getId());
                        cv.put(DailyTalliesRepository.COLUMN_VALUE, indicatorValue);
                        cv.put(DailyTalliesRepository.COLUMN_PROVIDER_ID, userName);
                        cv.put(DailyTalliesRepository.COLUMN_DAY, DAY_FORMAT.format(day));

                        if (id != null) {
                            database.update(TABLE_NAME, cv, COLUMN_ID + " = ?",
                                    new String[]{id.toString()});
                        } else {
                            database.insert(TABLE_NAME, null, cv);
                        }
                    }
                }
            }
            database.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            database.endTransaction();
        }
    }

    public List<Tally> findByProviderIdAndDay(String providerId, Date day) {
        List<Tally> tallies = null;
        Cursor cursor = null;
        try {
            cursor = getPathRepository().getReadableDatabase()
                    .query(TABLE_NAME, TABLE_COLUMNS,
                            COLUMN_PROVIDER_ID + " = ? AND " + COLUMN_DAY + "=?",
                            new String[]{providerId, DAY_FORMAT.format(day)},
                            null, null, null, null);
            tallies = readAllDataElements(cursor);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        return tallies;
    }

    private List<Tally> readAllDataElements(Cursor cursor) {
        List<Tally> tallies = new ArrayList<>();
        HashMap<Long, Hia2Indicator> indicatorMap = VaccinatorApplication.getInstance()
                .hIA2IndicatorsRepository().findAll();

        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    long indicatorId = cursor.getLong(cursor.getColumnIndex(COLUMN_INDICATOR_ID));
                    if (indicatorMap.containsKey(indicatorId)) {
                        Tally curTally = new Tally(Tally.Type.DAILY);
                        curTally.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                        curTally.setProviderId(
                                cursor.getString(cursor.getColumnIndex(COLUMN_PROVIDER_ID)));
                        curTally.setIndicator(indicatorMap.get(indicatorId));
                        curTally.setValue(cursor.getString(cursor.getColumnIndex(COLUMN_VALUE)));
                        curTally.setDate(
                                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_DAY))));
                        curTally.setUpdatedAt(
                                new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_UPDATED_AT)))
                        );

                        tallies.add(curTally);
                    }

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            cursor.close();
        }

        return tallies;
    }

    private Long checkIfExists(long indicatorId, Date day) {
        Cursor mCursor = null;
        try {
            String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_NAME +
                    " WHERE " + COLUMN_INDICATOR_ID + " = " + String.valueOf(indicatorId)
                    + " and " + COLUMN_DAY + "='" + DAY_FORMAT.format(day) + "'";
            mCursor = getPathRepository().getWritableDatabase().rawQuery(query, null);
            if (mCursor != null && mCursor.moveToFirst()) {
                return mCursor.getLong(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (mCursor != null) mCursor.close();
        }
        return null;
    }

}
