package shticell.servlets;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.manage.Permission;
import shticell.manage.SheetManager;
import shticell.utils.ServletUtils;

import java.io.IOException;

//@WebServlet("/ackOrDenyPermission")
public class AckOrDenyPermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // קריאת פרמטרים מהבקשה
        String sheetName = request.getParameter("sheetName");
        String userName = request.getParameter("userName");
        boolean approve = Boolean.parseBoolean(request.getParameter("approve"));
        String loggedInUser = request.getParameter("loggedInUser"); // קבלת שם המשתמש מהפרמטר
        int perID = Integer.parseInt(request.getParameter("perID"));

        if (loggedInUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"User not logged in\"}");
            return;
        }

        // קבלת המנהל גיליונות
        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());

        try {
            // בדיקה אם המשתמש המחובר הוא הבעלים של הגיליון
            if (!sheetManager.getSheet(sheetName).getOwner().equals(loggedInUser)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Only the owner can approve or deny permissions\"}");
                return;
            }

            // חיפוש הרשאה למשתמש וגיליון
            Permission permission = sheetManager.getPermission(sheetName, userName,perID);
            if (permission != null)
            {
                if(approve)
                {
                    sheetManager.approvePermission(permission.getSheetName(), userName, perID); // אישור או דחיית ההרשאה
                }
                else {
                    sheetManager.rejectPermission(permission.getSheetName(), userName, perID); // אישור או דחיית ההרשאה
                }
                // תגובה חיובית
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\": \"success\", \"message\": \"Permission updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"status\": \"error\", \"message\": \"Permission not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
