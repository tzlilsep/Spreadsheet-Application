package shticell.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.engine.cell.api.Cell;
import shticell.engine.sheet.impl.SheetImpl;
import shticell.manage.SheetManager;
import shticell.utils.ServletUtils;

import java.io.IOException;

//@WebServlet("/updateCellValue")
public class UpdateCellValueServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Parse the request parameters
        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));
        String newOriginalValue = request.getParameter("newOriginalValue");

        // Get the sheet and cell
        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());
        SheetImpl sheet = sheetManager.getSheet(sheetName);

        if (sheet == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Sheet not found\"}");
            return;
        }

        try {
            // Attempt to update the cell in the sheet
            sheet.setCell(row, col, newOriginalValue, true);
            sheetManager.increaseSheetVersions(sheet.getSheetName());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":\"success\", \"message\":\"Cell updated successfully\"}");
        } catch (Exception e) {
            // In case of an error, respond with an error message
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"Error updating cell: " + e.getMessage() + "\"}");
        }
    }
}


