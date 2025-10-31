package com.fashion.hub.Controller;

import com.fashion.hub.Model.Category;
import com.fashion.hub.Model.User;
import com.fashion.hub.Service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Add 'user' to every model automatically
     */
    @ModelAttribute
    public void addUserToModel(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user != null) {
            model.addAttribute("user", user);
        }
    }

    /**
     * List categories
     */
    @GetMapping
    public String listCategories(Model model,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 HttpSession session) {

        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.getAllCategories(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/categories";
    }

    /**
     * Show form to create new category
     */
    @GetMapping("/create")
    public String createCategoryForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        model.addAttribute("category", new Category());
        return "admin/category_form";
    }

    /**
     * Show form to edit existing category
     */
    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        Category category = categoryService.getCategoryById(id);
        model.addAttribute("category", category);
        return "admin/category_form";
    }

    /**
     * Save category (create or update)
     */
    @PostMapping("/save")
    public String saveCategory(@ModelAttribute Category category,
                               @RequestParam("imageFile") MultipartFile imageFile,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        try {
            categoryService.saveCategory(category, imageFile);
            model.addAttribute("success", "Category saved successfully!");
        } catch (Exception e) {
            model.addAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }


    /**
     * Delete a category
     */
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedUser");
        if (user == null || !Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            return "redirect:/login";
        }

        categoryService.deleteCategory(id);
        model.addAttribute("success", "Category deleted successfully!");
        return "redirect:/admin/categories";
    }
}
