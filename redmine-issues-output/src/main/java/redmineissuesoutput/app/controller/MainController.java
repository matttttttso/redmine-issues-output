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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.IssueStatus;
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
	@GetMapping
	String moveSearchView(Model model) {
		return "redirect:/search";
	}
	
	/**
	 * 検索画面を表示。
	 *
	 * @param model : Model
	 * @param searchForm : SearchForm
	 * @return search.htmlの階層
	 */
	@GetMapping("search")
	String showSearchView(Model model, SearchForm searchForm) {
		// プロジェクトリスト、チケットステータスリストを取得
		List<Project> projectList = new ArrayList<>();
		List<IssueStatus> issueStatusList = new ArrayList<>();
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		try {
			projectList = redmineManager.getProjectManager().getProjects();
			issueStatusList = redmineManager.getIssueManager().getStatuses();
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		// 検討課題プロジェクトだけのリストを作成
		List<Project> childProjectList = projectList.stream()
				.filter(p -> Objects.nonNull(p.getParentId()))
				.filter(p -> Objects.equals(p.getName().substring(p.getName().length() - 5), "_検討課題"))
				.collect(Collectors.toList());
		// parentIdのみのリストを作成(filter用)
		List<Integer> parentIdList = childProjectList.stream()
				.map(cp -> cp.getParentId())
				.collect(Collectors.toList());
		// 検討課題プロジェクトの親プロジェクトだけのリストを作成
		List<Project> parentProjectList = projectList.stream()
				.filter(p -> Objects.isNull(p.getParentId()))
				.filter(p -> parentIdList.contains(p.getId()))
				.collect(Collectors.toList());
		// 出力画面用にセット
		model.addAttribute("parentProjectList", parentProjectList);
		model.addAttribute("childProjectList", childProjectList);
		model.addAttribute("issueStatusList", issueStatusList);
		// IssueStatusリストをidのみのリストに変換
		List<Integer> issueStatusIdList = issueStatusList.stream()
				.map(is -> is.getId())
				.collect(Collectors.toList());
		// listから配列に変換し、フォームにセット
		searchForm.setIssueStatus(issueStatusIdList.toArray(new Integer[issueStatusIdList.size()]));
		return "search";
	}
	
//	/**
//	 * 検索画面、検索結果リスト返却、Ajax通信用
//	 * 
//	 * @param searchForm : SearchForm
//	 * @return issueList : List<Ticket>
//	 */
//	@RequestMapping(value = "*/get-issue-list", method = RequestMethod.POST)
//	@ResponseBody
//	public List<Ticket> returnStageCodeList(@RequestBody SearchForm searchForm) {
//		String projectId = searchForm.getProjectId();
//		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
//		final String TICKET_LIMIT = "100";
//		int page = 1;
//		Map<String,String> paramsMap = new HashMap<String,String>();
//		paramsMap.put("limit", TICKET_LIMIT);
//		paramsMap.put("page", String.valueOf(page));
//		paramsMap.put("status_id", "*");	// チケットのステータス：全て
//		paramsMap.put("project_id", projectId);
//		List<Issue> allIssueList = new ArrayList<>();
//		// APIでチケットを取得
//		try {
//			ResultsWrapper<Issue> rw = redmineManager.getIssueManager().getIssues(paramsMap);
//			allIssueList.addAll(rw.getResults());
//			boolean issuesRemaining = true;
//			while (issuesRemaining) {
//				if (rw.getResults().size() < Integer.valueOf(TICKET_LIMIT)) {
//					issuesRemaining = false;
//				} else {
//					page++;
//					paramsMap.put("page",String.valueOf(page));
//					rw = redmineManager.getIssueManager().getIssues(paramsMap);
//					allIssueList.addAll(rw.getResults());
//				}
//			}
//		} catch (RedmineException e) {
//			e.printStackTrace();
//		}
//		// チケットをフィルタリング
//		List<Issue> filteredIssueList = allIssueList.stream()
//				.filter(i -> !i.getCustomFieldByName("発生日").getValue().isEmpty())
//				.filter(i -> searchForm.getStartDate().minusDays(1)
//								.isBefore(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
//				.filter(i -> searchForm.getEndDate().plusDays(1)
//								.isAfter(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
//				.sorted(Comparator.comparing((Issue i) -> i.getCustomFieldByName("発生日").getValue())
//								.thenComparing((Issue i) -> Integer.valueOf(i.getCustomFieldByName("管理番号").getValue())))
//				.collect(Collectors.toList());
//		// ユーザー一覧を取得
//		List<User> userList = new ArrayList<>();
//		try {
//			userList = redmineManager.getUserManager().getUsers();
//		} catch (RedmineException e) {
//			e.printStackTrace();
//		}
//		// 必要情報のみ取り出す
//		List<Ticket> ticketList = new ArrayList<>();
//		int index = 1;
//		for (Issue issue : filteredIssueList) {
//			ticketList.add(new Ticket(issue, index++, userList));
//		}
//		
//		return ticketList;
//	}
}
