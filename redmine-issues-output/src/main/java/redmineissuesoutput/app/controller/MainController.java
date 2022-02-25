package redmineissuesoutput.app.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.internal.Transport;

import redmineissuesoutput.domain.model.RedmineInfo;
import redmineissuesoutput.domain.model.Ticket;

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
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		List<Project> projectList = new ArrayList<>();
		Project aki = null;
		Project akiKadai = null;
		List<Issue> akiTicketList = new ArrayList<>();
//		List<Issue> akiKadaiTicketList = new ArrayList<>();
//		Issue akiIssue0 = new Issue();
//		Collection<CustomField> customField = new HashSet<>();
//		List<Object> list = new ArrayList<>();
//		CustomField customFieldfuku = new CustomField();
		try {
			projectList = redmineManager.getProjectManager().getProjects();
			projectList.get(0).getParentId();
			aki = projectList.get(7);
//			aki = projectList.get(13);
			akiTicketList = redmineManager.getIssueManager().getIssues(aki.getIdentifier(), null);
//			akiIssue0 = akiTicketList.get(0);
//			customField = akiIssue0.getCustomFields();
//			customFieldfuku = akiIssue0.getCustomFieldByName("副担当者");
//			customFieldfuku = akiIssue0.getCustomFieldByName("部門");
//			customFieldfuku = akiIssue0.getCustomFieldByName("管理番号");
//			customFieldfuku = akiIssue0.getCustomFieldByName("発生日");
//			customFieldfuku = akiIssue0.getCustomFieldByName("検討案または結果");
//			customFieldfuku = akiIssue0.getCustomFieldByName("完了日");
//			customFieldfuku = akiIssue0.getCustomFieldByName("発信元担当者");
//			list = Arrays.asList(customField.toArray());;
//			akiIssue0.getDueDate();
//			akiKadai = projectList.get(14);
//			akiKadaiTicketList = redmineManager.getIssueManager().getIssues(akiKadai.getIdentifier(), akiKadai.getId());
		} catch (RedmineException e) {
			e.printStackTrace();
//			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
		}
		Transport transport = redmineManager.getTransport();
		List<Ticket> ticketList = new ArrayList<>();
		int index = 0;
		for (Issue issue : akiTicketList) {
			ticketList.add(new Ticket(issue, index++));
		}
		
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
