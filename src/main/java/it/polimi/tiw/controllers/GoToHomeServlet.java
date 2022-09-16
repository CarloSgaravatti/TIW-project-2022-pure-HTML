package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.ThymeleafTemplateEngineCreator;

@WebServlet("/GoToHome")
public class GoToHomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
       
    public GoToHomeServlet() {
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
		List<String> newNavigationHistory = new ArrayList<>();
		newNavigationHistory.add("GoToHome");
		user.setNavigationHistory(newNavigationHistory);
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		List<Directory> userDirectoryTree;
		try {
			userDirectoryTree = directoryDAO.findDirectoriesAndSubdirectoriesOfUser(user);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, please retry later");
			return;
		}		
		ServletContext servletContext = getServletContext();
		String path = "/WEB-INF/templates/home.html";
		final WebContext ctx = new WebContext(request, response, servletContext, new Locale((String) request.getSession().getAttribute("language")));
		ctx.setVariable("directoryTree", userDirectoryTree);
		Document documentToMove = (Document) request.getAttribute("documentToMove");
		Subdirectory sourceSubdirectory = (Subdirectory) request.getAttribute("sourceSubdirectory");
		if (documentToMove != null && sourceSubdirectory != null) {
			ctx.setVariable("documentToMove", documentToMove);
			ctx.setVariable("sourceSubdirectory", sourceSubdirectory);
		}
		templateEngine.process(path, ctx, response.getWriter());
	}

	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
