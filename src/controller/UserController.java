package controller;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import action.ActionAnnotation;
import action.RequestMapping;
import action.RequestMapping.RequestMethod;
import model.User;
import repository.MybatisUserDao;
import util.Gmail;
import util.KakaoAPI;
import util.SHA256;

@SuppressWarnings("serial")
public class UserController extends ActionAnnotation {

	@Override
	public void initProcess(HttpServletRequest request, HttpServletResponse response) {

	}
	@RequestMapping(value = "kakaoLoginForm", method = RequestMethod.GET)
	public String kakaoLoginForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
//		if -> īī�� �������� �α������� ��, �ش� ������ ��ġ�ϴ� userId�� �����ϴ��� üũ �� �������� ������ �Ʒ� ������ �ް� �����ϸ�(������ ������ �ߴٸ�) �ٷ� ���ǿ� ������ �������� ������.
		String code = request.getParameter("code");
		String error = request.getParameter("error"); // ���߿� Ȱ���� �� ����
		request.setAttribute("error", error);
		if(code != null) {
			KakaoAPI kakao = new KakaoAPI();
			String access_Token = kakao.getAccessToken(code);
			HashMap<String, Object> userInfo = kakao.getUserInfo(access_Token);
			if (userInfo.get("nickname") != null) {
				request.setAttribute("userId", userInfo.get("email"));
		    	request.setAttribute("userName", userInfo.get("nickname"));
		    }
		}
		
		return "/WEB-INF/view/user/kakaoLoginForm.jsp";
	}
	

	@RequestMapping(value = "loginForm", method = RequestMethod.GET)
	public String loginForm(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		return "/WEB-INF/view/user/loginForm.jsp";
	}

	@RequestMapping(value = "loginPro", method = RequestMethod.POST)
	public String loginPro(HttpServletRequest request, HttpServletResponse response) throws Exception {

		return "/WEB-INF/view/member/loginPro.jsp";
	}

	@RequestMapping(value = "logoutForm", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		KakaoAPI kakao = new KakaoAPI();
		kakao.kakaoLogout((String) session.getAttribute("access_Token"));
		session.removeAttribute("access_Token");
		session.removeAttribute("userId");
		return "/WEB-INF/view/user/loginForm.jsp";
	}

	// ȸ������ ��
	@RequestMapping(value = "joinForm", method = RequestMethod.GET)
	public String joinForm(HttpServletRequest request, HttpServletResponse response) throws Exception {

		return "/WEB-INF/view/user/joinForm.jsp";
	}

	// ȸ������ ó�� (�̸��� ����)
	@RequestMapping(value = "joinPro", method = RequestMethod.POST)
	public String joinPro(HttpServletRequest request, HttpServletResponse response) throws Exception {

		request.setCharacterEncoding("utf-8");

		HttpSession session = request.getSession();
		String userId = request.getParameter("userId");
		String userPasswd = request.getParameter("userPasswd");
		String userName = request.getParameter("userName");
		String userEmail = request.getParameter("userEmail");
		String userEmailHash = SHA256.getSHA256(userEmail);
		int userEmailCheck = 0;
		String userPhone = request.getParameter("phone1") + request.getParameter("phone2")
				+ request.getParameter("phone3");
		String userAddress = request.getParameter("userAddress");

		MybatisUserDao service = MybatisUserDao.getInstance();

		User user = new User();

		user.setUserId(userId);
		user.setUserPasswd(userPasswd);
		user.setUserName(userName);
		user.setUserEmail(userEmail);
		user.setUserEmailHash(userEmailHash);
		user.setUserEmailCheck(userEmailCheck);
		user.setUserPhone(userPhone);
		user.setUserAddress(userAddress);

		service.joinUser(user);
		session.setAttribute("userId", userId);

		return "redirect:/user/joinSendEmail";
	}

	// �������� ������
	@RequestMapping(value = "joinSendEmail", method = RequestMethod.GET)
	public String joinSendEmail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("utf-8");

		MybatisUserDao service = MybatisUserDao.getInstance();
		HttpSession session = request.getSession();

		String userId = null;

		// ���ǿ� ����� id�� null�� �ƴ϶�� �� ����
		if (session.getAttribute("userId") != null) {
			userId = (String) session.getAttribute("userId");
		}

		if (userId == null) {
			// userId�� ���ٸ� �α��������� �̵�
			PrintWriter script = response.getWriter();
			script.println("<script>");
			script.println("alert('�α����� ���ּ���.');");
			script.println("location.href = '/zSpringProject/user/loginForm'");
			script.println("</script>");
			script.close();

			return "redirect:/user/loginForm";
		}

		int emailChecked = service.getUserEmailChecked(userId);
		
		if (emailChecked == 1) {
			PrintWriter script = response.getWriter();
			script.println("<script>");
			script.println("alert('�̹� ���� �� ȸ���Դϴ�.');");
			script.println("location.href = '/zSpringProject/main/main'");
			script.println("</script>");
			script.close();

			return "redirect:/main/main";
		}else if (emailChecked == 0) {
			// ����ڿ��� ���� �̸��� ������ �Է�
			String host = "http://localhost:8080/zSpringProject/user/";
			String from = "oakNutSpring@gmail.com";
			String to = service.getUserEmail(userId);

			String subject = "���丮���� ȸ������ �̸��� ���������Դϴ�!";
			String content = "���� ��ũ�� �����Ͽ� �̸��� Ȯ���� �������ּ���:D" +

					"<a href='" + host + "joinEmailCheckPro?code=" + new SHA256().getSHA256(to) + "'>�̸��� �����ϱ�</a>";

			// SMTP�� �����ϱ� ���� ������ �Է��ϴ� �κ�
			Properties p = new Properties();
			p.put("mail.smtp.user", from);
			p.put("mail.smtp.host", "smtp.googlemail.com");
			p.put("mail.smtp.port", "465");
			p.put("mail.smtp.starttls.enable", "true");
			p.put("mail.smtp.auth", "true");
			p.put("mail.smtp.debug", "true");
			p.put("mail.smtp.socketFactory.port", "465");
			p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			p.put("mail.smtp.socketFactory.fallback", "false");

			try {

				Authenticator auth = new Gmail();
				Session ses = Session.getInstance(p, auth);
				ses.setDebug(true);
				MimeMessage msg = new MimeMessage(ses);
				msg.setSubject(subject);
				Address fromAddr = new InternetAddress(from);
				msg.setFrom(fromAddr);
				Address toAddr = new InternetAddress(to);
				msg.addRecipient(Message.RecipientType.TO, toAddr);
				msg.setContent(content, "text/html;charset=UTF-8");
				Transport.send(msg);
			} catch (Exception e) {

				e.printStackTrace();
				PrintWriter script = response.getWriter();
				script.println("<script>");
				script.println("alert('������ �߻��߽��ϴ�..');");
				script.println("history.back();");
				script.println("</script>");
				script.close();

				return "redirect:/user/joinForm";

			}
			return "/WEB-INF/view/user/joinSendEmail.jsp";
		}
		return "/WEB-INF/view/user/joinSendEmail.jsp";
	}
	
	
	//���� ���� Ȯ��
	@RequestMapping(value="joinEmailCheckPro", method=RequestMethod.GET)
	public String joinEmailCheckPro(HttpServletRequest request, HttpServletResponse response) throws Exception  {
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("utf-8");

		HttpSession session = request.getSession();
		String code = request.getParameter("code");
		
		System.out.println(code);
		
		String userId = null;
		MybatisUserDao service = MybatisUserDao.getInstance();
		
		if(session.getAttribute("userId") != null) {
			userId = (String)session.getAttribute("userId");
		}else if(userId == null) {

			PrintWriter script = response.getWriter();
			script.println("<script>");
			script.println("alert('�α����� ���ּ���.');");
			script.println("location.href = '/zSpringProject/user/loginForm'");
			script.println("</script>");
			script.close();
		}
		
		String userEmail = service.getUserEmail(userId);
		
		//�����ڵ�� ��� ����� �ڵ� Ȯ��
		boolean rightCode = (new SHA256().getSHA256(userEmail).equals(code)) ? true : false;
		System.out.println(rightCode);
		if(rightCode == true) {
			service.setUserEmailChecked(userId);
			PrintWriter script = response.getWriter();
			script.println("<script>");
			script.println("alert('������ �����߽��ϴ�.');");
			script.println("location.href = '/zSpringProject/main/main'");
			script.println("</script>");
			script.close();		
		}else {
			
			PrintWriter script = response.getWriter();
			script.println("<script>");
			script.println("alert('��ȿ���� ���� �ڵ��Դϴ�.');");
			script.println("location.href = '/zSpringProject/main/main'");
			script.println("</script>");
			script.close();		
			return "redirect:/main/main";
		}

		return "redirect:/main/main";
	}
}
