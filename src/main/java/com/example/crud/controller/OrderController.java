package com.example.crud.controller;

import com.example.crud.constants.InputParam;
import com.example.crud.entity.*;
import com.example.crud.response.MessageResponse;
import com.example.crud.response.OrderResponse;
import com.example.crud.output.OrderLineForm;
import com.example.crud.service.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/*
    created by HuyenNgTn on 15/11/2020
*/
@RestController
public class OrderController {
    public static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private OrderService orderService;
    private CartService cartService;
    private UserService userService;
    private OrderLineService orderLineService;
    private SendEmailService emailService;
    private JwtService jwtService;
    private VoucherService voucherService;
    private ProductService productService;
    private ShipService shipService;

    @Autowired
    public OrderController(OrderService orderService,
                           CartService cartService, UserService userService,
                           SendEmailService service,
                           OrderLineService orderLineService,
                           JwtService jwtService,
                           ShipService shipService,
                           ProductService productService,
                           VoucherService voucherService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.userService = userService;
        this.orderLineService = orderLineService;
        this.emailService= service;
        this.jwtService = jwtService;
        this.shipService= shipService;
        this.productService= productService;
        this.voucherService= voucherService;
    }

    //tạo đơn hàng mới từ giỏ hàng, điền các thông tin trong form ship, nhap ma giam gia
//    @PostMapping(value = "/userPage/orders")
//    public ResponseEntity<Order> createOrder(@RequestBody String data,
//                                             HttpServletRequest request) throws ParseException {
//        if(jwtService.isCustomer(request)){
//            User user= jwtService.getCurrentUser(request);
//            JSONObject jsonObject= new JSONObject(data);
//            long addressId= jsonObject.getLong("addressId");
//            Address address= shipService.getAddress(addressId);
//            if (address== null){
//                return new ResponseEntity("Địa chỉ không tổn tại!", HttpStatus.BAD_REQUEST);
//            }
//            address.setAddressId(addressId);
//            String note= jsonObject.getString("note");
//            String delivery= jsonObject.getString("delivery");
//            address.setUser(user);
//            String code= jsonObject.getString("code");
//            Voucher voucher= null;
//            if(code!= null && code!=""){
//                voucher= voucherService.getVoucherByCode(code);
//                if (voucher== null){
//                    return new ResponseEntity("This coupon is not exist!", HttpStatus.BAD_REQUEST);
//                }
//                String expiryDate= voucher.getDateEnd();
//                long expiry= TimeHelper.getInstance().convertTimestamp(expiryDate + " 23:59:59");
//                if (expiry< new Date().getTime()){
//                    return new ResponseEntity("Coupon code has expired!", HttpStatus.BAD_REQUEST);
//                }
//            }
//            OrderResponse orderResponse= orderService.createOrder(user, note, delivery, voucher, address);
//            return new ResponseEntity(orderResponse, HttpStatus.OK);
//        }
//        return new ResponseEntity("Đăng nhập trước khi thực hiện", HttpStatus.METHOD_NOT_ALLOWED);
//    }

    @PostMapping(value = "userPage/orders")
    public ResponseEntity<Order> createOrder(@RequestBody String body,
                                             HttpServletRequest request) throws ParseException {
        if (jwtService.isCustomer(request)){
            User user= jwtService.getCurrentUser(request);
            JSONObject jsonObject= new JSONObject(body);
            long addressId= jsonObject.getLong("addressId");
            Address address= shipService.getAddress(addressId);
            if (address== null){
                return new ResponseEntity(new MessageResponse().getResponse("Địa chỉ không tồn tại!"), HttpStatus.OK);
            }
            String note= jsonObject.getString("note");
            String delivery= jsonObject.getString("delivery");
            if (delivery== null || delivery.equals("")){
                return new ResponseEntity(new MessageResponse().getResponse("Hãy chọn hình thức vận chuyển"), HttpStatus.OK);
            }
            String code= jsonObject.getString("code");
            Voucher voucher= null;
            if (!code.equals("")){
                voucher= voucherService.getVoucherByCode(code);
                if (voucher== null){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá không hợp lệ!"), HttpStatus.BAD_REQUEST);
                }
                int checkDate= voucherService.checkDate(voucher);
                if (checkDate==1){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá chưa đến thời gian áp dụng."), HttpStatus.BAD_REQUEST);
                }
                if (checkDate==-1){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá đã hết hạn."), HttpStatus.BAD_REQUEST);
                }
            }
            JSONObject jsonProduct= jsonObject.getJSONObject("productList");
            Map<String, Integer> hashMap= toMap(jsonProduct);

            OrderResponse orderResponse= orderService.createOrder(user, note, delivery, voucher, address, hashMap);
            user.setLastActive(new Date().getTime());
            userService.add(user);
            return new ResponseEntity(orderResponse, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    public static Map<String, Integer> toMap(JSONObject object) throws JSONException {
        Map<String, Integer> map = new HashMap<String, Integer>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            int value = (int) object.get(key);
            map.put(key, value);
        }
        return map;
    }



    @GetMapping(value = "/userPage/order")
    public ResponseEntity<OrderResponse> showOrder(HttpServletRequest request){
        if (jwtService.isCustomer(request)){
            User user= jwtService.getCurrentUser(request);
            OrderResponse orderResponse= orderService.showOrder(user);
            return new ResponseEntity<>(orderResponse, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }


    @PostMapping(value = "/userPage/voucher")
    public ResponseEntity<Voucher> applyVoucher(@RequestBody String data,
                                             HttpServletRequest request) throws ParseException {
        if (jwtService.isCustomer(request)) {
            User user= jwtService.getCurrentUser(request);

            JSONObject jsonObject= new JSONObject(data);
            String code= jsonObject.getString("code");
            Voucher voucher= null;
            if (!code.equals("")){
                voucher= voucherService.getVoucherByCode(code);
                if (voucher== null){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá không hợp lệ!"), HttpStatus.BAD_REQUEST);
                }
                int checkDate= voucherService.checkDate(voucher);
                if (checkDate==1){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá chưa đến thời gian áp dụng."), HttpStatus.BAD_REQUEST);
                }
                if (checkDate==-1){
                    return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá đã hết hạn."), HttpStatus.BAD_REQUEST);
                }
            }
            OrderResponse orderResponse= orderService.showOrder(user, voucher);
            if (orderResponse== null){
                return new ResponseEntity(new MessageResponse().getResponse("Mã giảm giá áp dụng cho đơn từ "+ voucher.getPriceApply()), HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity(voucher, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }
    //lấy danh sách đơn hàng của 1 user
    //user chỉ được lấy ds đơn hàng của mình nên bắt buộc có user-id
    @GetMapping(value = "/userPage/orders")
    public ResponseEntity<OrderResponse> getlistOrder(@RequestParam(name = "status", required = false, defaultValue = "") String status,
                                                      @RequestParam(name = "dateStart", required = false, defaultValue = "-1") String dateStart,
                                                      @RequestParam(name = "dateEnd", required = false, defaultValue = "-1") String dateEnd,
                                                      @RequestParam(name = "sortBy", required = false, defaultValue = InputParam.DECREASE) String sortBy,
                                                      HttpServletRequest request) throws ParseException {
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            Map<String, Object> filter= new HashMap<>();
            filter.put(InputParam.USER_ID, userId);
            filter.put(InputParam.STATUS, status);
            filter.put(InputParam.TIME_START, dateStart);
            filter.put(InputParam.TIME_END, dateEnd);
            filter.put(InputParam.SORT_BY, sortBy);

            List<Order> orderFilter = orderService.filterOrder(filter);
            List<Order> orderTotal= orderService.getListOrderByUserId(userId);
            List<OrderResponse> orderResponses = new ArrayList<>();

            if (orderFilter != null && orderFilter.size() > 0) {
                for (Order order : orderFilter) {
                    List<OrderLine> orderLines = orderLineService.getListOrderLineInOrder(order.getOrderId());
                    List<OrderLineForm> orderLineForms = orderLineService.getListOrderLineForm(orderLines);
                    OrderResponse orderResponse = new OrderResponse(order, orderLineForms);
                    orderResponses.add(orderResponse);
                }

            }

            return new ResponseEntity(orderResponses, HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //Xóa 1 đơn hàng by user
    @DeleteMapping(value = "/userPage/orders/{order-id}")
    public ResponseEntity<Order> cancelOrder(@PathVariable("order-id") long orderId,
                                             HttpServletRequest request) {
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            try {
                Order order = orderService.findById(orderId);
                if (order.getUser().getUserId() != userId) {
                    logger.error("User not permitt");
                    return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
                }
                if (!order.getStatus().equals(InputParam.PROCESSING)) {
                    logger.error("Can't delete this order");
                    return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể xóa đơn hàng này."), HttpStatus.BAD_REQUEST);
                } else {
                    order.setStatus(InputParam.CANCEL);
                    orderService.save(order);
                    return new ResponseEntity(new MessageResponse().getResponse("Xóa đơn hàng thành công!"),HttpStatus.OK);
                }
            } catch (Exception e) {
                logger.error(String.valueOf(e));
                return new ResponseEntity(new MessageResponse().getResponse("Không tìm thấy đơn hàng."),HttpStatus.NOT_FOUND);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //xem chi tiết 1 đơn hàng
    @CrossOrigin
    @GetMapping(value = "/userPage/orders/{order-id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("order-id") long orderId,
                                                  HttpServletRequest request) {
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            try {
                Order order= orderService.findById(orderId);
                List<OrderLine> orderLines= orderLineService.getListOrderLineInOrder(orderId);
                List<OrderLineForm> orderLineForms= new OrderLineForm().change(orderLines);
                OrderResponse orderResponse = new OrderResponse(order, orderLineForms);
                if (order.getUser().getUserId() != userId) {
                    logger.error("User not permitt");
                    return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
                }
                return new ResponseEntity(orderResponse, HttpStatus.OK);
            } catch (Exception e) {
                logger.error(String.valueOf(e));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @PutMapping(value = "/userPage/order/{order-id}")
    public ResponseEntity<Order> finishOrder(@PathVariable(name = "order-id") long orderId,
                                             HttpServletRequest request){
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            try{
                Order order= orderService.findById(orderId);
                User user= order.getUser();
                if(userId != user.getUserId() || !order.getStatus().equals(InputParam.SHIPPING) ){
                    return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể kế thúc đơn hàng này."), HttpStatus.METHOD_NOT_ALLOWED);
                }
                order.setStatus(InputParam.FINISHED);
                orderService.save(order);
                return new ResponseEntity(new MessageResponse().getResponse("Cảm ơn bạn đã xác nhận đơn hàng."),HttpStatus.OK);
            }
            catch (Exception e){
                logger.error("Order is not exist");
                return new ResponseEntity(new MessageResponse().getResponse("Không thể kết thúc đơn hàng."),HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //----------------------------ADMIN-----------------------------------------

    //admin lấy danh sách đơn đặt hàng của khách, lọc theo user-id, productId, status, time, giá trị đơn
    @GetMapping(value = "/adminPage/orders")
    public ResponseEntity<OrderResponse> getlistOrderByAdmin(@RequestParam(name = "status", required = true, defaultValue = "") String status,
                                                             @RequestParam(name = "dateStart", required = false, defaultValue = "-1") String dateStart,
                                                             @RequestParam(name = "dateEnd", required = false, defaultValue = "-1") String dateEnd,
                                                             @RequestParam(name = "priceMin", required = false, defaultValue = "-1") double priceMin,
                                                             @RequestParam(name = "priceMax", required = false, defaultValue = "-1") double priceMax,
                                                             @RequestParam(name = "userId", required = false, defaultValue = "-1") long userId,
                                                             @RequestParam(name = "sortBy", required = false, defaultValue = InputParam.DECREASE) String sortBy,
                                                             @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
                                                             @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                                                             HttpServletRequest request) throws ParseException {
        if(jwtService.isAdmin(request)){
            Map<String, Object> filter= new HashMap<>();
            filter.put(InputParam.USER_ID, userId);
            filter.put(InputParam.STATUS, status);
            filter.put(InputParam.TIME_START, dateStart);
            filter.put(InputParam.TIME_END, dateEnd);
            filter.put(InputParam.SORT_BY, sortBy);
            filter.put(InputParam.PRICE_MIN, priceMin);
            filter.put(InputParam.PRICE_MAX, priceMax);

            List<Order> orderFilter = orderService.filterOrder(filter);
            List<Order> orderTotal= orderService.findAllOrder();
            List<OrderResponse> orderResponses = new ArrayList<>();
            Map<String, Object> result= new HashMap<>();


            if (orderFilter != null && orderFilter.size() > 0) {
                for (Order order : orderFilter) {
                    List<OrderLine> orderLines = orderLineService.getListOrderLineInOrder(order.getOrderId());
                    List<OrderLineForm> orderLineForms = orderLineService.getListOrderLineForm(orderLines);
                    OrderResponse orderResponse = new OrderResponse(order, orderLineForms);
                    orderResponses.add(orderResponse);
                }
                result.put(InputParam.DATA, orderResponses);
                Map<String, Object> paging= new HashMap<>();
                int totalPage = (orderTotal.size()) / limit + ((orderTotal.size() % limit == 0) ? 0 : 1);
                int recordInPage=limit;
                int currentPage=page;
                int totalCount=orderFilter.size();
                paging.put(InputParam.RECORD_IN_PAGE, recordInPage);
                paging.put(InputParam.TOTAL_COUNT, totalCount);
                paging.put(InputParam.CURRENT_PAGE, currentPage);
                paging.put(InputParam.TOTAL_PAGE, totalPage);
                result.put(InputParam.PAGING, paging);
                return new ResponseEntity(result, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "/adminPage/order/{order-id}")
    public ResponseEntity<OrderResponse> getOrderByAdmin(@PathVariable("order-id") long orderId,
                                                         HttpServletRequest request) {
        if(jwtService.isAdmin(request)){
            try {
                Order order = orderService.findById(orderId);
                List<OrderLine> orderLine= orderLineService.getListOrderLineInOrder(orderId);
                List<OrderLineForm> orderLineForms= new OrderLineForm().change(orderLine);
                OrderResponse orderResponse = new OrderResponse(order, orderLineForms);
                return new ResponseEntity(orderResponse, HttpStatus.OK);
            } catch (Exception e) {
                logger.error(String.valueOf(e));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    //phê duyệt các đơn đang chờ xử lý <input là list các đơn>
    @PutMapping(value = "/adminPage/orders/{order-id}")
    public ResponseEntity<Order> approvalOrder(@PathVariable(name = "order-id") long orderId,
                                               HttpServletRequest request) {
        if(jwtService.isAdmin(request)){
            try{
                Order order= orderService.findById(orderId);
                if(order.getStatus().equals(InputParam.PROCESSING)){
                    User user= order.getUser();
                    if(user == null){
                        logger.error("Người dùng không tồn tại");
                        userService.delete(user);
                    }
                    order.setStatus(InputParam.SHIPPING);
                    orderService.save(order);

                    // send email notification
                    Calendar calendar= Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                    Date date1= calendar.getTime();
                    String dateStr= new SimpleDateFormat("dd/MM/yyyy").format(date1);
                    String message= "Đơn hàng có mã "+ order.getOrderId()+ " của bạn đã được giao cho shipper. Đơn sẽ được giao muộn nhất vào ngày " + dateStr+". Hãy để ý điện thoại.";
                    emailService.notifyOrder(message, user.getEmail());
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                else {
                    return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể thực hiện hành động này."), HttpStatus.METHOD_NOT_ALLOWED);
                }
            }
            catch (Exception e){
                return new ResponseEntity(new MessageResponse().getResponse("Đơn hàng không tồn tại."), HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin."), HttpStatus.METHOD_NOT_ALLOWED);

    }

    //xóa đơn đặt của người dùng, đơn đã đặt thành công không được phép xóa
    @DeleteMapping(value = "/adminPage/orders/{order-id}")
    public ResponseEntity<Order> deleteOrderByAdmin(@PathVariable(name = "order-id") long orderId,
                                                    HttpServletRequest request) {
        if(jwtService.isAdmin(request)){
            try{
                Order order= orderService.findById(orderId);
                if (!order.getStatus().equals(InputParam.PROCESSING)) {
                    return new ResponseEntity(new MessageResponse().getResponse("Bạn không thể thực hiện hành động này."), HttpStatus.METHOD_NOT_ALLOWED);
                }
//                List<OrderLine> orderLines= orderLineService.getListOrderLineInOrder(orderId);
//                for(OrderLine orderLine: orderLines){
//                    orderLineService.remove(orderLine);
//                }
                orderService.remove(order);
                return new ResponseEntity(new MessageResponse().getResponse("Xóa đơn hàng thành công!"), HttpStatus.OK);
            }
            catch (Exception e){
                logger.error("Order is not exist");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin."), HttpStatus.METHOD_NOT_ALLOWED);
    }


}
