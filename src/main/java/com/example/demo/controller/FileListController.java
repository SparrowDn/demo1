package com.example.demo.controller;

import com.example.demo.domain.BoardFileDomain;
import com.example.demo.domain.BoardListDomain;
import com.example.demo.service.UploadService;
import com.example.demo.vo.FileListVO;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
public class FileListController {

    @Qualifier("uploadServiceImpl")
    @Autowired
    private UploadService uploadService;


    @PostMapping(value = "upload")
    public ModelAndView bdUpload(FileListVO fileListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException, ParseException {

        ModelAndView mav = new ModelAndView();
        int bdSeq = uploadService.fileProcess(fileListVO, request, httpReq);
        fileListVO.setContent(""); //초기화
        fileListVO.setTitle(""); //초기화

        // 화면에서 넘어올때는 bdSeq String이라 string으로 변환해서 넣어즘
        mav = bdSelectOneCall(fileListVO, String.valueOf(bdSeq),request);
        mav.setViewName("board/boardList");
        return mav;

    }
    //리스트 하나 가져오기 따로 함수뺌
    public ModelAndView bdSelectOneCall(@ModelAttribute("fileListVO") FileListVO fileListVO, String bdSeq, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        HashMap<String, Object> map = new HashMap<String, Object>();
        HttpSession session = request.getSession();

        map.put("bdSeq", Integer.parseInt(bdSeq));
        BoardListDomain boardListDomain =uploadService.boardSelectOne(map);
        System.out.println("boardListDomain"+boardListDomain);
        List<BoardFileDomain> fileList =  uploadService.boardSelectOneFile(map);

        for (BoardFileDomain list : fileList) {
            String path = list.getUpFilePath().replaceAll("\\\\", "/");
            list.setUpFilePath(path);
        }
        mav.addObject("detail", boardListDomain);
        mav.addObject("files", fileList);

        //삭제시 사용할 용도
        session.setAttribute("files", fileList);

        return mav;
    }

}