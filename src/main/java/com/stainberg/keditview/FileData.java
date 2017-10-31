package com.stainberg.keditview;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

/**
 * Created by Lynn.
 */

public class FileData {
    public String fileName;
    public String filePath;
    public String fileUrl;
    public int width;
    public int height;
    public String contentType;
    public long fileSize;
    public String fileExtension;
    public String iconUrl;
    @DrawableRes
    public int iconResId;
    public String desc;

    public FileData() {
    }

    private static final String PDF_EXTENSION = ".pdf";
    protected static final String PDF = "PDF";
    private static final String PPT_EXTENSION = ".ppt";
    protected static final String PPT = "PPT";
    private static final String PPTX_EXTENSION = ".pptx";
    protected static final String PPTX = "PPT";
    private static final String WORD_EXTENSION = ".doc";
    protected static final String DOC = "DOC";
    private static final String WORD_X_EXTENSION = ".docx";
    protected static final String DOCX = "DOCX";
    private static final String EXCEL_EXTENSION = ".xls";
    protected static final String XLS = "XLS";
    private static final String EXCEL_X_EXTENSION = ".xlsx";
    protected static final String XLSX = "XLSX";
    private static final String TXT_EXTENSION = ".txt";
    protected static final String TXT = "TXT";
    private static final String EPUB_EXTENSION = ".epub";
    protected static final String EPUB = "EPUB";
    protected static final String UNKNOWN = "?";

    public String getFileType() {
        return getFileType(fileExtension);
    }

    public static String getFileType(String filePath) {
        String fileExtension = filePath;
        if (filePath.indexOf(".") != -1) {
            fileExtension = filePath.substring(filePath.lastIndexOf("."));
        }
        if (!TextUtils.isEmpty(fileExtension)) {
            switch (fileExtension) {
                case PDF_EXTENSION:
                    return PDF;
                case PPT_EXTENSION:
                    return PPT;
                case PPTX_EXTENSION:
                    return PPTX;
                case WORD_EXTENSION:
                    return DOC;
                case WORD_X_EXTENSION:
                    return DOCX;
                case EXCEL_EXTENSION:
                    return XLS;
                case EXCEL_X_EXTENSION:
                    return XLSX;
                case TXT_EXTENSION:
                    return TXT;
                case EPUB_EXTENSION:
                    return EPUB;
                default:
                    break;
            }
        }
        return UNKNOWN;
    }

    public static String getFileColorType(String filePath) {
        String fileExtension = filePath;
        if (filePath.indexOf(".") != -1) {
            fileExtension = filePath.substring(filePath.lastIndexOf("."));
        }
        if (!TextUtils.isEmpty(fileExtension)) {
            switch (fileExtension) {
                case PDF_EXTENSION:
                    return PDF;
                case PPT_EXTENSION:
                case PPTX_EXTENSION:
                    return PPT;
                case WORD_EXTENSION:
                case WORD_X_EXTENSION:
                    return DOC;
                case EXCEL_EXTENSION:
                case EXCEL_X_EXTENSION:
                    return XLS;
                case TXT_EXTENSION:
                    return TXT;
                case EPUB_EXTENSION:
                    return EPUB;
                default:
                    break;
            }
        }
        return UNKNOWN;
    }
}
