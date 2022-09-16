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

import it.polimi.tiw.beans.Directory;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.utils.ConnectionCreator;

@WebServlet("/CreateSubdirectory")
public class CreateSubdirectoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public CreateSubdirectoryServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String subdirectoryName = request.getParameter("name");
		if (subdirectoryName == null || subdirectoryName.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing subdirectory name");
			return;
		}
		int directoryId;
		try {
			directoryId = Integer.parseInt(request.getParameter("directoryId"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong parameters format");
			return;
		}
		Date creationDate = (Date) request.getAttribute("creationDate");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		try {
			Directory directory = directoryDAO.findDirectoryById(directoryId);
			int userId = ((User) request.getSession().getAttribute("user")).getUserId();
			if (directory.getUserId() != userId) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to access this directory");
				return;
			}
			directoryDAO.createSubdirectory(subdirectoryName, creationDate, directoryId, ((User) request.getSession().getAttribute("user")).getUserId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in subdirectory creation, retry with another name");
			return;
		} catch (NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Directory not found in your directory tree");
			return;
		}
		response.sendRedirect("GoToHome");
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
