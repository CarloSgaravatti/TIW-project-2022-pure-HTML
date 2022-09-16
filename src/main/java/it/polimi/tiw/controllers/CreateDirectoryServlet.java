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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.DirectoryDAO;
import it.polimi.tiw.utils.ConnectionCreator;

@WebServlet("/CreateDirectory")
public class CreateDirectoryServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public CreateDirectoryServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
    	ServletContext servletContext = getServletContext();
    	connection = ConnectionCreator.newConnection(servletContext);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String directoryName = request.getParameter("name");
		if (directoryName == null || directoryName.isBlank()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing directory name");
			return;
		}
		Date creationDate = (Date) request.getAttribute("creationDate");
		DirectoryDAO directoryDAO = new DirectoryDAO(connection);
		try {
			directoryDAO.createDirectory(directoryName, creationDate, ((User) request.getSession().getAttribute("user")).getUserId());
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in directory creation, retry with another name");
			return;
		}
		response.sendRedirect("GoToHome");
	}
	
	@Override
	public void destroy() {
		ConnectionCreator.closeConnection(connection);
	}
}
