
package com.uni.ppg.domain.saveResult;



//
//import android.content.Context;
//import android.os.Environment;
//import android.util.Log;
//import com.uni.ppg.constant.GlobalConstants;
//import com.uni.ppg.domain.predict.PredictAPI;
//
//import org.apache.poi.hssf.usermodel.HSSFCellStyle;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.hssf.util.HSSFColor;
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.CellStyle;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Iterator;
//
//public class saveResult {
//    static Cell cell;
//    private static Sheet sheet;
//    private static final String TAG = saveResult.class.getName();
//    private static String EXCEL_SHEET_NAME = "Sheet1";
//
//    /**
//     * Method: Generate Excel Workbook
//     */
//    public static void createExcelWorkbook(double[] feature, String prediction) {
//        // New Workbook
//        Workbook workbook = new HSSFWorkbook();
//
//        cell = null;
//
//        // Cell style for header row
//        CellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//        cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//
//        // New Sheet
//        sheet = null;
//        sheet = workbook.createSheet(EXCEL_SHEET_NAME);
//
//        // Generate column headings
//        Row row = sheet.createRow(0);
//
//        for (int i = 0; i< GlobalConstants.rriLength + GlobalConstants.addedFeature;i++){
//            cell = row.createCell(i);
//            cell.setCellValue(feature[i]);
//            cell.setCellStyle(cellStyle);
//        }
//
//        cell = row.createCell(GlobalConstants.rriLength + GlobalConstants.addedFeature);
//        cell.setCellValue(prediction);
//        cell.setCellStyle(cellStyle);
//    }
//
//    public static boolean exportDataIntoWorkbook(Context context, String fileName,
//                                                 List<ContactResponse> dataList) {
//        boolean isWorkbookWrittenIntoStorage;
//
//        // Check if available and not read only
//        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
//            Log.i(TAG, "Storage not available or read only");
//            return false;
//        }
//
//        // Creating a New HSSF Workbook (.xls format)
//        Workbook workbook = new HSSFWorkbook();
//
//        setHeaderCellStyle();
//
//        // Creating a New Sheet and Setting width for each column
//        sheet = workbook.createSheet(Constants.EXCEL_SHEET_NAME);
//        sheet.setColumnWidth(0, (15 * 400));
//        sheet.setColumnWidth(1, (15 * 400));
//        sheet.setColumnWidth(2, (15 * 400));
//        sheet.setColumnWidth(3, (15 * 400));
//
//        setHeaderRow();
//        fillDataIntoExcel(dataList);
//        isWorkbookWrittenIntoStorage = storeExcelInStorage(context, fileName);
//
//        return isWorkbookWrittenIntoStorage;
//    }
//
//    /**
//     * Checks if Storage is READ-ONLY
//     *
//     * @return boolean
//     */
//    private static boolean isExternalStorageReadOnly() {
//        String externalStorageState = Environment.getExternalStorageState();
//        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
//    }
//
//    /**
//     * Checks if Storage is Available
//     *
//     * @return boolean
//     */
//    private static boolean isExternalStorageAvailable() {
//        String externalStorageState = Environment.getExternalStorageState();
//        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
//    }
//
//    /**
//     * Setup header cell style
//     */
//    private static void setHeaderCellStyle() {
//        headerCellStyle = workbook.createCellStyle();
//        headerCellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
//        headerCellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//        headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
//    }
//
//    /**
//     * Setup Header Row
//     */
//    private static void setHeaderRow() {
//        Row headerRow = sheet.createRow(0);
//
//        cell = row.createCell(0);
//        cell.setCellValue("First Name");
//        cell.setCellStyle(cellStyle);
//
//        cell = row.createCell(1);
//        cell.setCellValue("Last Name");
//        cell.setCellStyle(cellStyle);
//
//        cell = row.createCell(2);
//        cell.setCellValue("Phone Number");
//        cell.setCellStyle(cellStyle);
//
//        cell = row.createCell(3);
//        cell.setCellValue("Mail ID");
//    }
//
//    /**
//     * Fills Data into Excel Sheet
//     * <p>
//     * NOTE: Set row index as i+1 since 0th index belongs to header row
//     *
//     * @param dataList - List containing data to be filled into excel
//     */
//    private static void fillDataIntoExcel(List<ContactResponse> dataList) {
//        for (int i = 0; i < dataList.size(); i++) {
//            // Create a New Row for every new entry in list
//            Row rowData = sheet.createRow(i + 1);
//
//            // Create Cells for each row
//            cell = rowData.createCell(0);
//            cell.setCellValue(dataList.get(i).getFirstName());
//
//            cell = rowData.createCell(1);
//            cell.setCellValue(dataList.get(i).getLastName());
//
//            cell = rowData.createCell(2);
//            cell.setCellValue(dataList.get(i).getPhoneNumber());
//
//            cell = rowData.createCell(4);
//            cell.setCellValue(dataList.get(i).getMailId());
//        }
//    }
//
//    /**
//     * Store Excel Workbook in external storage
//     *
//     * @param context  - application context
//     * @param fileName - name of workbook which will be stored in device
//     * @return boolean - returns state whether workbook is written into storage or not
//     */
//    private static boolean storeExcelInStorage(Context context, String fileName) {
//        boolean isSuccess;
//        File file = new File(context.getExternalFilesDir(null), fileName);
//        FileOutputStream fileOutputStream = null;
//
//        try {
//            fileOutputStream = new FileOutputStream(file);
//            workbook.write(fileOutputStream);
//            Log.e(TAG, "Writing file" + file);
//            isSuccess = true;
//        } catch (IOException e) {
//            Log.e(TAG, "Error writing Exception: ", e);
//            isSuccess = false;
//        } catch (Exception e) {
//            Log.e(TAG, "Failed to save file due to Exception: ", e);
//            isSuccess = false;
//        } finally {
//            try {
//                if (null != fileOutputStream) {
//                    fileOutputStream.close();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//        return isSuccess;
//    }
//}