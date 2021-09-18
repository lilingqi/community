package com.llq.community.controller;

import com.llq.community.annatation.LoginRequired;
import com.llq.community.entity.User;
import com.llq.community.service.FollowService;
import com.llq.community.service.LikeService;
import com.llq.community.service.UserService;
import com.llq.community.utils.CommunityConstant;
import com.llq.community.utils.CommunityUtil;
import com.llq.community.utils.HostHolder;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.Multipart;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;

/**
 * @author llq
 * @create 2021-09-02  10:20
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${server.servlet.context-path}")
    private String contextPath;

    //文件保存在服务器的路径
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    //去到账号设置页面
//    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    //表单提交，一定要用post请求
    /* MultipartFile
      在多部分请求中接收的上载文件的表示。文件内容存储在内存中或临时存储在磁盘上。在任何一种情况下，
      如果需要，用户负责将文件内容复制到会话级或持久性存储。临时存储将在请求处理结束时清除。
     */
//    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String upload(MultipartFile headerImage, Model model) {
        //判断是否上传了图片
        if (headerImage == null) {
            model.addAttribute("error", "您还没选择图片");
            return "/site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        assert filename != null;
        String suffix = filename.substring(filename.lastIndexOf("."));
        //判断格式
        if (StringUtils.isBlank(suffix) || !(suffix.equals(".jpg")) || suffix.equals(".jpeg") || suffix.equals(".png")) {
            model.addAttribute("error", "文件格式错误");
            return "/site/setting";
        }
        if (headerImage.getSize() > 1024 * 500) {
            model.addAttribute("error", "图片资源过大，请重新选择，限制500kb");
            return "/site/setting";
        }
        //随机文件名
        filename = CommunityUtil.generateUUID() + suffix;
        //文件上传路径
        File dest = new File(uploadPath + "/" + filename);  //这个知识点有点模糊

        //存储文件到本地
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传图片失败" + e.getMessage());
            throw new RuntimeException("上传图片失败", e);
        }
        //更新头像路径
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    //获取头像资源
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        //服务器文件路径
        filename = uploadPath + "//" + filename;
        //文件后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //设置响应格式
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename) //写在括号里面的流，最后会默认关闭
        ) {
            byte[] bytes = new byte[1024];
            int len;
            while ((len = fis.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }

    //修改密码
//    @LoginRequired
    @RequestMapping(path = "/change", method = RequestMethod.POST)
    public String change(String oldWord, String newWord, String confirmWord, Model model) {
        if (StringUtils.isBlank(oldWord)) {
            model.addAttribute("oldMsg", "原密码不能为空！");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newWord)) {
            model.addAttribute("newMsg", "新密码不能为空！");
            return "/site/setting";
        }
        if (!newWord.equals(confirmWord)) {
            model.addAttribute("cfMsg", "两次密码不一致！");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        if (!CommunityUtil.md5(oldWord + user.getSalt()).equals(user.getPassword())) {
            model.addAttribute("oldMsg", "原密码不正确！");
            return "/site/setting";
        }
        if (oldWord.equals(newWord)) {
            model.addAttribute("newMsg", "不能修改为原来的密码！");
            return "/site/setting";
        }
        int userId = user.getId();
        String salt = user.getSalt();
        newWord = CommunityUtil.md5(newWord + salt);
        userService.changePassword(userId, newWord);
        model.addAttribute("msg", "修改密码成功！");
        model.addAttribute("target", "/index");

        return "/site/operate-result";
    }

   //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);
        //点赞数
        int likeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "/site/profile";

    }
}
