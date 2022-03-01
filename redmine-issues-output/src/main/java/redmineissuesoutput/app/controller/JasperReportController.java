package redmineissuesoutput.app.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	String showTicketList(Model model, @RequestParam("identifier")String identifier, SearchForm searchForm, HttpServletResponse response) throws RedmineException {
		model.addAttribute("identifier", identifier);
		RedmineManager redmineManager = RedmineManagerFactory.createWithApiKey(redmineInfo.getUrl(), redmineInfo.getApiKey());
		List<Issue> allIssueList = new ArrayList<>();
		try {
			allIssueList = redmineManager.getIssueManager().getIssues(identifier, null);
		} catch (RedmineException e) {
			e.printStackTrace();
		}
		List<Issue> filteredIssueList = allIssueList.stream()
				.filter(i -> searchForm.getStartDate().minusDays(1)
								.isBefore(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-M-d"))))
				.filter(i -> searchForm.getEndDate().plusDays(1)
								.isAfter(LocalDate.parse(i.getCustomFieldByName("発生日").getValue(), DateTimeFormatter.ofPattern("yyyy-M-d"))))
				.collect(Collectors.toList());
		List<Ticket> ticketList = new ArrayList<>();
		int index = 1;
		for (Issue issue : filteredIssueList) {
			ticketList.add(new Ticket(issue, index++));
		}
		
		try {
			//テンプレート読み込み
			String jasperFilePath =  getClass().getResource("/jasperreports/jasper-template.jasper").getPath();
			//パラメーター、データソースの設定
			HashMap<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("datasource1", ticketList);
			parameterMap.put("customerName", identifier);
			parameterMap.put("outputDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
			//PDFを作成し、レスポンスボディに設定
			JasperPrint jasperPrint= JasperFillManager.fillReport(jasperFilePath, parameterMap, new JREmptyDataSource());
			response.setContentType(MediaType.APPLICATION_PDF_VALUE);
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
			
		} catch (IOException | JRException e) {
			//エラー処理
			e.printStackTrace();
		}
		
		return "ticket-list";
	}
}
