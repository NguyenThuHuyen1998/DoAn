package com.example.crud.controller;

import com.example.crud.constants.InputParam;
import com.example.crud.entity.*;
import com.example.crud.helper.TimeHelper;
import com.example.crud.response.MessageResponse;
import com.example.crud.service.*;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FeedbackController {

    private OrderService orderService;
    private JwtService jwtService;
    private ProductService productService;
    private FeedbackService feedbackService;
    private FilesStorageService filesStorageService;

    public FeedbackController(JwtService jwtService, OrderService orderService, FeedbackService feedbackService, ProductService productService, FilesStorageService filesStorageService) {
        this.feedbackService= feedbackService;
        this.orderService = orderService;
        this.jwtService = jwtService;
        this.productService= productService;
        this.filesStorageService= filesStorageService;
    }

    @PostMapping(value = "/userPage/feedbacks")
    public ResponseEntity<FeedBack> createFeedback(@RequestParam("content") String content,
                                                   @RequestParam("productId") long productId,
                                                   @RequestParam("star") int star,
                                                   @RequestParam(value = "image", required = false, defaultValue = "") MultipartFile image,
                                                   HttpServletRequest request) {
        if (jwtService.isCustomer(request)) {
            User user= jwtService.getCurrentUser(request);
            long userId= jwtService.getCurrentUser(request).getUserId();
            //check validator + permission
            Product product = productService.findById(productId);
            if (product == null) {
                return new ResponseEntity(new MessageResponse().getResponse("Sán phẩm không tồn tại!"), HttpStatus.BAD_REQUEST);
            }
            List<Long> getlistProductBought = orderService.getlistProductBought(userId);
            if (getlistProductBought.size()==0) return new ResponseEntity(new MessageResponse().getResponse("Bạn chưa mua sản phẩm nào!"), HttpStatus.BAD_REQUEST);
            if (!getlistProductBought.contains(productId)) {
                return new ResponseEntity(new MessageResponse().getResponse("Bạn chưa mua sản phẩm này!"), HttpStatus.BAD_REQUEST);
            }
            FeedBack feedBack= new FeedBack(content, star, product, TimeHelper.getInstance().getNow(), user);
            if (image.equals("")){
                feedBack.setImage("");
            }
            else {
                feedBack.setImage(filesStorageService.save(image));
            }
            feedbackService.save(feedBack);
            return new ResponseEntity<>(feedBack, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);

    }

    @PutMapping(value = "/userPage/feedback/{feedback-id}")
    public ResponseEntity<FeedBack> updateFeedback(@PathVariable(name = "feedback-id") long feedbackId,
                                                   @RequestBody FeedBack feedBack, HttpServletRequest request) {
        if (jwtService.isCustomer(request)) {
            try {
                long userId = jwtService.getCurrentUser(request).getUserId();
                FeedBack currentFeedBack = feedbackService.getFeedback(feedbackId);
                if (currentFeedBack.getUser().getUserId() != userId || feedbackId != feedBack.getFeedbackId()) {
                    return new ResponseEntity("Bạn không thể sửa đánh giá này", HttpStatus.METHOD_NOT_ALLOWED);
                }
                feedbackService.save(feedBack);
                feedbackService.updateFeedback(feedBack);
                return new ResponseEntity("Cập nhật đánh giá thành công!",HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity("Đăng nhập trước khi thực hiện", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "/userPage/feedback/{feedback-id}")
    public ResponseEntity<FeedBack> deleteFeedback(@PathVariable(name = "feedback-id") long feebackId,
                                                   HttpServletRequest request) {
        if (jwtService.isCustomer(request)) {
            long userId = jwtService.getCurrentUser(request).getUserId();
            FeedBack feedBack = feedbackService.getFeedback(feebackId);
            if (feedBack.getUser().getUserId() != userId) {
                return new ResponseEntity("Bạn không thể xóa đánh giá của người khác", HttpStatus.METHOD_NOT_ALLOWED);
            }
            feedbackService.deleteFeedback(feedBack);
            return new ResponseEntity("Xóa đánh giá thành công!",HttpStatus.OK);
        }

        return new ResponseEntity("Đăng nhập trước khi thực hiện", HttpStatus.METHOD_NOT_ALLOWED);
    }

    //____________________________________ADMIN______________________________________


    @GetMapping(value = "/feedbacks/{product-id}")
    public ResponseEntity<FeedBack> getListFeedBack(@PathVariable(name = "product-id") long productId) {
        try {
            List<FeedBack> feedBacks = feedbackService.getFeedbackByProduct(productId);
            feedBacks = feedbackService.sortByDatePost(feedBacks);
            return new ResponseEntity(feedBacks, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
