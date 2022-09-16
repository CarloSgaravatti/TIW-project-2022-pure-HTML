package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.daos.DocumentDAO;
import it.polimi.tiw.utils.ConnectionCreator;
import it.polimi.tiw.utils.Pair;

@WebServlet("/MoveDocument")
public class MoveDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public MoveDocumentServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int documentId;
		try {
			documentId = Integer.parseInt(request.getParameter("documentId"));
		} catch(NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		DocumentDAO documentDAO = new DocumentDAO(connection);
		Pair<Document, Subdirectory> pair = null;
		try {
			pair = documentDAO.findDocumentAndSubdirectory(documentId);
		} catch(SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, please retry later");
			return;
		}
		if (pair == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document not found");
		} else if (pair.getSecondElement().getUserId() != ((User) request.getSession().getAttribute("user")).getUserId()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not allowed to move this document because you are not the owner");
		} else {
			request.setAttribute("documentToMove", pair.getFirstElement());
			request.setAttribute("sourceSubdirectory", pair.getSecondElement());
			String path = "/GoToHome";
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(path);
			requestDispatcher.forward(request, response);
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int documentId, subdirectoryId;
		try {
			documentId = Integer.parseInt(request.getParameter("documentId"));
			subdirectoryId = Integer.parseInt(request.getParameter("subdirectoryId"));
		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		DocumentDAO documentDAO = new DocumentDAO(connection);
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		Subdirectory subdirectory;
		Pair<Document, Subdirectory> pair;
		try {
			subdirectory = (Subdirectory) directoryDAO.findDirectoryById(subdirectoryId);
			pair = documentDAO.findDocumentAndSubdirectory(documentId);
			if (subdirectory.getUserId() != ((User) request.getSession().getAttribute("user")).getUserId() ||
					pair.getSecondElement().getUserId() != ((User) request.getSession().getAttribute("user")).getUserId()) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You are not allowed to access this subdirectory because you are not the owner.");
				return;
			}
			documentDAO.moveDocument(documentId, subdirectoryId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wasn't able to move the document in the specified directory");
			return;
		} catch (ClassCastException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Document not found");
			return;
		}
		String path = "SelectSubdirectory?subdirectoryId=" + subdirectoryId;
		response.sendRedirect(path);
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
