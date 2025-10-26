package com.fashion.hub.Controller;

import com.fashion.hub.Model.Product;
import com.fashion.hub.Model.User;
import com.fashion.hub.Service.ProductService;
import com.fashion.hub.Service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("products", productService.getAllProducts(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/products";
    }

    @GetMapping("/create")
    public String createProductForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories(null));
        return "admin/product_form";
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        Product product = productService.getProductById(id);
        model.addAttribute("user", user);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories(null));
        return "admin/product_form";
    }

    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam("imageFile") MultipartFile imageFile,
                              Model model) {
        try {
            
            Product existing = null;
            if (product.getId() != null) {
                existing = productService.getProductById(product.getId());
            }

            // If editing and no new file uploaded, preserve old image
            if (existing != null && (imageFile == null || imageFile.isEmpty())) {
                product.setImage(existing.getImage());
            }

            // If a new image uploaded, set it
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = productService.uploadImage(imageFile);
                product.setImage(imageUrl);
            }

            // Save product
            productService.saveProduct(product);
            model.addAttribute("success", "Product saved successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        productService.deleteProduct(id);
        model.addAttribute("success", "Product deleted successfully!");
        return "redirect:/admin/products";
    }
}
