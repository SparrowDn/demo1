package com.example.demo.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.example.demo.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.domain.BoardListDomain;
import com.example.demo.domain.LoginDomain;
import com.example.demo.service.UploadService;
import com.example.demo.service.UserService;
import com.example.demo.util.CommonUtils;
import com.example.demo.vo.LoginVO;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequestMapping(value = "/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadService uploadService;

    @RequestMapping(value = "board")
    public ModelAndView login(LoginVO loginDTO, HttpServletRequest request, HttpServletResponse response) throws IOException {

        //session 처리
        HttpSession session = request.getSession();
        ModelAndView mav = new ModelAndView();
        // 중복체크
        Map<String, String> map = new HashMap<>();
        map.put("mbId", loginDTO.getId());
        map.put("mbPw", loginDTO.getPw());

        // 중복체크
        int dupleCheck = userService.mbDuplicationCheck(map);
        LoginDomain loginDomain = userService.mbGetId(map);

        if(dupleCheck == 0) {
            String alertText = "없는 아이디이거나 패스워드가 잘못되었습니다. 가입해주세요";
            String redirectPath = "/main/signin";
            CommonUtils.redirect(alertText, redirectPath, response);
            return mav;
        }


        //현재아이피 추출
        String IP = CommonUtils.getClientIP(request);

        //session 저장
        session.setAttribute("ip",IP);
        session.setAttribute("id", loginDomain.getMbId());
        session.setAttribute("mbLevel", loginDomain.getMbLevel());

        List<BoardListDomain> items = uploadService.boardList();
        System.out.println("items ==> "+ items);
        mav.addObject("items", items);

        mav.setViewName("board/boardList");

        return mav;
    }

    // 좌측 메뉴 클릭시 보드화면 이동 (로그인된 상태)
    @RequestMapping(value = "bdList")
    public ModelAndView bdList() {
        ModelAndView mav = new ModelAndView();
        List<BoardListDomain> items = uploadService.boardList();
        System.out.println("items ==> "+ items);
        mav.addObject("items", items);
        mav.setViewName("board/boardList");
        return mav;
    }

    //대시보드 리스트 보여주기
    @GetMapping("mbList")
    public ModelAndView mbList(HttpServletRequest request) {

        ModelAndView mav = new ModelAndView();
        HttpSession session = request.getSession();
        String page = (String) session.getAttribute("page"); // session에 담고 있는 page 꺼냄
        if(page == null)page = "1"; // 없으면 1

        //클릭페이지 세션에 담아줌
        session.setAttribute("page", page);

        //페이지네이션
        mav = mbListCall(request);  //리스트만 가져오기

        mav.setViewName("admin/adminList.html");
        return mav;
    };


    //페이징으로 리스트 가져오기
    public ModelAndView mbListCall(HttpServletRequest request) { //클릭페이지 널이면
        ModelAndView mav = new ModelAndView();
        //페이지네이션 쿼리 참고
        // SELECT * FROM jsp.member order by mb_update_at limit 1, 5; {offset}{limit}

        //전체 갯수
        int totalcount = userService.mbGetAll();
        int contentnum = 10; // 데이터 가져올 갯수


        //데이터 유무 분기때 사용
        boolean itemsNotEmpty;

        if(totalcount > 0) { // 데이터 있을때

            // itemsNotEmpty true일때만, 리스트 & 페이징 보여주기
            itemsNotEmpty = true;
            //페이지 표현 데이터 가져오기
            Map<String,Object> pagination = Pagination.pagination(totalcount, request);

            Map map = new HashMap<String, Integer>();
            map.put("offset",pagination.get("offset"));
            map.put("contentnum",contentnum);

            //페이지별 데이터 가져오기
            List<LoginDomain> loginDomain = userService.mbAllList(map);

            //모델객체 넣어주기
            mav.addObject("itemsNotEmpty", itemsNotEmpty);
            mav.addObject("items", loginDomain);
            mav.addObject("rowNUM", pagination.get("rowNUM"));
            mav.addObject("pageNum", pagination.get("pageNum"));
            mav.addObject("startpage", pagination.get("startpage"));
            mav.addObject("endpage", pagination.get("endpage"));

        }else {
            itemsNotEmpty = false;
        }

        return mav;
    };
}