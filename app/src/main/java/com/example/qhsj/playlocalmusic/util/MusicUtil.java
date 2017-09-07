package com.example.qhsj.playlocalmusic.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.qhsj.playlocalmusic.entry.MusicInfo;
import com.github.promeg.pinyinhelper.Pinyin;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chris on 2017/8/4.
 * 获取本地音乐文件
 */
public class MusicUtil {

    public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟

    private static String[] proj_music = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE};

    public static ArrayList<MusicInfo> queryMusic(Context context) {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        StringBuilder select = new StringBuilder(" 1=1 and title != ''");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
        select.append(" and " + MediaStore.Audio.Media.SIZE + " > " + FILTER_SIZE);
        select.append(" and " + MediaStore.Audio.Media.DURATION + " > " + FILTER_DURATION);

        final String songSortOrder = "album";

        ArrayList<MusicInfo> list3 = getMusicListCursor(cr.query(uri, proj_music,
                select.toString(), null,
                songSortOrder));
        return list3;
    }

    public static ArrayList<MusicInfo> getMusicListCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        ArrayList<MusicInfo> musicList = new ArrayList<>();
        while (cursor.moveToNext()) {
            MusicInfo music = new MusicInfo();
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            music.albumName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            music.albumData = getAlbumArtUri(music.albumId) + "";
            music.duration = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE));
            music.artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));
            music.artistId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
            String filePath = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));
            music.data = filePath;
            music.folder = filePath.substring(0, filePath.lastIndexOf(File.separator));
            music.size = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));
            music.islocal = true;
            music.sort = Pinyin.toPinyin(music.musicName.charAt(0)).substring(0, 1).toUpperCase();
            musicList.add(music);
        }
        cursor.close();
        return musicList;
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }
}
