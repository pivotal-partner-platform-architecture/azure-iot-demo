package io.pivotal.azureiot.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

	@Autowired
	private Device device;

	@RequestMapping("/home")
    public String home(Model model, @RequestParam(value="name", required=false, defaultValue="World") String name) 
	{
        model.addAttribute("name", name);
        return "home";
    }
	
	@RequestMapping("/")
	public String index(Model model)
	{
		model.addAttribute("device", device);
		return "index";
	}
	
}
