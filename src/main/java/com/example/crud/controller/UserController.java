package com.example.crud.controller;

import com.example.crud.entity.Address;
import com.example.crud.entity.Order;
import com.example.crud.input.ChangePasswordForm;
import com.example.crud.input.ConfirmPassword;
import com.example.crud.predicate.PredicateOrderFilter;
import com.example.crud.response.MessageResponse;
import com.example.crud.service.*;
import com.example.crud.service.impl.JwtServiceImpl;
import com.example.crud.constants.InputParam;
import com.example.crud.entity.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Predicate;

/*

    created by HuyenNgTn on 15/11/2020
*/
@RestController
public class UserController {
    public static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private JwtServiceImpl jwtHandler;
    private UserService userService;
    private OrderService orderService;
    private SendEmailService sendEmailService;
    private ShipService shipService;
    private PasswordEncoder passwordEncoder;
    private FilesStorageService filesStorageService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Value("${file.upload-dir}")
    private String fileDir;

    @Autowired
    public UserController(UserService userService,
                          OrderService orderService,
                          JwtServiceImpl jwtHandler,
                          SendEmailService service,
                          ShipService shipService,
                          FilesStorageService filesStorageService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.orderService = orderService;
        this.jwtHandler = jwtHandler;
        this.sendEmailService= service;
        this.shipService= shipService;
        this.filesStorageService= filesStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    // xem thông tin cá nhân user
    @RequestMapping(value = "/userPage/users", method = RequestMethod.GET)
    public ResponseEntity<Object> getDetailUser(HttpServletRequest request) {
        if (jwtHandler.isUser(request)) {
            long userId = jwtHandler.getCurrentUser(request).getUserId();
            User user = userService.findById(userId);
            return new ResponseEntity<Object>(user, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PutMapping(value = "/userPage/users")
    public ResponseEntity<User> editUser(@RequestParam("fullName") String fullName,
                                         @RequestParam("email") String email,
                                         @RequestParam("phone") String phone,
                                         @RequestParam("avatar") MultipartFile avatar,
                                         HttpServletRequest request) {
        if (jwtHandler.isUser(request)) {
            try {
                User user = jwtHandler.getCurrentUser(request);
                user.setFullName(fullName);
                user.setPhone(phone);
                user.setEmail(email);
                String filename= filesStorageService.save(avatar);
                user.setAvatar(filename);
                userService.add(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            } catch (Exception e) {
                logger.error(e.getMessage());
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //Thay đổi mật khẩu người dùng
    @PutMapping(value = "/user/changePassword")
    public ResponseEntity<User> changePassword(@RequestBody ChangePasswordForm data, HttpServletRequest request) {
        if (jwtHandler.isUser(request)) {
            try {
                long userId = jwtHandler.getCurrentUser(request).getUserId();
                User user = userService.findById(userId);
                passwordEncoder = new BCryptPasswordEncoder();
                if (userService.changePassword(user, data.getOldPass(), data.getNewPass())) {
                    return new ResponseEntity(new MessageResponse().getResponse("Thay đổi mật khẩu thành công."), HttpStatus.OK);
                }
                return new ResponseEntity(new MessageResponse().getResponse("Mật khẩu không đúng."), HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }


    @PostMapping(value = "/user/forgetPassword")
    public ResponseEntity<User> forgetPassword(@RequestParam(name = "userName") String userName) {
//        try{
        User user = userService.findByName(userName);
        if (user == null) {
            return new ResponseEntity(new MessageResponse().getResponse("Tên đăng nhập không tồn tại."), HttpStatus.BAD_REQUEST);
        }
        String randomStr= userService.forgetPassword(user);
        String message = "Mã xác nhận của tài khoản #"+ userName+ " là: " + randomStr;
        if (!sendEmailService.resetPassword(message, user.getEmail())) {
            return new ResponseEntity(new MessageResponse().getResponse("Không thể gửi mã xác nhận đến tài khoản của bạn"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Mã xác nhận đã được gửi tới email của bạn."), HttpStatus.OK);
    }

    @PostMapping(value = "/user/confirmPassword")
    public ResponseEntity<User> confirmPassword(@RequestBody String data){
        JSONObject jsonObject= new JSONObject(data);
        String userName= jsonObject.getString(InputParam.USER_NAME);
        String confirmCode= jsonObject.getString("confirmCode");
        String newPassword= jsonObject.getString("newPassword");
        User user= userService.findByName(userName);
        if(user== null){
            return new ResponseEntity(new MessageResponse().getResponse("Không tìm thấy tài khoản."), HttpStatus.BAD_REQUEST);
        }
        if(userService.changePassword(user, confirmCode, newPassword)){
            return new ResponseEntity(new MessageResponse().getResponse("Cập nhật mật khẩu mới thành công."),HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Mã xác nhận sai."), HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "test")
    public ResponseEntity<Order> test(){
        return new ResponseEntity(new MessageResponse("test"), HttpStatus.OK);
    }
    @GetMapping(value = "/userPage/addressShip")
    public ResponseEntity<Address> getListAddressOfUser(HttpServletRequest request){
            long userId= jwtHandler.getCurrentUser(request).getUserId();
            List<Address> list= shipService.getListAddressOfUser(userId);
            if (list== null || list.size()==0){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity(list, HttpStatus.OK);
    }

    @PostMapping(value = "/userPage/addressShip")
    public ResponseEntity<Address> createShipAddress(@RequestBody Address address,
                                                     HttpServletRequest request){
            User user= jwtHandler.getCurrentUser(request);
            long userId= jwtHandler.getCurrentUser(request).getUserId();
            List<Address> list= shipService.getListAddressOfUser(userId);
            if (list.size() >4){
                return new ResponseEntity(new MessageResponse().getResponse("Đã quá số lượng địa chỉ mối người, bạn không thể thêm mới!"), HttpStatus.BAD_REQUEST);
            }
            address.setUser(user);
            shipService.save(address);
            return new ResponseEntity(address, HttpStatus.OK);
    }

    @PutMapping(value = "/userPage/addressShip/{id}")
    public ResponseEntity<Address> editAddress(@PathVariable("id") long id,
                                               @RequestBody Address address,
                                               HttpServletRequest request){
        if(jwtHandler.isCustomer(request)){
            User user= jwtHandler.getCurrentUser(request);
            if (address== null || address.getUser().getUserId()!= user.getUserId()|| address.getAddressId()!= id){
                return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể sửa địa chỉ của người khác."), HttpStatus.BAD_REQUEST);
            }
            shipService.save(address);
            return new ResponseEntity(new MessageResponse().getResponse("Cập nhật thành công!"), HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin!"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "/userPage/addressShip/{id}")
    public ResponseEntity<Address> deleteAddress(@PathVariable("id") long id,
                                                 HttpServletRequest request){
        if(jwtHandler.isCustomer(request)){
            User user= jwtHandler.getCurrentUser(request);
            Address address= shipService.getAddress(id);
            if (address== null || address.getUser().getUserId()!= user.getUserId()){
                return new ResponseEntity(new MessageResponse().getResponse("Địa chỉ không hợp lệ!"), HttpStatus.BAD_REQUEST);
            }
            shipService.delete(address);
            return new ResponseEntity(new MessageResponse().getResponse("Xóa địa chỉ thành công."), HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin!"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //-------------------------------------------ADMIN---------------------------------------------
    //xem danh sách user hiện có và trạng thái hoạt động
    @GetMapping(value = "/adminPage/users")
    public ResponseEntity<User> getAllUser(HttpServletRequest request) {
        if (jwtHandler.isAdmin(request)) {
            List<User> userList = userService.findAllUser();
            if (userList == null || userList.size() == 0) {
                logger.error("User list empty");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity(userList, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin."), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping(value = "/adminPage/users/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable(name = "id") long userId, HttpServletRequest request) {
        if (jwtHandler.isAdmin(request)) {
            User user = userService.findById(userId);
            Predicate<Order> predicate = null;
            PredicateOrderFilter predicateOrderFilter = PredicateOrderFilter.getInstance();
            Predicate<Order> checkUser = predicateOrderFilter.checkUser(userId);
            Predicate<Order> checkStatusProcessing = predicateOrderFilter.checkStatus(InputParam.PROCESSING);
            predicate = checkUser.and(checkStatusProcessing);
            List<Order> orderList = predicateOrderFilter.filterOrder(orderService.findAllOrder(), predicate);
            for (Order order : orderList) {
                orderService.remove(order);
            }

            userService.delete(user);
            return new ResponseEntity(new MessageResponse().getResponse("Vô hiệu hóa tài khoản thành công."), HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin."), HttpStatus.METHOD_NOT_ALLOWED);
    }


    // phân quyền user làm admin
    @PostMapping(value = "/adminPage/decentralization/{id}")
    public ResponseEntity<User> decentralizationAdmin(@PathVariable("id") long id, HttpServletRequest request) {
        if (jwtHandler.isAdmin(request)) {
            User user = userService.findById(id);
            if (user == null) {
                return new ResponseEntity(new MessageResponse().getResponse("Người dùng không tồn tại"), HttpStatus.BAD_REQUEST);
            } else {
                user.setRole(InputParam.ADMIN);
                userService.update(user);
                return new ResponseEntity(user, HttpStatus.OK);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "/adminPage/lockUser/{id}")
    public ResponseEntity<User> lockUser(@PathVariable("id") long userId,
                                         HttpServletRequest request){
        if (jwtHandler.isAdmin(request)) {
            User user = userService.findById(userId);
            if (user == null) {
                return new ResponseEntity(new MessageResponse().getResponse("Người dùng không tồn tại"), HttpStatus.BAD_REQUEST);
            } else {
                user.setEnable(false);
                userService.update(user);
                return new ResponseEntity(user, HttpStatus.OK);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PostMapping(value = "/users/activate/{id}")
    public ResponseEntity<User> activeUser(@PathVariable("id") long userId,
                                           HttpServletRequest request){
        if (jwtHandler.isAdmin(request)){
            User user= userService.findById(userId);
            if (user== null){
                return new ResponseEntity(new MessageResponse().getResponse("Người dùng không tồn tại."), HttpStatus.BAD_REQUEST);
            }
            else {
                user.setEnable(true);
                userService.update(user);
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }
}
