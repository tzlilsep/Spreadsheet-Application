package shticell.servlets;

import com.google.gson.Gson;
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

//@WebServlet("/updateCellStyle")
public class UpdateCellStyleServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Parse the request parameters
        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));
        String textColor = request.getParameter("textColor");
        String backgroundColor = request.getParameter("backgroundColor");

        // Get the sheet and cell
        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());
        SheetImpl sheet = sheetManager.getSheet(sheetName);

        if (sheet == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Sheet not found");
            return;
        }

        Cell cell = sheet.getCell(row, col);
        if (cell == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Cell not found");
            return;
        }

        // Update the cell style
        cell.setTextColor(textColor);
        cell.setBackgroundColor(backgroundColor);

        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"success\", \"message\":\"Cell style updated successfully\"}");
    }
}
