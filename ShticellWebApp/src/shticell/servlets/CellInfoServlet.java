package shticell.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.engine.cell.api.Cell;
import shticell.engine.sheet.impl.SheetImpl;
import shticell.dto.CellDto;
import shticell.manage.SheetManager;
import shticell.utils.ServletUtils;
import shticell.engine.cell.api.CellType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//@WebServlet("/cellInfo")
public class CellInfoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());
        SheetImpl sheet = sheetManager.getSheet(sheetName);

        if (sheet == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Sheet not found");
            return;
        }

        Cell cell = sheet.getCell(row, col);
        //cell.printCellInfo();
        if (cell == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Cell not found");
            return;
        }

        // Convert the Cell to a CellDto, including dependencies and influences
        CellDto cellDto = convertCellToCellDto(cell,sheet.versionManager.getCurrentVersion()-1);

        // Convert each dependent Cell in dependsOn list to a CellDto
        for (Cell dependentCell : cell.getDependsOn()) {
                cellDto.dependsOn.add(convertCellToCellDto(dependentCell,sheet.versionManager.getCurrentVersion()-1)); // Recursive call for each dependency
        }
        // Optionally, repeat the process for influencingOn, if needed
        for (Cell influencingCell : cell.getInfluencingOn()) {
                cellDto.influencingOn.add(convertCellToCellDto(influencingCell,sheet.versionManager.getCurrentVersion()-1));
        }

        //cellDto.printCellInfo();
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(cellDto);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }

    // Method to convert a Cell to a CellDto, recursively handling dependencies and influences
    private CellDto convertCellToCellDto(Cell cell,int sheetVersion) {

        String originalVal = cell.getOriginalValue() != null ? cell.getOriginalValue() : "";
        String effectiveVal = "";
        // Set effective value as before
        if (cell.getEffectiveValue() != null) {
            effectiveVal = cell.getEffectiveValue().toString();
        }

        return new CellDto(cell.getCoordinate().getRow(),cell.getCoordinate().getColumn(),originalVal,effectiveVal,String.valueOf(cell.getVersion()),cell.getBackgroundColor(),cell.getTextColor(),sheetVersion);

    }






}
