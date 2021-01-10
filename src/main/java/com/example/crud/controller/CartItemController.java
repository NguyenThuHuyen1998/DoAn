package com.example.crud.controller;

import com.example.crud.constants.InputParam;
import com.example.crud.entity.Cart;
import com.example.crud.entity.CartItem;
import com.example.crud.entity.Product;
import com.example.crud.response.MessageResponse;
import com.example.crud.service.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CartItemController {

    public static final Logger logger = LoggerFactory.getLogger(CartItemController.class);

    private CartItemService cartItemService;
    private ProductService productService;
    private CartService cartService;
    private JwtService jwtService;

    public CartItemController(CartItemService cartItemService, ProductService productService, CartService cartService, JwtService jwtService){
        this.cartItemService= cartItemService;
        this.productService= productService;
        this.cartService= cartService;
        this.jwtService= jwtService;
    }

    //thêm sản phâm vào giỏ hàng
    @PostMapping("/userPage/cartItems/{product-id}")
    public ResponseEntity addProduct(@PathVariable(name = "product-id") long productId,
                                               @RequestBody String data,
                                               HttpServletRequest request){
        try{
            if(jwtService.isCustomer(request)){
                long userId= jwtService.getCurrentUser(request).getUserId();
                Cart cart= cartService.getCartByUserId(userId);
                int quantity= new JSONObject(data).getInt("quantity");
                List<CartItem> cartItemList= cartItemService.getListCartItemInCart(userId);
                Product product= productService.findById(productId);
                CartItem cartItemTarget= null;
                if(cartItemList.size()>0){
                    for(CartItem index: cartItemList) {
                        if (product.getId() == index.getProduct().getId()) {
                            cartItemTarget= index;
                        }
                    }
                }
                if (quantity<=0){
                    cartItemService.deleteCartItem(cartItemTarget);
                    cart.setTotalMoney(cart.getTotalMoney()- cartItemTarget.getValueLine());
                    cartService.save(cart);
                    return new ResponseEntity(new MessageResponse().getResponse("Đã xóa sản phẩm khỏi giỏ hàng"), HttpStatus.OK);
                }
                if(cartItemTarget!= null){
                    cartItemTarget.setQuantity(cartItemTarget.getQuantity() + quantity);
                    cartItemService.save(cartItemTarget);
                } else if(cartItemTarget== null){
                    cartItemTarget= new CartItem(cart, product, quantity);
                    cartItemService.save(cartItemTarget);
                }
                cart.setTotalMoney(cart.getTotalMoney()+ cartItemTarget.getValueLine());
                cartService.save(cart);
                return new ResponseEntity(cartItemTarget, HttpStatus.OK);
            }
            return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
        }
        catch(Exception e){
            logger.error(String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    //Thêm số lượng sản phẩm trong giỏ hàng
    @PutMapping("/userPage/cartItems/{cart-item-id}")
    public ResponseEntity<CartItem> updateQuantity(@PathVariable(name = "cart-item-id") long cartItemId,
                                                   @RequestBody String data, HttpServletRequest request)  {
        try{
            if(jwtService.isCustomer(request)) {
                JSONObject jsonObject = new JSONObject(data);
                int quantity = jsonObject.getInt(InputParam.QUANTITY);
                long userId= jwtService.getCurrentUser(request).getUserId();
                Cart cart = cartService.getCartByUserId(userId);
                double totalMoney= 0;
                CartItem cartItem= null;
                List<CartItem> cartItemList = cartItemService.getListCartItemInCart(userId);
                for (CartItem index : cartItemList) {
                    if(index.getCartItemId()== cartItemId){
                        int oldQuantity= index.getQuantity();
                        index.setQuantity(quantity);
                        cartItem= index;
                        if (quantity > 0) {
                            cartItemService.save(cartItem);
                            totalMoney= totalMoney+ index.getProduct().getPrice()* quantity;
                            cart.setTotalMoney(totalMoney);
                            cartService.save(cart);
                        } else {
                            //quantity=0 thi xoa cartItem va tru di tong tien
                            cart.setTotalMoney(cart.getTotalMoney()- index.getProduct().getPrice()*oldQuantity);
                            cartItemService.deleteCartItem(cartItem);
                        }
                        return new ResponseEntity(new MessageResponse().getResponse("Cập nhật giỏ hàng thành công!"),HttpStatus.OK);
                    }
                }
                cartItemService.save(cartItem);
                return new ResponseEntity<>(cartItem, HttpStatus.OK);
            }
            return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
        }
        catch (Exception e){
            logger.error(String.valueOf(e));
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //lấy danh sách sản phẩm cùng số lượng trong giỏ hàng
    @GetMapping(value = "/userPage/cartItems")
    public ResponseEntity<Cart> getListProduct(HttpServletRequest request){
        try {
            if(jwtService.isCustomer(request)){
                long userId= jwtService.getCurrentUser(request).getUserId();
                Cart cart= cartService.getCartByUserId(userId);
                double amount= 0;
                List<CartItem> cartItems= cartItemService.getListCartItemInCart(userId);
                if(cartItems!= null && cartItems.size()>0) {
                    for(CartItem cartItem: cartItems){
                        amount+= cartItem.getQuantity()* cartItem.getProduct().getPrice();
                        System.out.println(cartItem.getProduct().getId()+ "   "+ cartItem.getProduct().getName());
                    }
                    cart.setTotalMoney(amount);
                    cartService.save(cart);
                    return new ResponseEntity(cartItems, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
        }
        catch (Exception e){
            return new ResponseEntity(new MessageResponse().getResponse("Không có sản phẩm trong giỏ hàng."),HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/userPage/cartItems/{cart-item-id}")
    public ResponseEntity<CartItem> removeProduct(@PathVariable("cart-item-id") long cartItemId,
                                                  HttpServletRequest request){
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            CartItem cartItem= cartItemService.getCartItem(cartItemId);
            if (cartItem== null){
                return new ResponseEntity(new MessageResponse().getResponse("Kiểm tra lại đầu vào"), HttpStatus.BAD_REQUEST);
            }
            cartItemService.deleteCartItem(cartItem);
            return new ResponseEntity(new MessageResponse().getResponse("Xóa giỏ hàng thành công!"),HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @DeleteMapping("/userPage/cartItems")
    public ResponseEntity<CartItem> removeAllProduct(HttpServletRequest request){
        if(jwtService.isCustomer(request)){
            long userId= jwtService.getCurrentUser(request).getUserId();
            cartItemService.deleteAllCartItem(userId);
            return new ResponseEntity(new MessageResponse().getResponse("Xóa giỏ hàng thành công!"),HttpStatus.OK);
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @GetMapping(value = "/userPage/cartItems/{cart-item-id}")
    public ResponseEntity<CartItem> getACartItem(@PathVariable(name = "cart-item-id") long cartItemId,
                                                 HttpServletRequest request){
        if(jwtService.isCustomer(request)){
            try{
                CartItem cartItem= cartItemService.getCartItem(cartItemId);
                if(jwtService.getCurrentUser(request).getUserId()!= cartItem.getCart().getUser().getUserId())
                if(cartItem!= null){
                    return new ResponseEntity(cartItem, HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            catch (Exception e){
                logger.error(String.valueOf(e));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity(new MessageResponse().getResponse("Đăng nhập trước khi thực hiện"), HttpStatus.METHOD_NOT_ALLOWED);
    }
}
