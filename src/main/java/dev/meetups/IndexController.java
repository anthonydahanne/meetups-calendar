package dev.meetups;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {

	@GetMapping("/index.html")
	public String index(Model model) {
		return "index";
	}

	@GetMapping("/{city}")
	public String city(@PathVariable(name="city", required=false) String city, Model model) {
		model.addAttribute("city", city);
		return "city";
	}

}