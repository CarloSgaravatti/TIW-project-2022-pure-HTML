package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.daos.DocumentDAO;
import it.polimi.tiw.utils.ConnectionCreator;

@WebServlet("/CreateDocument")
public class CreateDocumentServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public CreateDocumentServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String documentName = request.getParameter("name");
		String summary = request.getParameter("summary");
		String type = request.getParameter("type");
		if (documentName == null || summary == null || documentName.isBlank() || summary.isBlank() ||
				type == null || type.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
			return;
		}
		if (type.charAt(0) != '.') {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not a valid document type");
			return;
		}
		int subdirectoryId;
		try {
			subdirectoryId = Integer.parseInt(request.getParameter("subdirectoryId"));
		} catch(NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		Date creationDate = (Date) request.getAttribute("creationDate");
		DocumentDAO documentDAO = new DocumentDAO(connection);
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		try {
			Subdirectory subdirectory = (Subdirectory) directoryDAO.findDirectoryById(subdirectoryId);
			if (subdirectory.getUserId() != ((User) request.getSession().getAttribute("user")).getUserId()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to access this subdirectory because you are not the owner");
				return;
			}
			documentDAO.createDocument(documentName, creationDate, summary, subdirectoryId, type);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in document creation, try with another name");
			return;
		} catch (NullPointerException | ClassCastException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Subdirectory not found in your directory tree");
			return;
		}
		response.sendRedirect("GoToHome");
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
