package redmineissuesoutput.app.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import com.taskadapter.redmineapi.bean.Project;

import redmineissuesoutput.app.form.SearchForm;
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
	
//	@Autowired
//	private ApplicationContext context;
	
	@ModelAttribute
	SearchForm setUpForm() {
		SearchForm searchForm = new SearchForm();
		// デフォルト値設定
		LocalDate initialStartDate = LocalDate.of(LocalDate.now().minusYears(1).getYear(), 1, 1);	// 1年前の1月1日をセット
		searchForm.setStartDate(initialStartDate);
		searchForm.setEndDate(LocalDate.of(initialStartDate.getYear(), 12, 31));					// 1年前の12月31日をセット
		return searchForm;
	}
	
	/**
	 * rootにアクセスした際に検索画面にリダイレクト
	 *
	 * @return リダイレクト(/search)
	 */
	@RequestMapping(method = RequestMethod.GET)
	String moveSearchView(Model model) {
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
		
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		List<Project> projectList = new ArrayList<>();
		List<Project> parentProjectList = new ArrayList<>();
		List<Project> childProjectList = new ArrayList<>();
		try {
			projectList = redmineManager.getProjectManager().getProjects();
			parentProjectList = projectList.stream()
					.filter(p -> Objects.isNull(p.getParentId()))
					.collect(Collectors.toList());
			childProjectList = projectList.stream()
					.filter(p -> Objects.nonNull(p.getParentId()))
					.filter(p -> Objects.equals(p.getName().substring(p.getName().length() - 5), "_検討課題"))
					.collect(Collectors.toList());
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		// 出力画面用にセット
		model.addAttribute("parentProjectList", parentProjectList);
		model.addAttribute("childProjectList", childProjectList);
		
		return "search";
	}
	
//	@RequestMapping(value = "ticket", method = RequestMethod.POST)
//	String showTicketList(Model model, @RequestParam("identifier")String identifier, SearchForm searchForm, HttpServletResponse response) throws RedmineException {
//		model.addAttribute("identifier", identifier);
//		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
//		List<Issue> allIssueList = new ArrayList<>();
//		try {
//			allIssueList = redmineManager.getIssueManager().getIssues(identifier, null);
//		} catch (RedmineException e) {
//			e.printStackTrace();
//		}
//		List<Issue> filteredIssueList = allIssueList.stream()
//				.filter(i -> searchForm.getStartDate().minusDays(1)
//								.isBefore(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-M-d"))))
//				.filter(i -> searchForm.getEndDate().plusDays(1)
//								.isAfter(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-M-d"))))
//				.collect(Collectors.toList());
//		List<Ticket> ticketList = new ArrayList<>();
//		int index = 1;
//		for (Issue issue : filteredIssueList) {
//			ticketList.add(new Ticket(issue, index++));
//		}
//		model.addAttribute("ticketList", ticketList);
//		
//		
//		try {
//		      //テンプレート読み込み
//		      Resource resource = context.getResource("classpath:jasperreports/sample.jrxml");
//		      InputStream in = resource.getInputStream();
//		      JasperReport report = JasperCompileManager.compileReport(in);
//		      //パラメーター、データソースの設定
//		      Map<String, Object> params = new HashMap<>();
//		      JRDataSource dataSource = new JREmptyDataSource();
//		      //PDFを作成し、レスポンスボディに設定
//		      JasperPrint print = JasperFillManager.fillReport(report, params, dataSource);
//		      response.setContentType(MediaType.APPLICATION_PDF_VALUE);
//		      JasperExportManager.exportReportToPdfStream(print, response.getOutputStream());
//		    } catch (IOException | JRException e) {
//		      //エラー処理
//		      e.printStackTrace();
//		    }
//		
//		return "ticket-list";
//	}

}
