package shticell.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.manage.Permission;
import shticell.manage.PermissionType;
import shticell.manage.SheetManager;
import shticell.users.UserManager;
import shticell.utils.ServletUtils;
import shticell.engine.sheet.impl.SheetImpl;
import shticell.utils.SessionUtils;

import java.io.IOException;
import java.util.Map;

import static shticell.constants.Constants.USERNAME;

//@WebServlet("/sheets/list")
public class SheetListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        String userName;
        if (usernameFromSession == null) { //user is not logged in yet
            String usernameFromParameter = request.getParameter(USERNAME);
            if (usernameFromParameter == null || usernameFromParameter.isEmpty()) {
                //no username in session and no username in parameter - not standard situation. it's a conflict

                // stands for conflict in server state
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Unauthorized");
                return;
            }
            userName = usernameFromParameter;
        }
        else {
            userName = usernameFromSession;
        }
        // קבלת SheetManager דרך ServletUtils
        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());

        // קבלת מפת הגיליונות
        Map<String, SheetImpl> sheets = sheetManager.getAllSheets();

        // התחלת בניית תגובה בפורמט JSON
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        for (Map.Entry<String, SheetImpl> entry : sheets.entrySet()) {
            SheetImpl sheet = entry.getValue();
            Permission permission = sheetManager.getLastApprovedPermission(sheet.getSheetName(), userName);
            String perType;

            if(sheet.getOwner().equals(userName))
                perType = PermissionType.OWNER.toString();
            else if(permission==null)
            {
                perType = "NONE";
            }
            else
                perType = permission.getPermissionType().toString();

            jsonBuilder.append("{")  // תחילת אובייקט JSON
                    .append("\"uploader\":\"").append(sheet.getOwner()).append("\",")
                    .append("\"sheetName\":\"").append(sheet.getSheetName()).append("\",")
                    .append("\"size\":\"").append(sheet.getNumRows()).append("x").append(sheet.getNumCols()).append("\",")
                    .append("\"permission\":\"").append(perType).append("\"")
                    .append("},");
        }
        // הסרת הפסיק האחרון אם יש פריטים ברשימה
        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("]");

        // הגדרת סוג התגובה ל-JSON
        response.setContentType("application/json");
        response.getWriter().write(jsonBuilder.toString());
    }
}
