package it.polimi.tiw.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.User;

@WebServlet("/GoToPreviousPage")
public class GoToPreviousPageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public GoToPreviousPageServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<String> navigationHistory = ((User) request.getSession().getAttribute("user")).getNavigationHistory();
		String previousPage = navigationHistory.get(navigationHistory.size() - 2);
		navigationHistory.remove(navigationHistory.get(navigationHistory.size() - 1));
		((User) request.getSession().getAttribute("user")).setNavigationHistory(navigationHistory);
		String newPageToGo;
		boolean isQueryStringPresent = previousPage.contains("?");
		if (isQueryStringPresent) newPageToGo = previousPage + "&prevPage=y";
		else newPageToGo = previousPage + "?prevPage=y";
		response.sendRedirect(newPageToGo);
	}
}
