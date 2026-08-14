package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ---------- READ (List all) ----------
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("productList", productRepository.findAll());
        return "product-list";
    }

    // ---------- CREATE (show empty form) ----------
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "add-product";
    }

    // ---------- CREATE / UPDATE (save) ----------
    @PostMapping("/save")
    public String saveProduct(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products";
    }

    // ---------- UPDATE (show form pre-filled) ----------
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        model.addAttribute("product", product);
        return "add-product";
    }

    // ---------- DELETE ----------
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products";
    }
}