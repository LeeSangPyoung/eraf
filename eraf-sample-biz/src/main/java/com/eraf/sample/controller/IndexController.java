package com.eraf.sample.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Main Index Controller
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "ERAF Sample Application");
        model.addAttribute("version", "1.0.0");
        return "index";
    }
}
