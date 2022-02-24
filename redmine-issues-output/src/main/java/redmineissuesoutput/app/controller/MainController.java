package redmineissuesoutput.app.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * メインのControllerクラス.
 *
 * @author 松尾
 */
@Controller
@RequestMapping("/")
public class MainController {
	@Autowired
	HttpSession session;
	
	@ModelAttribute
	void setUpForm() {
	}
	
	/**
	 * rootにアクセスした際に検索画面にリダイレクト
	 *
	 * @return リダイレクト(/search)
	 */
	@RequestMapping(method = RequestMethod.GET)
	String moveSearchView() {
		return "redirect:/search";
	}
	
	/**
	 * 検索画面を表示。
	 *
	 * @param searchForm : SearchForm
	 * @param model : Model
	 * @return search.htmlの階層
	 */
	@RequestMapping(value = "search", method = RequestMethod.GET)
	String showSearchView(Model model) {
		return "search";
	}

}
