package repository;


import org.apache.ibatis.session.SqlSession;

import model.User;
import mybatis.AbstractRepository;

public class MybatisUserDao extends AbstractRepository{

	private final String namespace = "mybatis.UserMapper";
	private static MybatisUserDao instance = new MybatisUserDao();
	
	private MybatisUserDao() {	}
	
	public static MybatisUserDao getInstance() {
		return instance;
	}
	
	//ȸ������
	public void joinUser(User user) {
		SqlSession sqlSession = getSqlSessionFactory().openSession();
		String statement = null;
		try {
			statement = namespace + ".joinUser";
			sqlSession.insert(statement, user);
			sqlSession.commit();
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			sqlSession.close();
		}
	}
	
	//�̸��� üũ
	public int getUserEmailChecked(String userId) {
		int checked = 0;
		SqlSession sqlSession = getSqlSessionFactory().openSession();
		String statement = null;
		try {
			statement = namespace + ".getUserEmailChecked";
			checked = sqlSession.selectOne(statement, userId);
			if(checked==1) {
				checked = 1;
			}else {
				checked = 0;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			sqlSession.close();
		}
		
		return checked;
	}

	//ȸ�� �̸��� ��ȸ
	public String getUserEmail(String userId) {
		String userEmail = null;
		SqlSession sqlSession = getSqlSessionFactory().openSession();
		String statement = null;
		
		try {
			statement = namespace + ".getUserEmail";
			userEmail = sqlSession.selectOne(statement, userId);
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			sqlSession.close();
		}
		
		return userEmail;
	}
	
	//�̸��� ���� Ȯ��
	public int setUserEmailChecked(String userId) {
		int checked = 0;
		SqlSession sqlSession = getSqlSessionFactory().openSession();
		String statement = null;
		try {
			statement = namespace + ".setUserEmailChecked";
			checked = sqlSession.selectOne(statement, userId);
			System.out.println(checked);
			sqlSession.commit();
			return checked;
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			sqlSession.close();
		}
		return checked;
	}
}
