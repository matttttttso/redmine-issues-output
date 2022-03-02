package redmineissuesoutput.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import redmineissuesoutput.app.form.SearchForm;
import redmineissuesoutput.domain.model.RedmineInfo;
import redmineissuesoutput.domain.model.Ticket;

@RestController
@RequestMapping("/")
public class JasperReportController {
	
	@Autowired
	private RedmineInfo redmineInfo;

	@RequestMapping(value = "ticket", method = RequestMethod.POST)
	public void showTicketList(Model model, @RequestParam("projectIdAndParentName") String projectIdAndParentName,
			SearchForm searchForm, HttpServletResponse response) throws RedmineException {
		String[] params = projectIdAndParentName.split(",");
		String projectId = params[0];
		String parentName = params[1].replaceAll("[0-9]+.", "") + "\t殿";
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		final String TICKET_LIMIT = "100";
		int page = 1;
		Map<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("limit", TICKET_LIMIT);
		paramsMap.put("page", String.valueOf(page));
		paramsMap.put("status_id", "*");	// チケットのステータス：全て
		paramsMap.put("project_id", projectId);
		List<Issue> allIssueList = new ArrayList<>();
		// APIでチケットを取得
		try {
			ResultsWrapper<Issue> rw = redmineManager.getIssueManager().getIssues(paramsMap);
			allIssueList.addAll(rw.getResults());
			boolean issuesRemaining = true;
			while (issuesRemaining) {
				if (rw.getResults().size() < Integer.valueOf(TICKET_LIMIT)) {
					issuesRemaining = false;
				} else {
					page++;
					paramsMap.put("page",String.valueOf(page));
					rw = redmineManager.getIssueManager().getIssues(paramsMap);
					allIssueList.addAll(rw.getResults());
				}
			}
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		// チケットをフィルタリング
		List<Issue> filteredIssueList = allIssueList.stream()
				.filter(i -> !i.getCustomFieldByName("発生日").getValue().isEmpty())
				.filter(i -> searchForm.getStartDate().minusDays(1)
								.isBefore(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.filter(i -> searchForm.getEndDate().plusDays(1)
								.isAfter(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				.sorted(Comparator.comparing((Issue i) -> i.getCustomFieldByName("発生日").getValue())
								.thenComparing((Issue i) -> Integer.valueOf(i.getCustomFieldByName("管理番号").getValue())))
				.collect(Collectors.toList());
		// 必要情報のみ取り出す
		List<User> userList = redmineManager.getUserManager().getUsers();
		List<Ticket> ticketList = new ArrayList<>();
		int index = 1;
		for (Issue issue : filteredIssueList) {
			ticketList.add(new Ticket(issue, index++, userList));
		}
		// PDF出力処理
		try {
			//テンプレート読み込み
			String jasperFilePath =  getClass().getResource("/jasperreports/jasper-template.jasper").getPath();
			//パラメーター、データソースの設定
			HashMap<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("datasource1", ticketList);
			parameterMap.put("customerName", parentName);
			parameterMap.put("outputDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
			//PDFを作成し、レスポンスボディに設定
			JasperPrint jasperPrint= JasperFillManager.fillReport(jasperFilePath, parameterMap, new JREmptyDataSource());
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
		} catch (IOException | JRException e) {
			//エラー処理
			e.printStackTrace();
		}
	}
}