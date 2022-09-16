package it.polimi.tiw.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.polimi.tiw.beans.User;

public class PreviousPageFilter extends HttpFilter implements Filter {
	private static final long serialVersionUID = 1L;

    public PreviousPageFilter() {
        super();
    }

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {		
		chain.doFilter(request, response);
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String isFromPreviousPage = req.getParameter("prevPage");
		String isFromChangeLanguage = req.getParameter("langChanged");
		int responseStatus = res.getStatus();
		if (responseStatus == 200 && (isFromPreviousPage == null || !isFromPreviousPage.equals("y")) &&
				(isFromChangeLanguage == null || !isFromChangeLanguage.equals("y"))) {
			String relativePath = req.getServletPath().substring(1); //to eliminate first /
			if (req.getPathInfo() != null) relativePath += req.getPathInfo();
			if (req.getQueryString() != null) relativePath += "?" + req.getQueryString();
			User user = (User) req.getSession().getAttribute("user");
			user.addNewPage(relativePath);
		}
	}
}
