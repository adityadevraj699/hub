package com.fashion.hub.Controller;

import com.fashion.hub.Model.*;
import com.fashion.hub.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private WishlistItemRepository wishlistRepository;

    @Autowired
    private CartItemRepository cartRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home | FashionHub");

        // Active products
        List<Product> activeProducts = productRepository.findAll()
                .stream()
                .filter(p -> p.getStatus() == Product.Status.ACTIVE)
                .collect(Collectors.toList());
        model.addAttribute("activeProducts", activeProducts);

        // Trending products
        List<Product> trendingProducts = activeProducts.stream()
                .filter(Product::getIsTrending)
                .collect(Collectors.toList());
        model.addAttribute("trendingProducts", trendingProducts);

        // Categories
        model.addAttribute("categories", categoryRepository.findAll());

        return "home";
    }


    // 🔐 Login Page
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login | FashionHub");
        return "login";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("title", "Categories | FashionHub");
        return "categories"; // Thymeleaf template: categories.html
    }

   
}
