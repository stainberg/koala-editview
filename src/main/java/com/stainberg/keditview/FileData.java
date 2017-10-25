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

    private final String PDF_E = ".pdf";
    protected static final String PDF = "PDF";
    private final String PPT_E = ".ppt";
    protected static final String PPT = "PPT";
    private final String WORD_E = ".doc";
    private final String WORD_X_E = ".docx";
    protected static final String DOC = "DOC";
    private final String EXCEL_E = ".xls";
    private final String EXCEL_X_E = ".xlsx";
    protected static final String XLS = "XLS";
    private final String TXT_E = ".txt";
    protected static final String TXT = "TXT";
    protected static final String UNKNOWN = "?";

    public String getFileType() {
        if (!TextUtils.isEmpty(fileExtension)) {
            switch (fileExtension) {
                case PDF_E:
                    return PDF;
                case PPT_E:
                    return PPT;
                case WORD_E:
                case WORD_X_E:
                    return DOC;
                case EXCEL_E:
                case EXCEL_X_E:
                    return XLS;
                case TXT_E:
                    return TXT;
                default:
                    break;
            }
        }
        return UNKNOWN;
    }
}
