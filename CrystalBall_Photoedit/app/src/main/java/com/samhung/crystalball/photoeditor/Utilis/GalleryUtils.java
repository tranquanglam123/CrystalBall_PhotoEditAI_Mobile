package com.samhung.crystalball.photoeditor.Utilis;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GalleryUtils {

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 * 
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	public static final boolean isKitKat() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	public static final int CAMERA_CODE = 0x10;
	public static final int GALLERY_INTENT_CALLED = 0x11;
	public static final int GALLERY_KITKAT_INTENT_CALLED = 0x12;
	public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY = 0x13;
	public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA = 0x14;
	public static final int GALLERY_INTENT_CALLED_MULTI = 0x15;
	public static final int GALLERY_KITKAT_INTENT_CALLED_MULTI = 0x16;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static  String getFilePathFromUri(Context context, final Uri uri) {
		// DocumentProvider
		if (isKitKat() && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (GalleryUtils.isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				//if ("primary".equalsIgnoreCase(type)) {
				return Environment.getExternalStorageDirectory() + "/"
						+ split[1];
				//}
			}
			// DownloadsProvider
			else if (GalleryUtils.isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));
				return GalleryUtils.getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (GalleryUtils.isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return GalleryUtils.getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return GalleryUtils.getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static void openMedia(final BaseActivity parent, String title) {

		final CharSequence[] items = {parent.getString(R.string.camera), parent.getString(R.string.gallery)};

		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setTitle(title);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					startCameraActivity(parent);
					dialog.dismiss();
				} else if (item == 1) {
					openGallery(parent);
					dialog.dismiss();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static void startCameraActivity(final BaseActivity parent/*, Uri outputUri*/) {
		int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(parent,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			Intent photoPickerIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//            Intent photoPickerIntent = new Intent("android.media.action.IMAGE_CAPTURE");
//			photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
//			selectedCameraPictureUri = outputUri;
			photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

			Cursor cr = loadCursor(parent);
			parent.m_arrOldPaths = getImagePaths(cr, 0);
			cr.close();

			parent.startActivityForResult(
					Intent.createChooser(photoPickerIntent, parent.getString(R.string.upload_picker_title)),
					CAMERA_CODE);
		} else {
			showMenu(parent,1);
		}
	}

	public static void startCamera(final BaseActivity parent, Uri outputUri) {
		int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(parent,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            Intent photoPickerIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			Intent photoPickerIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//            Intent photoPickerIntent = new Intent("android.media.action.IMAGE_CAPTURE");
//			photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
//			selectedCameraPictureUri = outputUri;
//			photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

//			photoPickerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			Cursor cr = loadCursor(parent);
//			parent.m_arrOldPaths = getImagePaths(cr, 0);
//			cr.close();

//			parent.startActivityForResult(
//					Intent.createChooser(photoPickerIntent, parent.getString(R.string.upload_picker_title)),
//					CAMERA_CODE);


			Intent i = new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
			try {
				PackageManager pm = parent.getPackageManager();

				final ResolveInfo mInfo = pm.resolveActivity(i, 0);

				Intent intent = new Intent();
				intent.setComponent(new ComponentName(mInfo.activityInfo.packageName, mInfo.activityInfo.name));
				intent.setAction(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);

				parent.startActivityForResult(intent, CAMERA_CODE);
			} catch (Exception e){ }
		} else {
			showMenu(parent,1);
		}
	}

	public static void openGallery(final BaseActivity parent) {
		int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(parent,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			if (!isKitKat()) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				parent.startActivityForResult(
						Intent.createChooser(intent, parent.getString(R.string.upload_picker_title)),
						GALLERY_INTENT_CALLED);
			} else {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				parent.startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
			}
		} else {
			showMenu(parent,2);
		}
	}

	public static void openGalleryMulti(final BaseActivity parent, int nCount) {
		int permissionCheck = PermissionChecker.checkCallingOrSelfPermission(parent,
				android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			String msg = ""+nCount +"장의 사진을 선택하십시오.";
			if (!isKitKat()) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
				parent.startActivityForResult(
						Intent.createChooser(intent, msg),
						GALLERY_INTENT_CALLED_MULTI);
			} else {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
				parent.startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED_MULTI);
			}
		} else {
			showMenu(parent,2);
		}
	}

	public static void showMenu(final BaseActivity parent, final int caller) {
		AlertDialog.Builder builder = new AlertDialog.Builder(parent);
		builder.setMessage(parent.getString(R.string.access_media_permissions_msg));
		builder.setPositiveButton(parent.getString(R.string.continue_txt), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (caller == 1) {
					ActivityCompat.requestPermissions(parent,
							new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
							MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CAMERA);
				} else {
					ActivityCompat.requestPermissions(parent,
							new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
							MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_GALLERY);
				}
			}
		});
		builder.setNegativeButton(parent.getString(R.string.not_now), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				Toast.makeText(parent, parent.getString(R.string.media_access_denied_msg), Toast.LENGTH_SHORT).show();
			}
		});
		builder.show();
	}

	public static String cameraPath = "";

	public static Uri getOutputMediaFileUri(final BaseActivity parent) {

		if (MainActivity.isSDCARDMounted()) {
			File mediaStorageDir = new File(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "/Camera");
			// Create a storage directory if it does not exist
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					return null;
				}
			}
			// Create a media file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File mediaFile;
			String selectedOutputPath = mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg";
			Log.d("MediaAbstractActivity", "selected camera path "
					+ selectedOutputPath);
			mediaFile = new File(selectedOutputPath);
			cameraPath = selectedOutputPath;
			return FileProvider.getUriForFile(parent.getApplicationContext(), parent.getApplicationContext().getPackageName() + ".provider", mediaFile);
			//return Uri.fromFile(mediaFile);
		} else {
			return null;
		}
	}

	public static Cursor loadCursor(final BaseActivity parent) {

		final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };

		final String orderBy = MediaStore.Images.Media.DATE_ADDED;

		return parent.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,null, orderBy);
	}

	public static String[] getImagePaths(Cursor cursor, int startPosition) {

		int size = cursor.getCount() - startPosition;

		if (size <= 0)
			return new String[]{};

		String[] paths = new String[size];

		int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

		for (int i = startPosition; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			paths[i - startPosition] = cursor.getString(dataColumnIndex);
		}

		return paths;
	}

	public static String[] getShootedImagePaths(String[] oldPaths, Cursor newCursor)
	{
		String[] paths = new String[oldPaths.length];

		String[] newPaths = getImagePaths(newCursor, 0);

		if(oldPaths.length == 0) return newPaths;

		int nCnt = 0;
		for(int i=0; i<newPaths.length; i++)
		{
			String newPath = newPaths[i];

			boolean checked = false;
			for(int j=0; j<oldPaths.length; j++)
			{
				String oldPath = oldPaths[j];
				if(newPath.equalsIgnoreCase(oldPath))
				{
					checked = true;
					break;
				}
			}

			if(checked == false)
			{
				paths[nCnt] = newPath;
				nCnt ++;
			}
		}
		String[] returnPaths = new String [nCnt];
		for(int k = 0; k<nCnt; k++)
		{
			returnPaths[k] = paths[k];
		}
		return returnPaths;
	}
}
