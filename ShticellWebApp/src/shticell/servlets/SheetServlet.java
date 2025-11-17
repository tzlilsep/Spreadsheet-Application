package shticell.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.engine.loadingXML.LoadingXML;
import shticell.engine.sheet.impl.SheetImpl;
import shticell.manage.SheetManager;
import shticell.utils.ServletUtils;

import java.io.*;
import java.io.IOException;

public class SheetServlet extends HttpServlet {


    private static final String UPLOAD_DIRECTORY = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String ownerName = request.getParameter("ownerName");
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            // המרה של הקובץ ל-SheetImpl דרך הפונקציה loadSheetFromXML
            SheetImpl sheet = loadSheetFromXML(request.getReader());
            // קבלת SheetManager באמצעות ServletUtils
            SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());
            // הוספת הגיליון למנהל הגיליונות עם השם של הגיליון
            sheetManager.addSheet(sheet.getSheetName(), sheet, ownerName);

            // הודעה שהקובץ נטען בהצלחה והוסף למערכת
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("File uploaded successfully and added to SheetManager.");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("File upload failed: " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("File upload failed: " + e.getMessage());
        }
    }


    public SheetImpl loadSheetFromXML(Reader reader) throws Exception {
        try {
            if (reader != null) {
                LoadingXML loader = new LoadingXML();
                return loader.loadSpreadsheetFromReader(reader); // Assuming loadSpreadsheet throws exceptions when it fails
            } else {
                throw new IllegalArgumentException("No file selected.");
            }
        } catch (Exception e) {
            // Re-throw the exception with a detailed message for the controller to display
            throw new Exception("Error loading the XML file: " + e.getMessage(), e);
        }
    }

}