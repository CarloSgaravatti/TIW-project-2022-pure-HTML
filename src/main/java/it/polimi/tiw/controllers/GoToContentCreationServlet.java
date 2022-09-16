package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.ThymeleafTemplateEngineCreator;

@WebServlet("/GoToContentCreation")
public class GoToContentCreationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
       
    public GoToContentCreationServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    	templateEngine = ThymeleafTemplateEngineCreator.getTemplateEngine(servletContext);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		List<Subdirectory> userSubdirectories = null;
		List<Directory> userDirectories = null;
		try {
			userSubdirectories = directoryDAO.getAllSubirectories(user);
			userDirectories = directoryDAO.getAllDirectories(user);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server erro, please retry later");
			return;
		}
		ServletContext servletContext = getServletContext();
		String path = "/WEB-INF/templates/contentCreation.html";
		final WebContext ctx = new WebContext(request, response, servletContext, new Locale((String) request.getSession().getAttribute("language")));
		ctx.setVariable("directories", userDirectories);
		ctx.setVariable("subdirectories", userSubdirectories);
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}

}
