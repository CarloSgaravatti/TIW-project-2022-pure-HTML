package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
import it.polimi.tiw.daos.DocumentDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.Pair;
import it.polimi.tiw.utils.ThymeleafTemplateEngineCreator;

@WebServlet("/AccessDocument")
public class AccessDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine = null;
       
    public AccessDocumentServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    	templateEngine = ThymeleafTemplateEngineCreator.getTemplateEngine(servletContext);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int documentId;
		try {
			documentId = Integer.parseInt(request.getParameter("documentId"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		DocumentDAO documentDAO = new DocumentDAO(connection);
		Pair<Document, Subdirectory> pair = null;
		try {
			pair = documentDAO.findDocumentAndSubdirectory(documentId);
			if (pair.getSecondElement().getUserId() != ((User) request.getSession().getAttribute("user")).getUserId()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not allowed to access this document because you are not the owner");
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, please retry later");
			return;
		} catch (NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document not found");
			return;
		}
		ServletContext servletContext = getServletContext();
		String path = "/WEB-INF/templates/document.html";
		final WebContext ctx = new WebContext(request, response, servletContext, new Locale((String) request.getSession().getAttribute("language")));
		ctx.setVariable("subdirectory", pair.getSecondElement());
		ctx.setVariable("document", pair.getFirstElement());
		templateEngine.process(path, ctx, response.getWriter());
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
