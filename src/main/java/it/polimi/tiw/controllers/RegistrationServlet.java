package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UsersDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.ThymeleafTemplateEngineCreator;

@WebServlet("/Registration")
public class RegistrationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
	
    public RegistrationServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	connection = ConnectionCreator.newConnection(getServletContext());
    	ServletContext servletContext = getServletContext();
    	templateEngine = ThymeleafTemplateEngineCreator.getTemplateEngine(servletContext);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String path = "/WEB-INF/registration.html";
		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String repeatedPassword = request.getParameter("repeatedPassword");
		Pattern emailPattern = Pattern.compile("^(.+)@(.+)$");
		boolean emailValid = emailPattern.matcher(email).matches();
		boolean isBadRequest = true;
		String badRequestMessage = "";
		if (username == null || password == null || repeatedPassword == null || email == null ||
				username.isBlank() || password.isBlank() || repeatedPassword.isBlank() || email.isBlank()) {
			badRequestMessage = "Missing parameters";
		} else if (!emailValid) {
			badRequestMessage = "Email not valid";
		} else if (!password.equals(repeatedPassword)) {
			badRequestMessage = "Password and repeated password are different";
		} else if (password.length() < 8) {
			badRequestMessage = "Password must be at least of 8 characters";
		} else {
			isBadRequest = false;
		}
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, badRequestMessage);
			return;
		}
		UsersDAO usersDAO = new UsersDAO(connection);
		User user = null;
		try {
			user = usersDAO.createUser(username, email, password);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, retry later");
			return;
		}
		if (user == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username or password not correct");
			return;
		}
		HttpSession session = request.getSession(true);
		session.setAttribute("user", user);
		session.setAttribute("language", request.getLocale().getLanguage());
		response.sendRedirect("GoToHome");
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}

}
