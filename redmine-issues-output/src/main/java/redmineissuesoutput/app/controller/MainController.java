package redmineissuesoutput.app.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.internal.Transport;

import redmineissuesoutput.domain.model.RedmineInfo;

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
	
	@Autowired
	private RedmineInfo redmineInfo;
	
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
		String url = redmineInfo.getUrl();
		String apiKey = redmineInfo.getApiKey();
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		Transport transport = redmineManager.getTransport();
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
