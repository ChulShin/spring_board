package com.chuls.persistence;

import javax.inject.Inject;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import com.chuls.domain.MemberVO;

@Service
public class MemberDAOImpl implements MemberDAO{

	// 마이바티스
	@Inject
	private SqlSession sql;
	
	// 매퍼
	private static String namespace = "com.chuls.mappers.memberMapper";
	
	// 회원가입
	@Override
	public void register(MemberVO vo) throws Exception {
		sql.insert(namespace + ".register", vo);
	}

	// 로그인
	@Override
	public MemberVO login(MemberVO vo) throws Exception {
		return sql.selectOne(namespace + ".loginBcrypt", vo);
	}

	// 회원정보 수정
	@Override
	public void modify(MemberVO vo) throws Exception {
		sql.update(namespace + ".modify", vo);
	}

	// 회원 탈퇴
	@Override
	public void withdrawal(MemberVO vo) throws Exception {
		sql.delete(namespace + ".withdrawal", vo);
	}

	// 아이디 확인
	@Override
	public MemberVO idCheck(String userId) throws Exception {
		return sql.selectOne(namespace + ".idCheck", userId);
	}

	// 닉네임 확인
	@Override
	public MemberVO userNameCheck(String userName) throws Exception {
		return sql.selectOne(namespace + ".userNameCheck", userName);
	}

	// 비밀번호 확인
	@Override
	public MemberVO userPassCheck(String userId) throws Exception {
		return sql.selectOne(namespace + ".userPassCheck", userId);
	}
}