package com.kayleh.tmall.controller;

import com.kayleh.tmall.comparator.*;
import com.kayleh.tmall.pojo.*;
import com.kayleh.tmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/**
 * @Author: Wizard
 * @Date: 2020/5/6 19:45
 */
@Controller
@RequestMapping("")
public class ForeController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    ReviewService reviewService;

    @RequestMapping("forehome")
    public String home(Model model) {
        //获取分类集合
        List<Category> categoryList = categoryService.list();
        //根据分类填满产品
        productService.fill(categoryList);
        //根据分类填满推荐产品
        productService.fillByRow(categoryList);
        model.addAttribute("cs", categoryList);
        return "fore/home";
    }

    @RequestMapping("foreregister")
    public String register(User user, Model model) {

        String name = user.getName();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);
        if (exist) {
            //注册失败，用户名存在
            String message = "用户名已经被使用,不能使用";
            model.addAttribute("msg", message);
            model.addAttribute("user", null);
            return "fore/register";
        }

        //注册成功，持久化操作，重定向到注册成功页面
        userService.add(user);
        return "redirect:registerSuccessPage";
    }

    /**
     * 登录
     *
     * @param name
     * @param password
     * @param session
     * @param model
     * @return
     */
    @RequestMapping("forelogin")
    public String login(@RequestParam("name") String name,
                        @RequestParam("password") String password,
                        HttpSession session,
                        Model model) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);
        if (user == null) {
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        session.setAttribute("user", user);
        return "redirect:forehome";
    }

    /**
     * 退出登录
     *
     * @param session
     * @return
     */
    @RequestMapping("forelogout")
    public String logout(HttpSession session) {
        session.removeAttribute("user");
        return "redirect:forehome";
    }

    @RequestMapping("foreproduct")
    public String profuct(int pid, Model model) {
        Product product = productService.get(pid);
        List<ProductImage> singeImage = productImageService.list(product.getId(), ProductImageService.type_single);
        List<ProductImage> detailImage = productImageService.list(product.getId(), ProductImageService.type_detail);
        product.setProductSingleImages(singeImage);
        product.setProductDetailImages(detailImage);

        List<PropertyValue> propertyValueList = propertyValueService.list(product.getId());
        List<Review> reviewList = reviewService.list(product.getId());
        productService.setSaleAndReviewNumber(product);
        model.addAttribute("reviews", reviewList);
        model.addAttribute("p", product);
        model.addAttribute("pvs", propertyValueService);
        return "force/product";
    }

    // ajax阿贾克斯检查登录
    @RequestMapping("forecheckLogin")
    @ResponseBody
    public String checkLogin(HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (null != user)
            return "success";
        return "fail";

    }

    @RequestMapping("foreloginAjax")
    @ResponseBody
    public String loginAjax(@RequestParam("name") String name, @RequestParam("password") String password, HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name, password);

        if (null == user) {
            return "fail";
        }
        session.setAttribute("user", user);
        return "success";
    }

    @RequestMapping("forecategory")
    public String category(int cid, String sort, Model model) {
        //根据cid查询分类
        Category category = categoryService.get(cid);
        //根据分类对象填充产品
        productService.fill(category);
        // 给产品填充销量和评价数据
        productService.setSaleAndReviewNumber(category.getProducts());
        if (null != sort) {
            switch (sort) {
                case "review":
                    Collections.sort(category.getProducts(), new ProductReviewComparator());
                    break;
                case "date":
                    Collections.sort(category.getProducts(), new ProductDateComparator());
                    break;
                case "saleCount":
                    Collections.sort(category.getProducts(), new ProductSaleCountComparator());
                    break;
                case "price":
                    Collections.sort(category.getProducts(), new ProductPriceComparator());
                    break;
                case "all":
                    Collections.sort(category.getProducts(), new ProductAllComparator());
                    break;
            }
        }
        model.addAttribute("c", category);
        return "fore/category";
    }

}



