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

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.daos.DocumentDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.ThymeleafTemplateEngineCreator;

@WebServlet("/SelectSubdirectory")
public class SelectSubdirectoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
       
    public SelectSubdirectoryServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    	templateEngine = ThymeleafTemplateEngineCreator.getTemplateEngine(servletContext);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int subdirectoryId;
		try {
			subdirectoryId = Integer.parseInt(request.getParameter("subdirectoryId"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		Subdirectory subdirectory;
		List<Document> documents = null;
		DocumentDAO documentDAO = new DocumentDAO(connection);
		try {
			subdirectory = (Subdirectory) directoryDAO.findDirectoryById(subdirectoryId);
			if (subdirectory.getUserId() != ((User) request.getSession().getAttribute("user")).getUserId()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not allowed to access this subdirectory because you are not the owner.");
				return;
			}
			documents = documentDAO.findAlldocumentsOfSubdirectory(subdirectoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, please retry later");
			return;
		} catch (ClassCastException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Subdirectory not found");
			return;
		}
		ServletContext servletContext = getServletContext();
		String path = "/WEB-INF/templates/documents.html";
		final WebContext ctx = new WebContext(request, response, servletContext, new Locale((String) request.getSession().getAttribute("language")));
		ctx.setVariable("subdirectory", subdirectory);
		ctx.setVariable("documents", documents);
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
