package com.chuls.controller;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chuls.domain.BoardVO;
import com.chuls.domain.Criteria;
import com.chuls.domain.MemberVO;
import com.chuls.domain.PageMaker;
import com.chuls.domain.ReplyVO;
import com.chuls.domain.SearchCriteria;
import com.chuls.service.BoardService;
import com.chuls.service.ReplyService;

@Controller
@RequestMapping("/board/*")
public class BoardController {

	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
   
	@Inject
	BoardService service;
	
	@Inject
	ReplyService RepService;
	
	// 글 작성 get
	@RequestMapping(value = "/write", method = RequestMethod.GET)
		public void getWrite(HttpSession session, Model model) throws Exception {
		logger.info("get write");
		
		Object loginInfo = session.getAttribute("member");
		
		if(loginInfo == null) {
			model.addAttribute("msg", "login_error");
		}
	}
	
	// 글 작성 post
	@RequestMapping(value = "/write", method = RequestMethod.POST)
	public String postWrite(BoardVO vo, HttpSession session, RedirectAttributes rttr) throws Exception {
		logger.info("post write");
		
		MemberVO member = (MemberVO) session.getAttribute("member");
		if(member == null || !member.getUserName().equals(vo.getWriter())) {
			rttr.addFlashAttribute("msg", "POST_write_error1");
			return "redirect:/board/listSearch";
		}
		
		if(vo.getTitle() == null || vo.getTitle().trim().isEmpty() || vo.getContent() == null || vo.getContent().trim().isEmpty()) {
			rttr.addFlashAttribute("msg", "POST_write_error2");
			return "redirect:/board/listSearch";
		}
		
		service.write(vo);
		rttr.addAttribute("bno", service.lastBoard().getBno());
		return "redirect:/board/read";
	}
	
	// 글 목록
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public void list(Model model) throws Exception {
		logger.info("get list");
		
		List<BoardVO> list = service.list();
		
		model.addAttribute("list", list);
	}
	
	// 글 조회
	@RequestMapping(value = "/read", method = RequestMethod.GET)
	public void getRead(@RequestParam("bno") int bno,
						@ModelAttribute("scri") SearchCriteria scri, Model model, HttpSession session) throws Exception {
		logger.info("get read");
		
		BoardVO vo = service.read(bno);
		MemberVO member = (MemberVO) session.getAttribute("member");
		
		if(member == null || !member.getUserName().equals(vo.getWriter())){
			model.addAttribute("msg", "hide_modify_delete_btn");
		}
		model.addAttribute("read", vo);
		model.addAttribute("scri", scri);
		
		List<ReplyVO> repList = RepService.readReply(bno);
		model.addAttribute("repList", repList);
	}
	
	// 글 수정
	@RequestMapping(value = "/modify", method = RequestMethod.GET)
	public String getModify(@RequestParam("bno") int bno,
						@ModelAttribute("scri") SearchCriteria scri, Model model, HttpSession session, RedirectAttributes rttr) throws Exception {
		logger.info("get modify");
		
		MemberVO member = (MemberVO) session.getAttribute("member");
		if(member == null) {
			return "redirect:/board/listSearch";
		}
		
		BoardVO vo = service.read(bno);
		
		model.addAttribute("modify", vo);
		
		if(!member.getUserName().equals(vo.getWriter())){
			rttr.addFlashAttribute("msg", "modify_error");
			return "redirect:/board/listSearch";
		}
		
		model.addAttribute("scri", scri);
		
		return "/board/modify";
	}   
	
	// 글 삭제
	@RequestMapping(value = "/delete", method = RequestMethod.GET)
	public String getDelete(@RequestParam("bno") int bno,
						@ModelAttribute("scri") SearchCriteria scri, Model model, HttpSession session, RedirectAttributes rttr) throws Exception {
		logger.info("get delete");
		
		MemberVO member = (MemberVO) session.getAttribute("member");
		if(member == null) {
			return "redirect:/board/listSearch";
		}

		BoardVO vo = service.read(bno);
		
		if(!member.getUserName().equals(vo.getWriter())){
			rttr.addFlashAttribute("msg", "delete_error");
			return "redirect:/board/listSearch";
		}
		
		model.addAttribute("delete", bno);
		model.addAttribute("scri", scri);
		
		return "/board/delete";
	}
	
	// 글 수정  POST   
	@RequestMapping(value = "/modifyPOST", method = RequestMethod.POST)
	public String postModify(BoardVO vo,
				@ModelAttribute("scri") SearchCriteria scri, RedirectAttributes rttr, HttpSession session, Model model) throws Exception {
		logger.info("post modify");

		rttr.addAttribute("page", scri.getPage());
		rttr.addAttribute("perPageNum", scri.getPerPageNum());
		rttr.addAttribute("searchType", scri.getSearchType());
		rttr.addAttribute("keyword", scri.getKeyword());
		
		if(vo.getTitle() == null || vo.getTitle().trim().isEmpty() || vo.getContent() == null || vo.getContent().trim().isEmpty()) {
			rttr.addFlashAttribute("msg", "modify_error1");
			return "redirect:/board/listSearch";
		}
		

		MemberVO member = (MemberVO) session.getAttribute("member");
		BoardVO writerCheck = service.read(vo.getBno());
		
		String writer = writerCheck.getWriter();
		String userName = member.getUserName();
		
		if(writer != null && userName != null && userName.equals(writer) && writer.equals(vo.getWriter())) {
			service.update(vo);
		} else {
			rttr.addFlashAttribute("msg", "modify_error");
		}
		
		return "redirect:/board/listSearch";
	}
	
	// 글 삭제  POST
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public String postDelete(@RequestParam("bno") int bno,
				@ModelAttribute("scri") SearchCriteria scri, RedirectAttributes rttr, HttpSession session, Model model) throws Exception {
		logger.info("post delete");
		MemberVO member = (MemberVO) session.getAttribute("member");
		BoardVO writerCheck = service.read(bno);
		
		String writer = writerCheck.getWriter();
		String userName = member.getUserName();
		
		if(userName.equals(writer)) {
			service.delete(bno);
		} else {
			rttr.addFlashAttribute("msg", "delete_error");
		}
		rttr.addAttribute("page", scri.getPage());
		rttr.addAttribute("perPageNum", scri.getPerPageNum());
		rttr.addAttribute("searchType", scri.getSearchType());
		rttr.addAttribute("keyword", scri.getKeyword());
		
		return "redirect:/board/listSearch";
	}
	
	// 글 목록 + 페이징
	@RequestMapping(value = "/listPage", method = RequestMethod.GET)
	public void listPage(@ModelAttribute("cri") Criteria cri, Model model) throws Exception {
		logger.info("get list page");
		
		List<BoardVO> list = service.listPage(cri);
		model.addAttribute("list", list);
	
		PageMaker pageMaker = new PageMaker();
		pageMaker.setCri(cri);
		pageMaker.setTotalCount(service.listCount());
		model.addAttribute("pageMaker", pageMaker);
	}
	
	// 글 목록 + 페이징 + 검색
	@RequestMapping(value = "/listSearch", method = RequestMethod.GET)
	public void listSearch(HttpSession session, @ModelAttribute("scri") SearchCriteria scri, Model model) throws Exception {
		logger.info("get list search");

		Object loginInfo = session.getAttribute("member");
		
		if(loginInfo == null) {
			model.addAttribute("msg", "login_error");
		}
		
		List<BoardVO> list = service.listSearch(scri);
		model.addAttribute("list", list);

		PageMaker pageMaker = new PageMaker();
		pageMaker.setCri(scri);
		// pageMaker.setTotalCount(service.listCount());
		pageMaker.setTotalCount(service.countSearch(scri));
		model.addAttribute("pageMaker", pageMaker);
	}
	
	// 댓글 작성
	@RequestMapping(value = "/replyWrite", method = RequestMethod.POST)
	public String replyWrite(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
		logger.info("reply write");
		
		rttr.addAttribute("bno", vo.getBno());
		rttr.addAttribute("page", scri.getPage());
		rttr.addAttribute("perPageNum", scri.getPerPageNum());
		rttr.addAttribute("searchType", scri.getSearchType());
		rttr.addAttribute("keyword", scri.getKeyword());

		if(vo.getWriter() == null || vo.getWriter().trim().isEmpty() || vo.getContent() == null || vo.getContent().trim().isEmpty()) {
			rttr.addAttribute("msg", "replyWrite_error");
			return "redirect:/board/read";
		}
		
		RepService.writeReply(vo);

		return "redirect:/board/read";
	}
	
	// 댓글 수정 POST
	@RequestMapping(value = "/replyUpdate", method = RequestMethod.POST)
	public String replyUpdate(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
		logger.info("reply update");
		
		rttr.addAttribute("bno", vo.getBno());
		rttr.addAttribute("page", scri.getPage());
		rttr.addAttribute("perPageNum", scri.getPerPageNum());
		rttr.addAttribute("searchType", scri.getSearchType());
		rttr.addAttribute("keyword", scri.getKeyword());

		if(vo.getContent() == null || vo.getContent().trim().isEmpty()) {
			rttr.addAttribute("msg", "replyUpdate_error");
			return "redirect:/board/read";
		}
		
		RepService.replyUpdate(vo);

		return "redirect:/board/read";
	}

	// 댓글 삭제 POST
	@RequestMapping(value = "/replyDelete", method = RequestMethod.POST)
	public String replyDelete(ReplyVO vo, SearchCriteria scri, RedirectAttributes rttr) throws Exception {
		logger.info("reply delete");

		RepService.replyDelete(vo);

		rttr.addAttribute("bno", vo.getBno());
		rttr.addAttribute("page", scri.getPage());
		rttr.addAttribute("perPageNum", scri.getPerPageNum());
		rttr.addAttribute("searchType", scri.getSearchType());
		rttr.addAttribute("keyword", scri.getKeyword());

		return "redirect:/board/read";
	}
	
	// 댓글 수정 GET
	@RequestMapping(value = "/replyUpdate", method = RequestMethod.GET)
	public void getReplyUpdate(@RequestParam("rno") int rno, 
							@ModelAttribute("scri") SearchCriteria scri, Model model) throws Exception {
		logger.info("reply update");

		ReplyVO vo = null;

		vo = RepService.readReplySelect(rno);

		model.addAttribute("readReply", vo);
		model.addAttribute("scri", scri);
	}

	// 댓글 삭제 GET
	@RequestMapping(value = "/replyDelete", method = RequestMethod.GET)
	public void getReplyDelete(@RequestParam("rno") int rno, 
							@ModelAttribute("scri") SearchCriteria scri, Model model) throws Exception {
		logger.info("reply delete");

		ReplyVO vo = null;

		vo = RepService.readReplySelect(rno);

		model.addAttribute("readReply", vo);
		model.addAttribute("scri", scri);
	}
}