package handler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.RespBean;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class AuthenticationAccessDeniedHandler implements AccessDeniedHandler{
	@Override
	public void handle(HttpServletRequest httpServletRequest,HttpServletResponse resp,AccessDeniedException e) throws IOException{
		resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		resp.setContentType("appication/json;charset=UTF-8");
		PrintWriter out=resp.getWriter();
		RespBean error=RespBean.error("权限不足！请联系管理员");
		out.write(new ObjectMapper().writeValueAsString(error));
		out.flush();
		out.close();
	}

}
