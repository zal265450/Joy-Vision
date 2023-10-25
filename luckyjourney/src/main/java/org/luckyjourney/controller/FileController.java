package org.luckyjourney.controller;


import org.luckyjourney.authority.Authority;
import org.luckyjourney.service.FileService;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 郭建勇
 * @since 2023-10-25
 */
@RestController
@RequestMapping("/luckyjourney/file")
public class FileController {

    @Autowired
    private FileService fileService;


    /**
     * kodo上传签名
     * @return 返回签名
     */
    @GetMapping("/policy")
    public R policy() {
        return fileService.policy();
    }


}

