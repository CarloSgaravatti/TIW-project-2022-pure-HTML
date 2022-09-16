package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.Document;
import it.polimi.tiw.beans.Subdirectory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.daos.DocumentDAO;
import it.polimi.tiw.utils.ConnectionCreator;

@WebServlet("/GetDocumentByPath")
public class GetDocumentByPathServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
    public GetDocumentByPathServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String path = request.getParameter("path");
		if (!path.contains("/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You haven't inserted a correct path");
			return;
		}
		int directoryNameFinalPos = path.indexOf("/");
		String directoryName = path.substring(0, directoryNameFinalPos);
		String relativePath = path.substring(directoryNameFinalPos + 1);
		if (!relativePath.contains("/")) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You haven't inserted a correct path");
			return;
		}
		int subdirectoryNameFinalPos = relativePath.indexOf("/");
		String subdirectoryName = relativePath.substring(0, subdirectoryNameFinalPos);
		String documentName = relativePath.substring(subdirectoryNameFinalPos + 1);
		Directory directory;
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		Subdirectory subdirectory;
		Document document;
		try {
			directory = directoryDAO.findDirectoryByNameAndUser(directoryName, user.getUserId());
			subdirectory = directoryDAO.findSubdirectoryByNameAndFatherDirectory(subdirectoryName, directory.getDirectoryId());
			document = new DocumentDAO(connection).findDocumentFromNameAndSubdirectory(documentName, subdirectory.getDirectoryId());
			String responsePath = "AccessDocument?documentId=" + document.getDocumentId();
			response.sendRedirect(responsePath);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error, please retry later");
		} catch (NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wasn't able to find such a document");
		}
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
