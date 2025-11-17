package shticell.servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.dto.PermissionRequestDto;
import shticell.manage.Permission;
import shticell.manage.SheetManager;
import shticell.utils.ServletUtils;
import shticell.utils.SessionUtils;

import java.io.IOException;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import static shticell.constants.Constants.USERNAME;

//@WebServlet("/permissions")
public class PermissionServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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

        Gson g = new Gson();
        String text = request.getReader().lines().collect(Collectors.joining("\n"));
        PermissionRequestDto permissionRequestDto = g.fromJson(text, PermissionRequestDto.class);

        SheetManager sheetManager = ServletUtils.getSheetManager(getServletContext());
        try {
            sheetManager.requestPermission(permissionRequestDto);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Permission requested.");
        } catch (Exception e) {
            e.printStackTrace(); // Add this to log the exception
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
        }
    }

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
        // קבלת שם הגיליון מהבקשה
        String sheetName = request.getParameter("sheetName");

        SheetManager sheetManager =  ServletUtils.getSheetManager(getServletContext());
        List<Permission> permissionList = sheetManager.getPermissions(sheetName);
        Gson g = new Gson();
        String json = g.toJson(permissionList);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}