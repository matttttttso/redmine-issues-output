package redmineissuesoutput.app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
	private ApplicationContext context;
	
	@Autowired
	private RedmineInfo redmineInfo;

	@RequestMapping(value = "output", method = RequestMethod.POST)
	public void showTicketList(Model model, @RequestParam("projectIdAndParentName") String projectIdAndParentName,
			SearchForm searchForm, HttpServletResponse response) {
		// リクエストパラメータ加工
		String[] params = projectIdAndParentName.split(",");
		String projectId = params[0];
		String customerName = params[1].replaceAll("[0-9]+.", "") + "\t殿";
		// チケット取得用のパラメータ準備
		final String TICKET_LIMIT = "100";
		int page = 1;
		Map<String,String> paramsMap = new HashMap<String,String>();
		paramsMap.put("limit", TICKET_LIMIT);
		paramsMap.put("page", String.valueOf(page));
		paramsMap.put("status_id", "*");	// チケットのステータス：全て
		paramsMap.put("project_id", projectId);
		// APIでチケットとユーザーのリストを取得
		List<Issue> allIssueList = new ArrayList<>();
		List<User> userList = new ArrayList<>();
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		try {
			// チケットリスト
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
			// ユーザーリスト
			userList = redmineManager.getUserManager().getUsers();
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		// チケットをフィルタリング
		List<Issue> filteredIssueList = allIssueList.stream()
				// フィルタ：出力したいチケットのステータスに該当する
				.filter(i -> Arrays.asList(searchForm.getIssueStatus()).contains(i.getStatusId()))
				// フィルタ：発生日が入力されている
				.filter(i -> !i.getCustomFieldByName("発生日").getValue().isEmpty())
				// フィルタ：発生日が範囲内（開始条件）
				.filter(i -> searchForm.getStartDate().minusDays(1)
						.isBefore(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				// フィルタ：発生日が範囲内（終了条件）
				.filter(i -> searchForm.getEndDate().plusDays(1)
						.isAfter(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
				// 並替：1.発生日昇順、2.管理番号昇順(nullは最後)
				.sorted(Comparator.comparing((Issue i) -> i.getCustomFieldByName("発生日").getValue())
						.thenComparing((Issue i) -> StringUtils.isBlank(i.getCustomFieldByName("管理番号").getValue()) ? null
								: Integer.valueOf(i.getCustomFieldByName("管理番号").getValue()),
						Comparator.nullsLast(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		// 必要情報のみ取り出す
		List<Ticket> ticketList = new ArrayList<>();
		int index = 1;
		for (Issue issue : filteredIssueList) {
			ticketList.add(new Ticket(issue, index++, userList));
		}
		// PDF出力処理
		try {
			// コンパイル済みテンプレート読み込み
			InputStream stream = context.getResource("classpath:jasperreports/jasper-template.jasper").getInputStream();
			// パラメーター、データソースの設定
			HashMap<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("datasource1", ticketList);
			parameterMap.put("customerName", customerName);
			parameterMap.put("outputDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
			parameterMap.put("startDate", searchForm.getStartDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
			parameterMap.put("endDate", searchForm.getEndDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
			// PDFを作成し、レスポンスボディに設定
			JasperPrint print = JasperFillManager.fillReport(stream, parameterMap, new JREmptyDataSource());
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			JasperExportManager.exportReportToPdfStream(print, response.getOutputStream());
		} catch (IOException | JRException e) {
			//エラー処理
			e.printStackTrace();
		}
	}
}
