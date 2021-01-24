package com.example.crud.controller;

import com.example.crud.entity.Order;
import com.example.crud.response.MessageResponse;
import com.example.crud.service.JwtService;
import com.example.crud.service.OrderService;
import com.example.crud.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@RestController
public class ReportController {

    public static OrderService orderService;
    private ReportService reportService;
    private JwtService jwtService;

    @Autowired
    public ReportController(OrderService orderService, ReportService reportService, JwtService jwtService) {
        this.orderService = orderService;
        this.reportService = reportService;
        this.jwtService = jwtService;
    }

//    @GetMapping(value = "/adminPage/report")
//    public ResponseEntity<Order> reportProduct(HttpServletRequest request) {
//        if (jwtService.isAdmin(request)) {
//            try {
//                //Map<String, Object> report= reportService.getReport();
//                List<Order> orderList = orderService.findAllOrder();
//
//                return new ResponseEntity(orderList, HttpStatus.OK);
//            } catch (Exception e) {
//                return new ResponseEntity(new MessageResponse().getResponse("Không thể xem báo cáo thống kê."), HttpStatus.BAD_REQUEST);
//            }
//        }
//
//        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
//    }

    @GetMapping(value = "/adminPage/reportTime")
    public ResponseEntity<Order> reportProductByTime(HttpServletRequest request) {
        if (jwtService.isAdmin(request)) {
            try {
                Map<String, Object> report = reportService.getReport();
                if (report.size() > 0) {
                    return new ResponseEntity(report, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "adminPage/report")
    public ResponseEntity<String> getReportByDay(@RequestParam(name = "dateStart", required = false, defaultValue = "-1") String dateStart,
                                                 @RequestParam(name = "dateEnd", required = false, defaultValue = "-1") String dateEnd,
                                                 HttpServletRequest request) throws ParseException {
        if (jwtService.isAdmin(request)){
            Map<String, Double> report= reportService.getReportEachDay(dateStart, dateEnd);
            if (report!= null){
                return new ResponseEntity(report, HttpStatus.OK);
            }
            else return new ResponseEntity(new MessageResponse().getResponse("Chưa có thống kê."), HttpStatus.BAD_REQUEST);
        }
       return new ResponseEntity(new MessageResponse().getResponse("Bạn không phải là admin."), HttpStatus.METHOD_NOT_ALLOWED);
    }
}


