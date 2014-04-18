package ru.magnat.android.service.openvpn.napi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.webkit.MimeTypeMap;

public class Utils {


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Intent getFilePickerIntent(Context c, FileType fileType) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        TreeSet<String> supportedMimeTypes = new TreeSet<String>();
        Vector<String> extensions = new Vector<String>();

        switch (fileType) {
            case PKCS12:
                i.setType("application/x-pkcs12");
                supportedMimeTypes.add("application/x-pkcs12");
                extensions.add("p12");
                extensions.add("pfx");
                break;
            case CLIENT_CERTIFICATE:
            case CA_CERTIFICATE:
                i.setType("application/x-pem-file");
                supportedMimeTypes.add("application/x-x509-ca-cert");
                supportedMimeTypes.add("application/x-x509-user-cert");
                supportedMimeTypes.add("application/x-pem-file");
                supportedMimeTypes.add("text/plain");

                extensions.add("pem");
                extensions.add("crt");
                break;
            case KEYFILE:
                i.setType("application/x-pem-file");
                supportedMimeTypes.add("application/x-pem-file");
                supportedMimeTypes.add("application/pkcs8");

                // Google drive ....
                supportedMimeTypes.add("application/x-iwork-keynote-sffkey");
                extensions.add("key");
                break;

            case TLS_AUTH_FILE:
                i.setType("text/plain");

                // Backup ....
                supportedMimeTypes.add("application/pkcs8");
                // Google Drive is kind of crazy .....
                supportedMimeTypes.add("application/x-iwork-keynote-sffkey");

                extensions.add("txt");
                extensions.add("key");
                break;

            case OVPN_CONFIG:
                i.setType("application/x-openvpn-profile");
                supportedMimeTypes.add("application/x-openvpn-profile");
                supportedMimeTypes.add("application/openvpn-profile");
                supportedMimeTypes.add("application/ovpn");
                supportedMimeTypes.add("text/plain");
                extensions.add("ovpn");
                extensions.add("conf");
                break;

            case USERPW_FILE:
                i.setType("text/plain");
                supportedMimeTypes.add("text/plain");
                break;
        }

        MimeTypeMap mtm = MimeTypeMap.getSingleton();

        for (String ext : extensions) {
            String mimeType = mtm.getMimeTypeFromExtension(ext);
            if (mimeType != null)
                supportedMimeTypes.add(mimeType);
        }

        // Always add this as fallback
        supportedMimeTypes.add("application/octet-stream");

        i.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes.toArray(new String[supportedMimeTypes.size()]));


        /* Samsung has decided to do something strange, on stock Android GET_CONTENT opens the document UI */
        /* fist try with documentsui */
        i.setPackage("com.android.documentsui");

        //noinspection ConstantConditions
//        if (true || !isIntentAvailable(c,i)) {
//            i.setAction(Intent.ACTION_OPEN_DOCUMENT);
//            i.setPackage(null);
//        }

        return i;
    }


    public static boolean isIntentAvailable(Context context, Intent i) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(i,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    public enum FileType {
        PKCS12(0),
        CLIENT_CERTIFICATE(1),
        CA_CERTIFICATE(2),
        OVPN_CONFIG(3),
        KEYFILE(4),
        TLS_AUTH_FILE(5),
        USERPW_FILE(6);

        private int value;

        FileType(int i) {
            value = i;
        }

        public static FileType getFileTypeByValue(int value) {
            switch (value) {
                case 0:
                    return PKCS12;
                case 1:
                    return CLIENT_CERTIFICATE;
                case 2:
                    return CA_CERTIFICATE;
                case 3:
                    return OVPN_CONFIG;
                case 4:
                    return KEYFILE;
                case 5:
                    return TLS_AUTH_FILE;
                case 6:
                    return USERPW_FILE;
                default:
                    return null;
            }
        }

        public int getValue() {
            return value;
        }
    }

    static private byte[] readBytesFromStream(InputStream input) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        input.close();
        return buffer.toByteArray();
    }

    public static String getFilePickerResult(FileType ft, Intent result, Context c) throws IOException, SecurityException {

        Uri uri = result.getData();
        if (uri == null)
            return null;

        byte[] fileData = readBytesFromStream(c.getContentResolver().openInputStream(uri));
        String newData = null;

        Cursor cursor = c.getContentResolver().query(uri, null, null, null, null);

        String prefix = "";
        try {
            if (cursor!=null && cursor.moveToFirst()) {
                int cidx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (cidx != -1) {
                    String displayName = cursor.getString(cidx);

                    if (!displayName.contains(VpnProfile.INLINE_TAG) && !displayName.contains(VpnProfile.DISPLAYNAME_TAG))
                        prefix = VpnProfile.DISPLAYNAME_TAG + displayName;
                }
            }
        } finally {
            if(cursor!=null)
                cursor.close();
        }

        switch (ft) {
            case PKCS12:
                newData = Base64.encodeToString(fileData, Base64.DEFAULT);
                break;
            default:
                newData = new String(fileData, "UTF-8");
                break;
        }

        return prefix + VpnProfile.INLINE_TAG + newData;
    }
}
