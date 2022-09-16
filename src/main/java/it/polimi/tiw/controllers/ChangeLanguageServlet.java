package it.polimi.tiw.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/ChangeLanguage")
public class ChangeLanguageServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public ChangeLanguageServlet() {
        super();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		session.setAttribute("language", request.getParameter("lan"));
		System.out.println("Changed language of user to " + request.getParameter("lan"));
		String lastPath = request.getHeader("Referer");  //Referer contains source page from which request comes
		String appendQueryString = "langChanged=y";
		boolean isQueryStringPresent = lastPath.contains("?");
		if (isQueryStringPresent) lastPath = lastPath + "&" + appendQueryString;
		else lastPath = lastPath + "?" + appendQueryString;
		response.sendRedirect(lastPath);
	}
}
